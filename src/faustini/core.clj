(ns faustini.core)

;; Simple matcher
(def ==> "==>")

;; Conditional matcher.
(def =?> "=?>")

(def CONST-RULE :_const )


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
  "Evaluates predicate for conditional mapping."
  [value match-type pred]
  (case match-type
    :match-set (some pred [value])
    nil))


(declare execute-line)

(defn execute-cond-line
  "Execute a =?> line."
  [item head tail]
  (let [value (get-in item head)
        parts (split-by-pred keyword? tail)
        results (filter (fn [[match-type pred & _]]
                            (cond-matches? value match-type pred)) parts)]
    (if-not (seq results)
            ;; No result found.
            (if (= (first (last parts)) :else)
              ;; `:else` branch found (as the last branch).
              (flatten (map #(execute-line item %) (rest (last parts))))
              nil)
            ;; We found a matching predicate.
            (let [rules (map #(drop 2 %) results)
                  ;; Merge rules from all the matching branches.
                  merged-rules (apply concat rules)]
              (flatten (map #(execute-line item %) merged-rules))))))


(defn execute-simple-line
  "Execute a ==> line."
  [item head rules]
  (cond
    ;; Constant mapping.
    (= (first rules) CONST-RULE) (let [[_ value fun] rules]
                                   (if (clojure.test/function? fun)
                                     {head (fun value)}
                                     {head value}))
    ;; Last item is a funcion.
    ;; TODO: Allow multiple functions (chaining).
    (clojure.test/function? (last rules)) (let [path (butlast rules)
                                                res (if path (get-in item path) item)]
                                            {head ((last rules) res)})

    ;; Just a good old simple mapping.
    :else {head (get-in item rules)}))


(defn execute-line
  "Parse and execute line from the mapping."
  [item [head arrow & rules]]
  (case arrow
    "==>" (execute-simple-line item head rules)
    "=?>" (execute-cond-line item head (first rules))))


(defmacro define-mapping
  ([mapping-name & entries]
   `(def ~mapping-name
      (fn [item#] (reduce #(into %1 (execute-line item# %2)) {} [~@entries])))))

