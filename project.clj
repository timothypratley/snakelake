(defproject snakelake "0.1.0-SNAPSHOT"
  :description "Snakelake is a multiplayer snake game"
  :url "http://github.com/timothypratley/snakelake"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"
  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/tools.reader]]
                 [reagent "0.5.1"]
                 [com.taoensso/sente "1.8.1"]
                 [environ "1.0.2"]
                 [http-kit "2.1.19"]
                 [compojure "1.5.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring-cors "0.1.7"]]

  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]
            [lein-environ "1.0.2"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :main ^:skip-aot snakelake.server.main

  :profiles {:dev {:env {:dev? "true"}}
             :uberjar {:env {:production "true"}}}

  :uberjar-name "snakelake-standalone.jar"

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {}
                :compiler {:main snakelake.main
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/snakelake.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/snakelake.js"
                           :main snakelake.main
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"] })
