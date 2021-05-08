(ns gomosdg.games.routes
  (:require [org.httpkit.server :as server]
            [gomosdg.games.views.core :as views]
            [gomosdg.games.server.rooms.tic-tac-toe.core :as ttt]
            [gomosdg.views.layout :as layouts]
            [ring.util.response :as ring]
            [aleph.http :as http]
            [manifold.bus :as bus]
            [manifold.deferred :as d]
            (compojure [core :refer [defroutes GET context]]
                       [route :as route])))

(def game-rooms (bus/event-bus))

(defn tic-tac-toe [req]
  (server/with-channel req channel
    (println "Connection established!")
    (if (server/websocket? channel)
      (do
        (println "Adding user to lobby")
        (swap! ttt/lobby conj channel) ;; add user to lobby
        (swap! ttt/lobby ttt/establish-rooms))

      (server/send!
        channel
        (layouts/main "SDG - Tic Tac Toe" (views/tic-tac-toe))))))

(comment
  @(tic-tac-toe nil))

(defroutes routes
  (GET "/tic-tac-toe" []
       tic-tac-toe))

(comment
  (server)
  @(http/get "http://localhost:3080/games/tic-tac-toe")
  @(http/websocket-client "ws://localhost:3080/games/tic-tac-toe")
  )

