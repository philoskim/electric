(ns hyperfiddle.photon-dom
  (:refer-clojure :exclude [for])
  (:require [hfdl.lang :refer [defnode for]]
            [missionary.core :as m]
            #?(:cljs [goog.dom :as d])
            #?(:cljs [goog.events :as e]))
  #?(:cljs (:import goog.events.EventType))
  #?(:cljs (:require-macros
             [hyperfiddle.photon-dom :refer
              [mount! element-with-children fragment div span h1 table thead tbody tr td select option]])))

(defn append-children [parent items] (reduce #?(:cljs #(doto %1 (.appendChild %2))) parent items))
(defn remove-children [parent items] (reduce #?(:cljs #(doto %1 (.removeChild %2))) parent items))

(defn mount-all [parent & items]
  (m/observe
    (fn [!]
      (! (append-children parent items))
      (fn []
        (remove-children parent items)))))

(defn mount [parent child]
  #?(:cljs
     (m/observe
       (fn [!]
         (! nil)
         (d/appendChild parent child)
         #(d/removeNode child)))))

(defn element [type]
  #?(:cljs (d/createElement type)))

(defn set-fragment! [e f]
  ;; TODO
  )

(defn set-text-content! [e t]
  #?(:cljs (d/setTextContent e t)))

(defn set-attribute! [e k v]
  #?(:cljs (.setAttribute e k v)))

(defn get-attribute [e k]
  #?(:cljs (.getAttribute e k)))

(defn events [e t]
  #?(:cljs (m/observe (fn [!] (e/listen e t !) #(e/unlisten e t !)))))

(def input-event
  #?(:cljs (.-INPUT EventType)))

(defn event-target [e]
  #?(:cljs (.-target e)))

(defn get-value [e]
  #?(:cljs (.-value e)))

(defnode mount! [parent child]
  ~(mount parent child))

(defnode element-with-children [type children]
  (let [e (element (name type))]
    (for [c children] (mount! e c)) e))

(defnode fragment [& body])
(defnode div [& body])

(defnode span [& children]
  (element-with-children :span children))

(defnode h1 [& children]
  (element-with-children :h1 children))

(defnode input [x]
  (doto (element "input")
    (set-attribute! "type" "text")
    (set-attribute! "value" x)))

(defnode table [& children]
  (element-with-children :table children))

(defnode thead [& children]
  (element-with-children :thead children))

(defnode tbody [& children]
  (element-with-children :tbody children))

(defnode tr [& children]
  (element-with-children :tr children))

(defnode td [& children]
  (element-with-children :td children))

;; TODO
(defnode radio [value options]
  (for [[id text] options]
    [(doto (element "input")
       (set-attribute! "type" "radio"))
     (doto (element "label")
       (set-attribute! "for" id)
       (set-text-content! text))]))

(defnode select [selected options]
  (doto (element "select")
    (mount! (for [[k v] options]
              (doto (element "option")
                (set-attribute! "value" k)
                (set-text-content! v))))
    (set-attribute! "selected" selected)))

(defnode option [& body])
