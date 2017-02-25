(ns movie-client.server
  (:gen-class :main true)
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [not-found]]
            [environ.core :refer [env]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :refer [file-response]]
            [ring.middleware.resource :refer [wrap-resource]]))

(defroutes app
  (GET "/" [] (file-response "index.html" {:root "public"}))
  (ANY "*" [] (not-found (file-response "404.html" {:root "public"}))))

(defn -main
  [& [port]]
  (let [port (Integer. (or port (env :port) 5001))]
    (jetty/run-jetty (site (wrap-resource #'app "public")) {:port port :join? false})))
