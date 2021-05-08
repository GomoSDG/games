(ns gomosdg.games.rooms.core)

(defprotocol Room
  "A room abstracts away all messaging concepts from the game."
  (add-user [this user]
    "Method to add user to room.")
  (remove-user [this user]
    "Method to remove user from room.")
  (start [this game]
    "Starts the room. Game is the function that starts the game loop.")
  (stop [this]
    "Handles ")
  (broadcast! [this message]
    "Broadcasts message to all members of the room.")
  (send! [this ]))



