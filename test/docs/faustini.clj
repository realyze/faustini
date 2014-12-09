(ns docs.faustini
  (:require [midje.sweet :refer :all]
            [clj-time.format :as tf]
            ))

[[:chapter {:tag "howto" :title "How to use faustini"}]]

[[:section {:title "A real world example"}]]

"Consider the following relatively complex hashmap."

(require '[faustini.core :refer [define-mapping ==> =?>]])

(def item {:occurred-at "2014-10-10T15:04:00Z",
               :primary-resources [{:kind "story",
                                    :id 42,
                                    :name "As a robot I want robots to kill all humans so that robots can rule the world."
                                    :story-type "feature",
                                    :url "https://www.pivotaltracker.com/story/show/42"}],
               :highlight "added",
               :performed-by {:kind "person", :id 442, :name "SkyNet", :initials "SN"},
               :project {:kind "project", :id 115, :name "Doomsday project"},
               :kind "story_create_activity",
               :changes [{:kind "story",
                          :change-type "create",
                          :id 436976,
                          :new-values
                          {:updated-at "2014-10-10T15:04:00Z",
                           :labels [],
                           :requested-by-id 442,
                           :current-state "unscheduled",
                           :follower-ids [],
                           :name "As a robot I want robots to kill all humans so that robots can rule the world."
                           :story-type "feature",
                           :owner-ids [],
                           :label-ids [],
                           :id 12345,
                           :after-id 12344,
                           :before-id 12346,
                           :project-id 115,
                           :created-at "2014-10-10T15:03:59Z"},
                          :name "As a robot I want robots to kill all humans so that robots can rule the world.",
                          :story-type "feature"}],
               :project-version 452,
               :message "SkyNet added this feature",
               :guid "1211254_233"})

"Let's see the various mappings we can apply."

[[:section {:title "Simple mapping"}]]

"Let's define a simple map."

(define-mapping simple-map
  [:link ==> :primary-resources 0 :url]
  [:objectType ==> :changes 0 :kind])

"And apply the mapping to our item."

(fact
  (simple-map item) => {:link "https://www.pivotaltracker.com/story/show/42"
                        :objectType "story"})

[[:subsection {:title "Using a function in the mapping"}]]
"When the last item of a mapping rule is a function, it is applied to the
result of the preceding rules."

(fact
  (define-mapping simple-map [:objectType ==> :changes 0 :kind keyword])
  (simple-map item) => {:objectType :story})

"If there are no preceding rules, it is applied to the whole item. This can be used
as a fallback in case Faustini's declarative mapping isn't flexible or strong enough
for your needs."
(fact
  (define-mapping simple-map [:my-value ==> (fn [item] (str "type_"
                                                            (get-in item [:changes 0 :change-type])
                                                            "/" (:highlight item)))])
  (simple-map item) => {:my-value "type_create/added"})

[[:subsection {:title "_const mapping"}]]
"We can also use a `_const` mapping which directly resolves to a value.
"

(fact
  (define-mapping const-map [:service ==> :_const "pivotaltracker"])
  (const-map item) => {:service "pivotaltracker"})

"We can of course also apply a custom function to a const value."

(fact
  (define-mapping const-map-with-fun [:service ==> :_const "pivotaltracker" keyword])
  (const-map-with-fun item) => {:service :pivotaltracker})


[[:section {:title "Conditional Mapping"}]]
"If we want to express more complex logic, we can use the conditional mappings."

"The generic format of a conditional mapping looks like this:"

(comment
  [[rule] =?> [predicate [& rules]
               predicate [& rules]
               ...]])

"Faustini will get the value for `rule` and match it against the `predicate`. Rules of
the first matching predicate will then be used to determine the resulting value."

[[:subsection {:title "match-set mapping"}]]
"The `match-set` mapping can be used to select the resulting mapping based on
 whether a specified value belongs to a predefined set."

(fact
  
  (def parse-time #(tf/parse (tf/formatters :date-time-no-ms) %))

  (define-mapping match-set-map
    [[:primary-resources 0 :kind] =?> [:match-set #{"story" "epic"}
                                       [:published ==> :changes 0 :new-values :updated-at parse-time]]])
  (match-set-map item) => {:published (parse-time "2014-10-10T15:04:00Z")})


[[:section {:title "Real World Example"}]]

(fact
  (def ^:const service-name :pivotaltracker)

  (def ^:const API-BASE-URL "https://www.pivotaltracker.com/services/v5/")

  (define-mapping object-mapping
    [:objectType ==> :changes 0 :kind keyword]
    [:link ==> :primary-resources 0 :url]
    [[:changes 0 :kind] =?> [:match-set #{"story" "epic"}
                             [:title ==> :primary-resources 0 :name]
                             [:url   ==> #(str API-BASE-URL (get-in % [:project :id])
                                               "/stories/" (get-in % [:primary-resources 0 :id]))]

                             :match-set #{"comment"}
                             [:title ==> :primary-resources 0 :name]]])

  (define-mapping actor-mapping
    [:id ==> :performed-by :id]
    [:objectType ==> :_const :person])

  (define-mapping target-mapping
    [[:changes 0 :kind] =?> [:match-set #{"story" "epic"}
                             [:title ==> :primary-resources 0 :name]
                             [:url   ==> #(str API-BASE-URL
                                               (get-in % [:project :id]) "/stories/"
                                               (get-in % [:primary-resources 0 :id]))]

                             :match-set #{"comment"}
                             [:title ==> :primary-resources 0 :name]]])

  (define-mapping item-mapping
    [:verb      ==> :changes 0 :change-type keyword]
    [:published ==> :occurred-at #(tf/parse (tf/formatters :date-time-no-ms) %)]
    [:title     ==> :message]
    [:actor     ==> actor-mapping]
    [:target    ==> target-mapping]
    [:object    ==> object-mapping]
    [:service   ==> :_const service-name])

  (item-mapping item) => {:service :pivotaltracker,
                          :verb :create,
                          :title "SkyNet added this feature",
                          :target {:title "As a robot I want robots to kill all humans so that robots can rule the world.",
                                   :url "https://www.pivotaltracker.com/services/v5/115/stories/42"},
                          :object {:title "As a robot I want robots to kill all humans so that robots can rule the world.",
                                   :objectType :story,
                                   :link "https://www.pivotaltracker.com/story/show/42",
                                   :url "https://www.pivotaltracker.com/services/v5/115/stories/42"},
                          :published (tf/parse "2014-10-10T15:04:00.000Z"),
                          :actor {:objectType :person, :id 442}} )
