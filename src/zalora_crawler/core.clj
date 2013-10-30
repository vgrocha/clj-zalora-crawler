(ns zalora-crawler.core
  (:require [net.cgrand.enlive-html :as h]
            [clojure.set :as cset])
  (:require [zalora-crawler.datastructures :refer [persist! make-sku make-page-stats]]
            [zalora-crawler.parsers :as parse]
            [zalora-crawler.html-parsers :as hparse])
  (:import [java.io File]))

(def default-output-file "output.csv")

(def default-stats-file "stats.csv")

(def default-to-visit-file "tovisit.txt")

(def default-visited "visited.txt")

(defn fetch-url
  "Fetches an 'url' 'sub-path'es, return an html node"
  [url & sub-paths]
  (println "Fetching " (apply str url sub-paths))
  (try
    (h/html-resource (java.net.URL. (apply str url sub-paths)))
    (catch Exception e nil)))

(defn calculate-skus-stats [its]
  (when-not (empty? its)
    (let [page-title (-> its first :page-title)
          prices (map :price its)
          max-price (apply max prices)
          min-price (apply min prices)
          
          discounts (keep #(let [{:keys [price old-price]} %]
                             (when (and (number? price)
                                        (number? old-price)
                                        (not (zero? old-price)))
                               (/ price old-price))) its)
          avg-discount (when-not (empty? discounts)
                         (/ (apply + discounts)
                            (count discounts)))
          
          discount-fraction (/ (count discounts) (count its))
          res (make-page-stats page-title min-price max-price avg-discount discount-fraction)]
      (println res)
      res
      )))  

(defn process-url
  ([root-node]
     (loop [to-visit-urls #{"/"}
            visited #{}]
       ;;stats display
       (println "Visited "(count visited)
                "currently remaining" (count to-visit-urls))

       ;;spit data into files for visualization
       (spit default-visited visited)
       (spit default-to-visit-file to-visit-urls)
             
       (when-not (empty? to-visit-urls)
         (let [[visiting-url & _] (seq to-visit-urls)
               html-page (fetch-url root-node visiting-url)
               found-to-visit-urls (hparse/extract-urls html-page)
               newly-acquainted-urls (cset/difference found-to-visit-urls visited to-visit-urls)
               skus (hparse/skus-page html-page)]
           
           (println "New skus " (count skus))
           (println "Found urls" (count found-to-visit-urls) ", newly acquainted" (count newly-acquainted-urls))

           (doseq [s skus]
             (persist! s default-output-file))

           (persist! (calculate-skus-stats skus) default-stats-file)
           
           (recur (into (cset/difference to-visit-urls #{visiting-url}) newly-acquainted-urls)
                  (conj visited visiting-url)))))))

(defn scrap-zalora []
  ;;clean output file and write header
  
  (-> (File. default-output-file)
      .delete)
  (persist! (make-sku "page-title" "brand" "title" "price" "old-price") default-output-file)
  
  (-> (File. default-stats-file)
      .delete)
  (make-page-stats "page-title" "min-price" "max-price" "avg-discount" "discount-fraction")
  
  (process-url "http://www.zalora.sg"))