(ns com.caioaao.benchmark
  (:require [criterium.core :as criterium]
            [clojure.string :as str])
  (:import [java.io BufferedReader StringReader]))

(def ^:dynamic *samples-multiplier* 1)
(def ^:dynamic *indent-count* 0)
(def ^:dynamic *indent-char* " ")

(defn- get-indent [] (apply str (repeat *indent-count* *indent-char*)))

(defmacro ^:private with-inc-indent [& body]
  `(binding [*indent-count* (inc *indent-count*)]
     ~@body))

(defn- split-lines
  "Preserves empty results in the end (so that (str/join \"\n\" (split-lines s)) = s)"
  [s]
  (line-seq (BufferedReader. (StringReader. (str s "\n")))))

(defn- indented-str [s]
  (->> (split-lines s)
       (map (partial apply str) (repeatedly get-indent))
       (str/join "\n")))

(defn- indented-print [& more]
  (binding [*print-readably* nil]
    (apply pr (map indented-str more))))

(defn- indented-println [& more]
  (binding [*print-readably* nil]
    (apply prn (map indented-str more))))

(defmacro ^:private with-indent-control [& body]
  `(with-redefs [print   indented-print
                 println indented-println]
     ~@body))

(defmacro with-samples-multiplier [x & body]
  `(binding [*samples-multiplier* ~x]
     ~@body))

(defn samples [n]
  (int (* *samples-multiplier* n)))

(defmacro defbenchmark [sym config]
  `(def ~(vary-meta sym assoc ::benchmark true) ~config))

(defn config->runner [conf-sym report-opts {::keys [datasets runner]}]
  (fn [opts]
    (with-indent-control
      (println (str "Running benchmark " conf-sym))
      (with-inc-indent
        (doseq [[dataset-k dataset-conf] datasets]
          (println (str "\nDataset " dataset-k))
          (with-inc-indent
            (doseq [[sample-k sample-base] (::sample-sizes dataset-conf)]
              (println (str "\nSample size " (samples sample-base) " (name = "sample-k ", base =" sample-base ")"))
              (let [runner-fn (runner (-> ((::dataset-fn dataset-conf))
                                          (->> (take (samples sample-base)))
                                          doall))]
                (criterium/report-result
                 (criterium/benchmark (runner-fn) opts)
                 report-opts)))))))))

(def ^:dynamic *default-report-opts* {:verbose true})

(defmacro run-benchmark!
  [config & {:keys [samples-multiplier report-opts]
             :as   opts}]
  `((config->runner '~config (merge *default-report-opts* ~report-opts) ~config) ~opts))

(run-benchmark! {::datasets {:bla {::dataset-fn   (constantly [1 2 3])
                                   ::sample-sizes {:small 1
                                                   :large 3}}}
                 ::runner   #(constantly (count %))})
