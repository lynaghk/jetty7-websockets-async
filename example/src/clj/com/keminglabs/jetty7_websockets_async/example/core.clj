(ns com.keminglabs.jetty7-websockets-async.example.core
  (:require [com.keminglabs.jetty7-websockets-async.core :as ws]
            [compojure.core :refer [routes]]
            [compojure.route :as route]))

(def app
  (routes
   (route/files "/" {:root "example/public"})))