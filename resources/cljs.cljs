(ns obis-shared.cljs)

(defn js->cljs
  [ob]
  (cond
    (#{"boolean" "string" "null" "number"} (goog.typeOf ob))
      ob
    (= "array" (goog.typeOf ob))
      (map js->cljs ob)
    (= "object" (goog.typeOf ob))
      (reduce
        (fn [m k] (assoc m (keyword k) (js->cljs (goog.object.get ob k))))
        {}
        (goog.object.getKeys ob))))

(defn cljs->js
  [ob]
  (cond
    (or (keyword? ob) (symbol? ob))
      (name ob)
    (#{"boolean" "string" "null" "number"} (goog.typeOf ob))
      ob
    (sequential? ob)
      (apply array (map cljs->js ob))
    (map? ob)
      (let [ret (js-obj)]
        (doseq [[k v] ob]
          (goog.object.set ret (name k) (cljs->js v)))
        ret)
    :else (throw (str "STINK AND CRAP: " (pr-str ob)))))

(defn js-wrap
  [f]
  (fn [& args]
    (cljs->js
      (apply f (map js->cljs args)))))
