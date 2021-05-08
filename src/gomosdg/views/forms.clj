(ns gomosdg.views.forms
  (:require [clojure.string :as str]))

(defmulti render-control :control)


(def ->color {:light   "is-light"
              :dark    "is-dark"
              :primary "is-primary"})

(def ->text-color {:danger  "has-text-danger"
                   :primary "has-text-primary"
                   :success "has-text-success"
                   :warning "has-text-warning"})

(defn text-colors->class [colors]
  (str/join " " (map #(->text-color % "") colors)))

(defn colors->class [colors]
  (str/join " " (map #(->color % "") colors)))

(comment
  (text-colors->class [:danger]))

(defmethod render-control :input
  [{label       :label
    type        :type
    value       :value
    ctr-name    :name
    placeholder :placeholder
    help-text   :help-text
    help-colors :help-colors
    error       :error}]
  (println "value: " value)
  [:div.field
   [:label.label label]
   [:div.control
    [:input.input {:type        (name type)
                   :value       value
                   :class (str (when (seq error) "is-danger"))
                   :name        ctr-name
                   :placeholder placeholder}]]
   (when help-text
     [:p.help {:class (text-colors->class help-colors)}
      help-text])
   (when error
     [:p.help {:class (text-colors->class [:danger])}
      error])])

(defmethod render-control :button
  [{:keys [value type colors]}]
  [:div.field
   [:input.button {:type  (name type)
                   :class (colors->class colors)
                   :value value}]])


(defn render-form [options fields]
  [:form options
   (map render-control fields)])
