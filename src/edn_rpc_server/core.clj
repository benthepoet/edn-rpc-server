(ns edn-rpc-server.core
  (:require [clojure.edn :as edn]
            [ring.util.request :refer [body-string]]
            [ring.util.response :as response]))

(def remote-fns (atom {}))

(defn add-remote
  [sym func]
  (swap! remote-fns assoc (keyword sym) func))

(add-remote 'add
            (fn [& args] (apply + args)))

(add-remote 'multiply
            (fn [& args] (apply * args)))

(add-remote 'concat
            (fn [& args] (apply str args)))

(defn call-remote
  [sym & args]
  (let [func (get @remote-fns (keyword sym))]
    (apply func args)))

(defn parse-body
  [request]
  (let [rdr (clojure.java.io/reader (:body request))]
    (slurp rdr)))

(defn handler
  [request]
  (response/response
   (pr-str (apply call-remote
                 (edn/read-string (parse-body request))))))
