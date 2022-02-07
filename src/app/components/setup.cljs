(ns app.components.setup
  (:require [re-frame.core :as rf]))

(defn setup-row [setup-field cls lbl rng selected]
  (let [row-class (case cls
                    "exercises" "row_exercises"
                    "rest" "row_rest"
                    "work" "row_work")
        kwd-cls (keyword (str "div.row." row-class))]
    [kwd-cls
     [:label lbl]
     [:select {:default-value selected
               :on-change     #(rf/dispatch [:update-setup setup-field (.. % -target -value)])}
      (for [n rng]
        [:option {:key n} (str n)])]]))

(defn setup []
  (let [{:keys [exercises-count work rest rounds rest-between-rounds]} @(rf/subscribe [:workout-setup])]
    [:div.timer
     [:form {:on-submit (fn [ev]
                          (.preventDefault ev)
                          ;; Make audio playable on mobile
                          (.resumeAudioContext js/window)
                          (rf/dispatch [:start-countdown-timer]))}
      [:section.repeats
       [:section.exercises
        [:section.phases
         (setup-row :work "work" "Work" (range 5 201 5) work)
         (setup-row :rest "rest" "Rest" (range 5 201 5) rest)
         (setup-row :exercises-count "exercises" "Exercises" (range 1 11) exercises-count)]]
       [:div.row.row_round
        [:div.row__inner
         [:label "Rounds"]
         [:select {:default-value rounds
                   :on-change     #(rf/dispatch [:update-setup :rounds (.. % -target -value)])}
          (map (fn [n]
                 [:option {:key n} (str n)])
               (range 1 11))]]
        [:div.row__inner
         [:label "with rest between rounds"]
         [:select {:default-value rest-between-rounds
                   :on-change     #(rf/dispatch [:update-setup :rest-between-rounds (.. % -target -value)])}
          (map (fn [n] [:option {:key n} (str n)])
               (range 0 241))]]]]
      [:div.row.row_btn.row_go
       [:button.btn.btn_go "Get it done!"]]]]))

;; Subscriptions
(rf/reg-sub
  :workout-setup
  (fn [db _]
    (:workout-setup db)))

;; Events
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

(rf/reg-event-db
  :update-setup
  (fn [db [_ field val]]
    (assoc-in db [:workout-setup field] (int val))))

(rf/reg-event-db
  :show-screen
  (fn [db [_ screen]]
    (assoc db :current-screen screen)))

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
