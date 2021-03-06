(ns gomosdg.auth.core
  (:require [buddy.auth :refer [authenticated? throw-unauthorized]]
            [ring.util.response :as response]
            [buddy.auth.backends.session :refer [session-backend]]
            [medley.core :as m]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(def users (atom {}))

(defn- compose-validators [v1 v2]
  (fn [val] (and (v1 val) (v2 val))))

(defn join-validators
  "Joins all validators running them from left to right."
  [& validators]
  (reduce compose-validators identity validators))

(defn has-matching-passwords? [{:keys [password password-repeat] :as form}]
  (println "TESTING!")
  (= password password-repeat))

(defn get-errors [validations form]
  (let [attach-error-message (fn [k [f m]]
                               (when-not (f form)
                                 m))]

    {:errors (->> (m/map-kv-vals attach-error-message validations)
                  (m/filter-vals identity))}))

(comment
  (let [validations {:password [has-matching-passwords? "Please make sure that your password matches requirements"]
                     :something [identity "Also this"]}]
    (get-errors validations {:password "Hello" :password-repeat "Hello1" :something false})))

(def valid-registration-form? (join-validators has-matching-passwords?))

(comment (valid-registration-form? {:password "Hello" :password-repeat "Hello1" :something false}))

(defn unauthorized-handler
  [request metadata]
  (cond
    ;; If request is authenticated, raise 403 instead
    ;; of 401 (because user is authenticated but permission
    ;; denied is raised).
    (authenticated? request)
    (-> {:body "Sorry! Not allowed"}
        (assoc :status 403))
    ;; In other cases, redirect the user to login page.
    :else
    (let [current-url (:uri request)]
      (response/redirect (format "/login?next-url=%s" current-url) :see-other))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))
