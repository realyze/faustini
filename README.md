# Faustini

[![Clojars Project](http://clojars.org/faustini/latest-version.svg)](http://clojars.org/faustini)

[![Circle CI](https://circleci.com/gh/realyze/faustini.svg?style=svg)](https://circleci.com/gh/realyze/faustini)

It maps things to other things! Which is particularly useful if you have, say, some web services that you get JSON data from and you want to store them in your very own app data format.

Steps:

1. Describe the mapping to Faustini (via `define-mapping`).
2. Pass items to the mapping function and get items in your app format.
3. ...
4. PROFIT!

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
          :item-2-1 {:item-2-2 "foobar!"
                     :item-2-3 "barbar!"}})

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

Copyright © 2014 Tomas Brambora, Salsita, l.t.d.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
