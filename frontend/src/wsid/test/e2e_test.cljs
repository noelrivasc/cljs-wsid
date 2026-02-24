(ns wsid.test.e2e-test
  (:require
   [cljs.test :refer-macros [deftest testing is]]
   [day8.re-frame.test :as rf-test]
   [re-frame.core :as rf]
   [wsid.events.util :refer [evt>]]
   [wsid.domain.scenarios :refer [calculate-scenario-score]]
   [wsid.test.sample-data :as samples]
   ;; Import events to register handlers
   [wsid.events.main]
   [wsid.subs.main]))

(deftest test-factor-value-update-and-score-calculation
  (testing "Load sample decision, update factor value, and verify score calculation"
    (rf-test/run-test-sync
      ;; Reset the app-db to an empty state
      (rf/dispatch [:app/wipe-state])
      
      ;; Load the simple sample decision
      (rf/dispatch [:app/load-decision samples/sample-decision-simple])
      
      ;; Update factor values for Option A scenario
      ;; Set Factor 1 (weight 5) = 8, Factor 2 (weight 1) = 6, Factor 3 (weight 10) = 4
      (let [scenario-id "baa92a52-1641-41f2-984d-1a55969dc248"  ;; Option A
            factor-values {"637852c0-7340-409f-a835-b8fb77ab48f4" 8  ;; Factor 1
                           "e777be62-379c-4bfb-8be2-aec7f04567e9" 6  ;; Factor 2  
                           "47570f6f-9dc5-497c-ad30-6c913a7f5484" 4} ;; Factor 3
            expected-score (+ (* 8 5) (* 6 1) (* 4 10))]  ;; 8*5 + 6*1 + 4*10 = 40 + 6 + 40 = 86
        
        ;; Update the factor values
        (rf/dispatch [:scenario-factor-values-update scenario-id factor-values])
        
        ;; Get the current state using subscriptions (now in reactive context)
        (let [db @(rf/subscribe [:app/db])
              factors (get-in db [:decision :factors])
              stored-factor-values (get-in db [:decision :scenario-factor-values scenario-id])
              calculated-score (calculate-scenario-score stored-factor-values factors)]
          
          ;; Verify the factor values were stored correctly
          (is (= factor-values stored-factor-values))
          
          ;; Verify the calculated score matches expected
          (is (= expected-score calculated-score)))))))

(comment
  ;; Reset the app-db to an empty state.
  (evt> [:app/wipe-state])
  
  ;; Load the Doritos sample decision
  (evt> [:app/load-decision samples/sample-decision-doritos])
  
  ;; Run the test
  (test-factor-value-update-and-score-calculation)
  )
