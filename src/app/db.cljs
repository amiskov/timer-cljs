(ns app.db
  (:require [re-frame.core :as rf]))

(def default-workout
  {:title               "Default Workout"
   :exercises-count     4
   :work                5                                   ;30
   :rest                5                                   ;30
   :rounds              3
   :rest-between-rounds 60})

(def init-db (merge
               default-workout
               {:seconds-passed    0

                :current-work-time (:work default-workout)
                :current-rest-time (:rest default-workout)
                :current-exercise  1
                :current-round     1

                :countdown         3
                ;; Screens are: :setup :countdown :workout-work :workout-paused :workout-rest :finished.
                :current-screen    :setup-screen}))

;; Subscriptions
(rf/reg-sub
  :current-screen
  (fn [db _]
    (:current-screen db)))

;; Events
(rf/reg-event-db
  :initialize-db
  (fn [_ _]
    init-db))
