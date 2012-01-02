(ns leiningen.cljs
  "Functions for bootstrapping the clojure code to be compiled as
  ClojureScript and used from native JavaScript.

  Coding assumptions (subject to change):
    * the only top-level forms are ns and defn.
    * each function has only one body"
  (:use [robert.hooke])
  (:use [clojure.pprint :only [pprint]])
  (:require leiningen.test)
  (:require [fs.core :as fs]
            [clojure.string :as s]))

(defn- transform-clojure
  "Given a list of clojure forms, transforms them to be good clojurescript."
  [clj-src]
  (let [[ns-decl & clj-forms] (read-string (str "(" clj-src ")"))]
    (apply str
           (map prn-str
                (cons (concat ns-decl '[(:require [obis-shared.cljs :as obis-cljs])
                                        (:require-macros [obis-shared.cljs-macros :as obis-cljs-macros])])
                      (for [[_defn & forms] clj-forms]
                        (cons 'obis-cljs-macros/def-js-fn forms)))))))

(defn- jsify-name
  "Basically just strips off punctuation."
  [fn-name]
  (symbol
    (s/replace (name fn-name) #"[^\w\d-_]" "")))

(defn- api-ns-for
  [clj-forms]
  (let [ns-name (-> clj-forms first second),
        ns-decl (list 'ns
                      (symbol (str ns-name ".js"))
                      (list :require-macros '[obis-shared.cljs-macros :as max])
                      (list :require [ns-name :as 'orig] '[obis-shared.cljs :as oscljs])),
        fn-forms
          (for [form (rest clj-forms)]
            (let [[_defn fn-name] form]
              (assert (= 'defn _defn)) ; only supporting defn's at the moment
              (list 'max/def-js-delegate ns-name fn-name)))]
    (s/join "\n" (map pr-str (cons ns-decl fn-forms)))))

(defn- outfile-for-ns
  [out-dir ns-sym]
  (let [components (s/split (name ns-sym) #"\.")]
    (fs/file (str (apply str out-dir (for [s components] (str "/" (s/replace s "-" "_")))) ".cljs"))))

(defn- mkdirs
  [file]
  (fs/mkdirs (fs/parent file)))

(defn cljs
  "Given a clojure file and an out-dir, writes two clojurescript files: one representing
  the original namespace, and a js API namespace."
  [in-file out-dir]
  (let [src (slurp in-file),
        src-forms (read-string (str "(" src ")")),
        ns-name (-> src-forms first second),
        outfile-1 (outfile-for-ns out-dir ns-name),
        outfile-2 (outfile-for-ns out-dir (str ns-name ".js"))]
    (mkdirs outfile-1)
    (mkdirs outfile-2)
    (spit outfile-1 src)
    (spit outfile-2 (api-ns-for src-forms))
    (println "Wrote to" outfile-1)
    (println "Wrote to" outfile-2)))

(defn- business-js
  []
  "alert('SHUT UP');")

(add-hook #'leiningen.compile/compile
          (fn [task & args]
            (let [ret (apply task args)
                  f (fs/file "classes/obis_shared/business.js")]
              (mkdirs f)
              (spit f (business-js)
              ret)))