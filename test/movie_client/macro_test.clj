(ns movie-client.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [movie-client.macro :refer-macros [is-equal]]
            [movie-client.core :as core]))

(deftest movies-request
  (testing "build request"
    (is (= {:method :get,
            :uri "http://192.168.1.141:8000/movies",
            :on-success [:process-movies],
            :on-failure [:handle-movies-failure]}
           (dissoc (core/movies-request) :response-format)))))

(deftest initialize
  (testing "initializes"
    (is=
     {}
     (update (core/initialize) :http-xhrio dissoc :response-format))))
