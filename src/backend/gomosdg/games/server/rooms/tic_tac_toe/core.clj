(ns gomosdg.games.server.rooms.tic-tac-toe.core
  (:require [org.httpkit.server :as server]
            [clojure.tools.logging :refer [info]]
            [clojure.data.json :refer [read-json write-str]]
            [gomosdg.games.tic-tac-toe.core :as ttt]
            [clojure.core.async :refer [<!! timeout go-loop chan put! <!]]))


(def lobby (atom clojure.lang.PersistentQueue/EMPTY))

(defn onboard-player [game-state p-ch sym]
  (let [player-info {:id      (.toString (java.util.UUID/randomUUID))
                     :channel p-ch
                     :symbol  sym}]
    (swap! game-state update :players assoc (:id player-info) player-info)
    (<!! (timeout 2500))
    (server/send! p-ch (write-str (dissoc player-info :channel)))
    (info "Message sent to player!")))

(defn set-turns [gs]
  (keys (:players gs)))

(defmulti process-message (fn [_ m] (:type m)))

(defmethod process-message "place-symbol"
  [{:keys [turns] :as game-state} {:keys [id pos] :as m}]

  ;; Make sure it's is the player's turn.
  (when-not (= id (-> (first turns)
                      :id))
    (throw (ex-info "Not the player's turn"
                    {:type :not-players-turn-exception})))

  ;; handle place symbol message.
  (let [cur-player (first turns)
        nxt-player (second turns)
        nxt-turn   (reverse turns)
        nxt-state  (update game-state :board ttt/place-symbol! pos (:symbol cur-player))]
    (server/send! (:channel cur-player) (write-str {:type :ack}))
    (server/send! (:channel nxt-player) (write-str (assoc m :symbol (:symbol cur-player))))
    (assoc nxt-state :turns nxt-turn)))

(defn start-room [game-chan game-state]
  (go-loop []
    (info "Waiting for message.")
    (let [msge (<! game-chan)]
      (info "Received message: " msge)
      (swap! game-state process-message msge)
      (when-let [winner (ttt/get-winner (:board @game-state))]
          (info "The game has been won by player: " winner))
      (info "Processed message. New game-state: " @game-state))
    (recur)))

(defn create-room [p1-chan p2-chan]
  (info "starting room with 2 players")
  (let [game-chan  (chan)
        game-state (atom {:board   ttt/init-board
                          :players {}})]

    ;; Onboard players
    (onboard-player game-state p1-chan :x)
    (onboard-player game-state p2-chan :o)

    ;; Set turns
    (swap! game-state update :turns set-turns)

    ;; Setup communications
    (server/on-receive p1-chan #(put! game-chan (read-json %)))
    (server/on-receive p2-chan #(put! game-chan (read-json %)))

    ;; Start room
    (start-room game-chan game-state)))

(defn establish-rooms [players]
  (loop [l players]
    (if-not (>= (count l) 2)
      l ;; give back the new uneven list

      (do ;; start-rooms for users with partners
        (apply create-room (seq (take 2 players)))
        (recur (nnext l))))))