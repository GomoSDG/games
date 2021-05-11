(ns gomosdg.games.tic-tac-toe.domain.core)

(def init-board
  [:- :- :-
   :- :- :-
   :- :- :-])

(defn get-only-element [s]
  (when (= (count s) 1)
    (first s)))

(defn check-row-winner
  ([board r]
   (get-only-element
     (set (subvec board (* r 3) (+ (* r 3) 3)))))
  ([board]
   (check-row-winner board 0)))

(defn check-column-winner
  ([board c]
   (get-only-element
     (set (map #(get board %) [(+ 0 c) (+ 3 c) (+ 6 c)]))))
  ([board]
   (check-column-winner board 0)))

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
  {:post [(contains? #{:x :o nil} %)]}

  (some #{:x :o}
        (-> []
          (conj (check-diagonal-winner board :left))
          (conj (check-diagonal-winner board :right))
          (conj (check-column-winner board 0))
          (conj (check-column-winner board 1))
          (conj (check-column-winner board 2))
          (conj (check-row-winner board 0))
          (conj (check-row-winner board 1))
          (conj (check-row-winner board 2)))))

(defn place-symbol [board pos s]
  {:post [(= (count %) 9)]}

  (when-not (<= 0 pos 8)
    (throw (ex-info "Position out of bounds."
                    {:type :invalid-position})))

  (when-not (#{:x :o} s)
    (throw (ex-info "Invalid symbol."
                    {:type :invalid-symbol})))

  (when-not (= :- (get board pos))
    (throw (ex-info "Cannot place symbol on another symbol."
                    {:type :invalid-position})))

  (assoc board pos s))
