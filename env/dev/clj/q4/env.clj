(ns q4.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [q4.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[q4 started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[q4 has shut down successfully]=-"))
   :middleware wrap-dev})
