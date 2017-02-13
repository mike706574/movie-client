(ns rama.macro)

(defmacro let-map
  [kvs]
  {:pre [(vector? kvs)
         (even? (count kvs))]}
  `(let ~(vec kvs)
     ~(into {} (map #(vector (keyword %) %) (take-nth 2 kvs)))))
