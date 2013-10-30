(ns zalora-crawler.datastructures)


(defrecord SKU [page-title brand title price old-price])

(defn make-sku [page-title brand title price old-price]
  (SKU. page-title brand title price old-price))