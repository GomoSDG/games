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

;; TODO: Things that need to get done before we can start doing the UI:
;; * Send back acknowledgements. Might not be that necessary. ????
;; * Send back who is the winner.
;; * Handle leaving the room and sending people out.
;; * Move code into the right files. Determine suitable file structure.

