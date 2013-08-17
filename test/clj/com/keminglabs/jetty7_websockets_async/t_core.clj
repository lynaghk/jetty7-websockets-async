(ns com.keminglabs.jetty7-websockets-async.t-core
  (:require [com.keminglabs.jetty7-websockets-async.core :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.core.async :refer [go close! >!! chan timeout alt!!]]
            clojure.core.async.impl.protocols
            [midje.sweet :refer :all])
  (:import (org.eclipse.jetty.websocket WebSocketClientFactory WebSocket WebSocket$OnTextMessage)
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

(fact "Websocket server"
  (with-state-changes [(around :facts
                               (let [new-connections (chan)
                                     ctx-path "/ws"
                                     server (run-jetty nil {:configurator (configurator new-connections {:path ctx-path})
                                                            :port test-port :join? false})
                                     client (.newWebSocketClient ws-client-factory)]

                                 (try
                                   ?form
                                   (finally
                                     (.stop server)))))]

    (fact "New websocket connections are put onto the channel"
      (<!! new-connections 0 :empty) => :empty
      (.open client (URI. (str "ws://localhost:" test-port ctx-path))
             (proxy [WebSocket] []
               (onOpen [_])
               (onClose [_ _])))
      (let [{:keys [conn in out uri]} (<!! new-connections 100 :fail)]
        uri => ctx-path
        conn => #(instance? org.eclipse.jetty.websocket.WebSocket$Connection %)
        in => #(satisfies? clojure.core.async.impl.protocols/WritePort %)
        out => #(satisfies? clojure.core.async.impl.protocols/ReadPort %)))

    (fact "Send to client"
      (let [received-messages (chan)]

        (.open client (URI. (str "ws://localhost:" test-port ctx-path))
               (proxy [WebSocket$OnTextMessage] []
                 (onOpen [conn])
                 (onClose [close-code msg])
                 (onMessage [msg]
                   (>!! received-messages msg))))

        (let [test-message "test-message"
              {:keys [in]} (<!! new-connections 100 :fail)]
          (>!! in test-message)
          (<!! received-messages 100 :fail) => test-message)))


    (fact "Receive from client"
      (let [test-message "test-message"]
        (.open client (URI. (str "ws://localhost:" test-port ctx-path))
               (proxy [WebSocket$OnTextMessage] []
                 (onOpen [conn]
                   (.sendMessage conn test-message))
                 (onClose [close-code msg])
                 (onMessage [msg])))

        (let [{:keys [out]} (<!! new-connections 100 :fail)]
          (<!! out 100 :fail) => test-message)))))



(fact "Websocket client"
  (with-state-changes [(around :facts
                               (let [server-new-connections (chan)
                                     server (run-jetty nil {:configurator (configurator server-new-connections)
                                                            :port test-port :join? false})]

                                 (go ;;echo first message back to client
                                   (let [{:keys [in out]} (<! server-new-connections)]
                                     (>! in (<! out))))

                                 (try
                                   (let [echo-url (str "ws://localhost:" test-port)
                                         new-connections (chan)]
                                     ?form)

                                   (finally
                                     (.stop server)))))]

    (fact "New websocket connections are put onto the channel"
      (<!! new-connections 0 :empty) => :empty
      (connect! new-connections echo-url)
      (let [{:keys [conn in out uri]} (<!! new-connections 100 :fail)]
        uri => echo-url
        conn => #(instance? org.eclipse.jetty.websocket.WebSocket$Connection %)
        in => #(satisfies? clojure.core.async.impl.protocols/WritePort %)
        out => #(satisfies? clojure.core.async.impl.protocols/ReadPort %)))

    (fact "Echo"
      (connect! new-connections echo-url)
      (let [test-message "test-message"
            {:keys [in out]} (<!! new-connections 100 :fail)]
        (>!! in test-message)
        (<!! out 100 :fail) => test-message))))
