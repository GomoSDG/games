(ns gomosdg.games.tic-tac-toe.core
  "Contains multiple hotwire web client implementation of tic tac toe."
  (:require [gomosdg.games.rooms.core :refer [Room] :as r]
            [clojure.data.json :refer [read-json write-str]]
            [hiccup.core :as h]
            [gomosdg.games.tic-tac-toe.domain.core :as d.ttt]
            [gomosdg.games.rooms.messages.core :as m]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [gomosdg.views.forms :as forms]
            [gomosdg.games.views.core :as views]))

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
  (r/send-to-user! room
                   (get-in msge [:user :username])
                   (m/render-html {:action "append"
                                   :type   :danger
                                   :body   "It is not currently your turn"})))

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

(defn ttt-cell [i val]
  [:button.title.button.is-light {:data-pos    i
                                  :data-action "click->tic-tac-toe#placeSymbol"}
   val])

(defn game-board-3 [board-vals]
  [:div.columns.is-centered.is-multiline.is-mobile {:id "game-board"}
   (map-indexed (fn [i val]
                  [:div.column.is-one-third.has-text-centered.p-1
                   {:style "border-style: solid; border-width: 0.5px"}
                   (ttt-cell i val)])
                board-vals)])

(defn board->stream [board]
  (h/html
   [:turbo-stream {:action "replace"
                   :target "game-board"}
    [:template
     (game-board-3 board)]]))

(defn view
  ([] (view (repeat 9 " - ") nil))

  ([{:keys [board] :as room} username]
   (println "The room is" room)
   [:turbo-frame {:id              "tic-tac-toe"
                  :data-controller "tic-tac-toe"}
    [:section.section
     [:section.hero
      [:div.hero-body
       [:h1.title "Tic Tac Toe"]
       [:h1.subtitle "The Game Is On!"]]]
     [:div
      [:div.has-text-centered.block
       [:div#messages]]
      [:div.columns.is-centered
       [:div.column.is-3-widescreem.is-5-desktop
        (game-board-3 @board)
        (r/options-panel-for room username)]]]]]))

(defrecord TicTacToeRoom [room-id name board users players bus turns invited])

(defn pick-random-players [users]
  (let [x-player (rand-nth users)
        o-player (rand-nth (vec (clojure.set/difference (set users) #{x-player})))]
    {:x x-player
     :o o-player}))

(defn get-player-by-symbol [room symbol]
  (-> (:players room)
      deref
      symbol
      :username))

(defn game-loop [room msge]
  (try

    ;; process message
    (swap! (:board room) process-message msge @(:players room) (first @(:turns room)))
    (swap! (:turns room) reverse)

    (println "Processed message. New game-state: " @(:board room))

    (r/broadcast! room (board->stream @(:board room)))

    ;; check for winner
    (when-let [winner (d.ttt/get-winner @(:board room))]
      (println "The game has been won by player: " winner)
      (r/broadcast! room (m/render-html {:action "append"
                                         :type   :info
                                         :body   (str "The game has been won by " (get-player-by-symbol room winner))})))

    (catch clojure.lang.ExceptionInfo e
      (handle-game-exception e @(:board room) msge room))))

(extend-type TicTacToeRoom
  Room
  (add-user! [room user]
    (let [bus (:bus room)
          stream (:stream user)
          topic :room]
      (swap! (:users room) assoc (:username user) user)

      (s/consume #(bus/publish! bus topic {:command (read-json %)
                                           :user user})
                 stream)

      (s/on-closed stream #(swap! (:users room) dissoc (:username user)))

      (s/connect (bus/subscribe bus (:username user)) stream)

      (s/connect (bus/subscribe bus :broadcast) stream)))

  (remove-user! [room user]
    (swap! (:players room) dissoc (:username user)))

  (send-to-user! [room username message]
    (bus/publish! (:bus room) username message))

  (start! [room]
    (let [stream (bus/subscribe (:bus room) :room)]
      (reset! (:players room) (pick-random-players (vals @(:users room))))
      (s/consume (partial game-loop room) stream)))

  (stop! [room]
    nil)

  (reset! [room]
    (let [board (:board room)
          players (:players room)
          users (:users room)]
      (reset! players (pick-random-players (vals @users)))
      (reset! board d.ttt/init-board)
      (r/broadcast! room (board->stream @board))))

  (broadcast! [room message]
    (bus/publish! (:bus room) :broadcast message))

  (contains-user? [room user]
    (contains? @(:users room) (:username user)))

  (get-name [room]
    (:name room))

  (get-id [room]
    (:room-id room))

  (options-panel-for [room username]
    [:div
     [:div.is-group
      (forms/render-form {:action (str "/games/rooms/" (:room-id room))
                          :method "post"}
                         [{:control :input
                           :type :hidden
                           :name "command"
                           :value "start"}
                          {:control :button
                           :type :submit
                           :value "Start"
                           :colors [:light :primary]}])

      (forms/render-form {:action (str "/games/rooms/" (:room-id room))
                          :method "post"}
                         [{:control :input
                           :type :hidden
                           :name "command"
                           :value "reset"}
                          {:control :button
                           :type :submit
                           :value "Reset"
                           :colors [:light :info]}])]])

  (get-view [room]
    (view room nil))

  (list-players [room]
    (vals @(:users room))))

(defmethod r/create-room  :tic-tac-toe
  [{:keys [name invited]}]
  (TicTacToeRoom. (.toString (java.util.UUID/randomUUID))
                  name
                  (atom d.ttt/init-board)
                  (atom {})
                  (atom {})
                  (bus/event-bus)
                  (atom [:x :o])
                  (atom invited)))
