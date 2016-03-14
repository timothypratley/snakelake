(ns snakelake.model
  (:require
    [reagent.core :as reagent]))

(defonce app-state
  (reagent/atom {}))

(defn world! [world]
  (swap! app-state assoc :world world))

(defn uid! [uid]
  (swap! app-state assoc :uid uid))