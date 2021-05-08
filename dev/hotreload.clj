(ns hotreload
  (:require [gomosdg.core :refer [app]]
            [ring.middleware.reload :refer [wrap-reload]]

            [aleph.http :as http]
            [org.httpkit.server :as server])
  (:gen-class))

(def server (atom nil))

(def dev-handler
  (-> #'app
      (wrap-reload)))

(defn -main [& args]
  (reset! server (server/run-server dev-handler {:port 3080})))








