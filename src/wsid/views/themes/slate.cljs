(ns wsid.views.themes.slate)

; Styles from theme-library will be applied to every component
; It allows for building shareable component styles, for example
; for form elements and other common UI
(def theme-library 
  {
   :svg.icon ["w-4" "h-4"]
   :input.button ["font-sans" "px-1" "rounded-sm" "border" "border-b-4" "border-slate-300" "bg-slate-100" "hover:bg-slate-200" "transition-colors" "duration-250"]
   :input.button.button--danger ["font-sans" "px-1" "rounded-sm" "border" "border-b-4" "border-slate-300" "border-b-red-400" "bg-slate-100" "hover:bg-slate-200" "transition-colors" "duration-250"]
   :input.button.button--primary ["font-sans" "px-1" "rounded-sm" "border" "border-b-4" "border-slate-300" "border-b-slate-400" "bg-slate-100" "hover:bg-slate-200" "transition-colors" "duration-250"]
   })

(def theme {:library theme-library
            :main.main-container {:main.main-container
                                  ["mx-auto"
                                   "max-w-2xl"
                                   "px-4"
                                   "md:px-0"
                                   "text-cyan-700"]

                                  :div.title__wrapper
                                  ["my-4"]

                                  :span.title__acronym
                                  ["text-3xl"
                                   "uppercase"]

                                  :h1.title
                                  ["text-l"]}

            :div.modal {:div.modal
                        ["fixed"
                         "w-screen"
                         "h-screen"
                         "left-0"
                         "top-0"
                         "text-cyan-700"]

                        :div.modal__backdrop
                        ["size-full"
                         "flex"
                         "items-center"
                         "bg-stone-100/80"
                         "backdrop-blur-xs"]
                        
                        :div.modal__dialog
                        ["shadow-2xl"
                         "min-h-20"
                         "mx-auto"
                         "rounded-sm"
                         "border"
                         "border-slate-300"
                         "bg-slate-100"]

                        :div.modal-container__heading
                        ["py-2"
                         "px-4"
                         "bg-slate-200"
                         "border-b"
                         "border-slate-300"
                         "font-normal"]

                        :div.modal-container__main
                        ["py-2"
                         "px-4"]}

            :div.factor-form {:h2.factor-form__heading
                              ["text-xl"
                               "bg-slate-200"]

                              :div.factor-form__actions
                              ["flex"
                               "gap-1"
                               "justify-end"
                               "mt-4"
                               "mb-2"]}

            :div.factor-card {:div.factor-card
                              ["bg-slate-200"
                               "hover:bg-slate-400"]}})