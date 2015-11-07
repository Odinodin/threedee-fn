(ns threedee.core
  (:require [cljs.pprint :refer [pprint]]
            [cljsjs.three :as THREE]
            [threedee.vectors :as v]))

(enable-console-print!)

(def WIDTH 600)
(def HEIGHT 600)
(def attractor-acceleration 0.01)
(def max-velocity 0.2)

(defonce scene (THREE.Scene.))
(def camera (THREE.PerspectiveCamera. 75 (/ WIDTH HEIGHT) 0.2 1000))
(defonce renderer (THREE.WebGLRenderer. #js {"antialias" true}))
(defonce light (do
                 (let [light (THREE.DirectionalLight. 0x0000ff 1)]
                   (.set (.-position light) 0.5 -1 0)
                   (.add scene light))))

(.setPixelRatio renderer js.window.devicePixelRatio)
(.setSize renderer WIDTH HEIGHT)

(defonce x (.appendChild (.getElementById js/document "main") renderer.domElement))

(defonce model (atom {:attractor {:id "attractor" :pos [0 0]}
                      :balls     (for [n (range 1 10)]
                                   {:id n :pos [(* n 0.4) 0] :velocity [0 0.3] :acceleration [0 0]})}))

(defn accelerate-entities [entities]
  (for [entity entities]
    (assoc entity :velocity (->
                              (v/vplus (:velocity entity) (:acceleration entity))
                              (v/vlimit max-velocity)))))

(defn move-entities [entities]
  (for [entity entities]
    (assoc entity :pos (v/vplus (:pos entity) (:velocity entity)))))

(defn calculate-attraction-force [ball attractor]
  (let [force-direction (v/vsub (:pos attractor) (:pos ball))
        normalized (v/vnormalize force-direction)
        with-strength (v/vmult normalized attractor-acceleration)]
    with-strength))

(defn apply-attract-force [balls attractor]
  (for [ball balls]
    (assoc ball :acceleration (calculate-attraction-force ball attractor))))

(defn accelerate-balls [model]
  (update-in model [:balls] accelerate-entities))

(defn attract-balls [model]
  (update-in model [:balls] apply-attract-force (:attractor model)))

(defn move-balls [model]
  (update-in model [:balls] move-entities))

(defn update-model [model]
  "Updates the model"
  (-> model
      attract-balls
      accelerate-balls
      move-balls))

(def geometry (THREE.SphereGeometry. 0.2 0.3 0.1))
(def material (THREE.MeshPhongMaterial. #js {"color" 0xffffff
                                             "specular" 0x555555
                                             "shininess" 30}))

(defn add-items-to-scene [scene model]
  (doseq [ball (conj (:balls model) (:attractor model))]
    (prn (str "Adding: " ball))
    (let [ball-mesh (THREE.Mesh.
                      geometry
                      material)]
      (set! (.. ball-mesh -name) (:id ball))
      (set! (.. ball-mesh -position -x) (first (:pos ball)))
      (set! (.. ball-mesh -position -y) (second (:pos ball)))
      (.add scene ball-mesh))))

(set! (.-z camera.position) 5)

(defn animate [model scene]
  #_(prn "obj: " (.. (.getObjectByName scene 2) -position -x))
  (doseq [ball (:balls model)]
    (let [mesh (.getObjectByName scene (:id ball))]
      (set! (.. mesh -position -x) (first (:pos ball)))
      (set! (.. mesh -position -y) (second (:pos ball)))))

  #_(set! (.-x cube.rotation) (+ cube.rotation.x 0.002))
  #_(set! (.-y cube.rotation) (+ cube.rotation.y 0.004)))

(defn main []
  (swap! model update-model)
  (animate @model scene)
  (.render renderer scene camera))

(defn mainloop []
  ;; assign every call to js/requestAnimationFrame to global RAF var
  ;; required to clean up render loop before each evaluation
  #_(set! RAF (js/requestAnimationFrame render))
  (main)
  (js/requestAnimationFrame mainloop))

(defonce initial-setup
         (do
           (prn "Initial setup")
           (add-items-to-scene scene @model)
           (mainloop)))