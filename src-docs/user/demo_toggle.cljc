(ns user.demo-toggle
  (:require
   [hyperfiddle.electric :as e]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.electric-ui4 :as ui]))

; a full stack function with both frontend and backend parts,
; all defined in the same expression

#?(:clj (defonce !x (atom true)) ; server state
   :cljs (defonce !client-counter (atom 0)))  ; <--

(e/def x (e/server (e/watch !x))) ; reactive signal derived from atom
#?(:cljs (e/def client-counter (e/client (e/watch !client-counter)))) ; <--

(e/defn Toggle []
  (e/client
    (dom/h1 (dom/text "Toggle Client/Server"))

    (dom/div
      (dom/text "number type here is: "
        (case x
          true (e/client (pr-str (type 1))) ; javascript number type
          false (e/server (pr-str (type 1)))))) ; java number type

    (dom/div (dom/text "current site: "
               (case x
                 true "ClojureScript (client)"
                 false "Clojure (server)")))

    (dom/div (dom/text "client state: "   ; <--
               (str client-counter)))     ; <--

    (ui/button (e/fn []
                 (e/server (swap! !x not)))
      (dom/text "toggle client/server"))

    (ui/button (e/fn []                                   ; <--
                 (e/client (swap! !client-counter inc)))  ; <--
      (dom/text "increase client-counter") )))            ; <--
