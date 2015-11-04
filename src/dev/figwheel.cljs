(ns dev.figwheel
  (:require [cljs.core.async :refer [put!]]))

(defn reload-hook []
  (fn []
    (print "reloaded")))