(ns gomosdg.auth.views
  (:require [gomosdg.views.forms :as forms]
            [gomosdg.emojis :refer [emoji]]))

(defn login [user]
  (let [has-username? :username]
    [:div
     [:section.section
      [:h1.title "Welcome to SDG"]
      [:h1.subtitle "You will need to login in order to use some of our applications."]]
     [:section.columns.is-centered
      [:div.column.is-5-tablet.is-6-desktop.is-3-widescreen
       (forms/render-form {:method "post"}
                          [{:name        "username"
                            :control     :input
                            :type        :text
                            :placeholder (str "e.g. cooluser95 " (emoji :cool-guy-95 ""))
                            :label       "Username"}
                           {:name        "password"
                            :control     :input
                            :type        :text
                            :placeholder "Enter your password"
                            :label       "Password"}
                           {:control :button
                            :type    :submit
                            :value   "Submit"
                            :colors  [:primary]}
                           {:control :link
                            :href "/register"
                            :text "Don't have an account? Sign up now!"}])]]]))

(defn register
  ([] (register {}))

  ([{:keys [errors initial-form] :as options}]

   [:div
    [:h1.title.is-4 "Hello Register"]
    [:turbo-frame#registration-form
     [:section.columns.is-centered
      [:div.column.is-5-tablet.is-4-desktop.is-3-widescreen
       (forms/render-form {:action "/register"
                           :method "post"}

                          [{:name        :full-name
                            :control     :input
                            :type        :text
                            :placeholder "Full names"
                            :error       (:full-name errors)
                            :value       (:full-name initial-form)
                            :label       "Full names"}
                           {:name        :username
                            :control     :input
                            :type        :text
                            :placeholder "e.g. SDG-User"
                            :error       (:username errors)
                            :value       (:username initial-form)
                            :label       "Username"}
                           {:name        "password"
                            :control     :input
                            :type        :password
                            :label       "Password"
                            :value       (or (:password initial-form) "")
                            :error       (:password errors)
                            :placeholder "Enter password"}
                           {:name        "password-repeat"
                            :control     :input
                            :type        :password
                            :label       "Repeat password"
                            :placeholder "Enter password again"}
                           {:control :button
                            :type    :submit
                            :value   "Submit"
                            :colors  [:primary]}])]]]]))
