(ns ^:figwheel-no-load q4.app
  (:require [q4.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
