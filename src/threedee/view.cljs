(ns threedee.view)

(defn mouse-x->world-x [x canvas-width visible-width]
  (- (/ x (/ canvas-width visible-width))
     (/ visible-width 2)))

(defn mouse-y->world-y [y canvas-height visible-height]
  (- (/ y (/ canvas-height visible-height))
     (/ visible-height 2)))

(defn move-mesh! [mesh [x y z]]
  (.set (.-position mesh) x y z))

(defn sphere [radius]
  (THREE.SphereGeometry. radius 32 32 0 6.3 0 3.1))

(defn material [color]
  (THREE.MeshPhongMaterial. #js {"color"     color
                                 "specular"  0x555555
                                 "shininess" 30}))

(defn add-directional-light! [scene [x y z]]
  (let [light (THREE.DirectionalLight. 0xffffff 1)]
    (.set (.-position light) x y z)
    (.add scene light)))

(defn add-hemi-light! [scene]
  (let [hemi-light (THREE.HemisphereLight. 0xffffff 0xffffff 0.6)]
    (.set (.-position hemi-light) 0 500 0)
    (.add scene hemi-light)))

(defn camera [width height [x y z]]
  (let [c (THREE.PerspectiveCamera. 75 (/ width height) 0.2 1000)]
    (.set (.-position c) x y z)
    c))

(defn renderer [width height]
  (let [r (THREE.WebGLRenderer. #js {"antialias" true})]
    (.setPixelRatio r js.window.devicePixelRatio)
    (.setSize r width height)
    r))