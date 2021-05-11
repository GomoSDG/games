(ns hotreload
  (:require [gomosdg.core :refer [app]]
            [ring.middleware.reload :refer [wrap-reload]]

            [aleph.http :as http])
  (:gen-class))


(def dev-handler
  #'app)

(defn -main [& args]
  (def server (http/start-server dev-handler {:port 3080})))










