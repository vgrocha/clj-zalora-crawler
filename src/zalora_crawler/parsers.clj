(ns zalora-crawler.parsers
  (:require [clojure.string :as str]))

(defn text
  "Cleans up text fields"
  [s]
  (->> s
       str/trim))

(defn price
  "extract price according to format 123.45 and
tries to parse as double"
  [s]
  (let [price-str (->> s
                       str/trim
                       (re-find #"(\d+\.\d{2}).SGD")
                       last)]
    (when-not (nil? price-str)
      (try
        (Double/parseDouble price-str)
        (catch NumberFormatException e 0)))))