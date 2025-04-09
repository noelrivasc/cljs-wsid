(ns wsid.views.themes.slate)

(def theme-library 
  {
   :svg.icon ["w-4" "h-4"]
   })

(def theme {:library theme-library
            :main.main-container {:span.title__acronym ["text-xl"]}
            :div.factor-card {:div.factor-card ["bg-slate-300" "hover:bg-slate-400"]}})