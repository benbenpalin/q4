(ns q4.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[q4 started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[q4 has shut down successfully]=-"))
   :middleware identity})
