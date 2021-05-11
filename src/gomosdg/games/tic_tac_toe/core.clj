 (ns gomosdg.games.tic-tac-toe.core
  (:require [gomosdg.games.rooms.core :refer [Room] :as r]
            [clojure.data.json :refer [read-json write-str]]
            [hiccup.core :as h]
            [gomosdg.games.tic-tac-toe.domain.core :as d.ttt]
            [gomosdg.games.rooms.messages.core :as m]
            [manifold.deferred :as d]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [clojure.core.async :refer [chan] :as a]
            [gomosdg.games.views.core :as views]))

(defn board->stream [board]
  (h/html
    [:turbo-stream {:action "replace"
                    :target "game-board"}
     [:template
      (views/game-board-3 board)]]))

(defmulti handle-game-exception
  (fn [ex game-state msge room]
    (-> (ex-data ex)
        :type)))

(defmethod handle-game-exception :invalid-position
  [ex game-state msge room]
  (println "Game State: " game-state)
  (r/send-to-user! room (get-in msge [:user :username])
                (m/render-html {:action "append"
                                :type   :danger
                                :body   "You cannot place symbol there."})))

(defmethod handle-game-exception :not-players-turn-exception
  [_ game-state msge room]
  (r/send-to-user! room (get-in msge [:user :username])
                   (m/render-html {:action "append"
                                   :type   :danger
                                   :body   "It is not currently your turn"})
                   (h/html [:turbo-stream {:action "append"
                                           :target "messages"}
                            [:template
                             [:span#message-box.has-text-danger
                              {:data-controller "message-box"}
                              "It is not currently your turn"]]])))

(defmulti process-message (fn [board msge players turn]
                            (-> (:command msge)
                                (:type)
                                (keyword))))

(defmethod process-message :place-symbol
  [board {:keys [user command]} players turn]
  (let [current-players (set (map :username (vals players)))
        is-player?      (comp current-players :username)
        is-current-turn? #(= (:username %) (get-in players [turn :username]))]

    (when-not (is-player? user)
      (throw (ex-info "You are only a spectator. Cannot play."
                      {:type    :not-players-turn-exception
                       :message "You are only a spectator. Cannot play."})))

    (when-not (is-current-turn? user)
      (throw (ex-info "It is not yet your turn. Please wait..."
                      {:type    :not-players-turn-exception
                       :message "It is not yet your turn. Please wait..."})))

    (d.ttt/place-symbol board (:pos command) turn)))

(defrecord TicTacToeRoom [room-id board players bus turns])

(extend-type TicTacToeRoom
  Room
  (add-user! [room user]
    (let [bus (:bus room)
          stream (:stream user)
          topic :room]
      (swap! (:players room) assoc (:username user) user)

      (s/consume #(bus/publish! bus topic {:command (read-json %)
                                           :user user})
                 stream)

      (s/connect (bus/subscribe bus (:username user)) stream)

      (s/connect (bus/subscribe bus :broadcast) stream)))

  (remove-user! [room user]
    (swap! (:players room) dissoc (:username user)))

  (send-to-user! [room username message]
    (bus/publish! (:bus room) username message))

  (start! [room]
        (let [pls {:x (first (vals @(:players room)))
                   :o (second (vals @(:players room)))}
              stream (bus/subscribe (:bus room) :room)]

          

          (s/consume
           (fn [msge]
             (try

               ;; process message
               (swap! (:board room) process-message msge pls (first @(:turns room)))
               (swap! (:turns room) reverse)
               (println "Processed message. New game-state: " @(:board room))

               (r/broadcast! room (board->stream @(:board room)))

               ;; check for winner
               (when-let [winner (d.ttt/get-winner @(:board room))]
                 (println "The game has been won by player: " winner)
                 )


               (catch clojure.lang.ExceptionInfo e
                 (handle-game-exception e @(:board room) msge room))))
           stream)))
  (stop! [room]
    nil)

  (broadcast! [room message]
    (bus/publish! (:bus room) :broadcast message))

  (contains-user? [room user]
    (contains? @(:players room) (:username user)))

  (list-players [room]
    (vals @(:players room))))

(defn create-room [bus]
  (TicTacToeRoom. (.toString (java.util.UUID/randomUUID))
                  (atom d.ttt/init-board)
                  (atom {})
                  bus
                  (atom [:x :o])))
