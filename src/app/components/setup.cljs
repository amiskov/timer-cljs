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

(rf/reg-sub
  :current-workout
  (fn [db _]
    db))

(defn setup []
  (let [{:keys [work rest rounds exercises-count rest-between-rounds]} @(rf/subscribe [:current-workout])]
    [:div.timer
     [:form {:on-submit #(.preventDefault %)}
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


