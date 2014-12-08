# faustini

Faustini maps things to other things!

## Simple Example
```clojure
(require '[faustini.core :refer [define-mapping ==> =?>]])

(def foo {:item1 [{:item2 "value!"}]})

(define-mapping my-map
  [:remapped-entry ==> :item1 0 :item2])

(my-map foo)
;; {:remapped-entry "value!"}
```

## Usage

FIXME

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
