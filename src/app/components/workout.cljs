(ns app.components.workout
  (:require [re-frame.core :as rf]))

(defn workout [{:keys [paused?]}]
  (let [{:keys [running-workout workout-setup]} @(rf/subscribe [:workout])
        {:keys [round exercise phase phase-remaining-time]} running-workout
        {:keys [exercises-count rounds]} workout-setup]
    [:div.phase {:class (str
                          (if (or (= :rest phase) (= :rest-after-round phase)) "phase_rest" "phase_work")
                          (when paused? " phase_paused"))}
     [:h2.phase-title
      (case phase
        :rest (str "Rest: " phase-remaining-time)
        :rest-after-round (str "Rest after round: " phase-remaining-time)
        :work (str "Work: " phase-remaining-time)
        :finished "Finished!"
        (str phase " is the unknown phase!"))]
     [:p.exercise (str "Exercise " exercise " of " exercises-count)]
     [:p.round (str "Round " round " of " rounds)]
     [:div.row.row_btn.row_pause
      [:button.btn.btn_pause
       {:on-click #(rf/dispatch [:toggle-workout-pause])}
       (if paused? "Resume" "Pause")]]
     [:div.row.row_btn.row_cancel
      [:button.btn.btn_cancel
       {:on-click #(rf/dispatch [:stop-workout-timer :setup-screen])}
       "Cancel"]]]))

;; Subscriptions
(rf/reg-sub
  :workout
  (fn [db _]
    {:running-workout (:running-workout db)
     :workout-setup   (:workout-setup db)}))

;; Events
(rf/reg-event-fx
  :start-workout-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :workout-timer
                    :action     :start}
     :db           (merge db {:current-screen  :workout-work-screen
                              :seconds-passed  0
                              :running-workout {:round                1
                                                :exercise             1
                                                :phase                :work
                                                :phase-remaining-time (get-in db [:workout-setup :work])}})}))

(rf/reg-event-fx
  :toggle-workout-pause
  (fn [{:keys [db]} _]
    (let [paused? (= :workout-paused-screen (:current-screen db))]
      {:handle-timer {:timer-type :workout-timer
                      :action     (if paused?
                                    :start
                                    :pause)}
       :db           (merge db {:current-screen (if paused? :workout-work-screen :workout-paused-screen)})})))

(rf/reg-event-fx
  :stop-workout-timer
  (fn [{:keys [db]} [_ screen]]
    {:handle-timer {:timer-type :workout-timer
                    :action     :stop}
     :db           (assoc db :current-screen screen)}))
