(ns user.tutorial-7guis-1-counter3
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]
    [missionary.core :as m] ))

#?(:clj (def counter! (atom 0)))
(e/def counter (e/server (e/watch counter!)))

(e/defn Counter []
  (e/client
    (let [!state (atom 0)]
      (dom/button (dom/props {:disabled false})
                  (dom/on "click" (e/fn [e]
                                    (e/client (dom/props {:disabled true}))
                                    (e/server
                                      ;(new (e/task->cp (m/sleep 3000)))
                                      (Thread/sleep 3000)
                                      (swap! counter! inc))))
        (dom/text "Count"))
      (dom/text (e/server counter) ))))
