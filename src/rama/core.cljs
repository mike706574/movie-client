(ns rama.core
  (:require-macros [rama.macro :refer [let-map]])
  (:require [clojure.string :as str]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [rama.alphabet :as alphabet]
            [rama.nav :as nav]))

;; TODO:

;; -- Development --------------------------------------------------------------
(enable-console-print!)

(defn includes-ignore-case?
  [string sub]
  (not (nil? (.match string (re-pattern (str "(?i)" sub))))))


;; -- Event Dispatch -----------------------------------------------------------


;; -- Event Handlers -----------------------------------------------------------

(def page-size 12)

(defn movies-request []
  {:method          :get
   :uri             "http://192.168.1.141:8000/movies"
   :response-format (ajax/json-response-format {:keywords? true})
   :on-success      [:process-movies]
   :on-failure      [:handle-movies-failure]})

(defn initialize [_ _]
  (println "Initializing.")
  {:http-xhrio (movies-request)
   :db {:movie-status :loading
        :page-number nil
        :movie-letter "A"
        :movie-letter-input-type :full
        :movies nil}})

(rf/reg-event-fx :initialize initialize)

(defn fetch-movies
  [{db :db} _]
;;  (println "Fetching movies.")
  {:http-xhrio (movies-request)
   :db (assoc db
              :movies nil
              :movie-status :loading)})

(rf/reg-event-fx :fetch-movies fetch-movies)

(rf/reg-event-db
 :process-movies
 (fn [db [_ response]]
;;   (println "Processing movies.")
   (let [_ (println "PARSING")
         movies (js->clj response)
         _ (println "DONE PARSING")
         new-db (-> db
                    (merge {:movie-status :loaded
                            :page-number 1
                            :movie-letter "A"
                            :movies movies}))]
;;     (println "Done!")
     new-db)))

;; paging
(defn previous-page
  [db _]
  (update db :page-number dec))

(defn next-page
  [db _]
  (update db :page-number inc))

(def to-page
 (fn [db [_ page-number]]
;;   (println (str "[Page jump] " (:page-number db) " -> " page-number))
   (assoc db :page-number page-number)))

(rf/reg-event-db :next-page next-page)
(rf/reg-event-db :previous-page previous-page)
(rf/reg-event-db :to-page to-page)

(rf/reg-event-db
 :previous-letter
 (fn [db _]
   (-> db
       (update :movie-letter alphabet/previous)
       (assoc :page-number 1))))

(rf/reg-event-db
 :next-letter
 (fn [db _]
   (-> db
       (update :movie-letter alphabet/next)
       (assoc :page-number 1))))

(rf/reg-event-db
 :to-letter
 (fn [db [_ new-letter]]
   (assoc db :movie-letter new-letter :page-number 1)))

(rf/reg-event-db
 :movie-filter-change
 (fn [db [_ text]]
   (assoc db :movie-filter-text text :page-number 1)))

(rf/reg-event-db
 :toggle-movie-letter-input-type
 (fn [db [_ text]]
   (update db :movie-letter-input-type
           #(case %
              :full :skinny
              :skinny :full))))

(rf/reg-event-db
 :handle-movies-failure
 (fn [db [_ response]]
   (merge db {:movie-status :error
              :error-message (:status-text response)})))


;; -- Query  -------------------------------------------------------------------

(rf/reg-sub
  :movies
  (fn [db _]
    (:movies db)))

(rf/reg-sub
  :movie-state
  (fn [db _]
    (select-keys db [:movie-status :error-message])))

(rf/reg-sub
  :movie-count
  (fn [{:keys [page-number movies] :as db} _]
    (when movies
      (count movies))))

