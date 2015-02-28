(ns om-i.hacks
  (:require [goog.dom :as dom]))

(def om-i-css "@keyframes admin-slide-down{0%{transform:translate(0, -100%);-webkit-transform:translate(0, -100%)}100%{transform:translate(0, 0);-webkit-transform:translate(0, 0)}}@-webkit-keyframes admin-slide-down{0%{transform:translate(0, -100%);-webkit-transform:translate(0, -100%)}100%{transform:translate(0, 0);-webkit-transform:translate(0, 0)}}.om-instrumentation figure{margin:0}.om-instrumentation .admin-stats{animation:admin-slide-down 200ms ease-in-out;-webkit-animation:admin-slide-down 200ms ease-in-out;width:100%;color:white;background-color:black;z-index:2000;position:fixed;top:0;left:0;opacity:.7;pointer-events:none;user-select:none}.om-instrumentation table{font-family:monaco;font-size:12px;width:calc(98%);margin:0 1em 1em}.om-instrumentation table th,.om-instrumentation table td{text-align:left;white-space:pre}.om-instrumentation table tr:not(:last-child){border-bottom:1px dashed rgba(255,255,255,0.1)}.om-instrumentation table .number{text-align:right}")

(defn insert-styles
  "This shouldn't be used in real code, but it can be useful when
   exploring the code for the first time. Closure will eliminate this
   function in production unless you're using it in production. You're not
   using it in production, are you?"
  []
  (let [s (goog.dom/createElement "style")]
    (goog.dom/setTextContent s om-i-css)
    (goog.dom/appendChild js/document.head s)))
