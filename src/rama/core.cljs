(ns rama.core
  (:require-macros [rama.macro :refer [let-map]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

;; A detailed walk-through of this source code is provied in the docs:
;; https://github.com/Day8/re-frame/blob/master/docs/CodeWalkthrough.md

;; -- Domino 1 - Event Dispatch -----------------------------------------------


;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-fx
  :initialize
  (fn [_ _]
    {:http-xhrio {:method          :get
                  :uri             "http://192.168.1.141:8081/movies"
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-movies]
                  :on-failure      [:handle-movies-failure]}
     :db {:loading-movies? true
          :page-number nil
          :movies nil}}))

(rf/reg-event-fx
  :fetch-movies
  (fn [{db :db} _]
    {:http-xhrio {:method          :get
                  :uri             "http://192.168.1.141:8081/movies"
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-movies]
                  :on-failure      [:handle-movies-failure]}
     :db (assoc db :loading-movies? true)}))

(rf/reg-event-db
 :process-movies
 (fn [db [_ response]]
   (let [movies (js->clj response)]
     (merge db {:loading-movies? false
                :page-number 1
                :movies movies} ))))

(rf/reg-event-db
 :previous-page
 (fn [db [_ response]]
   (update db :page-number dec)))

(rf/reg-event-db
 :next-page
 (fn [db [_ response]]
   (update db :page-number inc)))

(rf/reg-event-db
 :handle-movies-failure
 (fn [db [_ response]]
   db))


;; -- Domino 4 - Query  -------------------------------------------------------

(defn reg-val
  [k]
  (rf/reg-sub k (fn [db _] (get db k))))

(reg-val :loading-movies?)

(rf/reg-sub
  :movie-count
  (fn [{:keys [page-number movies] :as db} _]
    (when movies
      (count movies))))

(rf/reg-sub
  :page
  (fn [{:keys [page-number movies] :as db} _]
    (when movies
      (let-map [movie-count (count movies)
                page-count (quot movie-count 10)
                page-index (* (dec page-number) 10)
                page (subvec movies page-index (+ page-index 10))]))))


;; -- Domino 5 - View Functions ----------------------------------------------

(defn button
  [label on-click]
  [:button {:type "button"
            :on-click  on-click}
   label])

(defn movie-item
  [{:keys [status movie-path letter category] :as movie}]
  [:li {:key movie-path} movie-path])

(defn movie-listing
  []
  (if-let [{:keys [page-number
                   page-count
                   movie-count
                   page]} @(rf/subscribe [:page])]
    [:div
     [:span (str movie-count " total movies.")]
     [:span (str "Page " page-number " of " page-count)]
     [:ul
      (for [{:keys [status name]} page]
        [:li {:key name} name])]]
    [:span "No movies loaded."]))


(defn break
  []
  [:br])

(defn loading
  []
  (when @(rf/subscribe [:loading-movies?])
    [:span "Loading..."]))

(defn ui
  []
  [:div
   [button "Load Movies" #(rf/dispatch [:fetch-movies])]
   [loading]
   [:h3 "Movies"]
   [button "Previous Page" #(rf/dispatch [:previous-page])]
   [button "Next Page" #(rf/dispatch [:next-page])]
   [break]
   [movie-listing]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])     ;; puts a value into application state
  (reagent/render [ui]              ;; mount the application's ui into '<div id="app" />'
                  (js/document.getElementById "app")))
