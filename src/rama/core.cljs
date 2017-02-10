(ns rama.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

;; A detailed walk-through of this source code is provied in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------


;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:loading-videos? false
     :videos nil}))


(rf/reg-event-fx
  :fetch-videos
  (fn [{db :db} _]
    {:http-xhrio {:method          :get
                  :uri             "http://localhost:8081/videos"
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-videos]
                  :on-failure      [:handle-videos-failure]}
     :db (assoc db :loading-videos? true)}))

(rf/reg-event-db
 :process-videos
 (fn [db [_ response]]
   (assoc db
          :loading-videos? false
          :videos (js->clj response))))

(rf/reg-event-db
 :handle-videos-failure
 (fn [db [_ response]]
   db))


;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
  :videos
  (fn [db _]
    (:videos db)))

;; -- Domino 5 - View Functions ----------------------------------------------

(defn video-item
  [{:keys [status video-path letter category] :as video}]
  [:li {:key video-path} video-path])

(defn video-listing
  []
  (let [videos @(rf/subscribe [:videos])]
    (if-not videos
      [:span "load the videos"]
      [:span (str (count videos))])))

(defn video-button
  []
  [:button {:type "button"
            :on-click  #(rf/dispatch [:fetch-videos])}  ;; get data from the server !!
   "load"])

(defn ui
  []
  [:div
   [video-button]
   [:h3 "videos"]
   [video-listing]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))
