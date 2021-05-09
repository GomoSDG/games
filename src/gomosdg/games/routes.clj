(ns gomosdg.games.routes
  (:require [org.httpkit.server :as server]
            [gomosdg.games.views.core :as views]
            [gomosdg.games.server.rooms.tic-tac-toe.core :as ttt]
            [gomosdg.views.layout :as layouts]
            [manifold.bus :as bus]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            (compojure [core :refer [defroutes GET]])))

(def game-rooms (bus/event-bus))

(defn tic-tac-toe [req]
  ;; First check if the user is authenticated.
  (when-not (authenticated? req)
    (throw-unauthorized))

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

(defroutes routes*
  (GET "/tic-tac-toe" []
       tic-tac-toe))

(def routes (-> routes*
                (wrap-authentication gomosdg.auth.core/auth-backend)
                (wrap-authorization gomosdg.auth.core/auth-backend)))

(comment
  (server)
  @(http/get "http://localhost:3080/games/tic-tac-toe")
  @(http/websocket-client "ws://localhost:3080/games/tic-tac-toe")
  )

