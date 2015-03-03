(defproject precursor/om-i "0.1.4"
  :description "Instrumentation helpers for Om applications"
  :url "https://github.com/PrecursorApp/om-i"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2755" :scope "provided"]
                 [org.omcljs/om "0.8.8" :scope "provided"]]

  :plugins [[lein-cljsbuild "1.0.4"]]
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src"]
                        :compiler {:output-to "script/tests.simple.js"
                                   :output-dir "script/out"
                                   :source-map "script/tests.simple.js.map"
                                   :output-wrapper false
                                   :optimizations :simple}}]}

  :source-paths ["src"])
