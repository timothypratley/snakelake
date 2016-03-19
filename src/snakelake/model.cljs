(ns snakelake.model
  (:require
    [reagent.core :as reagent]))

(def dictionary
  ["Slimy" "Slippery" "Slithering" "Serpentine" "Sassy" "Slick" "Sick" "Sophisticated"
   "Sentimental" "Super" "Sad" "Silly" "Scary" "Scheming" "Smart" "Smelly"
   "Snake" "Serpent" "Worm" "Viper" "Bite" "Coil" "Lake" "Coder"
   "Ninja" "Monk" "Pirate" "Guard" "Warrior" "Dancer" "Acrobat" "Mongoose"
   "Sente" "Dragon" "Clojure" "Nomnom"])

(defonce app-state
  (reagent/atom
    {:username (str (rand-nth dictionary) " " (rand-nth dictionary))}))

(defn world! [world]
  (swap! app-state assoc :world world))

(defn uid! [uid]
  (swap! app-state assoc :uid uid))

(defn username! [username]
  (swap! app-state assoc :username username))

(defn alive? []
  (when-let [me (:uid @app-state)]
    (= :alive (some-> @app-state :world :players (get me) (get 0)))))