(ns threedee.core
  (:require [cljs.pprint :refer [pprint]]
            [cljsjs.three :as THREE]
            [threedee.vectors :as v]))

(enable-console-print!)

(def WIDTH 600)
(def HEIGHT 600)
(def attractor-acceleration 0.005)
(def max-velocity 0.11)

(defonce scene (THREE.Scene.))
(def camera (THREE.PerspectiveCamera. 75 (/ WIDTH HEIGHT) 0.2 1000))
(set! (.-z camera.position) 7)
(defonce renderer (THREE.WebGLRenderer. #js {"antialias" true}))
(.setPixelRatio renderer js.window.devicePixelRatio)
(.setSize renderer WIDTH HEIGHT)
(defonce lighting (do
                    (let [light (THREE.DirectionalLight. 0xffffff 1)]
                      (.set (.-position light) 0.5 -1 0)
                      (.add scene light))

                    (let [hemi-light (THREE.HemisphereLight. 0xffffff 0xffffff 0.6)]
                      #_(.setHSL (.-color hemi-light) 0xffffff)
                      (.set (.-position hemi-light) 0 500 0)
                      (.add scene hemi-light))))

(def model (atom {:attractor {:id     "attractor"
                              :pos    [0 0 0]
                              :color  0xff4444
                              :radius 0.2}
                  :balls     (for [n (range 1 70)]
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

(defn sphere [radius] (THREE.SphereGeometry. radius 32 32 0 6.3 0 3.1))
(defn material [color] (THREE.MeshPhongMaterial. #js {"color"     color
                                                      "specular"  0x555555
                                                      "shininess" 30}))

(defn move-mesh! [mesh [x y z]]
  (.set (.-position mesh) x y z))

(defn add-items-to-scene [scene model]
  (doseq [ball (conj (:balls model) (:attractor model))]
    (let [ball-mesh (THREE.Mesh.
                      (sphere (:radius ball))
                      (material (:color ball)))]
      (set! (.. ball-mesh -name) (:id ball))
      (move-mesh! ball-mesh (:pos ball))
      (.add scene ball-mesh))))

(def visible-height (let [vfov (/ (* camera.fov js/Math.PI) 180)]
                      (* 2 (js/Math.tan (/ vfov 2)) (.-z camera.position))))

(def visible-width (* visible-height (/ WIDTH HEIGHT)))

(defn animate [model scene]
  #_(prn "obj: " (.. (.getObjectByName scene 2) -position -x))
  (doseq [ball (conj (:balls model) (:attractor model))]
    (let [mesh (.getObjectByName scene (:id ball))]
      (move-mesh! mesh (:pos ball)))))

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

(defn mouse-x->world-x [x]
  (- (/ x (/ WIDTH visible-width)) (/ visible-width 2)))

(defn mouse-y->world-y [y]
  (- (/ y (/ HEIGHT visible-height)) (/ visible-height 2)))

(defn on-mouse-down [e]
  (let [x-mouse (- (.-clientX e) (.-offsetLeft renderer.domElement))
        y-mouse (- HEIGHT (- (+ (.-clientY e) (.-scrollY js/window)) (.-offsetTop renderer.domElement)))]
    (swap! model (fn [old] (-> old
                               (assoc-in [:attractor :pos 0] (mouse-x->world-x x-mouse))
                               (assoc-in [:attractor :pos 1] (mouse-y->world-y y-mouse)))))))

(defonce initial-setup
         (do
           (prn "Initial setup")
           (.addEventListener js/document "mousedown" on-mouse-down false)
           (add-items-to-scene scene @model)
           (.appendChild (.getElementById js/document "main") renderer.domElement)
           (mainloop)))