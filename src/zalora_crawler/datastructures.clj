(ns zalora-crawler.datastructures)

(defprotocol PersistProtocol
  (persist! [this filename] "Saves the element in filename"))

(defn key-vals-to-csv [m keys]
  (->> (map #(% m) keys)
       (interpose ", ")
       (apply str)))

(defrecord SKU [page-title brand title price old-price]
  PersistProtocol
  (persist! [this filename]
    (spit filename (str (key-vals-to-csv this [:page-title :brand :title :price :old-price]) "\n") :append true)))

(defn make-sku [page-title brand title price old-price]
  (SKU. page-title brand title price old-price))

(defrecord PageStats [page-title min-price max-price avg-discount discount-fraction]
  PersistProtocol
  (persist! [this filename]
    (spit filename (str (key-vals-to-csv this [:page-title :min-price :max-price :avg-discount :discount-fraction]) "\n") :append true)))

(defn make-page-stats [page-title min-price max-price avg-discount discount-fraction]
  (PageStats. page-title min-price max-price avg-discount discount-fraction))


