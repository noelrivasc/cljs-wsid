(ns wsid.util.theming
  (:require
   [clojure.walk :refer [postwalk]]))

(def current-theme (atom nil))

(defn set-theme [theme]
  (reset! current-theme theme))

; Input hiccup example
#_(def input-hiccup [:div.panel__wrapper
                   [:div.panel
                    [:div.panel__left]
                    [:div.panel__right]]])
#_(def component-key :main-panel)

#_(def theme {
            :main-panel {
                         :div.panel ["flex" "w-full"]
                         :div.panel__left ["w-1/2" "p-4" "border-r" "border-gray-200"]
                         :div.panel__right ["w-1/2" "p-4"]
            }
})

(defn apply-theme
  "Walks the hiccup structure and, for each element, looks for
   classes in the component-key or the :library key of the theme
   (which is meant to provide global styles), and applies them 
   to the element."
  [hiccup ^keyword component-key ^map theme]
  (let [component-theme (get theme component-key)
        library-theme (get theme :library)
        result (postwalk
                  (fn [form]
                    (if (and (vector? form) (not (map-entry? form)) (keyword? (first form)))
                      (let [tag (first form)
                            theme-classes (get component-theme tag)
                            library-classes (get library-theme tag)
                            new-classes (into [] (concat theme-classes library-classes))
                            has-classes (not= 0 (count new-classes))]
                        (if has-classes
                          (if (map? (second form))
                            (update form 1 update :class concat new-classes)
                            (into [tag {:class new-classes}] (rest form))) ; THIS LINE IS BREAKING THINGs
                          form))
                      form)) ; - else - not a vector starting with a keyword
                  hiccup)] ; - else - no theme for component
    result))

#_(print (apply-theme input-hiccup component-key theme))
; => [:div.panel__wrapper [:div.panel {:class [flex w-full]} [:div.panel__left {:class [w-1/2 p-4 border-r border-gray-200]}] [:div.panel__right {:class [w-1/2 p-4]}]]]

(defn apply-current-theme
  "Applies the theme set in current-theme to the hiccup provided as input."

  ([hiccup]
   (apply-current-theme hiccup (first hiccup)))

  ([hiccup ^keyword component-key]
   (if (nil? current-theme)
    (throw (js/Error. "Current theme is nil. Call set-theme before using apply-current-theme."))
    (apply-theme hiccup component-key @current-theme))))