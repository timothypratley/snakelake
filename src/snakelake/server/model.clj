(ns snakelake.server.model
  (:require
    [clojure.set :as set]))

(def width 80)
(def height 80)
(def max-food 5)

(defn new-board []
  (vec (repeat width (vec (repeat height nil)))))

(defonce world
  (ref {:board (new-board)
        :players {}}))

(def colors
  #{"#181818" "#282828" "#383838" "#585858"
    "#B8B8B8" "#D8D8D8" "#E8E8E8" "#F8F8F8"
    "#AB4642" "#DC9656" "#F7CA88" "#A1B56C"
    "#86C1B9" "#7CAFC2" "#BA8BAF" "#A16946"})

(def colors2
  (set (for [c1 colors
             c2 colors
             :when (not= c1 c2)]
         (str c1 c2))))

;; TODO: what to do when more than 16 players?
(defn next-uid [req]
  (let [available (set/difference colors2 (set (keys (:players @world))))]
    (some-> (seq available) (rand-nth))))

(defn clear? [x y]
  (every? nil?
          (for [i (range -2 3)
                j (range -2 3)]
            (get-in @world [:board (+ y j) (+ x i)]))))

(defn find-start
  ([] (find-start 0))
  ([depth]
   (let [x (+ 5 (rand-int 10))
         y (+ 5 (rand-int 10))
         dx (rand-nth [1 -1 0 0])
         dy (if (zero? dx)
              (rand-nth [1 -1])
              0)]
     (cond
       (clear? x y) [x y dx dy]
       (> depth 1000) nil
       :else (recur (inc depth))))))

(defn with-new-food [world]
  (if (< (count (for [i (range width)
                      j (range height)
                      :when (= (get-in world [:board j i]) "food")]
                  1))
         max-food)
    (if-let [available (seq (for [i (range width)
                                  j (range height)
                                  :when (not (get-in world [:board j i]))]
                              [j i]))]
      (assoc-in world (cons :board (rand-nth available)) "food")
      world)
    world))

(defn new-player [world uid]
  (let [health :alive
        [x y dx dy] (find-start)
        length 3
        path [[x y]]]
    (if x
      (-> world
        (assoc-in [:players uid] [health x y dx dy length path])
        (assoc-in [:board y x] uid)
        (with-new-food))
      (do
        (println "No space for new player")
        world))))

(defn enter-game [uid]
  (dosync (alter world new-player uid)))

(defn trim-tail [world [uid [health x y dx dy length path]]]
  (let [length (cond-> length (and (pos? length) (not= health :alive)) dec)]
    (if (> (count path) length -1)
      (-> world
        (assoc-in (cons :board (reverse (last path))) nil)
        (update-in [:players uid 6] #(take length %))
        (assoc-in [:players uid 5] length))
      world)))

(defn trim-tails [world]
  (reduce trim-tail world (:players world)))

(defn move [world [uid [health x y dx dy]]]
  (if (= health :alive)
    (let [nx (+ x dx)
          ny (+ y dy)]
      (if (or (not (and (<= 0 nx (dec width)) (<= 0 ny (dec height))))
              (when-let [owner (get-in world [:board ny nx])]
                (not= owner "food")))
        (assoc-in world [:players uid 0] :dead)
        (let [ate? (= (get-in world [:board ny nx]) "food")]
          (-> world
            (assoc-in [:board ny nx] uid)
            (assoc-in [:players uid 1] nx)
            (assoc-in [:players uid 2] ny)
            (update-in [:players uid 5] #(cond-> % ate? (+ 3)))
            (update-in [:players uid 6] #(cons [nx ny] %))
            (cond-> ate? with-new-food)))))
    world))

(defn moves [world]
  (reduce move world (:players world)))

(defn apply-dir [world [uid [dx dy]]]
  (if (get-in world [:players uid])
    (-> world
      (assoc-in [:players uid 3] dx)
      (assoc-in [:players uid 4] dy))
    world))

(defn dirs [world]
  (reduce apply-dir world (:dirs world)))

(defn step [world]
  (moves (trim-tails (dirs world))))

(defn tick []
  (dosync (alter world step)))

(def valid? #{1 -1 0})

(defn with-dir [world uid dx dy]
  (if-let [[health x y cdx cdy] (get-in world [:players uid])]
    (if (and
          (or (zero? dx) (zero? dy))
          (not (= dx dy 0))
          (and (valid? dx) (valid? dy))
          (and (not= dx (- cdx)))
          (and (not= dy (- cdy))))
      (assoc-in world [:dirs uid] [dx dy])
      world)
    world))

(defn dir [uid dx dy]
  (dosync (alter world with-dir uid dx dy)))

(defn board-without-player [board uid]
  (mapv (fn [v]
          (mapv #(when (not= uid %) %) v))
        board))

(defn without-player [world uid]
  (-> world
    (update :players dissoc uid)
    (update :board board-without-player uid)))

(defn remove-player [uid]
  (dosync (alter world without-player uid)))