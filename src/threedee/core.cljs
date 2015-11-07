(ns threedee.core
  (:require [cljs.pprint :refer [pprint]]
            [cljsjs.three :as THREE]
            [threedee.vectors :as v]
            [threedee.view :as view]))

(enable-console-print!)

(def WIDTH 600)
(def HEIGHT 600)
(def attractor-acceleration 0.005)
(def max-velocity 0.11)

(defonce scene (THREE.Scene.))

(def camera (view/camera WIDTH HEIGHT [0 0 6]))

(defonce renderer (view/renderer WIDTH HEIGHT))

(defonce model (atom {:attractor {:id     "attractor"
                                  :pos    [0 0 0]
                                  :color  0xff4444
                                  :radius 0.2}
                      :balls     (for [n (range 1 100)]
                                   {:id           n
                                    :pos          [4 0 (* n 1)]
                                    :velocity     [0 0.3 0]
                                    :acceleration [0 0 0]
                                    :color        0xeedddd
                                    :radius       0.1})}))

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

(defn add-items-to-scene [scene model]
  (doseq [ball (conj (:balls model) (:attractor model))]
    (let [ball-mesh (THREE.Mesh.
                      (view/sphere (:radius ball))
                      (view/material (:color ball)))]
      (set! (.. ball-mesh -name) (:id ball))
      (view/move-mesh! ball-mesh (:pos ball))
      (.add scene ball-mesh))))

(def visible-height (let [vfov (/ (* camera.fov js/Math.PI) 180)]
                      (* 2 (js/Math.tan (/ vfov 2)) (.-z camera.position))))

(def visible-width (* visible-height (/ WIDTH HEIGHT)))

(defn on-mouse-down [e]
  (let [x-mouse (- (.-clientX e) (.-offsetLeft renderer.domElement))
        y-mouse (- HEIGHT (- (+ (.-clientY e) (.-scrollY js/window)) (.-offsetTop renderer.domElement)))]
    (swap! model (fn [old] (-> old
                               (assoc-in [:attractor :pos 0] (view/mouse-x->world-x x-mouse WIDTH visible-width))
                               (assoc-in [:attractor :pos 1] (view/mouse-y->world-y y-mouse HEIGHT visible-height)))))))

(defn animate [model scene]
  #_(prn "obj: " (.. (.getObjectByName scene 2) -position -x))
  (doseq [ball (conj (:balls model) (:attractor model))]
    (let [mesh (.getObjectByName scene (:id ball))]
      (view/move-mesh! mesh (:pos ball)))))

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
           (.addEventListener js/document "mousedown" on-mouse-down false)
           (add-items-to-scene scene @model)
           (view/add-hemi-light! scene)
           (view/add-directional-light! scene [0.5 -1 0])
           (.appendChild (.getElementById js/document "main") renderer.domElement)
           (mainloop)))