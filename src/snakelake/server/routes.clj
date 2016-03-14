(ns snakelake.server.routes
  (:require
    [snakelake.server.model :as model]
    [ring.middleware.defaults :as defaults]
    [ring.middleware.reload :as reload]
    [ring.util.response :as response]
    [environ.core :as environ]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.http-kit :as http-kit]
    [compojure.core :refer [defroutes GET POST]]))

(defonce channel-socket
  (sente/make-channel-socket!
    http-kit/sente-web-server-adapter
    {:user-id-fn #'model/next-uid}))
(defonce ring-ajax-post (:ajax-post-fn channel-socket))
(defonce ring-ajax-get-or-ws-handshake (:ajax-get-or-ws-handshake-fn channel-socket))
(defonce chsk-send! (:send-fn channel-socket))
(defonce connected-uids (:connected-uids channel-socket))

(defroutes routes
  (GET "/" req (response/content-type
                 (response/resource-response "public/index.html")
                 "text/html"))
  (GET "/status" req (str "Running: " (pr-str @connected-uids)))
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req)))

(def handler
  (-> #'routes
    (defaults/wrap-defaults defaults/site-defaults)
    (cond-> (environ/env :dev?) (reload/wrap-reload))))

(defmulti event :id)

(defmethod event :default [{:as ev-msg :keys [event]}]
  (println "Unhandled event: " event))

(defmethod event :snakelake/dir [{:as ev-msg :keys [event uid ?data]}]
  (let [[dx dy] ?data]
    (model/dir uid dx dy)))

(defmethod event :chsk/uidport-open [{:keys [uid client-id]}]
  (println "New connection:" uid client-id)
  (model/enter-game uid))

(defmethod event :chsk/uidport-close [{:keys [uid]}]
  (println "Disconnected:" uid)
  (model/remove-player uid))

(defmethod event :chsk/ws-ping [_])

(defonce router
  (sente/start-chsk-router! (:ch-recv channel-socket) event))

(defn broadcast []
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid [:snakelake/world @model/world])))

(defn ticker []
  (while true
    (Thread/sleep 200)
    (try
      (model/tick)
      (broadcast)
      (catch Exception ex
        (println ex)))))

(defonce ticker-thread
  (doto (Thread. ticker)
    (.start)))
