(ns app.db
  (:require [re-frame.core :as rf]))

(def default-workout
  {:title               "Default Workout"
   :exercises-count     4
   :work                30
   :rest                30
   :rounds              3
   :rest-between-rounds 60})

(def current-workout (assoc default-workout
                       :timer-id nil
                       :seconds-passed 0
                       :current-activity :not-started))     ; (or :not-started :countdown :work :rest :pause :finished)

(def init-db default-workout)

(rf/reg-event-db
  :initialize-db
  (fn [_ _]
    init-db))
