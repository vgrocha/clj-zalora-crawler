(ns zalora-crawler.parsers
  (:require [clojure.string :as str]))

(defn text
  "Cleans up text and extract characters and white-spaces only"
  [s]
  (->> s
       str/trim))

(defn price
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