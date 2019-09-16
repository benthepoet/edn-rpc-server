(ns edn-rpc-server.core
  (:require [clojure.edn :as edn]
            [ring.util.response :as response]))

(def remote-fns (atom {}))

(defn add-remote
  [sym func]
  (swap! remote-fns assoc (keyword sym) func))

(defn call-remote
  [sym & args]
  (let [func (get @remote-fns (keyword sym))]
    (apply func args)))

(defn parse-body
  [request]
  (let [rdr (clojure.java.io/reader (:body request))]
    (slurp rdr)))

(add-remote 'add
            (fn [& args] (apply + args)))

(add-remote 'multiply
            (fn [& args] (apply * args)))

(add-remote 'concat
            (fn [& args] (apply str args)))

(defn handler
  [request]
  (let [body (parse-body request)
        expr (edn/read-string body)]
    (if (or (nil? body) (nil? expr))
      (response/bad-request "Invalid request.")
      (response/response (pr-str (apply call-remote expr))))))
