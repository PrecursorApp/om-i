(ns om-i.hacks
  (:require [goog.dom :as dom]))

(def om-i-css "@keyframes in-fade-top-soft{0%{opacity:0;transform:translate3d(0, -4rem, 0)}100%{opacity:1;transform:none}}@-webkit-keyframes in-fade-top-soft{0%{opacity:0;-webkit-transform:translate3d(0, -4rem, 0)}100%{opacity:1;-webkit-transform:none}}.om-instrumentation{user-select:none;-moz-user-select:none;-webkit-user-select:none;pointer-events:none;color:#888;position:fixed;z-index:1000;top:0;left:0;width:100%}.instrumentation-table{-webkit-animation:in-fade-top-soft 500ms;animation:in-fade-top-soft 500ms;background-color:rgba(0,0,0,0.6);font-family:Monaco,monospace;font-size:.75rem;line-height:2;width:100%}.instrumentation-table th{color:#fff;line-height:1rem;padding:1.5rem 0;text-transform:uppercase;text-align:left}.instrumentation-table th:not(:first-child){text-align:center}.instrumentation-table th.left{text-align:left}.instrumentation-table th.right{text-align:right}.instrumentation-table th,.instrumentation-table td{white-space:pre-wrap}.instrumentation-table th:first-child,.instrumentation-table td:first-child{padding-left:1.5rem}.instrumentation-table tbody tr:nth-child(odd){background-color:rgba(0,0,0,0.4)}.instrumentation-table tbody td:nth-child(even){text-align:right;border-right:1px dashed;padding-right:.5em}.instrumentation-table tbody td:nth-child(odd){text-align:left;padding-left:.5em}.instrumentation-table tbody td:first-child{padding-left:1.5rem}.instrumentation-table tfoot td{line-height:1rem;text-align:center;padding:1.5rem 0}.instrumentation-table small{font-size:1em;opacity:.5}")

(defn insert-styles
  "This shouldn't be used in real code, but it can be useful when
   exploring the code for the first time. Closure will eliminate this
   function in production unless you're using it in production. You're not
   using it in production, are you?"
  []
  (let [s (goog.dom/createElement "style")]
    (goog.dom/setTextContent s om-i-css)
    (goog.dom/appendChild js/document.head s)))
