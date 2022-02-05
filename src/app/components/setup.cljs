(ns app.components.setup
  (:require [re-frame.core :as rf]))

(defn setup-row [cls lbl rng selected]
  (let [row-class (case cls
                    "exercises" "row_exercises"
                    "rest" "row_rest"
                    "work" "row_work")
        kwd-cls (keyword (str "div.row." row-class))]
    [kwd-cls
     [:label lbl]
     [:select {:default-value selected}
      (for [n rng]
        [:option {:key n} (str n)])]]))

(defn setup []
  (let [{:keys [work rest rounds exercises-count rest-between-rounds]} @(rf/subscribe [:workout-setup])]
    [:div.timer
     [:form {:on-submit
             (fn [ev] (.preventDefault ev)
               (rf/dispatch [:start-countdown-timer]))}
      [:section.repeats
       [:section.exercises
        [:section.phases
         (setup-row "work" "Work" (range 5 201 5) work)
         (setup-row "rest" "Rest" (range 5 201 5) rest)
         (setup-row "exercises" "Exercises" (range 1 11) exercises-count)]]
       [:div.row.row_round
        [:div.row__inner
         [:label "Rounds"]
         [:select {:default-value rounds}
          (map (fn [n]
                 [:option {:key n} (str n)])
               (range 1 11))]]
        [:div.row__inner
         [:label "with rest between rounds"]
         [:select {:default-value rest-between-rounds}
          (map (fn [n] [:option {:key n} (str n)])
               (range 0 241))]]]]
      [:div.row.row_btn.row_go
       [:button.btn.btn_go "Get it done!"]]]]))

;; Subscriptions
(rf/reg-sub
  :workout-setup
  (fn [db _]
    db))

;; Events
(defn update-workout-info [db]
  (let [seconds-passed (:seconds-passed db)
        current-exercise (:current-exercise db)
        current-work-time (:current-work-time db)
        current-round (:current-round db)]

    (merge db
           {:seconds-passed (inc seconds-passed)
            :current-work-time (case (:current-screen db)
                                 (dec current-work-time))
            :current-screen :workout-rest-screen})))


(rf/reg-event-fx
  :on-tick
  (fn [{:keys [db]} [_ timer-type]]
    (js/console.log "timer type:" timer-type)
    (case timer-type
      :countdown-timer (if (> (:countdown db) 0)
                         {:db (update db :countdown dec)}
                         {:dispatch [:stop-countdown-timer]})

      :workout-timer {:db (update-workout-info db)}

      ; default
      (do (prn timer-type " is not implemented.")
          {:db db}))))

;; Coeffects
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


