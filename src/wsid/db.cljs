(ns wsid.db)

(def default-db
  {
   :name "Some name pues"
   :factors [
             {
              :id "99699f15-3dc7-4f48-b5c2-827092c26e9f"
              :title "Factor A - only positive, high weight"
              :description "Description of the factor A"
              :min 0
              :max 10
              :weight 0
             }
             {
              :id "02ff1662-21b0-4dc4-894b-ffdaae42b5da"
              :title "Factor B - both ways, mild weight"
              :description "Description of the factor B"
              :min 0
              :max 10
              :weight 0
             }
             {
              :id "7e55b826-5101-4c64-a9e9-0993e35c7d3c"
              :title "Factor C - only negative, high weight"
              :description "Description of the factor C"
              :min -10
              :max 0
              :weight 0
             }
   ]})