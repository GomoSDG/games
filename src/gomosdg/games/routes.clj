(ns gomosdg.games.routes
  (:require [gomosdg.games.views.core :as views]
            [manifold.deferred :as d]
            [gomosdg.auth.core :as auth]
            [gomosdg.views.layout :as layouts]
            [manifold.stream :as s]
            [aleph.http :as http]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [gomosdg.games.rooms.core :as r]
            [gomosdg.games.rooms.views :as v]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            (compojure [core :refer [defroutes GET POST]])))

(comment
  (-> (vals @rooms)
      first
      :board
      deref)
  @rooms
  (let [room (r/create-room {:name "My Cool Room"
                             :invited #{"Gomotso" "Stha"}
                             :game :tic-tac-toe})]
    (swap! rooms assoc (:room-id room) room))
  )

;; TODO: put the handlers together again.

(defn rooms-handler [req]
  ;; (when-not (authenticated? req)
  ;;   (throw-unauthorized))

  (let [username (get-in req [:session :identity])
        rooms (r/list-rooms-for username)
        ;; user-rooms (filter #(r/contains-user? % ) rooms)
        ]
    (layouts/main "SDG - Rooms" (v/list-rooms rooms))))

(defn room-ws-handler [req]
  (d/let-flow [room-id (get-in req [:params :room-id])
               username (get-in req [:session :identity])
               room (r/get-room room-id)
               conn (d/catch
                        (http/websocket-connection req)
                        (fn [_] nil))]

              (if conn
                (r/add-user! room {:username (.toString (java.util.UUID/randomUUID))
                                   :stream conn})
                (layouts/main (str "SDG Rooms - " (r/get-name room))
                              (r/get-view room)))))

(defn room-handler [req]
  (let [room-id (get-in req [:params :room-id])
        room (r/get-room room-id)]

    (layouts/main (str "SDG Rooms - " (r/get-name room))
                  (r/get-view room))))

(defn handle-room-command [req]
  (let [room-id (get-in req [:params :room-id])
        command (get-in req [:params :command])
        room (r/get-room room-id)]

    (case command
      "start" (r/start! room)
      "reset" (r/reset! room))))

(defroutes routes*
  (GET "/rooms" []
       rooms-handler)
  (GET "/rooms/:room-id" []
       room-handler)
  (POST "/rooms/:room-id" []
        handle-room-command)
  (GET "/rooms/:room-id/ws" []
       room-ws-handler))

(for [n (range 5)]

  (let [room (r/create-room {:name (str "My Cool Room " n)
                           :invited #{"Gomotso" "Stha"}
                           :game :tic-tac-toe})]
    (swap! r/rooms assoc (:room-id room) room)))

(def routes (-> #'routes*
                (wrap-authentication auth/auth-backend)
                (wrap-authorization auth/auth-backend)))
