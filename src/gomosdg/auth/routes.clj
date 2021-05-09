(ns gomosdg.auth.routes
  (:require [compojure.core :refer [defroutes GET POST]]
            [gomosdg.auth.views :as v]
            [gomosdg.auth.core :refer [has-matching-passwords? get-errors valid-registration-form?]]
            [ring.util.response :as response]
            [gomosdg.views.layout :as layouts]))

;;; ---- Controllers ------ ;;;

(defn handle-registration [{form :params
                            session :session}]
  (let [validations {:password
                     [has-matching-passwords?
                      "Please make sure that your password matches requirements"]}]

    (println "Session!" session)

    (cond
      (contains? session :identity)
      (response/redirect "/login")

      (contains? @gomosdg.auth.core/users (:username form))
      (layouts/main "Register" (v/register (merge (get-errors validations form)
                                                  {:initial-form form})))


      (valid-registration-form? form)
      (do
        (swap! gomosdg.auth.core/users assoc (:username form) (dissoc form :password-repeat))
        (response/redirect "/login" :see-other))

      :else
      (layouts/main "Register" (v/register (merge (get-errors validations form)
                                                  {:initial-form form}))))))

(defn login-authenticate
  "Check request username and password against authdata
  username and passwords.

  On successful authentication, set appropriate user
  into the session and redirect to the value of
  (:next (:query-params request)). On failed
  authentication, renders the login page."
  [request]
  (let [username       (get-in request [:params :username])
        password       (get-in request [:params :password])
        session        (:session request)
        found-password (get-in @gomosdg.auth.core/users [username :password])]

    (if (and found-password (= found-password password))
      (let [next-url        (get-in request [:params :next-url] "/")
            updated-session (assoc session :identity (keyword username))]
        (println "Matching password!")
        (-> (response/redirect next-url :see-other)
            (assoc :session updated-session)))
      (response/redirect "/login" :see-other))))

(defroutes routes
  (GET "/login" req
       (println "SESSION: " (:session req))
       (layouts/main "Login" (v/login {:username "G"})))
  (GET "/register" []
       (layouts/main "Hello" (v/register)))
  (GET "/logout" []
       (-> (response/redirect "/login")
           (assoc :session {})))
  (POST "/register" {params :params}
        handle-registration)
  (POST "/login" []
        login-authenticate))
