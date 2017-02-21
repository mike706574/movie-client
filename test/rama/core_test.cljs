(ns rama.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [rama.macro :refer-macros [is=]]
            [rama.core :as core]))

(deftest movies-request
  (testing "build request"
    (is (= {:method :get,
            :uri "http://192.168.1.141:8000/movies",
            :on-success [:process-movies],
            :on-failure [:handle-movies-failure]}
           (dissoc (core/movies-request) :response-format)))))

(deftest initialize
  (testing "initializes"
    (is= {:http-xhrio
          {:method :get,
           :uri "http://192.168.1.141:8000/movies",
           :on-success [:process-movies],
           :on-failure [:handle-movies-failure]},
          :db
          {:movie-status :loading,
           :page-number nil,
           :movie-letter "A",
           :movie-letter-input-type :full,
           :movies nil}}
         (update (core/initialize {} nil) :http-xhrio dissoc :response-format))))

(deftest fetch-movies
  (testing "fetches movies"
    (is= {:http-xhrio
          {:method :get,
           :uri "http://192.168.1.141:8000/movies",
           :on-success [:process-movies],
           :on-failure [:handle-movies-failure]},
          :db {:movie-status :loading}}
         (update (core/fetch-movies {} nil) :http-xhrio dissoc :response-format))))

(comment
  (deftest paging
    (testing "previous page"
      (is= {:page-number 4
            :page-count 10}
           (core/previous-page {:page-number 5
                                :page-count 10} nil))
      (is= {:page-number 0
            :page-count 10}
           (core/previous-page {:page-number 0
                                :page-count 10} nil)))

    (testing "next page"
      (is= {:page-number 3
            :page-count 3}
           (core/next-page {:page-number 2
                            :page-count 3} nil))
      (is= {:page-number 4
            :page-count 4}
           (core/next-page {:page-number 4
                            :page-count 4} nil)))))
