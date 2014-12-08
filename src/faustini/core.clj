(ns faustini.core)

;; Simple matcher
(def ==> "==>")

;; Conditional matcher.
(def =?> "=?>")


(defn split-by-pred
  [pred coll]
  (when-not (pred (first coll))
            (throw (Exception.) "`pred` must be true for the first item!"))
  (loop [items coll
         result []]
        (if (seq items)
          (recur (drop-while (comp not pred) (rest items))
                (concat result [(concat [(first items)] (take-while (comp not pred) (rest items)))]))
          result)))

(defn cond-matches?
  [value match-type pred]
  (case match-type
    :match-set (some pred [value])
    nil))


(declare execute-line)

(defn execute-cond-line
  [item head tail]
  (let [value (get-in item head)
        parts (split-by-pred keyword? tail)
        results (filter (fn [[match-type pred & _]] (cond-matches? value match-type pred)) parts)]
    (if-not results
            nil
            (let [[_ _ & rules] (first results)]
              (map #(execute-line item %) rules)))))


(defn execute-line
  [item [head arrow & rules]]
  (case arrow
    "==>" (cond
            (= (first rules) :const)
            (let [[_ value fun] rules]
              (if (clojure.test/function? fun)
                {head (fun value)}
                {head value}))

            (clojure.test/function? (last rules))
            {head ((last rules) (or (get-in item (butlast rules)) item))}


            :else {head (get-in item rules)})
    "=?>" (execute-cond-line item head (first rules))))

(defmacro define-mapping
  [mapping-name & entries]
  `(def ~mapping-name (fn [item#]
                          (reduce #(into %1 (execute-line item# %2)) {} [~@entries]))))

