(ns gomosdg.games.rooms.views
  (:require [gomosdg.views.components.table :refer [table]]
            [gomosdg.games.rooms.core :as r]))

(defn list-rooms
  "Display a table that includes the room's name"
  [rooms]
  (let [room->row (juxt (comp (fn [name]
                             )r/get-name))
        rows (map room->row rooms)]
    [:div.container
     [:div.columns.is-centered
      [:div.column.is-4
       [:table.table.is-striped.block
        [:thead
         [:tr [:th "Name"] [:th "Users"]]]
        [:tbody
         (map (fn [room]
                [:tr
                 [:td
                  [:a {:href (str "/games/rooms/" (r/get-id room))}
                   (r/get-name room)]]
                 [:td
                  (count (r/list-players room))]])
              rooms)]]]]]))
