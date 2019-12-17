(ns com.caioaao.benchmark
  (:require [criterium.core :as criterium]))

(def ^:dynamic *samples-multiplier* 1)
(def ^:dynamic *indent-count* 0)
(def ^:dynamic *indent-char* " ")

(defn- get-indent [] (apply str (repeat *indent-count* *indent-char*)))

(defmacro ^:private with-inc-indent [& body]
  `(binding [*indent-count* (inc *indent-count*)]
     ~@body))

(defn- indented-print [& more]
  (binding [*print-readably* nil]
    (apply pr (get-indent))
    (apply pr more)))

(defn- indented-println [& more]
  (binding [*print-readably* nil]
    (apply pr (get-indent))
    (apply prn more)))

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
          (println (str "Dataset " dataset-k))
          (with-inc-indent
            (doseq [[sample-k sample-base] (::sample-sizes dataset-conf)]
              (println (str "Sample size " (samples sample-base) " (name = "sample-k ", base =" sample-base ")"))
              (let [selected-data (-> ((::dataset-fn dataset-conf))
                                      (->> (take (samples sample-base)))
                                      doall)
                    runner-fn (runner)]
                (criterium/report-result
                 (criterium/benchmark (runner-fn selected-data) opts)
                 report-opts)))))))))

(def ^:dynamic *default-report-opts* {:verbose true})

(defmacro run-benchmark!
  [config & {:keys [samples-multiplier report-opts]
             :as   opts}]
  `((config->runner '~config (merge *default-report-opts* ~report-opts) ~config) ~opts))
