(ns gomosdg.client.core)

(defn log-last-message [k t o n]
  (js/console.log "New Message: " (.parse js/JSON (last n))))

(def p1-msge (atom []))
(def p2-msge (atom []))

(add-watch p1-msge :watcher log-last-message)
(add-watch p2-msge :watcher log-last-message)

(comment
  @p1-msge
  @p2-msge
  (def p1 (js/WebSocket. "ws://localhost:8080/tic-tac-toe"))
  (.send p1 (.stringify js/JSON (js-obj "type" "place-symbol"
                                        "id" (.-id (.parse js/JSON (first @p1-msge)))
                                        "symbol" "o"
                                        "pos" 7)))
  (set! (.-onmessage p1)
        (fn [e]
          (swap! p1-msge conj (.-data e))))
  (def p2 (js/WebSocket. "ws://localhost:8080/tic-tac-toe"))
  (set! (.-onmessage p2)
        (fn [e]
          (swap! p2-msge conj (.-data e))))
  (.send p2 (.stringify js/JSON (js-obj "type" "place-symbol"
                                        "id" (.-id (.parse js/JSON (first @p2-msge)))
                                        "symbol" "o"
                                        "pos" 0)))
  )
