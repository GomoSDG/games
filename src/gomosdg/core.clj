(ns gomosdg.core
  (:require [ring.middleware.session :refer [wrap-session]]
            [org.httpkit.server :as server]
            [gomosdg.games.routes :as games]
            [gomosdg.auth.routes :as auth]
            [gomosdg.views.layout :as layouts]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            (compojure [core :refer [defroutes GET context ANY]]
                       [route :as route])))



(defroutes routes
  (context "/games" []
    games/routes)
  (GET "/" []
       (layouts/main "SDG"))
  auth/routes
  (route/resources "/"))

(def app
  (-> #'routes
      (wrap-keyword-params)
      (wrap-params)
      (wrap-session)))
