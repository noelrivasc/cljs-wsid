(ns wsid.views.icons
  (:require
   [clojure.string :as str]))

(defn get-icon
  ([icon] (assoc icon 0 :svg.icon))

  ([icon tag]
   (let [not-svg (not (= ":svg" (first (str/split (str tag) #"\."))))]

     (when not-svg (js/console.error "The tag passed to get-icon must be a keyword starting with :svg"))

     (assoc icon 0 tag))))

(def edit [:svg {:xmlns "http://www.w3.org/2000/svg", :viewBox "0 0 512 512"} "<!--!Font Awesome Free 6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.-->"
           [:path {:d "M362.7 19.3L314.3 67.7 444.3 197.7l48.4-48.4c25-25 25-65.5 0-90.5L453.3 19.3c-25-25-65.5-25-90.5 0zm-71 71L58.6 323.5c-10.4 10.4-18 23.3-22.2 37.4L1 481.2C-1.5 489.7 .8 498.8 7 505s15.3 8.5 23.7 6.1l120.3-35.4c14.1-4.2 27-11.8 37.4-22.2L421.7 220.3 291.7 90.3z"}]])

(def square-plus [:svg {:xmlns "http://www.w3.org/2000/svg", :viewBox "0 0 448 512"} "<!--!Font Awesome Free 6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.-->"
                  [:path {:d "M64 80c-8.8 0-16 7.2-16 16l0 320c0 8.8 7.2 16 16 16l320 0c8.8 0 16-7.2 16-16l0-320c0-8.8-7.2-16-16-16L64 80zM0 96C0 60.7 28.7 32 64 32l320 0c35.3 0 64 28.7 64 64l0 320c0 35.3-28.7 64-64 64L64 480c-35.3 0-64-28.7-64-64L0 96zM200 344l0-64-64 0c-13.3 0-24-10.7-24-24s10.7-24 24-24l64 0 0-64c0-13.3 10.7-24 24-24s24 10.7 24 24l0 64 64 0c13.3 0 24 10.7 24 24s-10.7 24-24 24l-64 0 0 64c0 13.3-10.7 24-24 24s-24-10.7-24-24z"}]])

(def circle-question [:svg {:xmlns "http://www.w3.org/2000/svg", :viewBox "0 0 512 512"} "<!--!Font Awesome Free 6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.-->"
                      [:path {:d "M464 256A208 208 0 1 0 48 256a208 208 0 1 0 416 0zM0 256a256 256 0 1 1 512 0A256 256 0 1 1 0 256zm169.8-90.7c7.9-22.3 29.1-37.3 52.8-37.3l58.3 0c34.9 0 63.1 28.3 63.1 63.1c0 22.6-12.1 43.5-31.7 54.8L280 264.4c-.2 13-10.9 23.6-24 23.6c-13.3 0-24-10.7-24-24l0-13.5c0-8.6 4.6-16.5 12.1-20.8l44.3-25.4c4.7-2.7 7.6-7.7 7.6-13.1c0-8.4-6.8-15.1-15.1-15.1l-58.3 0c-3.4 0-6.4 2.1-7.5 5.3l-.4 1.2c-4.4 12.5-18.2 19-30.6 14.6s-19-18.2-14.6-30.6l.4-1.2zM224 352a32 32 0 1 1 64 0 32 32 0 1 1 -64 0z"}]])
