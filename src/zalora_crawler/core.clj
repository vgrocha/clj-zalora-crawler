(ns zalora-crawler.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as h]
            [clojure.set :as cset])
  (:require [zalora-crawler.datastructures :refer [initialize-sku-file! initialize-stats-file! persist! make-sku make-page-stats]]
            [zalora-crawler.parsers :as parse]
            [zalora-crawler.html-parsers :as hparse])
  (:import [java.io File]))

(def default-to-visit-file "tovisit.txt")

(def default-visited "visited.txt")

(defn fetch-url
  "Fetches an 'url' 'sub-path'es, return an html node"
  [url & sub-paths]
  (println "Fetching " (apply str url sub-paths))
  (try
    (h/html-resource (java.net.URL. (apply str url sub-paths)))
    (catch Exception e nil)))

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
             (persist! s))

           (persist! (make-page-stats skus))
           
           (recur (into (cset/difference to-visit-urls #{visiting-url}) newly-acquainted-urls)
                  (conj visited visiting-url)))))))

(defn scrap-zalora []
  ;;clean output file and write header
  (initialize-sku-file!)
  (initialize-stats-file!)
  
  (process-url "http://www.zalora.sg"))

(defn -main [& _]
  (scrap-zalora))