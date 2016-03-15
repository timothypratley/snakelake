(ns snakelake.communication
  (:require
    [snakelake.ainit]
    [taoensso.sente :as sente]
    [snakelake.model :as model]))

(defonce channel-socket (sente/make-channel-socket! "/chsk" {:type :auto}))
(defonce chsk (:chsk channel-socket))
(defonce ch-chsk (:ch-recv channel-socket))
(defonce chsk-send! (:send-fn channel-socket))
(defonce chsk-state (:state channel-socket))

(defn dir [dx dy]
  (chsk-send! [:snakelake/dir [dx dy]]))

(defmulti event-msg-handler :id)

(defmethod event-msg-handler :default [{:as ev-msg :keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod event-msg-handler :chsk/state [{:as ev-msg :keys [?data]}]
  (if (= ?data {:first-open? true})
    (println "Channel socket successfully established!")
    (println "Channel socket state change:" ?data)))

(defmethod event-msg-handler :chsk/recv [{:as ev-msg :keys [?data]}]
  (model/world! (second ?data)))

(defmethod event-msg-handler :chsk/handshake [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (println "Handshake:" ?data)
    (model/uid! ?uid)))

(defonce router
  (sente/start-client-chsk-router! ch-chsk event-msg-handler))

(defn reconnect []
  (sente/chsk-reconnect! chsk))
