(ns zalora-crawler.datastructures
  (:import [java.io File]))

(defprotocol PersistProtocol
  (persist! [this] [this filename] "Saves the element in filename or to default"))

(extend-type nil
  PersistProtocol
  (persist! [this filename])
  (persist! [this]))

(defn key-vals-to-csv
  "Given a map 'm' take its 'keys' and convert to a CSV string"
  [m keys]
  (->> (map #(% m) keys)
       (interpose ", ")
       (apply str)))

;;(TODO) create a default initializer for the files, that prints
;;the header according to record fields

;;SKU datastructure
(def default-sku-file "skus.csv")

(defn initialize-sku-file!
  ([] (initialize-sku-file! default-sku-file))
  ([f]
     (-> (File. f)
         .delete)
     (spit f (apply str (interpose ", " [ "page-title" "brand" "title" "price" "old-price\n"])))))

(defrecord SKU [page-title brand title price old-price]
  PersistProtocol
  (persist! [this filename]
    (spit filename (str (key-vals-to-csv this [:page-title :brand :title :price :old-price]) "\n") :append true))
  (persist! [this] (persist! this default-sku-file)))

(defn make-sku [page-title brand title price old-price]
  (SKU. page-title brand title price old-price))

;;PageStats datastructure
(def default-stats-file "stats.csv")

(defn initialize-stats-file! 
  ([] (initialize-stats-file! default-stats-file))
  ([f]
     (-> (File. f)
         .delete)
     (spit f (apply str (interpose ", " [ "page-title" "min-price" "max-price" "avg-discount" "discount-fraction\n"])))))

(defrecord PageStats [page-title min-price max-price avg-discount discount-fraction]
  PersistProtocol
  (persist! [this filename]
    (spit filename (str (key-vals-to-csv this [:page-title :min-price :max-price :avg-discount :discount-fraction]) "\n") :append true))
  (persist! [this] (persist! this default-stats-file)))

(defn make-page-stats
  "Given a list of 'sku's in a page, calculate the page stats"
  [skus]
  (when-not (empty? skus)
    (let [page-title (-> skus first :page-title)
          prices (map :price skus)
          max-price (apply max prices)
          min-price (apply min prices)
          
          discounts (keep #(let [{:keys [price old-price]} %]
                             (when (and (number? price)
                                        (number? old-price)
                                        (not (zero? old-price)))
                               (- 1 (/ price old-price))))
                          skus)
          avg-discount (when-not (empty? discounts)
                         (/ (apply + discounts)
                            (count discounts)))
          
          discount-fraction (/ (count discounts) (count skus))]
      (PageStats. page-title min-price max-price avg-discount discount-fraction))))
