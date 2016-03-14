(ns snakelake.server.main
  (:require
    [org.httpkit.server :as server]
    [snakelake.server.routes :as routes]
    [environ.core :as environ]))

(defn -main []
  (println "Server starting...")
  (server/run-server #'routes/handler {:port (environ/env :http-port 3000)}))
