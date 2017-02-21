(ns rama.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [rama.core-test]))

(doo-tests 'rama.core-test)
