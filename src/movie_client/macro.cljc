(ns rama.macro)

(defmacro let-map
  [kvs]
  {:pre [(vector? kvs)
         (even? (count kvs))]}
  `(let ~(vec kvs)
     ~(into {} (map #(vector (keyword %) %) (take-nth 2 kvs)))))

(defmacro is=
  [expected actual]
  `(cljs.test/is (= ~expected
                    ~actual)
                 (with-out-str (cljs.pprint/pprint ~actual))))
