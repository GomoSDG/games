(ns gomosdg.games.routes
  (:require [gomosdg.games.views.core :as views]
            [manifold.deferred :as d]
            [gomosdg.games.server.rooms.tic-tac-toe.core :as ttt]
            [gomosdg.auth.core :as auth]
            [gomosdg.views.layout :as layouts]
            [manifold.bus :as bus]
            [manifold.stream :as s]
            [aleph.http :as http]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [gomosdg.games.tic-tac-toe.core :as r.ttt]
            [gomosdg.games.rooms.core :as r]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            (compojure [core :refer [defroutes GET]])))

(def game-rooms (bus/event-bus))

(def rooms {"room1" (r.ttt/create-room game-rooms)})

(comment
  (if (:websocket? req)
    (server/as-channel req
                       {:on-open (fn [channel]
                                   (println "Adding user to lobby")
                                   (swap! ttt/lobby conj channel))})

    (layouts/main "SDG - Tic Tac Toe" (views/tic-tac-toe))))

(defn tic-tac-toe-ws [req]
  (when-not (authenticated? req)
    (throw-unauthorized))

  (clojure.pprint/pprint req)

  (d/let-flow [conn (d/catch
                        (http/websocket-connection req)
                        (fn [_] nil))
               room (get-in rooms ["room1"])]

              (if conn
                (do
                  (r/add-user! room {:username (java.util.UUID/randomUUID)
                                     :stream conn})
                  nil)
                (layouts/main "SDG - Tic Tac Toe" (views/tic-tac-toe)))))

(defn tic-tac-toe [req]
  ;; First check if the user is authenticated.
  (when-not (authenticated? req)
    (throw-unauthorized))

  (layouts/main "SDG - Tic Tac Toe" (views/tic-tac-toe)))

(defroutes routes*
  (GET "/tic-tac-toe" []
       tic-tac-toe)
  (GET "/tic-tac-toe-ws" []
       tic-tac-toe-ws))

(def routes (-> #'routes*
                (wrap-authentication auth/auth-backend)
                (wrap-authorization auth/auth-backend)))
