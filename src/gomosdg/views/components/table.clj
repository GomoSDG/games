(ns gomosdg.views.components.table)

(defn td
  ([cell] (td {} cell))
  ([{:keys [type]} cell]
   (let [c (case type
            :header :th
            :td)]
    [c cell])))


(defn table [{:keys [headers rows]}]
  {:pre [#(vector? rows)]}
  [:table.table.is-striped
   (when (seq headers)
     [:thead
      [:tr
       (map (partial td {:type :header})
            headers)]])

   [:tbody
    (map (fn [c]
           [:tr (map td c)]) rows)]])
