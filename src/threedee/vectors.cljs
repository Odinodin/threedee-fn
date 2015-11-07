(ns threedee.vectors)

(defn vplus [a b]
  "Add two vectors together"
  [(+ (first a) (first b))
   (+ (second a) (second b))
   (+ (nth a 2) (nth b 2))])

(defn vsub [a b]
  "Subtractor two vectors"
  [(- (first a) (first b))
   (- (second a) (second b))
   (- (nth a 2) (nth b 2))])

(defn vmult [vector factor]
  "Multiply the vector by a factor"
  [(* factor (first vector))
   (* factor (second vector))
   (* factor (nth vector 2))])

(defn vdiv [vector dividend]
  "Divide a vector by dividend"
  [(/ (first vector) dividend)
   (/ (second vector) dividend)
   (/ (nth vector 2) dividend)])

(defn vmagnitude [vector]
  "The magnitude of the vector, an absolute number. Calculated as
  square root of (x^2 + y^2 + z^2)"
  (let [x (first vector)
        y (second vector)
        z (nth vector 2)]
    (->> (* x x)
         (+ (* y y))
         (+ (* z z))
         (.sqrt js/Math))))

(defn vnormalize [vector]
  "Normalize the vector"
  (let [magnitude (vmagnitude vector)]
    (if (not= magnitude 0)
      (vdiv vector magnitude)
      vector)))

(defn vlimit [vector max-magnitude]
  "Limit the magnitude of the vector"
  (if (> (vmagnitude vector) max-magnitude)
    (vmult (vnormalize vector) max-magnitude)
    vector))