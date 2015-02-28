(ns om-i.util)

(defn react-id [x]
  (let [id (aget x "_rootNodeID")]
    (assert id)
    id))
