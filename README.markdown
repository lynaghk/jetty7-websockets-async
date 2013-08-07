# Jetty 7 websockets async

Did you know that your familiar friend Jetty 7 (of [ring-jetty-adapter](https://github.com/ring-clojure/ring/tree/master/ring-jetty-adapter) fame) can talk websockets?

This library provides a Jetty 7 configurator that exposes websockets as core.async channels.


## Quick start

Add to your `project.clj`:

    [com.keminglabs/jetty7-websockets-async "0.1.0-SNAPSHOT"]
    
Here's a small example of getting goin' with ring.
See the [in-depth example](example/) for fancy core.match message dispatch and a core.async client in ClojureScript.

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


## Server

## Client

## Thanks
