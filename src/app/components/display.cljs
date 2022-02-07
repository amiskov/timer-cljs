(ns app.components.display
  (:require [re-frame.core :as rf]
            [app.components.setup :refer [setup]]
            [app.components.countdown :refer [countdown]]
            [app.components.workout :refer [workout]]
            [app.components.finished :refer [finished]]))

(defn display []
  (let [screen @(rf/subscribe [:current-screen])]
    (case screen
      :setup-screen [setup]
      :countdown-screen [countdown]
      :workout-work-screen [workout {:paused? false}]
      :workout-paused-screen [workout {:paused? true}]
      :finished-screen [finished]
      (str "There's no " screen))))

(rf/reg-sub
  :current-screen
  (fn [db _]
    (:current-screen db)))

(rf/reg-event-db
  :show-screen
  (fn [db [_ screen]]
    (assoc db :current-screen screen)))
