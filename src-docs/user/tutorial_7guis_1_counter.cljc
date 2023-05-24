(ns user.tutorial-7guis-1-counter
  (:import [hyperfiddle.electric Pending])
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]
    [missionary.core :as m] ))

#?(:clj (def counter! (atom 0)))
(e/def counter (e/server (e/watch counter!)))

(e/defn Counter []
  (e/client
    (dom/button
      (let [disabled (try
                       (dom/on "click" (e/fn [e]
                                         (e/server
                                           (case (new (e/task->cp (m/sleep 3000)))
                                             (swap! counter! inc)))))
                       false
                       (catch Pending _
                         true))]
        (dom/props {:disabled disabled
                    :class (str "aaa bbb " (when disabled "disabled"))
                    :style {:background-color (if disabled "yellow" "green")}}))
      (dom/text "Count"))
    (dom/text (e/server counter))))
