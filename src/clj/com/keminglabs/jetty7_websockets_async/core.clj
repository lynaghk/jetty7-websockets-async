(ns com.keminglabs.jetty7-websockets-async.core
  (:require [clojure.core.async :refer [go chan close! <!! <! >!! >!]])
  (:import (org.eclipse.jetty.websocket WebSocket WebSocket$OnTextMessage WebSocketHandler)
           org.eclipse.jetty.server.handler.ContextHandler
           org.eclipse.jetty.server.handler.ContextHandlerCollection))

(defn handler
  [connection-chan send-thunk recv-thunk]
  (proxy [WebSocketHandler] []
    (doWebSocketConnect [request response]
      (let [send (send-thunk) recv (recv-thunk)]
        (proxy [WebSocket$OnTextMessage] []
          (onOpen [conn]
            (>!! connection-chan {:uri (.getRequestURI request)
                                  :conn conn :send send :recv recv})
            (go (loop []
                  (let [^String msg (<! send)]
                    (if (nil? msg)
                      (do
                          (close! recv)
                        (.close conn))
                      (do
                          (.sendMessage conn msg)
                        (recur)))))))
          (onMessage [msg]
            (>!! recv msg))
          (onClose [close-code msg]
            (prn ["closed" close-code msg])
            (close! send)
            (close! recv)))))))

(defn configurator
  "Returns a Jetty configurator that configures server to listen for websocket connections and put request maps on `connection-chan`.
Request maps contain following keys:

  :uri  - the string URI on which the connection was made
  :conn - the underlying Jetty7 websocket connection (see: http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/websocket/WebSocket.Connection.html)
  :send - a core.async port where you can put string messages
  :recv - a core.async port whence string messages

This configurator constructor accepts the following options:

  :send - a zero-arg function called to create the :send port for each new websocket connection
  :recv - a zero-arg function called to create the :recv port for each new websocket connection
"
  ([connection-chan]
     (configurator connection-chan {}))
  ([connection-chan options]
     (fn [server]
       (let [ws-handler (handler connection-chan
                                 ;;TODO: change these to defaults that drop
                                 (:send options chan)
                                 (:recv options chan))
             existing-handler (.getHandler server)
             contexts (doto (ContextHandlerCollection.)
                        (.setHandlers (into-array [(doto (ContextHandler.)
                                                     (.setContextPath "/") ;;TODO: make route(s) configurable
                                                     (.setHandler ws-handler))

                                                   (doto (ContextHandler.)
                                                     (.setContextPath "/")
                                                     (.setHandler existing-handler))])))]
         (.setHandler server contexts)))))