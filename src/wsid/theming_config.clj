(ns wsid.theming-config
  (:require
   [remolino :as r]
   [wsid.views.themes.slate :as slate]))

(defmacro theme [component] 
 (if (vector? component)
   (r/apply-theme component slate/theme)
   component))