(ns app.db
  (:require [re-frame.core :as rf]))

(def init-db {:seconds-passed  0
              :countdown       3
              :workout-setup   {:exercises-count     2
                                :work                5
                                :rest                5
                                :rounds              2
                                :rest-between-rounds 7}
              ; TODO: Probably, use cofx for :running-workout?
              :running-workout {#_will_be_populated_on_workout_start}
              ; Screens are: :setup :countdown :workout-work :workout-paused :workout-rest :finished.
              :current-screen  :setup-screen})

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
