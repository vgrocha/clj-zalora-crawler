(defproject zalora-crawler "0.1.0"
  :description "A small single-threaded scrapper,
                intended for scrapping zalora.sg home page."
  :url "https://github.com/vgrocha/clj-zalora-crawler"
  :license {:name "Beerware"
            :url "http://en.wikipedia.org/wiki/Beerware"}
  :main zalora-crawler.core
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enlive "1.1.4"]])
