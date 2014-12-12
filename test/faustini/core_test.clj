(ns faustini.core_test
    (:use midje.sweet)
    (:require [faustini.core :refer [define-mapping ==> =?>]]))

(fact-group "define-mapping"
  (let [item {:foo [{:bar "value-1"}]
              :baz {:baz-inner ["a" "b" "c"]}}]

    (facts "simple mapping"

      (fact "works"
        (define-mapping simple-map
          [:entry-1 ==> :foo 0 :bar]
          [:entry-2 ==> :baz :baz-inner 2])
        (simple-map item) => {:entry-1 "value-1" :entry-2 "c"})

      (facts "constant simple mapping works"
        (define-mapping const-map
          [:const-entry ==> :_const "const-value"])
        (const-map item) => {:const-entry "const-value"}
        )

      (facts "when last rule is a function"

        (fact "if there are preceding rules it is invoked with their resolved value"
          (define-mapping fun-map
            [:fun-value ==> :foo 0 :bar keyword])
          (fun-map item) => {:fun-value :value-1})

        (facts "if the preceding rules are not a valid path in the item"

          (fact "the function is called with `nil`"
            (define-mapping fun-map
              [:fun-value ==> :invalid 0 :bar identity])
            (fun-map item) => {:fun-value nil}))

        (facts "when there are no preceding rules"

          (fact "the last rule is invoked with the whole item"
            (define-mapping fun-map
              [:fun-value ==> #(get-in % [:baz :baz-inner 0])])
            (fun-map item) => {:fun-value "a"}))))

    (fact-group "conditional mapping"

      (fact-group ":match-set"

        (fact "works in simple case"
          (define-mapping cond-map
            [[:foo 0 :bar] =?> [:match-set #{"value-1"}
                                [:cond-entry-1 ==> :baz :baz-inner 0]]])
          (cond-map item) => {:cond-entry-1 "a"})

        (fact "works with multiple conditions"
          (define-mapping cond-map
            [[:foo 0 :bar] =?> [:match-set #{"value-invalid"}
                                [:cond-entry-1 ==> :baz :baz-inner 0]

                                :match-set #{"value-1"}
                                [:cond-entry-1 ==> :baz :baz-inner 1]
                                [:cond-entry-2 ==> :foo 0 :bar]]])
          (cond-map item) => {:cond-entry-1 "b"
                              :cond-entry-2 "value-1"}))

      (fact "when no match is found, do not put the conditional entry in the result"
        (define-mapping cond-map
          [[:foo 0 :bar] =?> [:match-set #{"value-invalid"}
                              [:cond-entry-1 ==> :baz :baz-inner 0]]])
        (cond-map item) => {})

      (fact "works recursively" recursive
        (define-mapping cond-recursive-map
          [[:foo 0 :bar] =?> [:match-set #{"value-1"}
                              [[:foo 0 :bar] =?> [:match-set #{"value-1"}
                                                  [:cond-recursive ==> :_const 42]]]]])
        (cond-recursive-map item) => {:cond-recursive 42})

      (fact "with a match but a wrong path yields the entry with `nil` value"
        (define-mapping cond-map
          [[:foo 0 :bar] =?> [:match-set #{"value-1"}
                              [:cond-entry-1 ==> :invalid :baz-inner 0]]])
        (cond-map item) => {:cond-entry-1 nil})
      )))
