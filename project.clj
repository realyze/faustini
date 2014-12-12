(defproject faustini "0.1.3-SNAPSHOT"
  :description "Behold! It maps things to...other things!"
  :url "http://github.com/realyze/faustini"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :documentation {:files {"docs/index"
                          {:input "test/docs/faustini.clj"
                           :title "Faustini"
                           :sub-title "Learn to use Faustini"
                           :author "Tomas Brambora"
                           :email  "tomas.brambora@gmail.com"}}}
  :plugins [[lein-midje "3.1.3"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [midje  "1.6.3"]]
  :profiles
  {:dev {:dependencies [[clj-time  "0.9.0-beta1"]
                        [org.clojure/tools.nrepl  "0.2.5"]]}}
  )
