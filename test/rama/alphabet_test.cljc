(ns rama.alphabet-test
  (:refer-clojure :excude [next])
  (:require [clojure.test :refer :all]
            [rama.alphabet :refer [next
                                   previous
                                   take-after
                                   take-before]]))

(deftest next-letter
  (testing "getting the next letter"
    (is (= "B" (next "A")))
    (is (= "C" (next "B")))
    (is (= "D" (next "C")))
    (is (= "E" (next "D")))
    (is (= "F" (next "E")))
    (is (= "G" (next "F")))
    (is (= "H" (next "G")))
    (is (= "I" (next "H")))
    (is (= "J" (next "I")))
    (is (= "K" (next "J")))
    (is (= "L" (next "K")))
    (is (= "M" (next "L")))
    (is (= "N" (next "M")))
    (is (= "O" (next "N")))
    (is (= "P" (next "O")))
    (is (= "Q" (next "P")))
    (is (= "R" (next "Q")))
    (is (= "S" (next "R")))
    (is (= "T" (next "S")))
    (is (= "U" (next "T")))
    (is (= "V" (next "U")))
    (is (= "W" (next "V")))
    (is (= "X" (next "W")))
    (is (= "Y" (next "X")))
    (is (= "Z" (next "Y")))
    (is (= "A" (next "Z")))))

(deftest previous-letter
  (testing "getting the previous letter"
    (is (= "Z" (previous "A")))
    (is (= "A" (previous "B")))
    (is (= "B" (previous "C")))
    (is (= "C" (previous "D")))
    (is (= "D" (previous "E")))
    (is (= "E" (previous "F")))
    (is (= "F" (previous "G")))
    (is (= "G" (previous "H")))
    (is (= "H" (previous "I")))
    (is (= "I" (previous "J")))
    (is (= "J" (previous "K")))
    (is (= "K" (previous "L")))
    (is (= "L" (previous "M")))
    (is (= "M" (previous "N")))
    (is (= "N" (previous "O")))
    (is (= "O" (previous "P")))
    (is (= "P" (previous "Q")))
    (is (= "Q" (previous "R")))
    (is (= "R" (previous "S")))
    (is (= "S" (previous "T")))
    (is (= "T" (previous "U")))
    (is (= "U" (previous "V")))
    (is (= "V" (previous "W")))
    (is (= "W" (previous "X")))
    (is (= "X" (previous "Y")))
    (is (= "Y" (previous "Z")))))

(deftest get-next-letters
  (testing "getting the next n letters"
    (is (= ["A" "B" "C"] (take-after 3 "Z")))))

(deftest get-previous-letters
  (testing "getting the previous n letters"
    (is (= ["Z" "A" "B"] (take-before 3 "C")))
    (is (= ["H" "I" "J" "K" "L"] (take-before 5 "M")))))
