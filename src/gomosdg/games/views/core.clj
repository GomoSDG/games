(ns gomosdg.games.views.core)

(defn ttt-cell [i val]
  [:button.title.is-3 {:data-pos    i
                       :style "height: 15vh;width: 15vw;"
                       :data-action "click->tic-tac-toe#placeSymbol"}
   val])

(defn game-board [board-vals]
  (let [board (partition 3 (partition 2 (interleave board-vals
                                                    (range 9))))]
    [:table.table.is-bordered.block {:id "game-board"
                               :style "margin: auto;"}
     (map (fn [row]
            [:tr
             (doall (map (fn [[val i]]
                          [:td.has-text-centered
                           (ttt-cell i val)])
                         row))])
          board)]))

(defn game-board-2 [board-vals]
  [:div.columns.is-multiline {:id "game-board"}
   (map-indexed (fn [[i val]]
                  (println "We're in")
                  [:div.column.is-one-third.has-text-centered.m-0 {:style "border-style: solid;"}
                   [:a {:data-action "click->tic-tac-toe#placeSymbol"}
                    [:span.title.is-3 {:data-pos i}
                     val]]])
                board-vals)])

(defn tic-tac-toe []
  (let [board (repeat 9 " -- ")]
    [:turbo-frame {:id              "tic-tac-toe"
                   :data-controller "tic-tac-toe"}
     [:section.section
      [:section.hero
       [:div.hero-body
        [:h1.title "Tic Tac Toe"]
        [:h1.subtitle "The Game Is On!"]]]
      [:div
       [:div.has-text-centered.block
        [:div#messages
         ]]
       (game-board board)]]]))

(comment
  (tic-tac-toe))
