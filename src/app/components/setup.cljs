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
(rf/reg-event-db
  :update-setup
  (fn [db [_ field val]]
    (assoc-in db [:workout-setup field] (int val))))
