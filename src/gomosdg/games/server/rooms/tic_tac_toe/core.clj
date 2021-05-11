(ns gomosdg.games.server.rooms.tic-tac-toe.core
  (:require [org.httpkit.server :as server]
            [clojure.data.json :refer [read-json write-str]]
            [gomosdg.games.tic-tac-toe.domain.core :as ttt]
            [gomosdg.games.rooms.messages.core :as m]
            [hiccup.core :as h]
            [gomosdg.games.rooms.core :as r]
            [gomosdg.games.tic-tac-toe.core :as c.ttt]
            [gomosdg.games.views.core :as views]
            [clojure.core.async :refer [<!! timeout go-loop chan put! <!]]))


(def lobby (atom clojure.lang.PersistentQueue/EMPTY))

(defn onboard-player [game-state p-ch sym]
  (let [player-info {:id      (.toString (java.util.UUID/randomUUID))
                     :channel p-ch
                     :symbol  sym}]
    (swap! game-state update :players assoc (:id player-info) player-info)

    (server/send! p-ch (write-str (dissoc player-info :channel)))
    (println "Message sent to player!")))

(defn set-turns [gs]
  (assoc gs :turns (vals (:players gs))))

(defmulti process-message (fn [_ {:keys [command]}] (:type command)))

(defn board->stream [board]
  (h/html
    [:turbo-stream {:action "replace"
                    :target "game-board"}
     [:template
      (views/game-board-3 board)]]))

(defmethod process-message "place-symbol"
  [game-state {:keys [command]}]
  (let [cur-player (first (:turns game-state))
        nxt-player (second (:turns game-state))
        nxt-turn   (reverse (:turns game-state))
        nxt-state  (update game-state :board ttt/place-symbol (:pos command) (:symbol cur-player))]
    ;; Make sure it's is the player's turn.
    (when-not (= (:id command) (:id cur-player))
      (throw (ex-info "Not the player's turn"
                      {:type :not-players-turn-exception})))

    ;; handle place symbol message.
    (server/send! (:channel cur-player) (board->stream (:board nxt-state)))
    (server/send! (:channel nxt-player) (board->stream (:board nxt-state)))
    (assoc nxt-state :turns nxt-turn)))

(defmulti handle-game-exception
  (fn [ex game-state msge]
    (-> (ex-data ex)
        :type)))

(defmethod handle-game-exception :invalid-position
  [ex game-state msge]
  (println "Game State: " game-state)
  (server/send! (:channel msge)
                (m/render-html {:action "append"
                                  :type   :danger
                                  :body   "You cannot place symbol there."})))

(defmethod handle-game-exception :not-players-turn-exception
  [_ game-state msge]
  (server/send! (:channel msge) (h/html [:turbo-stream {:action "append"
                                                        :target "messages"}
                                         [:template
                                          [:span#message-box.has-text-danger
                                           {:data-controller "message-box"}
                                           "It is not currently your turn"]]])))

(defn announce-winner [game-state winner]
  (let [chans (map :channel (vals (:players game-state)))]
    (doseq [c chans]
      (server/send! c (h/html [:turbo-stream {:target "messages"
                                              :action "append"}
                               [:template
                                [:span#message-box.has-text-info
                                 {:data-controller "message-box"}
                                 "The game has been won by player: " winner]]])))))

(defn start-room [game-chan game-state]
  (go-loop []
    (println "Waiting for message.")
    (let [msge (<! game-chan)]
      (println "Received message: " msge)
      (try

        ;; process message
        (swap! game-state process-message msge)
        (println "Processed message. New game-state: " @game-state)

        ;; check for winner
        (when-let [winner (ttt/get-winner (:board @game-state))]
          (println "The game has been won by player: " winner)
          (announce-winner @game-state winner))

        (catch clojure.lang.ExceptionInfo e
          (handle-game-exception e @game-state msge))))
    (recur)))

(defn create-room [p1-chan p2-chan]
  (println "starting room with 2 players")
  (let [game-chan  (chan)
        game-state (atom {:board   ttt/init-board
                          :players (sorted-map)})]

    ;; Onboard players
    (onboard-player game-state p1-chan :x)
    (onboard-player game-state p2-chan :o)

    ;; Set turns
    (swap! game-state set-turns)
    (println "First player is: "
          (-> @game-state :turns first :id)
          " -- "
          (-> @game-state :turns first :symbol))

    ;; Setup communications
    (server/on-receive p1-chan #(put! game-chan (do
                                                  (println "The command: " %)
                                                  {:command (read-json %)
                                                 :channel p1-chan})))
    (server/on-receive p2-chan #(put! game-chan {:command (read-json %)
                                                 :channel p2-chan}))

    ;; Start room
    (start-room game-chan game-state)))

(defn establish-rooms [players]
  (let [room (c.ttt/create-room)
        players (loop [l players]
                  (if-not (>= (count l) 2)
                    l ;; give back the new uneven list



                    (do ;; start-rooms for users with partners
                      (for [p (take 2 l)]
                        (do
                          (println "Added to: " (r/add-user! room {:username (java.util.UUID/randomUUID)
                                                                   :chan     p}))
                          (println "Players: " (r/list-players room))))
                      (r/start! room)

                      (println (r/list-players room))

                      (recur (drop 2 l)))))]
    players))
