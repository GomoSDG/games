(ns gomosdg.games.core
  (:require [org.httpkit.server :as server]
            [clojure.tools.logging :refer [info]]
            [ring.middleware.json :refer [wrap-json-params]]
            [clojure.data.json :refer [read-json write-str]]
            [gomosdg.games.tic-tac-toe.core :as ttt]
            [clojure.core.async :refer [<!! timeout]]
            (compojure [core :refer [defroutes GET POST]])))

(def lobby (atom clojure.lang.PersistentQueue/EMPTY))

(defn onboard-player [game-state p-ch sym]
  (let [player-info {:id      (.toString (java.util.UUID/randomUUID))
                     :channel p-ch
                     :symbol  sym}]
    (swap! game-state update :players assoc (:id player-info) player-info)
    (<!! (timeout 2500))
    (server/send! p-ch (write-str (dissoc player-info :channel)))
    (info "Message sent to player!")))

(defn set-turns [gs]
  (keys (:players gs)))

(defmulti process-message (fn [game-stae m] (:type m)))

(defmethod process-message "place-symbol"
  [{:keys [board players] :as game-state} {:keys [id symbol pos] :as m}]
  (let [cur-player (first (vals players))
        nxt-player (second (vals players))
        nxt-turn   (reverse (vals players))
        nxt-state  (update game-state :board ttt/place-symbol! pos (:symbol cur-player))]
    (server/send! (:channel cur-player) (write-str {:type :ack}))
    (server/send! (:channel nxt-player) (write-str m))
    (assoc nxt-state :turns nxt-turn)))

(def gs (atom {:board   ttt/init-board
               :players {}}))

(defn start-room [p1-chan p2-chan]
  (info "starting room with 2 players")
  (let []
    (onboard-player gs p1-chan :x)
    (onboard-player gs p2-chan :o)
    (swap! gs update :turns set-turns)
    (server/on-receive p1-chan #(swap! gs process-message (read-json %)))
    (server/on-receive p2-chan #(swap! gs process-message (read-json %)))))

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

