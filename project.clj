(defproject com.keminglabs/jetty7-websockets-async "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [org.eclipse.jetty/jetty-server "7.6.8.v20121106"]
                 [org.eclipse.jetty/jetty-websocket "7.6.8.v20121106"]]

  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :min-lein-version "2.0.0"

  :profiles {:dev {:source-paths ["dev" "example/src/clj"]
                   :dependencies [[ring/ring-jetty-adapter "1.2.0"]
                                  [compojure "1.1.5" :exclusions [ring/ring-core]]
                                  [org.clojure/core.match "0.2.0-rc5"]
                                  [midje "1.5.1"]]}}
  
  :source-paths ["src/clj" "src/cljs"]
  
  :plugins [[lein-cljsbuild "0.3.2"]]
  :cljsbuild {:builds
              [{:source-paths ["example/src/cljs"]
                :compiler {:output-to "example/public/example.js"
                           :pretty-print true
                           :optimizations :whitespace}}]})