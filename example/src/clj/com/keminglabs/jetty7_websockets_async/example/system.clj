(ns com.keminglabs.jetty7-websockets-async.example.system
  (:require [clojure.core.async :refer [chan]]
            [com.keminglabs.jetty7-websockets-async.example.core :as core]
            [com.keminglabs.jetty7-websockets-async.core :as ws]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn system
  []
  {:connection-chan (chan)})

(defn start! [system]

  (core/register-ws-app! (system :connection-chan))
  
  (assoc system
    :server (run-jetty core/app
                       {:join? false :port 8080
                        :configurator (ws/configurator (system :connection-chan))})))


(defn stop! [system]
  (when-let [server (:server system)]
    (.stop server))

  (dissoc system :server))
