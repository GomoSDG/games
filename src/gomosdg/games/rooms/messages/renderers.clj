(ns gomosdg.games.rooms.messages.renderers
  (:require [hiccup.core :as h]
            [gomosdg.games.rooms.messages.backend :refer [MessageRenderer]]))

(defn type->message-class [type]
  (case type
    :warning "is-warning"
    :info    "is-info"
    :daner   "is-danger"
    :default "is-info"))

(defrecord HTMLRenderer []
    MessageRenderer
  (render [_ {:keys [body type action]}]
    (h/html
      (doall
        [:turbo-stream {:action (name action)
                        :target "messages"}
         [:template
          [:div.notification {:class (type->message-class type)}
           body]]]))))
