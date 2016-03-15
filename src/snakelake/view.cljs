(ns snakelake.view
  (:require
    [goog.events :as events]
    [goog.events.KeyCodes :as KeyCodes]
    [snakelake.model :as model]
    [snakelake.communication :as communication]))

(defn dir [e [dx dy]]
  (.preventDefault e)
  (communication/dir dx dy))

(defn keydown [e]
  (condp = (.-keyCode e)
    KeyCodes/LEFT (dir e [-1 0])
    KeyCodes/A (dir e [-1 0])
    KeyCodes/RIGHT (dir e [1 0])
    KeyCodes/D (dir e [1 0])
    KeyCodes/UP (dir e [0 -1])
    KeyCodes/W (dir e [0 -1])
    KeyCodes/DOWN (dir e [0 1])
    KeyCodes/S (dir e [0 1])
    nil))

(defonce listener
  (events/listen js/document "keydown" keydown))

(defn segment [uid i j me?]
  [:rect
   {:x (+ i 0.55)
    :y (+ j 0.55)
    :fill (subs uid 0 7)
    :stroke-width 0.3
    :stroke (subs uid 7 14)
    :rx (if me? 0.4 0.2)
    :width 0.9
    :height 0.9}])

(defn food [i j]
  [:circle
   {:cx (inc i)
    :cy (inc j)
    :r 0.45
    :fill "lightgreen"
    :stroke-width 0.2
    :stroke "green"}])

(defn pixel [uid i j my-uid]
  (if (= uid "food")
    [food i j]
    [segment uid i j (= my-uid uid)]))

(defn eye [dx dy]
  [:circle
   {:cx (/ dx 2)
    :cy (/ dy 2)
    :r 0.2
    :stroke "black"
    :stroke-width 0.05
    :fill "red"}])

(defn click [e]
  (let [elem (.-target e)
         r (.getBoundingClientRect elem)
         left (.-left r)
         top (.-top r)
         width (.-width r)
         height (.-height r)
         ex (.-clientX e)
         ey (.-clientY e)
         x (- ex left (/ width 2))
         y (- ey top (/ height 2))]
    (dir e
         (if (> (js/Math.abs y) (js/Math.abs x))
           (if (pos? y)
             [0 1]
             [0 -1])
           (if (pos? x)
             [1 0]
             [-1 0])))))

(defn board [{{:keys [board players]} :world my-uid :uid}]
  (let [width (count (first board))
        height (count board)]
    [:svg.board
     {:on-click click
      :style {:cursor "pointer"
              :width "80%"
              :border "1px solid black"}
      :view-box [0 0 (inc width) (inc height)]
      :preserve-aspect-ratio "xMidYMid meet"}
     (doall
       (for [i (range width)
             j (range height)
             :let [uid (get-in board [j i])]
             :when uid]
         ^{:key [i j]}
         [pixel uid i j my-uid]))
     (doall
       (for [[uid [health x y dx dy]] players
             :when (= health :alive)]
         ^{:key uid}
         [:g
          {:transform (str "translate(" (inc x) " " (inc y) ")")}
          [eye
           (+ (/ dx 2) (/ dy 2))
           (+ (/ dy 2) (/ dx 2))]
          [eye
           (- (/ dx 2) (/ dy 2))
           (- (/ dy 2) (/ dx 2))]]))]))

(defn main []
  [:div.content
   [:h1 "Snakelake" (when (not (string? (:uid @model/app-state)))
                      " - Server is full!")]

   [:center
    [:audio
     {:controls "true"
      :loop "true"}
     [:source {:src "http://serve01.mp3skull.onl/get?id=FjNdYp2gXRY"}]
     "Your browser does not support the audio element."]
    [:div "Ahrix - Nova [NCS Release]"]]
   [:center
        [board @model/app-state]
    [:p "Steer with the arrow keys, WASD, or click/touch the side of the board."]]
   [:h1
    [:button
     {:on-click
      (fn [e]
        (communication/reconnect))}
     "Respawn"]]])
