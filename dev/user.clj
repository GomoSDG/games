(ns user
  (:require [hotreload]))

(defn start-server []
  (hotreload/-main))
