(ns gomosdg.games.core
  (:require [org.httpkit.server :as server]
            [clojure.tools.logging :refer [info]]
            [ring.middleware.json :refer [wrap-json-params]]
            [clojure.data.json :refer [read-json]]
            (compojure [core :refer [defroutes GET POST]])))

(def lobby (atom clojure.lang.PersistentQueue/EMPTY))

(defn start-room [p1-chan p2-chan]
  (info "Room started with players: " p1-chan p2-chan))
(take 2 @lobby)

(defn establish-rooms [players]
  (loop [l players]
    (if-not (>= (count l) 2)
      l ;; give back the new uneven list

      (do ;; start-rooms for users with partners
        (apply start-room (seq (take 2 players)))
        (recur (nnext l))))))

(comment

  (swap! lobby conj 2)

  (count @lobby)

  (= (mod (count @lobby) 2) 0)

  (seq @lobby)



  (server/on-receive channel (fn [msge]
                               (let [data (read-json msge)]
                                 (info "Received data: " data)
                                 (server/send! channel "hello")))))

(defn async-handler [ring-request]
  (server/with-channel ring-request channel
    (info "Connection established!")
    (if (server/websocket? channel)
      (do
        (info "Adding user to lobby")
        (swap! lobby conj channel) ;; add user to lobby
        (swap! lobby establish-rooms))

      (server/send! channel {:status  200
                             :headers {"Content-Type" "text/plain"}
                             :body    "We can do both at the same time!"}))))

(defroutes games
  (GET "/tic-tac-toe" []
       #'async-handler))

(comment
  (def server (server/run-server (-> #'games
                                     (wrap-json-params :keywords? true)) {:port 8080}))

  (def i (atom 0))

  (info "hello")
  (println "Hello!")
  (server)
  )

