(ns gomosdg.games.rooms.messages.core
  (:require [gomosdg.games.rooms.messages.backend :as m]
            [gomosdg.games.rooms.messages.renderers :as r]))

(def html (r/->HTMLRenderer))

(defn render-html [message]
  (m/render html message))

(comment
  (m/render m/html {:action :append
                    :body   "Hello"
                    :type   :warning})
  )
