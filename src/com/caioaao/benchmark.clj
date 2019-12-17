(ns com.caioaao.benchmark
  (:require [criterium.core :as criterium]
            [clojure.string :as str])
  (:import [java.io BufferedReader StringReader]))

(def ^:dynamic *samples-multiplier* 1)

(defmacro with-samples-multiplier [x & body]
  `(binding [*samples-multiplier* ~x]
     ~@body))

(defn samples [n]
  (int (* *samples-multiplier* n)))

(defmacro benchmark [name config]
  `(vary-meta ~config assoc ::benchmark '~(symbol (resolve name))))

(defmacro defbenchmark [name config]
  `(def ~name (benchmark ~name ~config)))

(defn config->runner [{::keys [datasets runner] :as conf} report-opts]
  (fn [opts]
    (doseq [[dataset-k dataset-conf] datasets]
      (doseq [[sample-k sample-base] (::sample-sizes dataset-conf)]
        (binding [*flush-on-newline* true]
          (printf "Benchmark %s, dataset %s, sample size %d (sample size key = %s, sample size base = %d)"
                  (::benchmark (meta conf)) dataset-k (samples sample-base) sample-k sample-base)
          (println)
          (let [runner-fn (runner (-> ((::dataset-fn dataset-conf))
                                      (->> (take (samples sample-base)))
                                      doall))]
            (criterium/report-result
             (criterium/benchmark (runner-fn) opts)
             report-opts)
            (println "\n")))))))

(def ^:dynamic *default-report-opts* {:verbose true})

(defmacro run-benchmark!
  [config &
   {:keys [samples-multiplier report-opts]
    :as   opts}]
  `((config->runner ~config ~(merge *default-report-opts* report-opts))
    ~opts))