(defn filter-movies
  [{:keys [movie-letter movie-filter-text movies page-number]}]
  (let [letter-movies (get movies (keyword movie-letter))
        filtered-movies (if (str/blank? movie-filter-text)
                          letter-movies
                          (into [] (filter #(includes-ignore-case? (:title %) movie-filter-text) letter-movies)))
        movie-count (count filtered-movies)
        page-count (max 1 (quot movie-count page-size))
        page-index (* (dec page-number) page-size)
        end-index (min movie-count (+ page-index page-size))
        page-movies (subvec filtered-movies page-index end-index)]
;;    (println (str "Filtering movies: " (count letter-movies) " for letter " movie-letter ", " (count filtered-movies) " after text filter \"" movie-filter-text "\"."))
    {:filtered-movie-count (count filtered-movies)
     :page-count page-count
     :page-number page-number
     :page-movies page-movies}))

(rf/reg-sub
  :page
  (fn [db _]
    (filter-movies db)))

(rf/reg-sub
  :movie-filter-text
  (fn [db _]
    (:movie-filter-text db)))

(rf/reg-sub
  :movie-letter-state
  (fn [db _]
    (select-keys db [:movie-letter
                     :movie-letter-input-type])))

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

(defn movie-filter-input
  []
  [:div.pb-3
   [:input.form-control
    {:id "movie-filter-input"
     :placeholder "Title"
     :type "text"
     :value @(rf/subscribe [:movie-filter-text])
     :on-change #(rf/dispatch [:movie-filter-change (-> % .-target .-value)])}]])

(defn movie-letter-input
  []
  (let [{:keys [movie-letter
                movie-letter-input-type]} @(rf/subscribe [:movie-letter-state])]
    [:div
;;     [button "Toggle Input Type" #(rf/dispatch [:toggle-movie-letter-input-type])]
     (comment [:ul.pagination
               (for [item alphabet/alphabet-vector]
                 (if (= item movie-letter)
                   (nav/active-page-link item)
                   (nav/page-link item #(rf/dispatch [:to-letter item]))))])
     (let [[before-previous previous] (alphabet/take-before 2 movie-letter)
           [next after-next] (alphabet/take-after 2 movie-letter)]
       [:ul.pagination
        (nav/previous-link #(rf/dispatch [:previous-letter]))
        (nav/page-link before-previous #(rf/dispatch [:to-letter before-previous]))
        (nav/page-link previous #(rf/dispatch [:previous-letter]))
        (nav/active-page-link movie-letter)
        (nav/page-link next #(rf/dispatch [:next-letter]))
        (nav/page-link after-next #(rf/dispatch [:to-letter after-next]))
        (nav/next-link #(rf/dispatch [:next-letter]))])]))

(defn movie-pagination
  []
  (let [{:keys [page-number page-count filtered-movie-count] :as response} @(rf/subscribe [:page])]
;;    (println (str "Rendering pagination: " response))
    (when-not (zero? filtered-movie-count)
      [:ul.pagination
       (if (= 1 page-number)
         (nav/disabled-previous-link)
         (nav/previous-link #(rf/dispatch [:previous-page])))
       (for [index (range 1 (inc page-count))]
         (if (= index page-number)
           (nav/active-page-link index)
           (nav/page-link index #(rf/dispatch [:to-page index]))))
       (if (= page-number page-count)
         (nav/disabled-next-link)
         (nav/next-link #(rf/dispatch [:next-page])))])))

(defn movies
  []
  (let [movies (:page-movies @(rf/subscribe [:page]))]
    (if (empty? movies)
      [:div.text-center.pt-5.pb-5
       [:h3 "No movies found."]]
      [:div
       [:div.row
        (for [{:keys [moviedb-id
                      imdb-id
                      title
                      overview
                      backdrop-path
                      release-date] :as movie} movies]
          [:div.card {:key title
                      :style {"width" "20rem"
                              "margin" ".75rem"}}
           [:img.card-img-top
            {:src (if backdrop-path
                    (str "http://image.tmdb.org/t/p/w300" backdrop-path)
                    "https://placeholdit.imgix.net/~text?txtsize=28&txt=300%C3%97169&w=300&h=169")
             :style {"display" "block"
                     "width" "20rem"
                     "height" "auth"}
             :alt title}]
           [:div.card-block
            [:h4.card-title title]
            [:h6.card-subtitle.mb-2.text-muted release-date]
            [:p.card-text overview]
            [:a.card-link
             {:href (str "https://www.themoviedb.org/movie/" moviedb-id)
              :target "_blank"}
             "themoviedb"]
            [:a.card-link
             {:href (str "http://www.imdb.com/title/" imdb-id)
              :target "_blank"}
             "IMDb"]]])]])))

(defn bottom
  []
  [:div.container.text-muted
   {:style {"minHeight" "45em"}}
   (when-not (= :loading (:movie-status @(rf/subscribe [:movie-state])))
     [:div
      [:nav
       [movie-letter-input]
       [movie-filter-input]
       [movie-pagination]]
      [movies]])])

(defn top
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
   [top]
   [bottom]])

;; -- Entry Point -------------------------------------------------------------

(defn ^:export run
  []
  (rf/dispatch-sync [:initialize])
  (reagent/render [ui] (js/document.getElementById "app")))
