(ns snakelake.view
  (:require
    [clojure.string :as string]
    [goog.events :as events]
    [goog.events.KeyCodes :as KeyCodes]
    [snakelake.model :as model]
    [snakelake.communication :as communication]
    [goog.crypt :as crypt]
    [goog.dom.forms :as forms])
  (:import
    [goog.crypt Md5]))

(defn dir [e [dx dy]]
  (.preventDefault e)
  (communication/dir dx dy))

(defn keydown [e]
  (when (model/alive?)
    (condp = (.-keyCode e)
      KeyCodes/LEFT (dir e [-1 0])
      KeyCodes/A (dir e [-1 0])
      KeyCodes/RIGHT (dir e [1 0])
      KeyCodes/D (dir e [1 0])
      KeyCodes/UP (dir e [0 -1])
      KeyCodes/W (dir e [0 -1])
      KeyCodes/DOWN (dir e [0 1])
      KeyCodes/S (dir e [0 1])
      nil)))

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
      :view-box [0 0 (inc width) (inc height)]
      :preserve-aspect-ratio "xMidYMid meet"}
     [:rect
      {:width (inc width)
       :height (inc height)
       :fill "white"}]
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

(defn respawn-form []
  [:div
   {:style {:position "relative"
            :top "100px"
            :height 0
            :z-index 1}}
   (if (model/alive?)
     [:h1]
     [:h1
      [:form
       {:on-submit
        (fn [e]
          (.preventDefault e)
          (model/username! (forms/getValueByName (.-target e) "username"))
          (communication/send-username)
          (communication/respawn))}
       [:label "You are "]
       [:input
        {:type "text"
         :name "username"
         :auto-focus "autofocus"
         :default-value (:username @model/app-state)
         :style {:font-size "0.9em"}}]
       " "
       [:button {:type "submit"}
        "Respawn"]
       [:br]
       [:br]
       [:div {:style {:font-size "0.5em"}}
        "(Can be a "
        [:a {:href "https://en.gravatar.com/"
                            :target "_blank"}
         "gravatar"]
        ")"]]])])

(defn get-length [[uid [health x y dx dy length path username]]]
  [length (string/lower-case username)])

(defn md5-hash [s]
  (let [md5 (Md5.)]
    (.update md5 (string/trim s))
    (crypt/byteArrayToHex (.digest md5))))

(defn gravatar-img [email]
  (str "//www.gravatar.com/avatar/" (md5-hash email) "?s=30&d=wavatar"))

(defn scores [{{:keys [board players]} :world my-uid :uid}]
  [:div
   {:style {:position "relative"
            :float "right"
            :height "0"}}
   [:table
    [:thead
     [:tr
      [:th ""]
      [:th "Players"]
      [:th ""]]]
    [:tbody
     (doall
       (for [[uid [health x y dx dy length path username]] (reverse (sort-by get-length players))]
         ^{:key uid}
         [:tr
          [:td.number (if (zero? length) "Dead" length)]
          [:td {:style {:color (subs uid 0 7)
                        :background-color (subs uid 7 14)}}
           (first (string/split username #"@"))]
          [:td [:img {:src (gravatar-img username)}]]]))]]])

(defn sound-track []
  [:div
   [:audio
    {:controls "true"
     :auto-play "true"
     :loop "true"}
    [:source {:src "https://ia801504.us.archive.org/16/items/AhrixNova/Ahrix%20-%20Nova.mp3"}]
    "Your browser does not support the audio element."]
   [:div "Ahrix - Nova [NCS Release]"]])

(defn main []
  [:div.content
   [:h1 "Snake Lake" (when (not (string? (:uid @model/app-state)))
                      " - Server is full!")]
   [:center
    [sound-track]
    [respawn-form]
    [scores @model/app-state]
    [board @model/app-state]
    [:p "Multiplayer - invite your friends. Steer with the arrow keys, WASD, or click/touch the side of the board."]
    [:p [:a {:href "https://github.com/timothypratley/snakelake"
             :target "_blank"}
         "Source code"]]]])
