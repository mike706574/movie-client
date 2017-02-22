(ns movie-client.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [movie-client.core-test]))

(doo-tests 'movie-client.core-test)
