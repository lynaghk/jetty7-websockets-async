(ns com.keminglabs.jetty7-websockets-async.example.system
  (:require [clojure.core.async :refer [chan]]
            [com.keminglabs.jetty7-websockets-async.example.core :as core]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn system
  []
  {:conn-chan (chan)})

(defn start! [system]
  (assoc system
    :server (run-jetty core/app
                       {:join? false :port 8080
                        :configurator (ws/configurator (system :conn-chan))})))


(defn stop! [system]
  (when-let [server (:server system)]
    (.stop server))

  (dissoc system :server))
