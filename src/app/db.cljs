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
               {:timer-id        nil
                :seconds-passed  0
                :workout-paused? false
                :countdown       3
                ;; Screens are: :setup :countdown :workout :workout-paused :workout-rest :finished.
                :current-screen  :setup}))

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
