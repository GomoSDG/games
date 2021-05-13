(ns gomosdg.games.rooms.core)

(def rooms (atom {}))

(defprotocol Room
  "A room abstracts away all messaging concepts from the game."
  (add-user! [this user]
    "Method to add user to room.")
  (remove-user! [this username]
    "Method to remove user from room.")
  (start! [this]
    "Starts the room. Game is the function that starts the game loop.")
  (stop! [this]
    "Handles ")
  (broadcast! [this message]
    "Broadcasts message to all members of the room.")
  (contains-user? [this username]
    "Checks if the user is in the room.")
  (send-to-user! [this username message])

  (get-id [this])

  (is-owner? [this username])

  (get-name [this])

  (get-view [this])

  (has-access? [this username])

  (options-panel-for [this username])

  (configure! [this username options])

  (restart! [this])

  (started? [this]
    "States whether game has started or not.")
  (list-players [this]))

(defn list-rooms-for [username]
  (vals @rooms))

(defn get-room [id]
  (@rooms id))

(defmulti create-room :game)

(comment
  (-> (vals @rooms)
      first)

  (reset! (-> (vals @rooms)
                    first))
  @rooms
  (let [room (create-room {:name "My Cool Room 4"
                           :invited #{"Gomotso" "Stha"}
                           :game :tic-tac-toe})]
    (swap! rooms assoc (:room-id room) room))
r/rooms
  )

