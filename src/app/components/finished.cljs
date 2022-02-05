(ns app.components.finished
  (:require [re-frame.core :as rf]))

(defn finished []
  [:div.final
   [:h1.my-6.text-4xl.font-bold "Well Done!"]
   [:div.row.row_btn.row_btn-ok
    [:button.btn.btn_ok
     {:on-click #(rf/dispatch [:stop-workout-timer :setup-screen])}
     "← Back"]]
   [:div.final-image
    [:img {:src "./img/arny_thumbs_up.png"}]]])
