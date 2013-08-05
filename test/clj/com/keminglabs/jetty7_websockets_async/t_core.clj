(ns com.keminglabs.jetty7-websockets-async.t-core
  (:require [com.keminglabs.jetty7-websockets-async.core :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.core.async :refer [go close! >!! chan timeout alt!!]]
            clojure.core.async.impl.protocols
            [midje.sweet :refer :all])
  (:import (org.eclipse.jetty.websocket WebSocketClientFactory WebSocket)
           java.net.URI))

(defn <!!
  "Like <!!, but with an optional timeout.
Why is this not in core.async, yo?"
  ([port]
     (clojure.core.async/<!! port))
  ([port timeout-ms timeout-val]
     (alt!!
       (timeout timeout-ms) ([_] timeout-val)
       port ([val] val))))


(def test-port
  8090)

(def ws-client-factory
  (doto (WebSocketClientFactory.)
    (.start)))

(fact "Websockets"
      (with-state-changes [(around :facts
                                   (let [new-connections (chan)
                                         server (run-jetty nil {:configurator (configurator new-connections)
                                                                :port test-port :join? false})
                                         client (.newWebSocketClient ws-client-factory)]

                                     (try
                                       ?form
                                       (finally
                                         (.stop server)))))]

        (fact "New websocket connections are put onto the channel"
              (<!! new-connections 100 :empty) => :empty
              (.open client (URI. (str "ws://localhost:" test-port))
                     (proxy [WebSocket] []))
              (let [{:keys [conn send recv uri]} (<!! new-connections 100 :fail)]
                uri => "/"
                conn => #(instance? org.eclipse.jetty.websocket.WebSocket$Connection %)
                send => #(satisfies? clojure.core.async.impl.protocols/WritePort %)
                recv => #(satisfies? clojure.core.async.impl.protocols/ReadPort %)))))
