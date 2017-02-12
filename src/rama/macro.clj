(ns rama.macro)

(defmacro let-map
  {:pre [(vector? kvs)
         (even? (count kvs))]}
  [kvs]
  `(let ~(vec kvs)
     ~(into {} (map #(vector (keyword %) %) (take-nth 2 kvs)))))
