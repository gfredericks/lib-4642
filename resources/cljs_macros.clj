(ns obis-shared.cljs-macros
  (:require [clojure.string :as s]))

(defn jsify-name
  "Basically just strips off punctuation."
  [fn-name]
  (symbol
    (s/replace (name fn-name) #"[^\w\d-_]" "")))

; TODO: Maybe allow metadata on the functions to define a custom jsified name, so we
; can do things like change one-time-fee? to is_one_time_fee
(defmacro def-js-fn
  [name & forms]
  (let [[arg-list & body] (if (string? (first forms)) (rest forms) forms),
        jsified-name (jsify-name name),
        fn-def
        `(defn ~(with-meta name {:export (= jsified-name name)})
           [& args#]
           (~'obis-shared.cljs/cljs->js
            (let [~arg-list (map ~'obis-shared.cljs/js->cljs args#)] ~@body)))]
    (if (= jsified-name name)
      fn-def
      `(do
         ~fn-def
         (def ~(with-meta jsified-name {:export true}) ~name)))))

(defn- js-delegate
  "Returns a defn form that looks like:

  (defn ^:export js-name
    [& args]
    (obis-shared.cljs/cljs->js
      (apply ns-name/fn-name
        (map obis-shared.cljs/js->cljs args))))"
  [ns-name fn-name js-name]
  (list `defn (with-meta js-name {:export true}) `[& args]
        (list 'obis-shared.cljs/cljs->js
              (list 'apply (symbol (name ns-name) (name fn-name))
                    '(map obis-shared.cljs/js->cljs args)))))
  

(defmacro def-js-delegate
  ([ns-name fn-name] (js-delegate ns-name fn-name (jsify-name fn-name)))
  ([ns-name fn-name js-name] (js-delegate ns-name fn-name js-name)))