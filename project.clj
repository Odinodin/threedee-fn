(defproject threedee-fn "0.1.0-SNAPSHOT"
            :description "Threedee fn"
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [org.clojure/clojurescript "1.7.145"]

                           [figwheel "0.4.1"]
                           [cljsjs/three "0.0.70-0"]

                           ]
            :plugins [[lein-cljsbuild "1.1.0"]
                      [lein-figwheel "0.4.1"]]

            :jvm-opts ["-Xmx1G"]

            :cljsbuild {
                        :builds [{:id           "dev"
                                  :source-paths ["src" "src/dev"]

                                  :figwheel true

                                  :compiler     {:main "threedee.core"
                                                 :asset-path "js/out"
                                                 :output-to     "resources/public/js/app.js"
                                                 :output-dir    "resources/public/js/out"
                                                 :optimizations :none
                                                 :source-map    true
                                                 :pretty-print  true}}]}

            :figwheel {
                       :http-server-root "public"           ;; default and assumes "resources"
                       :server-port      4000               ;; default
                       :css-dirs         ["resources/public/css"] ;; watch and update CSS

                       :nrepl-port 4444})