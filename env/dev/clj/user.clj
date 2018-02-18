(ns user
  (:require 
            [mount.core :as mount]
            [q4.figwheel :refer [start-fw stop-fw cljs]]
            [q4.core :refer [start-app]]))

(defn start []
  (mount/start-without #'q4.core/repl-server))

(defn stop []
  (mount/stop-except #'q4.core/repl-server))

(defn restart []
  (stop)
  (start))


