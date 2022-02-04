(ns app.components.countdown
  (:require [re-frame.core :as rf]))

(defn countdown []
  (let [countdown @(rf/subscribe [:countdown-count])]
    [:div.countdown
     [:div {:class (if (>= countdown 3) "countdown_tick" "countdown_go")}
      (if (> countdown 0) countdown "Go!")]]))

;; Subscriptions
(rf/reg-sub
  :countdown-count
  (fn [db _]
    (:countdown db)))

;; Events
(rf/reg-event-fx
  :start-countdown-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :countdown
                    :action     :start}
     :db           (merge db {:current-screen :countdown})}))

(rf/reg-event-fx
  :stop-countdown-timer
  (fn [{:keys [db]}]
    {:handle-timer {:timer-type :countdown
                    :action     :stop}
     :db           (merge db {:current-screen :workout
                              :countdown      3})
     :dispatch     [:start-workout-timer]}))
