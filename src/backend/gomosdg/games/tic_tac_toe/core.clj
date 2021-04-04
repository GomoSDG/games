(ns gomosdg.games.tic-tac-toe.core)

(def init-board
  [nil nil nil
   nil nil nil
   nil nil nil])

(defn get-only-element [s]
  (when (= (count s) 1)
    (first s)))

(defn check-row-win
  ([board r]
   (get-only-element
     (set (subvec board (* r 3) (+ (* r 3) 3)))))
  ([board]
   (check-row-win board 0)))

(defn check-column-win
  ([board c]
   (get-only-element
     (set (map #(get board %) [(+ 0 c) (+ 3 c) (+ 6 c)]))))
  ([board]
   (check-column-win board 0)))

(defn check-diagonal-winner
  [board direction]
  (get-only-element
    (case direction
      :left
      (set (map #(get board %) [0 4 8]))

      :right
      (set (map #(get board %) [2 4 6])))))

(defn get-winner
  "Returns the tic tac toe winner given a board.
   It returns nill when ther is no winner"
  [board]
  (some identity
        (-> []
          (conj (check-diagonal-winner board :left))
          (conj (check-diagonal-winner board :right))
          (conj (check-column-win board 0))
          (conj (check-column-win board 1))
          (conj (check-column-win board 2))
          (conj (check-row-win board 0))
          (conj (check-row-win board 1))
          (conj (check-row-win board 2)))))

(defn place-symbol! [board pos s]
  {:pre  [(<= 0 pos 8)
          (nil? (get @board pos))]
   :post [(= (count %) 9)]}
  (swap! board assoc pos s))
