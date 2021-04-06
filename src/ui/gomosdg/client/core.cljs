(ns gomosdg.client.core)

(comment
  (def p1 (js/WebSocket. "ws://localhost:8080/tic-tac-toe"))
  (.send p1 (.stringify js/JSON (js-obj "type" "place-symbol"
                                        "id" "1"
                                        "symbol" "o"
                                        "pos" 2)))
  (set! (.-onmessage p1)
        (fn [e]
          (js/console.log "Message for p1: " (.-data e))))
  (def p2 (js/WebSocket. "ws://localhost:8080/tic-tac-toe"))
  (set! (.-onmessage p1)
        (fn [e]
          (js/console.log "Message for p2: " (.-data e))))
  conn
  (.send conn (.stringify js/JSON (js-obj "Hello" "world"))))


;; We to make sure that messages are received by the right component.
;; No users have an id in the beginning. Something that can be simplified by cognito, but no time.
;; Consider making the rooms reusable. Not really what I want.

