(ns rama.core
  (:require-macros [rama.macro :refer [let-map
                                       prevent-default]])
  (:require [clojure.string :as str]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]))

;; -- Development --------------------------------------------------------------
(enable-console-print!)

;; -- Event Dispatch -----------------------------------------------------------


;; -- Event Handlers -----------------------------------------------------------

(defn movies-request
  []
  {:method          :get
   :uri             "http://192.168.1.141:8000/movies"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:process-movies]
   :on-failure      [:handle-movies-failure]})

(rf/reg-event-fx
  :initialize
  (fn [_ _]
    {:http-xhrio (movies-request)
     :db {:movie-status :loading
          :page-number nil
          :letter nil
          :movies nil}}))

(rf/reg-event-fx
  :fetch-movies
  (fn [{db :db} _]
    {:http-xhrio (movies-request)
     :db (assoc db :movie-status :loading)}))

(rf/reg-event-db
 :process-movies
 (fn [db [_ response]]
   (let [movies (js->clj response)]
     (merge db {:movie-status :loaded
                :page-number 1
                :letter "A"
                :movies movies}))))

(rf/reg-event-db
 :previous-page
 (fn [db [_ response]]
   (update db :page-number dec)))

(rf/reg-event-db
 :next-page
 (fn [db [_ response]]
   (update db :page-number inc)))

(rf/reg-event-db
 :to-page
 (fn [db [_ page-number]]
   (assoc db :page-number page-number)))

(rf/reg-event-db
 :to-letter
 (fn [db [_ letter]]
   (merge db {:letter letter
              :page-number 1})))

(rf/reg-event-db
 :handle-movies-failure
 (fn [db [_ response]]
   (merge db {:movie-status :error
              :error-message (:status-text response)})))


;; -- Query  -------------------------------------------------------------------

(rf/reg-sub
  :movie-state
  (fn [db _]
    (select-keys db [:movie-status :error-message])))

(rf/reg-sub
  :movie-count
  (fn [{:keys [page-number movies] :as db} _]
    (when movies
      (count movies))))

(def page-size 12)

(rf/reg-sub
  :page
  (fn [{:keys [page-number letter movies] :as db} _]
    (when movies
      (let [movies-for-letter (vec (filter #(= (str/upper-case (:letter %)) letter) movies))]
        (let-map [letter letter
                  movie-count (count movies-for-letter)
                  page-size page-size
                  page-count (inc (quot movie-count page-size))
                  page-index (* (dec page-number) page-size)
                  page-number page-number
                  page (subvec movies-for-letter page-index (min movie-count (+ page-index page-size)))])))))


;; -- View Functions -----------------------------------------------------------

(defn button
  [label on-click]
  [:input.btn.btn-default
   {:type "button"
    :value label
    :on-click  on-click}])

(defn movie-item
  [{:keys [status movie-path letter category] :as movie}]
  [:li {:key movie-path} movie-path])

(defn page-link
  [label f]
  [:li.page-item
   {:key label}
   [:a.page-link
    {:style {"cursor" "pointer"
             "color" "#0275d8"}
     :on-click f}
    label]])

(defn disabled-page-link
  [label]
  [:li.page-item.disabled
   {:key label}
   [:span.page-link label]])

(defn active-page-link
  [label]
  [:li.page-item.active
   {:key label}
   [:span.page-link label]])

(def alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ")

(defn movie-listing
  []
  (when-let [{:keys [page-number
                     page-count
                     movie-count
                     letter
                     page]} @(rf/subscribe [:page])]
    [:div.album.text-muted
     {:style {"paddingBottom" "3em"}}
     [:div.container
      [:nav
       [:ul.pagination
        (for [item alphabet]
          (if (= item letter)
            (active-page-link item)
            (page-link item #(rf/dispatch [:to-letter item]))))]]
      [:nav
       [:ul.pagination
        (if (= 1 page-number)
          (disabled-page-link "Previous")
          (page-link "Previous" #(rf/dispatch [:previous-page])))
        (for [index (range 1 (inc page-count))]
          (if (= index page-number)
            (active-page-link index)
            (page-link index #(rf/dispatch [:to-page index]))))
        (if (= page-number page-count)
          (disabled-page-link "Next")
          (page-link "Next" #(rf/dispatch [:next-page])))]]
      [:div.row
       (for [{:keys [overview release_date status title
                     shortened-overview] :as movie} page]
         [:div.card {:key title
                     :style {"width" "20rem"
                             "margin" ".75rem"}}
          ;; [:img.card-img-top {:src "https://image.flaticon.com/icons/png/512/37/37232.png"
          ;;                     :alft title}]
          [:div.card-block
           [:h4.card-title title]
           [:p.card-text shortened-overview]
           [:div.card-text [:small.text-muted release_date]]]])]]]))

(defn jumbotron
  []
  (let [{:keys [movie-status error-message]} @(rf/subscribe [:movie-state])]
    [:section.jumbotron.text-center
     [:div.container
      [:h1.jumbotron-heading "Movies"]
      [:p.lead.text-muted
       (case movie-status
         :loading "Loading movies..."
         :loaded "Here they are!"
         :error (str "Eek! An error! It says \"" error-message "\"... what?"))]
      [:p
       [:a.btn.btn-primary
        {:href "#"
         :on-click #(rf/dispatch [:fetch-movies])}
        "Reload"]]]]))

(defn ui
  []
  [:div
   [jumbotron]
   [movie-listing]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])
  (reagent/render [ui] (js/document.getElementById "app")))
