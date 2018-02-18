(ns q4.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [q4.layout :refer [error-page]]
            [q4.routes.home :refer [home-routes]]
            [compojure.route :as route]
            [q4.env :refer [defaults]]
            [mount.core :as mount]
            [q4.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))
