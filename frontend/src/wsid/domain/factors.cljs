(ns wsid.domain.factors)

(defn create-new-factor
  "Creates a new empty factor with a default weight.
   Output:
   - A map that has the shape of ::db/factor but does NOT
     conform to it (empty title and description)."
  []

  {:id ""
   :title ""
   :description ""
   :weight 5})

(defn factor-create-id
  "Adds a UUID to a factor's :id if it's empty.
   
   Arguments:
   - factor: map conforming to ::db/factor
   
   Returns: [factor is-new]
   - factor: ::db/factor
   - is-new: boolean"
  [factor]
  (let [is-new (= "" (:id factor))
        factor-id (if is-new
                    (.toString (random-uuid))
                    (:id factor))]
    [(assoc factor :id factor-id) is-new]))

(defn factor-update-property
  "Updates the value of a property"
  [factor property value]
  (assoc factor (keyword property) value))