(ns om-i.core
  (:require [clojure.string :as str]
            [goog.dom]
            [goog.string :as gstring]
            [goog.string.format]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-i.keyboard :as keyboard]))

;; map of display name to component render stats, e.g.
;; {"App" {:last-will-update <time 3pm> :display-name "App" :last-did-update <time 3pm> :render-ms [10 39 20 40]}}
(defonce component-stats (atom {}))

(defn wrap-will-update
  "Tracks last call time of componentWillUpdate for each component, then calls
   the original componentWillUpdate."
  [f]
  (fn [next-props next-state]
    (this-as this
      (swap! component-stats update-in [((aget this "getDisplayName"))]
             merge {:display-name ((aget this "getDisplayName"))
                    :last-will-update (goog/now)})
      (.call f this next-props next-state))))

(defn wrap-did-update
  "Tracks last call time of componentDidUpdate for each component and updates
   the render times (using start time provided by wrap-will-update), then
   calls the original componentDidUpdate."
  [f]
  (fn [prev-props prev-state]
    (this-as this
      (swap! component-stats update-in [((aget this "getDisplayName"))]
             (fn [stats]
               (let [now (goog/now)]
                 (-> stats
                   (assoc :last-did-update now)
                   (update-in [:render-ms] (fnil conj [])
                              (max (- now (:last-will-update stats now)) 0))))))
      (.call f this prev-props prev-state))))

(defn wrap-will-mount
  "Tracks last call time of componentWillMount for each component, then calls
   the original componentWillMount."
  [f]
  (fn []
    (this-as this
      (swap! component-stats update-in [((aget this "getDisplayName"))]
             merge {:display-name ((aget this "getDisplayName"))
                    :last-will-mount (goog/now)})
      (.call f this))))

(defn wrap-did-mount
  "Tracks last call time of componentDidMount for each component and updates
   the render times (using start time provided by wrap-will-mount), then
   calls the original componentDidMount."
  [f]
  (fn []
    (this-as this
      (swap! component-stats update-in [((aget this "getDisplayName"))]
             (fn [stats]
               (let [now (goog/now)]
                 (-> stats
                   (assoc :last-did-mount now)
                   (update-in [:mount-ms] (fnil conj [])
                              (max (- now (:last-will-mount stats now)) 0))))))
      (.call f this))))

(defn instrument-methods [methods]
  (-> methods
    (update-in [:componentWillUpdate] wrap-will-update)
    (update-in [:componentDidUpdate] wrap-did-update)
    (update-in [:componentWillMount] wrap-will-mount)
    (update-in [:componentDidMount] wrap-did-mount)))

(def instrumentation-methods
  (om/specify-state-methods!
   (-> om/pure-methods
     (instrument-methods)
     (clj->js))))

(defn avg [coll]
  (/ (reduce + coll)
     (count coll)))

