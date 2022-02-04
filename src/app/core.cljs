(ns app.core
  (:require
    [reagent.dom :as rdom]
    [app.db]
    [re-frame.core :as rf]
    ;; components
    [app.components.setup :refer [setup]]
    [app.components.countdown :refer [countdown]]
    [app.components.workout :refer [workout]]))

(defn app []
  (let [screen @(rf/subscribe [:current-screen])]
    (js/console.log "screen:" screen)
    [:main.timer
     [:div.wrapper {:class (if (= screen :workout-paused) "paused" "")}
      (case screen
        :setup [setup]
        :countdown [countdown]
        :workout [workout]
        ; same component for paused workout, only the class for .wrapper above is different
        :workout-paused [workout]
        (str "Screen " screen " is not implemented."))]]))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rdom/render [app]
               (.getElementById js/document "app")))

(defn init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (rf/dispatch-sync [:initialize-db])
  (js/console.log "start")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
