(ns ^:figwheel-always snakelake.main
  (:require
    [snakelake.ainit]
    [snakelake.view :as view]
    [reagent.core :as reagent]))

(reagent/render-component
  [view/main]
  (. js/document (getElementById "app")))
