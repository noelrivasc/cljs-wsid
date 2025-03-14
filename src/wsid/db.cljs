(ns wsid.db)

(def default-db
  {
   ; Information that is used for procedures but that is
   ; not the resulting data that is the goal of the program
   :transient {
               :factor-edit-defaults nil
               :factor-active nil
   }
   :factors {
             :all []
   }})