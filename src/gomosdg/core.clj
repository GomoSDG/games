(ns gomosdg.core
  (:require [ring.middleware.session :refer [wrap-session]]
            [gomosdg.games.routes :as games]
            [gomosdg.auth.routes :as auth]
            [gomosdg.views.layout :as layouts]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as response]
            (compojure [core :refer [defroutes GET context ANY]]
                       [route :as route])))



(defroutes routes
  (context "/games" []
    games/routes)
  (GET "/" []
       (response/redirect "/games/rooms" :see-other))
  (GET "/users/save" []
       (spit "users.edn" @gomosdg.auth.core/users))
  auth/routes
  (route/resources "/"))

(def app
  (-> #'routes
      (wrap-keyword-params)
      (wrap-params)
      (wrap-session)))
