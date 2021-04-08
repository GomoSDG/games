(ns gomosdg.games.core
  (:require [org.httpkit.server :as server]
            [clojure.tools.logging :refer [info]]
            [ring.middleware.json :refer [wrap-json-params]]
            [gomosdg.games.server.rooms.tic-tac-toe.core :as ttt]
            (compojure [core :refer [defroutes GET]])))

(defn async-handler [ring-request]
  (server/with-channel ring-request channel
    (info "Connection established!")
    (if (server/websocket? channel)
      (do
        (info "Adding user to lobby")
        (swap! ttt/lobby conj channel) ;; add user to lobby
        (swap! ttt/lobby ttt/establish-rooms))

      (server/send! channel {:status  200
                             :headers {"Content-Type" "text/plain"}
                             :body    "We can do both at the same time!"}))))

(defroutes games
  (GET "/tic-tac-toe" []
       #'async-handler))

(def server (server/run-server (-> #'games
                                   (wrap-json-params :keywords? true)) {:port 8080}))

(comment
  

  (def i (atom 0))

  (info "hello")
  (println "Hello!")
  (server)
  )

