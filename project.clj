(defproject mike/rama "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.456"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [cljs-ajax "0.5.8"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [cider/cider-nrepl "0.14.0"]
            [org.clojure/tools.nrepl "0.2.12"]
            [lein-figwheel "0.5.9"]
            [lein-doo "0.1.7"]]

  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [figwheel-sidecar "0.5.0-2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :cljsbuild
                   {:builds {:client {:figwheel {:on-jsload "rama.core/run"
                                                 :websocket-host "192.168.1.141"}
                                      :compiler {:main "rama.core"
                                                 :asset-path "js"
                                                 :optimizations :none
                                                 :source-map true
                                                 :source-map-timestamp true}}}}}
             :prod {:cljsbuild
                    {:builds {:client {:compiler {:optimizations :advanced
                                                  :elide-asserts true
                                                  :pretty-print false}}}}}}
  :figwheel {:repl false}
  :clean-targets ^{:protect false} ["resources/public/js"]
  :doo {:build "test"
        :alias {:default [:phantom]}}
  :cljsbuild
  {:builds {:client {:source-paths ["src"]
                     :compiler {:output-dir "resources/public/js"
                                :output-to "resources/public/js/client.js"}}
            :test {:source-paths ["src" "test"]
                   :compiler {:output-dir "resources/public/js/test"
                              :output-to "resources/public/js/testable.js"
                              :main "rama.test-runner"}}}})
