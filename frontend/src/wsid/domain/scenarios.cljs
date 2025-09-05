(ns wsid.domain.scenarios)

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
