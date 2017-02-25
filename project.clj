(defproject mike/movie-client "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [environ "1.1.0"]
                 [org.clojure/clojurescript "1.9.456"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [cljs-ajax "0.5.8"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-cljsbuild "1.1.5"]
            [cider/cider-nrepl "0.14.0"]
            [org.clojure/tools.nrepl "0.2.12"]
            [lein-figwheel "0.5.9"]]
  :source-paths ["src/clj"]
  :aot :all
  :uberjar-name "movie-client.jar"
  :main movie-client.server
  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :cljsbuild
                   {:builds {:client {:figwheel {:on-jsload "movie-client.core/run"
                                                 :websocket-host "192.168.1.141"}
                                      :compiler {:main "movie-client.core"
                                                 :asset-path "js"
                                                 :optimizations :none
                                                 :closure-defines {movie-client.core/api-uri "http://192.168.1.141:8000"}
                                                 :source-map true
                                                 :source-map-timestamp true}}}}}
             :production {:cljsbuild
                          {:builds {:client {:compiler {:output-dir "target"
                                                        :optimizations :advanced
                                                        :elide-asserts true
                                                        :closure-defines {movie-client.core/api-uri "https://mike-movie-server.herokuapp.com"}
                                                        :pretty-print false}}}}}}
  :figwheel {:repl false}
  :clean-targets ^{:protect false} ["resources/public/js"]
  :cljsbuild {:builds {:client {:source-paths ["src/cljs"]
                                :compiler {:output-dir "resources/public/js"
                                           :output-to "resources/public/js/client.js"}}}})
