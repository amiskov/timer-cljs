(ns app.components.workout
  (:require [re-frame.core :as rf]))

(defn workout [{:keys [rest? paused?]}]
  (let [{:keys [seconds-passed current-round current-exercise]} @(rf/subscribe [:workout-flow])]
    [:div.phase {:class (str
                          (if rest? "phase_rest" "phase_work")
                          (when paused? "phase_paused"))}
     [:h2.phase-title
      (if rest? (str "Rest: " seconds-passed)
                (str "Work: " seconds-passed))]
     [:p.exercise (str "Exercise " current-exercise " of 10")]
     [:p.round (str "Round " current-round " of 3")]
     [:div.row.row_btn.row_pause
      [:button.btn.btn_pause
       {:on-click #(rf/dispatch [:toggle-workout-pause])}
       (if paused? "Resume" "Pause")]]
     [:div.row.row_btn.row_cancel
      [:button.btn.btn_cancel
       {:on-click #(rf/dispatch [:stop-workout-timer])}
       "Cancel"]]]))

;; Subscriptions
(rf/reg-sub
  :workout-flow
  (fn [db _]
    (select-keys db [:seconds-passed :current-round :current-exercise])))

;; Events
(rf/reg-event-fx
  :start-workout-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :workout-timer
                    :action     :start}
     :db           (merge db {:current-screen :workout-work-screen})}))

(rf/reg-event-fx
  :toggle-workout-pause
  (fn [{:keys [db]} _]
    (let [paused? (= :workout-paused (:current-screen db))]
      {:handle-timer {:timer-type :workout-timer
                      :action     (if paused?
                                    :start
                                    :pause)}
       :db           (merge db {:current-screen (if paused? :workout-work-screen :workout-paused-screen)})})))

(rf/reg-event-fx
  :stop-workout-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :workout-timer
                    :action     :stop}
     :db           (merge db {:current-screen :setup-screen
                              :seconds-passed 0})}))
