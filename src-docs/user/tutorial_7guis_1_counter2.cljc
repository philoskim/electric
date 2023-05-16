(ns user.tutorial-7guis-1-counter1
  (:require
    [hyperfiddle.electric :as e]
    [hyperfiddle.electric-dom2 :as dom]
    [hyperfiddle.electric-ui4 :as ui]
    #?(:cljs [debux.cs.electric :refer-macros [clog clogn dbg dbgn]]) ))

#?(:clj (use 'debux.electric))

#?(:clj (def count-in-server! (atom 0)))
(e/def count-in-server* (e/server (e/watch count-in-server!)))


;;; e/client와 e/server는 e/def, e/defn, e/fn 내부에서만 호출해야 한다.
(e/defn Counter []
  (e/client
    (dom/p (dom/text (e/server  (e/watch count-in-server!))))
    ;(dom/p (dom/text (e/client count-in-server*)))
    (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))
      (dom/text "Count"))))


;;; 만약 e/client와 e/server를 def, defn, fn 내부에서 호출하면,
;;; 다음과 같이 run-time 시에 예외가 발생한다.
(defn inc-100 []
  ;; e/server가 defn 내부에서 호출되어, 실행시 예외 발생
  (e/server (swap! count-in-server! #(+ % 100))))

(e/defn Counter2 []
  (e/client
    (dom/p (dom/text (e/client count-in-server*)))
    (ui/button (e/fn [] (inc-100))
      (dom/text "Count"))))
;; >> Invalid e/server in Clojure code block (use from Electric code only)


;;; 하지만 e/def가 아닌 def에 정의된 count-in-server!는 defn 내에서 변경 가능하다.
#?(:clj
   (defn inc-100 []
     (swap! count-in-server! #(+ % 100))))

(e/defn Counter3 []
  (e/client
    (dom/p (dom/text (e/client count-in-server*)))
    (ui/button (e/fn [] (e/server (inc-100)))
      (dom/text "Count"))))


;;; e/defn의 body 내의 최상단 form에 등장하는 e/server 또는 e/client는
;;; 생략하지 않는 것이 좋다.
;;; 에를 들어, 다음의 Counter4는 자신이 e/client로 호출될 것이라고 가정하겠지만,
(e/defn Counter4 []
  (dom/p (dom/text count-in-server*))
  (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))))

;;; 만약 Wrapper1에서 호출할 때에는, e/server로 감싸여 있어 electric은 Counter의 최상단 form을
;;; e/server로 가정하고 컴파일하게 되어 에러가 발생한다.
(e/defn Wrapper1 []
  (e/server
    (Counter4.)))

;;; 물론 Wrapper2에서처럼 호출하면 아무 문제가 없지만, Counter4가 e/server로 감싸여 호출될지,
;;; e/client로 감싸여 호출될지 미리 예상하기 어려우므로 명시적으로 e/client로 감싸주는 것이 낫다.
(e/defn Wrapper2 []
  (e/client
    (Counter4.)))


;;; 물론 Counter5에서처럼 count-in-server*를 감싸고 있던 e/client는 생략 가능하다.
;;; 하지만 명시적으로 e/client로 감싸주는 것이 좋을 듯하다.
(e/defn Counter5 []
  (e/client
    (dom/p (dom/text count-in-server*))
    (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))
      (dom/text "Count"))))


;;; 아울러 e/defn으로 정의한 객체의 생성 역시, 위의 Wrapper1이나 Wrapper2에처처럼
;;; 반드시 e/defn 안에서 이루어져아 햔다.
; (defn Wrapper3 []
;   ;;; defn 내부에서 electric 객체 생성 불가
;   (Counter4.)))


;;; e/fn은 e/defn 내부에서만 호출해야 한다.
; (defn inc-100' []
;   ;; e/fn은 defn 내부에서 호출 불가
;   ((e/fn [num] (swap! count-in-server! #(+ % num)))
;    100))


;;; couunt-in-server*의 값을 프린트(일종의 side effect)하기 위해
;;; PrintCount-1 함수를 정의
(e/defn PrintCount-1 []
  (println count-in-server*))

(e/defn Counter6 []
  (e/client
    (dom/p (dom/text (e/client count-in-server*)))
    ;; 하지만 실제로 프린트 되지 않음
    (PrintCount-1.)
    (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))
      (dom/text "Count"))))

;;; 다음과 같이 정의해야 실제로 프린트 됨
;;; 즉, electric compiler가 graph 자료구조 형태로 추적할 수 있어야 한다.
(e/defn PrintCount-2 [count-in-server]
  (println count-in-server))

(e/defn Counter7 []
  (e/client
    (dom/p (dom/text (e/client count-in-server*)))
    ;; 실제로 프린트 됨
    (PrintCount-2. count-in-server*)
    (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))
      (dom/text "Count"))))


;;; 현재 e/client 내에서 다음과 같이 #js를 사용하면,
;;; 알 수 없는 이유로(electric의 버그로 추정) 에러가 발생한다.
; (e/defn PrintCount-3 [count-in-server]
;   (js/console.log count-in-server #js {:a 10 :b 20}))

;;; 하지만 다음과 같이 작성해 주면 에러가 발생하지 않는다.
(e/defn PrintCount-4 [count-in-server]
  (js/console.log count-in-server (clj->js {:a 10 :b 20})))

(e/defn Counter8 []
  (e/client
    (dom/p (dom/text (e/client count-in-server*)))
    ;; 실제로 프린트 됨
    (PrintCount-4. count-in-server*)
    (ui/button (e/fn [] (e/server (swap! count-in-server! inc)))
      (dom/text "Count"))))


;;; 다음과 같이 e/server를 호출하지 않고, client 관련 작업만을 수행할 때에는
;;; *.cljs 파일에 e/defn 코드를 작성해도 된다.

;; user/counter.cljs
(e/defn Counter9 []
  (e/client
    (let [state! (atom 0)]
      (dom/p (dom/text (e/watch state!)))
      (ui/button (e/fn [] (swap! state! inc))
        (dom/text "Count") ))))
