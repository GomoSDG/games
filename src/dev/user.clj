(ns user
  (:require (shadow.cljs.devtools [server :as server]
                                  [api :as shadow])))

(defn connect-to-repl [app]
  (shadow/nrepl-select app))

(defn start-cljs [app]
  (server/watch-builds :ui))

(server/start!)


