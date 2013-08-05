(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [clojure.stacktrace :refer [e]]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [com.keminglabs.jetty7-websockets-async.example.system :as system]))

(def system nil)

(defn init
  "Constructs the current development system."
  []
  (alter-var-root #'system (constantly (system/system))))

(defn start!
  "Starts the current development system."
  []
  (alter-var-root #'system system/start!))

(defn stop!
  "Shuts down and destroys the current development system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (system/stop! s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (init)
  (start!))

(defn reset []
  (stop!)
  (refresh :after 'user/go)
  nil)

(comment
  (clojure.tools.namespace.repl/refresh)
  (reset)



  (require '[com.keminglabs.jetty7-websockets-async.core :refer [configurator]]
           '[clojure.core.async :refer [chan go >! <!]]
           '[ring.adapter.jetty :refer [run-jetty]])

  (defn http-handler
    [req]
    {:response 200 :body "HTTP hello" :headers {}})

  (def c (chan))

  (def ws-configurator
    (configurator c))

  (def server
    (run-jetty http-handler {:configurator ws-configurator
                             :port 8090, :join? false}))

  (go (loop []
        (let [ws-req (<! c)]
          (>! (:send ws-req) "Hello new websocket client!")
          (recur))))






  )