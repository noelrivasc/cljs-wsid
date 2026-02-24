(ns wsid.domain.scenarios)

(defn create-new-scenario
  "Creates a new empty scenario.
   Output:
   - A map that has the shape of ::db/scenario but does NOT
     conform to it (empty id, title and description)."
  []
  {:id ""
   :title ""
   :description ""})

(defn scenario-update-property
  "Updates the value of a property"
  [scenario property value]
  (assoc scenario (keyword property) value))

(defn scenario-create-id
  "Adds a UUID to a scenario's :id if it's empty.
   
   Arguments:
   - scenario: map conforming to ::db/scenario
   
   Returns: [scenario is-new]
   - scenario: ::db/scenario
   - is-new: boolean"
  [scenario]
  (let [is-new (= "" (:id scenario))
        scenario-id (if is-new
                      (.toString (random-uuid))
                      (:id scenario))]
    [(assoc scenario :id scenario-id) is-new]))

(defn initialize-scenario-factor-values
  "Creates a factor-values map for a scenario, initialized with nil values.
   
   Arguments:
   - factors: collection of factor maps with :id keys
   
   Returns:
   - map of factor-id -> nil"
  [factors]
  (zipmap (map :id factors) (repeat nil)))

(defn initialize-factor-in-scenario-values
  "Adds a new factor with nil value to all existing scenario factor values.
   
   Arguments:
   - scenario-factor-values: map of scenario-id -> {factor-id -> value}
   - factor-id: string, the id of the factor to add
   
   Returns:
   - Updated scenario-factor-values map"
  [scenario-factor-values factor-id]
  (update-vals scenario-factor-values
               (fn [factor-values]
                 (assoc factor-values factor-id nil))))

(defn prune-factor-from-scenario-values
  "Removes a factor from all scenario factor values.
   
   Arguments:
   - scenario-factor-values: map of scenario-id -> {factor-id -> value}
   - factor-id: string, the id of the factor to remove
   
   Returns:
   - Updated scenario-factor-values map"
  [scenario-factor-values factor-id]
  (update-vals scenario-factor-values
               (fn [factor-values]
                 (dissoc factor-values factor-id))))

(defn calculate-scenario-score
  "Calculate the weighted score for a scenario given factor values and factors.

  Args:
    factor-values: map of factor-id -> numeric value
    factors: collection of factor maps with :id and :weight keys

  Returns:
    Numeric total score (sum of factor-value * factor-weight)"
  [factor-values factors]
  (apply + (map #(* (get factor-values (:id %)) (:weight %)) factors)))

(defn enrich-factors-with-scenario-values
  "Enrich factors with scenario-specific values.

  Args:
    factor-values: map of factor-id -> numeric value
    factors: collection of factor maps

  Returns:
    Collection of factors with :scenario-value added to each"
  [factor-values factors]
  (for [f factors]
    (conj f {:scenario-value (get factor-values (:id f))})))
