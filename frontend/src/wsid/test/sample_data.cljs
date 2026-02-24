(ns wsid.test.sample-data)

(def sample-decision-simple
  {:title "Sample Decision"
   :description "This is a sample decision, to be used for testing."
   :factors [{:id "637852c0-7340-409f-a835-b8fb77ab48f4"
              :title "Factor 1"
              :description "First factor, weight 5."
              :weight 5}
             {:id "e777be62-379c-4bfb-8be2-aec7f04567e9"
              :title "Factor 2"
              :description "Second factor, weight 1."
              :weight 1}
             {:id "47570f6f-9dc5-497c-ad30-6c913a7f5484"
              :title "Factor 3"
              :description "Third factor, weight 10"
              :weight 10}]
   :scenarios [{:id "baa92a52-1641-41f2-984d-1a55969dc248"
                :title "Option A"
                :description "This is option a."}
               {:id "9375f0f1-d337-47f1-b66d-b01186f5c8bf"
                :title "Option B"
                :description "This is option b."}]
   :scenario-factor-values {"baa92a52-1641-41f2-984d-1a55969dc248" {"637852c0-7340-409f-a835-b8fb77ab48f4" nil
                                                                    "e777be62-379c-4bfb-8be2-aec7f04567e9" nil
                                                                    "47570f6f-9dc5-497c-ad30-6c913a7f5484" nil}
                            "9375f0f1-d337-47f1-b66d-b01186f5c8bf" {"637852c0-7340-409f-a835-b8fb77ab48f4" nil
                                                                    "e777be62-379c-4bfb-8be2-aec7f04567e9" nil
                                                                    "47570f6f-9dc5-497c-ad30-6c913a7f5484" nil}}})

(def sample-decision-doritos
  {:title "Eat an apple, a bag of doritos or something else"
   :description "I'm very hungry. I have an apple and a bag of doritos at hand, but those may not be the best options. I may be allergic to apples. There are some stores nearby. I'm doing exercise later, so I'll be needing the energy. I need to decide what to eat."
   :factors [{:id "a1b2c3d4-e5f6-7890-1234-567890abcdef"
              :title "Nutritional value"
              :description "How healthy and energizing the food is"
              :weight 8}
             {:id "b2c3d4e5-f6g7-8901-2345-67890abcdefa"
              :title "Allergy risk"
              :description "Potential for adverse reactions"
              :weight 9}
             {:id "c3d4e5f6-g7h8-9012-3456-7890abcdef12"
              :title "Convenience"
              :description "How quickly and easily the food can be consumed"
              :weight 6}
             {:id "d4e5f6g7-h8i9-0123-4567-890abcdef234"
              :title "Energy for exercise"
              :description "How well the food supports upcoming physical activity"
              :weight 7}
             {:id "e5f6g7h8-i9j0-1234-5678-90abcdef3456"
              :title "Cost"
              :description "Financial impact of the choice"
              :weight 5}]
   :scenarios [{:id "f6g7h8i9-j0k1-2345-6789-0abcdef45678"
                :title "Eat the apple"
                :description "Consume the apple despite potential allergy risk"}
               {:id "g7h8i9j0-k1l2-3456-7890-1abcdef56789"
                :title "Eat the doritos"
                :description "Consume the bag of doritos for quick energy"}
               {:id "h8i9j0k1-l2m3-4567-8901-2abcdef67890"
                :title "Go to a store"
                :description "Purchase a more balanced meal from a nearby store"}]
   :scenario-factor-values {"f6g7h8i9-j0k1-2345-6789-0abcdef45678" {"a1b2c3d4-e5f6-7890-1234-567890abcdef" 8
                                                                    "b2c3d4e5-f6g7-8901-2345-67890abcdefa" -5
                                                                    "c3d4e5f6-g7h8-9012-3456-7890abcdef12" 10
                                                                    "d4e5f6g7-h8i9-0123-4567-890abcdef234" 7
                                                                    "e5f6g7h8-i9j0-1234-5678-90abcdef3456" 0}
                            "g7h8i9j0-k1l2-3456-7890-1abcdef56789" {"a1b2c3d4-e5f6-7890-1234-567890abcdef" 3
                                                                    "b2c3d4e5-f6g7-8901-2345-67890abcdefa" 0
                                                                    "c3d4e5f6-g7h8-9012-3456-7890abcdef12" 9
                                                                    "d4e5f6g7-h8i9-0123-4567-890abcdef234" 6
                                                                    "e5f6g7h8-i9j0-1234-5678-90abcdef3456" 0}
                            "h8i9j0k1-l2m3-4567-8901-2abcdef67890" {"a1b2c3d4-e5f6-7890-1234-567890abcdef" 9
                                                                    "b2c3d4e5-f6g7-8901-2345-67890abcdefa" 0
                                                                    "c3d4e5f6-g7h8-9012-3456-7890abcdef12" 5
                                                                    "d4e5f6g7-h8i9-0123-4567-890abcdef234" 8
                                                                    "e5f6g7h8-i9j0-1234-5678-90abcdef3456" -3}}})