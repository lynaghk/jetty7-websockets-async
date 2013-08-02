(ns com.keminglabs.jetty7-websockets-async.example
  (:require-macros [com.keminglabs.jetty7-websockets-async.example.macros :refer [p pp]]
                   [cljs.core.async.macros :as m :refer [go]])
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! put! take! sliding-buffer
                     dropping-buffer timeout]]
            goog.net.WebSocket))

(def host
  (aget js/window "location" "host"))

(let [socket (goog.net.WebSocket.)]

  (.open socket (str "ws://" host "/"))
  (.addEventListener socket goog.net.WebSocket.EventType.MESSAGE
                     (fn [e]
                       (p e)
                       (.send socket "sup?")
                       (.send socket "yeah."))))
