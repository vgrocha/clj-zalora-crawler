(ns zalora-crawler.html-parsers
  (:require [net.cgrand.enlive-html :as h]
            [zalora-crawler.datastructures :refer [make-sku]]
            [zalora-crawler.parsers :as parse]))

(defn get-first-text-content [html-snippet selector]
  (-> (h/select html-snippet selector)
      first
      h/text))

(defn parse-sku-html-snippet [page-title html-snippet]
  (make-sku (parse/text page-title)
            (parse/text (get-first-text-content html-snippet [:span.itm-brand]))
            (parse/text (get-first-text-content html-snippet [:em.itm-title]))
            (parse/price (get-first-text-content html-snippet [[:span :.itm-price (h/but-node :.old)] [:span h/last-child]]))
            (parse/price (get-first-text-content html-snippet [:span.itm-price.old]))))

(defn skus-page
  ([page]
     (skus-page 20 page))
  ([n page]
     (let [page-title (get-first-text-content page [[:div.paging] [:h2] [:span.uc]])
           skus-nodes (h/select page [:ul#productsCatalog :li])
           skus (map (partial parse-sku-html-snippet page-title) (take n skus-nodes))]
       skus)))

(defn extract-urls [page]
  (->> (h/select page [:a])
       (keep #(get-in % [:attrs :href]))
       ;;only in subdomain, naive approach to url validation
       (filter #(re-matches #"(/[a-zA-Z0-9\-_]+)+" %))
       set))