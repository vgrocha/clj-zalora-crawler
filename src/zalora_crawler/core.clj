(ns zalora-crawler.core
  (:require [net.cgrand.enlive-html :as h]
            [clojure.string :as str]
            [clojure.set :as cset])
  (:require [zalora-crawler.datastructures :refer [persist! make-sku make-page-stats]])
  (:import [java.io File]))

(def default-output-file "output.csv")

(def default-stats-file "stats.csv")

(def default-to-visit-file "tovisit.txt")

(def default-visited "visited.txt")

(defn fetch-url [url & sub-paths]
  (println "Fetching " (apply str url sub-paths))
  (try
    (h/html-resource (java.net.URL. (apply str url sub-paths)))
    (catch Exception e nil)))

(defn text-parse
  "Cleans up text and extract characters and white-spaces only"
  [s]
  (->> s
      str/trim))

(defn price-parse
  "extract price according to format, returns double"
  [s]
  (let [price-str (->> s
                       str/trim
                       (re-find #"(\d+\.\d{2}).SGD")
                       last)]
    (when-not (nil? price-str)
      (try
        (Double/parseDouble price-str)
        (catch NumberFormatException e 0)))))

(defn get-first-text-content [html-seg selector]
  (-> (h/select html-seg selector)
      first
      h/text))


(defn parse-item [page-title html-seg]
  (make-sku (text-parse page-title)
            (text-parse (get-first-text-content html-seg [:span.itm-brand]))
            (text-parse (get-first-text-content html-seg [:em.itm-title]))
            (price-parse (get-first-text-content html-seg [[:span :.itm-price (h/but-node :.old)] [:span h/last-child]]))
            (price-parse (get-first-text-content html-seg [:span.itm-price.old]))))

(defn calculate-items-stats [page-title its]
  (when-not (empty its)
    (let [prices (map :price its)
          max-price (apply max prices)
          min-price (apply min prices)
          
          discounts (keep #(let [{:keys [price old-price]} %]
                             (when (and (number? price)
                                        (number? old-price)
                                        (not (zero? old-price)))
                               (/ price old-price))))
          avg-discount (/ (apply + discounts)
                          (count discounts))
          
          discount-fraction (/ discounts (count its))
          page-stats (make-page-stats page-title min-price max-price avg-discount discount-fraction)
          ]
      (persist! page-stats default-stats-file)
            
      ))
  
  )  

(defn process-items-page [page]
  (let [page-title (get-first-text-content page [[:div.paging] [:h2] [:span.uc]])
        items-nodes (h/select page [:ul#productsCatalog :li])
        skus (map (partial parse-item page-title) (take 20 items-nodes))]

    (doseq [s skus]
      (persist! s default-output-file))

    skus))

(defn extract-urls [page]
  (->> (h/select page [:a])
       (keep #(get-in % [:attrs :href]))
       ;;only in subdomain, naive approach to url validation
       (filter #(re-matches #"(/[a-zA-Z0-9\-_]+)+" %))
       set))

(defn process-url
  ([root-node]
     (loop [to-visit-urls #{"/"}
            visited #{}]
       (println "Visited "(count visited)
                "currently remaining" (count to-visit-urls))

       (spit default-visited visited)
       (spit default-to-visit-file to-visit-urls)
             
       (when-not (empty? to-visit-urls)
         (let [[visiting-url & _] (seq to-visit-urls)
               html-page (fetch-url root-node visiting-url)
               found-to-visit-urls (extract-urls html-page)
               newly-acquainted-urls (cset/difference found-to-visit-urls visited to-visit-urls)
               items (process-items-page html-page)]
           
           (println "New items " (count items))
           (println "Found urls" (count found-to-visit-urls) ", newly acquainted" (count newly-acquainted-urls))
           (recur (into (cset/difference to-visit-urls #{visiting-url}) newly-acquainted-urls)
                  (conj visited visiting-url)))))))

(defn scrap-zalora []
  ;;clean output file and write header
  (-> (File. default-output-file)
      .delete)
  (persist! (make-sku "page-title" "brand" "title" "price" "old-price") default-output-file)
  (process-url "http://www.zalora.sg"))