(defn std-dev [coll]
  (let [a (avg coll)]
    (Math/sqrt (avg (map #(Math/pow (- % a) 2) coll)))))

(defn compare-display-name [a b]
  (compare (:display-name b)
           (:display-name a)))

(defn compare-last-update [a b]
  (let [res (compare (max (:last-will-update a) (:last-will-mount a))
                     (max (:last-will-update b) (:last-will-mount b)))]
    (if (zero? res)
      (compare-display-name a b)
      res)))

(defn format-shortcut [key-set]
  (str/join "+" (sort-by (comp - count) key-set)))

(defn stats-view [data owner {:keys [clear-shortcut toggle-shortcut sort-shortcut]}]
  (reify
    om/IDisplayName (display-name [_] "Om Instrumentation")
    om/IInitState (init-state [_] {:shown? false
                                   :sort-orders (cycle [:last-update :display-name
                                                        :mount-count :render-count])})
    om/IDidMount
    (did-mount [_]
      (keyboard/register-key-handler owner {clear-shortcut #(om/transact! data (constantly {}))
                                            toggle-shortcut #(om/update-state! owner :shown? not)
                                            sort-shortcut #(om/update-state! owner :sort-orders rest)}))
    om/IWillUnmount
    (will-unmount [_] (keyboard/dispose-key-handler owner))
    om/IRenderState
    (render-state [_ {:keys [shown? sort-orders]}]
      (dom/figure nil
        (when shown?
          (let [sort-order (first sort-orders)
                stats-compare (case sort-order
                                :last-update compare-last-update
                                :display-name compare-display-name
                                (fn [x y] (compare (sort-order x) (sort-order y))))
                stats (map (fn [[display-name renders]]
                             (let [render-times (filter identity (mapcat :render-ms renders))
                                   mount-times (filter identity (mapcat :mount-ms renders))]
                               {:display-name (or display-name "Unknown")
                                :render-count (count render-times)
                                :mount-count (count mount-times)

                                :last-will-update (last (sort (map :last-will-update renders)))
                                :last-will-mount (last (sort (map :last-will-mount renders)))

                                :last-render-ms (last (:render-ms (last (sort-by :last-did-update renders))))
                                :last-mount-ms (last (:mount-ms (last (sort-by :last-did-mount renders))))

                                :average-render-ms (when (seq render-times) (int (avg render-times)))
                                :average-mount-ms (when (seq mount-times) (int (avg mount-times)))

                                :max-render-ms (when (seq render-times) (apply max render-times))
                                :max-mount-ms (when (seq mount-times) (apply max mount-times))

                                :min-render-ms (when (seq render-times) (apply min render-times))
                                :min-mount-ms (when (seq mount-times) (apply min mount-times))

                                :render-std-dev (when (seq render-times) (int (std-dev render-times)))
                                :mount-std-dev (when (seq mount-times) (int (std-dev mount-times)))}))
                           (reduce (fn [acc [display-name data]]
                                     (update-in acc [(:display-name data)] (fnil conj []) data))
                                   {} data))]
            (dom/table #js {:className "instrumentation-table"}
              (dom/thead nil
                (dom/tr nil
                  (dom/th nil "component")
                  (dom/th #js {:className "number right"} "render ")
                  (dom/th #js {:className "number left"} "/ mount")
                  (dom/th #js {:className "number" :colSpan "2"} "last-ms")
                  (dom/th #js {:className "number" :colSpan "2"} "average-ms")
                  (dom/th #js {:className "number" :colSpan "2"} "max-ms")
                  (dom/th #js {:className "number" :colSpan "2"} "min-ms")
                  (dom/th #js {:className "number" :colSpan "2"} "std-ms")))
              (apply dom/tbody nil
                     (for [{:keys [display-name
                                   last-will-update last-will-mount
                                   average-render-ms average-mount-ms
                                   max-render-ms max-mount-ms
                                   min-render-ms min-mount-ms
                                   render-std-dev mount-std-dev
                                   render-count mount-count
                                   last-render-ms last-mount-ms] :as stat}
                           (reverse (sort stats-compare stats))]
                       (dom/tr nil
                         (dom/td nil display-name)
                         (dom/td #js {:className "number" } render-count)
                         (dom/td #js {:className "number" } (when mount-count (gstring/format "%4d" mount-count)))

                         (dom/td #js {:className "number" } last-render-ms)
                         (dom/td #js {:className "number" } (when last-mount-ms (gstring/format "%3d" last-mount-ms)))

                         (dom/td #js {:className "number" } average-render-ms)
                         (dom/td #js {:className "number" } (when average-mount-ms (gstring/format "%3d" average-mount-ms)))

                         (dom/td #js {:className "number" } max-render-ms)
                         (dom/td #js {:className "number" } (when max-mount-ms (gstring/format "%3d" max-mount-ms)))

                         (dom/td #js {:className "number" } min-render-ms)
                         (dom/td #js {:className "number" } (when min-mount-ms (gstring/format "%3d" min-mount-ms)))

                         (dom/td #js {:className "number" } render-std-dev)
                         (dom/td #js {:className "number" } (when mount-std-dev (gstring/format "%3d" mount-std-dev))))))
              (dom/tfoot nil
                (dom/tr nil
                  (dom/td #js {:className "instrumentation-info" :colSpan "13"}
                          (gstring/format "Component render stats, sorted by %s (%s). Clicks go through. %s to toggle, %s to clear."
                                          sort-order
                                          (format-shortcut sort-shortcut)
                                          (format-shortcut toggle-shortcut)
                                          (format-shortcut clear-shortcut))))))))))))

(defn prepend-stats-node [classname]
  (let [node (goog.dom/htmlToDocumentFragment (gstring/format "<div class='%s'></div>" classname))
        body js/document.body]
    (.insertBefore body node (.-firstChild body))
    node))

(defn setup-component-stats!
  ([]
   (setup-component-stats! {}))
  ([{:keys [class clear-shortcut toggle-shortcut sort-shortcut]
     :or {class "om-instrumentation"
          clear-shortcut #{"shift" "ctrl" "alt" "k"}
          toggle-shortcut #{"shift" "ctrl" "alt" "j"}
          sort-shortcut #{"shift" "ctrl" "alt" "s"}}}]
   (let [stats-node (or (goog.dom/getElementByClass class)
                        (prepend-stats-node class))]
     (om/root
      stats-view
      component-stats
      {:target stats-node
       :opts {:clear-shortcut clear-shortcut
              :toggle-shortcut toggle-shortcut
              :sort-shortcut sort-shortcut}}))))
