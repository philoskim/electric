(ns hyperfiddle.hfql2.explorer
  (:require [hyperfiddle.photon :as p]
            [hyperfiddle.explorer :as ex]
            [hyperfiddle.photon-dom :as dom]
            [hyperfiddle.hfql2.ui :as ui]
            [hyperfiddle.spec :as spec]
            [hyperfiddle.api :as hf])
  #?(:cljs (:require-macros [hyperfiddle.hfql2.explorer])))

(p/defn Sequence ":: t m a -> m t a" [Vs] (p/fn [] (p/for [V Vs] (V.))))

(defmacro capture [dynvars & body]
  (let [syms (repeatedly #(gensym))]
    `(let [~@(interleave syms dynvars)]
       (p/fn []
         (binding [~@(interleave dynvars syms)]
           ~@body)))))

(p/def Rec)

(p/defn TreeToRows "Join all the tree, calling renderers when provided, return EDN" [V]
  (binding [Rec (p/fn [V]
                  (let [{::hf/keys [render cardinality columns leaf?]} (meta V)]
                    (if render
                      [(capture [Rec] [(render. V)])]
                      (let [v (V.)]
                        (case cardinality
                          ::hf/many (into [] cat (p/for [V v] (Rec. V)))
                          ;; infer
                          (cond
                            leaf?       [(p/fn [] [v])]
                            (vector? v) (into [] cat (p/for [V v] (Rec. V)))
                            (map? v)   (into [] cat (p/for [col columns]
                                                      (let [V (get v col)]
                                                        (if (::hf/leaf? (meta V))
                                                          [(capture [Rec] (into [col] cat (new (Sequence. (Rec. V)))))]
                                                          (into [(p/fn [] [col])]
                                                            (Rec. (get v col)))))))
                            :else       v))))))]
    (new Rec V)))

(defn inject-rows [rows]
  (assert (vector? rows) (every? vector? rows))
  (with-meta rows {:tag ::rows}))

(defn rows? [x] (= ::rows (:tag (meta x))))

(p/defn ListOfFormsWithIdentityHeader [V]
  (let [v     (V.)
        depth (::depth (meta V))]
    (inject-rows (into [] cat (p/for [V v]
                                (into [(p/fn [] [depth (p/fn [] [ (p/fn [] (new (:db/id (V.))))])])]
                                  (Rec. V (inc depth))))))))

(p/defn FormLabel [V depth]
  (let [{::hf/keys [attribute arguments]} (meta V)]
    (into [(p/fn [] [depth (p/fn [] [(p/fn [] attribute)])])]
      cat
      (p/for-by first [[name props :as arg] arguments]
        [(p/fn [] [(inc depth) (p/fn []
                                 (let [spec (ui/attr-spec attribute)]
                                   [(p/fn [] (p/client (dom/span {::dom/title (pr-str (:hyperfiddle.spec/form (spec/arg spec name)))}
                                                         (dom/text (str "🔎 " name))) nil))
                                    (p/fn [] (ui/GrayInput. false spec {::dom/placeholder "bob…"} arg))]))])]))))

(p/defn TreeToExplorer [V]
  (binding [Rec (p/fn [V depth]
                  (let [{::hf/keys [render cardinality columns leaf?]} (meta V)]
                    (if render
                      (let [v (render. (vary-meta V assoc ::depth depth))]
                        (if (rows? v)
                          v
                          [(capture [Rec] [(render. V)])]))
                      (let [v (V.)]
                        (case cardinality
                          ::hf/many (into [] cat (p/for [V v] (Rec. V (inc depth))))
                          ;; infer
                          (cond
                            leaf?       [(p/fn [] [depth (p/fn [] [(p/fn [] v)])])]
                            (vector? v) (into [] cat (p/for [V v] (Rec. V (inc depth))))
                            (map? v)    (into [] cat (p/for [col columns]
                                                       (let [V (get v col)]
                                                         (if (::hf/leaf? (meta V))
                                                           [(capture [Rec] [depth
                                                                            (p/fn []
                                                                              (into [(p/fn [] col)] cat
                                                                                [[(p/fn [] (if-let [render (::hf/render (meta V))]
                                                                                             (render. V)
                                                                                             (V.)))]]))])]
                                                           (into (FormLabel. V depth)
                                                             (Rec. (get v col) (inc depth)))))))
                            :else       (throw (ex-info "unreachable" {}))))))))]
    (new Rec V 0)))


(p/defn Explorer [title needle-fn style hfql]
  (let [xs (new (Sequence. (TreeToExplorer. hfql)))]
    (binding [ex/Format (p/fn [M a]
                          (let [row (M.)]
                            (some-> (get row (case a :k 0 :v 1))
                              (new)
                              (pr-str))))]
      (ex/Explorer. title (fn [needle] (needle-fn needle) xs) style))))
