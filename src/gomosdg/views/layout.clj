(ns gomosdg.views.layout
  (:use (hiccup core page)))

(defn main [title & content]
  (html5
    [:head
     [:title title]
     [:meta {:charsets "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-js "/js/turbo.es5-umd.js")
     (include-css "https://cdn.jsdelivr.net/npm/bulma@0.9.2/css/bulma.min.css")]
    [:body
     [:nav.navbar.is-primary {:role       "navigation"
                              :aria-label "main navigation"}
      [:div.navbar-brand [:h1.navbar-item.title "SDG"]]]
     [:div.container
      content]

     (include-js "https://unpkg.com/stimulus/dist/stimulus.umd.js")
     (include-js "/js/stimulus_application.js")
     (include-js "/js/tic_tac_toe.js")
     (include-js "/js/message_box.js")]))
