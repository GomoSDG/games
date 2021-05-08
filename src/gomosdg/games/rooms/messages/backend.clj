(ns gomosdg.games.rooms.messages.backend)

(defprotocol MessageRenderer
  (render [this message]))

(comment
  (extends? MessageRenderer html)
  )


