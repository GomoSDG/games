(ns hotreload
  (:require [gomosdg.core :refer [app]]
            [ring.middleware.reload :refer [wrap-reload]]
            [gomosdg.games.rooms.core :as r]
            [aleph.http :as http])
  (:gen-class))


(def dev-handler
  #'app)

(defn -main [& args]
  (try
    (reset! gomosdg.auth.core/users (clojure.edn/read-string (slurp "users.edn")))
    (catch Exception e
      ))

  (loop [rng (range 10)]
    (when-let [n (first rng)]
      (let [room (r/create-room {:name (str "My Cool Room " n)
                                 :invited #{"Gomotso" "Stha"}
                                 :game :tic-tac-toe})]
        (swap! r/rooms assoc (:room-id room) room)
        (recur (rest rng)))))

  (def server (http/start-server dev-handler {:port 3080})))










