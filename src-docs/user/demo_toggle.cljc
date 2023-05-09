(ns user.demo-toggle
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]
   [hyperfiddle.history :as history]
   #?(:cljs [debux.cs.electric :refer-macros [clog clogn dbg dbgn]]) ))

; a full stack function with both frontend and backend parts,
; all defined in the same expression

#?(:clj (defonce x! (atom true)) ; server state
   :cljs (defonce client-counter! (atom 0)))

(e/def x* (e/server (e/watch x!))) ; reactive signal derived from atom
#?(:cljs (e/def client-counter* (e/client (e/watch client-counter!))))

(e/defn Toggle []
  (e/client
    (dom/h1 (dom/text (dbg "Toggle Client/Server")))

    (dom/div
      (dom/text "number type here is: "
        (case x*
          true (pr-str (type 1)) ; javascript number type
          false (e/server {:a 1 :b 2})))) ; java number type

    (dom/div (dom/text "current site: "
               (case x*
                 true "ClojureScript (client)"
                 false "Clojure (server)")))

    (dom/div (dom/text "client state: "
               (str client-counter*)))

    (ui/button (e/fn []
                 (e/server (swap! x! not)))
      (dom/text "toggle client/server"))

    (ui/button (e/fn []
                 (e/client (swap! client-counter! inc)))
      (dom/text "increase client-counter") )

    (ui/button (e/fn []
                   (history/navigate! user-main/history*  [`user.demo-index/Demos]))
                 (dom/text "to demos") )))
