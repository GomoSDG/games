(ns gomosdg.client.core)

(comment
  (def conn (js/WebSocket. "ws://localhost:8080/tic-tac-toe"))
  conn
  (.send conn (.stringify js/JSON (js-obj "Hello" "world"))))


;; We to make sure that messages are received by the right component.
;; No users have an id in the beginning. Something that can be simplified by cognito, but no time.
;; Consider making the rooms reusable. Not really what I want.

