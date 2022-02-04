(ns app.components.workout
  (:require [re-frame.core :as rf]))

(defn workout [{:keys [paused?]}]
  (prn "paused?" paused?)
  (let [seconds-passed @(rf/subscribe [:seconds-passed])]
    [:div.phase.phase_work {:class (when paused? "phase_work-paused")}
     [:h2.phase-title (str "Work: " seconds-passed)]
     [:p.exercise "Exercise 1 of 10"]
     [:p.round "Round 1 of 3"]
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
  :seconds-passed
  (fn [db _]
    (:seconds-passed db)))

;; Events
(rf/reg-event-fx
  :start-workout-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :workout
                    :action     :start}
     :db           (merge db {:current-screen :workout})}))

(rf/reg-event-fx
  :toggle-workout-pause
  (fn [{:keys [db]} _]
    (let [paused? (= :workout-paused (:current-screen db))]
      {:handle-timer {:timer-type :workout
                      :action     (if paused?
                                    :start
                                    :pause)}
       :db           (merge db {:current-screen (if paused? :workout :workout-paused)})})))

(rf/reg-event-fx
  :stop-workout-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :workout
                    :action     :stop}
     :db           (merge db {:current-screen :setup
                              :seconds-passed 0})}))
