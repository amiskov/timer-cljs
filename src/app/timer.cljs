(ns app.timer
  (:require [re-frame.core :as rf]))

(defn update-running-workout [db]
  "Returns new :running-workout value."
  (let [{:keys [round exercise phase phase-remaining-time] :as rw} (:running-workout db)
        {:keys [exercises-count work rest rounds rest-between-rounds]} (:workout-setup db)
        last-exercise? (= exercises-count exercise)
        last-round? (= rounds round)
        phase-time-finished? (= 0 phase-remaining-time)
        workout-finished? (and last-exercise? last-round? phase-time-finished?)
        noop (fn [_])]
    (case phase
      :work (cond
              workout-finished? [(assoc rw :phase :finished
                                           :phase-remaining-time 0)
                                 "final"]
              (and phase-time-finished? last-exercise?) [(assoc rw :phase :rest-after-round
                                                                   :phase-remaining-time rest-between-rounds)
                                                         "alert"]
              phase-time-finished? [(assoc rw :phase :rest
                                              :phase-remaining-time rest)
                                    "alert"]
              :else [(assoc rw :phase :work
                               :phase-remaining-time (dec phase-remaining-time))
                     noop])
      :rest (case phase-remaining-time
              0 [(assoc rw :phase :work
                           :exercise (inc exercise)
                           :phase-remaining-time work)
                 "bell"]
              [(assoc rw :phase :rest
                         :phase-remaining-time (dec phase-remaining-time))
               noop])
      :rest-after-round (case phase-remaining-time
                          0 [(assoc rw :phase :work
                                       :phase-remaining-time work
                                       :exercise 1
                                       :round (inc round))
                             "bell"]
                          [(assoc rw :phase :rest-after-round
                                     :phase-remaining-time (dec phase-remaining-time))
                           noop])
      [rw noop])))

(rf/reg-event-fx
  :on-tick
  (fn [{:keys [db]} [_ timer-type]]
    (case timer-type
      :countdown-timer (if (> (:countdown db) 0)
                         {:db         (update db :countdown dec)
                          :play-audio (if (= (:countdown db) 1) "count_beep" "count_tick")}
                         {:dispatch   [:stop-countdown-timer]
                          :play-audio "bell"})

      :workout-timer (let [[{:keys [phase phase-remaining-time]
                             :as   new-running-workout} audio] (update-running-workout db)
                           finished? (and (= :finished phase) (= 0 phase-remaining-time))
                           new-cofx {:db         (merge db {:running-workout new-running-workout}
                                                        {:current-screen (if finished?
                                                                           :finished-screen
                                                                           :workout-work-screen)})
                                     :play-audio audio}]
                       (if finished?
                         (merge new-cofx {:dispatch [:stop-workout-timer :finished-screen]})
                         new-cofx))

      ; default
      (do (js/console.log timer-type " is not implemented.")
          {:db db}))))

;; Coeffects
(rf/reg-fx
  :play-audio
  (fn [audio]
    (.play js/window audio)))

(rf/reg-fx
  :stop-all-audios
  (fn [_]
    (.stopAllAudios js/window)))

(rf/reg-fx
  ;; One fx for all timers
  :handle-timer
  (let [timer-id (atom nil)]
    (fn [{:keys [action timer-type]}]
      (case action
        :start (do (reset! timer-id (js/setInterval #(rf/dispatch [:on-tick timer-type]) 1000))
                   (.log js/console (str "Timer " timer-type " started.")))
        :stop (do (js/clearInterval @timer-id)
                  (reset! timer-id nil)
                  (.log js/console (str "Timer " timer-type " stopped.")))
        :pause (do (js/clearInterval @timer-id)
                   (reset! timer-id nil)
                   (.log js/console (str "Timer " timer-type " paused.")))))))
