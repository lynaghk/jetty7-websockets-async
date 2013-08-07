# Jetty 7 websockets async

Did you know that your familiar friend Jetty 7 (of [ring-jetty-adapter](https://github.com/ring-clojure/ring/tree/master/ring-jetty-adapter) fame) can talk websockets?
This library provides a Jetty 7 configurator that exposes websockets as core.async channels.

[Install](#install) | [Server quick-start](#server-quick-start) | [Client quick-start](#client-quick-start) | [Example app](/example) | [Thanks!](#thanks)

## Install

Add to your `project.clj`:

    [com.keminglabs/jetty7-websockets-async "0.1.0-SNAPSHOT"]

See the [in-depth example](example/) for fancy core.match message dispatch and a core.async client in ClojureScript.


## Server quick start

```clojure
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
```

## Client quick start

```clojure
(require '[com.keminglabs.jetty7-websockets-async.core :refer [connect!]]
         '[clojure.core.async :refer [chan go >! <!]])

(def c (chan))

(connect! c "ws://remote-server")

(go (loop []
      (let [ws-req (<! c)]
        (>! (:send ws-req) "Hello remote websocket server!")
        (recur))))
```

## Thanks

[Zach Allaun](https://github.com/zachallaun) for suggesting that the websocket server and client code could be handled symmetrically.
