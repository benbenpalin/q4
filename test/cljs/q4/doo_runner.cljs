(ns q4.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [q4.core-test]))

(doo-tests 'q4.core-test)

