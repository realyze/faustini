# Faustini

[![Clojars Project](http://clojars.org/faustini/latest-version.svg)](http://clojars.org/faustini)

It maps things to other things!

## Simple Example
```clojure
(require '[faustini.core :refer [define-mapping ==> =?>]])

(def foo {:item1 [{:item2 "value!"}]})

(define-mapping my-map
  [:remapped-entry ==> :item1 0 :item2])

(my-map foo)
;; {:remapped-entry "value!"}
```

## More complex example
```clojure
(require '[faustini.core :refer [define-mapping ==> =?>]])

(def foo {:item-1-1 [{:item-1-2 "snickers!"}]
          :item-2-1 {:item-2-2 "foobar!"}})

(def bar {:item-1-1 [{:item-1-2 "mars!"}]
          :item-2-1 {:item-2-2 "foobar!"
                     :item-2-3 "barbar!"}})


(define-mapping my-map
  [[:item-1-1 0 :item-1-2] =?> [:match-set #{"snickers!"}
                                  [:remapped-entry ==> :item-2-1 :item-2-2]
                                  [:remapped-entry-2 ==> :item-1-1 0 :item-1-2]

                                :match-set #{"mars!" "twix!"}
                                  [:remapped-entry ==> :item-2-1 :item-2-2]
                                  [:remapped-entry-2 ==> :item-2-1 :item-2-3]]])

(my-map foo)
;; {:remapped-entry "foobar!"
;;  :remapped-entry-2 "snickers!"}

(my-map bar)
;; {:remapped-entry "foobar!"
;;  :remapped-entry-2 "barbar!"}
```

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
