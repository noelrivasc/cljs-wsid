(ns wsid.views.themes.slate)

(def theme-library 
  {
   :svg.icon ["w-4" "h-4"]
   })

(def theme {:library theme-library
            :main.main-container {:main.main-container ["mx-auto" "max-w-xl"]
                                  :span.title__acronym ["text-xl"]}
            :div.factor-card {:div.factor-card ["bg-slate-300" "hover:bg-slate-400"]}
            :dialog.modal-container {:dialog.modal-container ["absolute" "w-screen" "h-screen" "bg-red-300" "left-0" "top-0"]}
            })