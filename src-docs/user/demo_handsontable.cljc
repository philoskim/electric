(ns user.demo-handsontable
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            #?(:cljs [debux.cs.electric :refer-macros [clog clogn dbg dbgn]]) ))

#?(:clj (use 'debux.electric))

;;; server
#?(:clj
   (def persons!
     (atom [{:person-id 1
             :age 35
             :salary 35000
             :name "Vincent Jackson"
             :sex "Male"}
            {:person-id 2
             :age 25
             :salary 25000
             :name "Michael Tom"
             :sex "Male"}
            {:person-id 3
             :age 30
             :salary 30000
             :name "Jeniffer Mary"
             :sex "Female"}])))

#?(:clj
   (defn read-person-details [person-id]
     (some #(when (= person-id (:person-id %))
              %)
           @persons!)))


;;; client
#?(:cljs (def ^:private table! (atom nil)))

#?(:cljs (def ^:private person-id! (atom nil)))
(e/def person-id+ (e/client (e/watch person-id!)))

#?(:cljs
   (defn- get-person-id-from-table [table! row]
     (.getDataAtCell @table! row 0)))

#?(:cljs
   ;; This event handler has to be a normal js handler,
   ;; because HandsonTable intercepts all the event handlers occuring inside the table
   ;; and processes them in its own way.
   ;; So I set the person-id into the outside stom person-id!.
   (defn- on-cell-mouse-up [event coord td]
     (let [row (.-row coord)
           person-id (get-person-id-from-table table! row)]
       (js/console.log "(reset! person-id! person-id):"
                       (prn-str (reset! person-id! person-id)))  ;; < 1 >
       )))


;;; UI
(e/defn GetPersonDetails []
  (e/client
    ;; person-id+ is triggered by the above < 1 >
    (let [person-details (e/server (read-person-details (e/client person-id+)))]
      (js/console.log "person-daetails:"
                      (prn-str person-details))   ;; < 2 >
      )))

#?(:cljs
   (def ^:private table-opts*
     {:data [[1 "Jackson" "Male"]
             [2 "Tom"     "Male"]
             [3 "Mary"    "Female"]]
      :colHeaders ["ID" "Name" "Sex"]
      :licenseKey "non-commercial-and-evaluation"
      :afterOnCellMouseUp on-cell-mouse-up}))

;; persons table
(e/defn Persons []
  (e/client
    (dom/div
      (let [table (js/Handsontable. dom/node (clj->js table-opts*))]
        (reset! table! table)
        (GetPersonDetails.) ))))
