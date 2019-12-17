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

(defn benchmark [name-sym config]
  (vary-meta config merge {::benchmark name-sym}))

(defmacro defbenchmark [sym config]
  `(def ~(vary-meta sym assoc ::benchmark true) (benchmark '~sym ~config)))

(defn config->runner [conf-name report-opts {::keys [datasets runner]}]
  (fn [opts]
    (doseq [[dataset-k dataset-conf] datasets]
      (doseq [[sample-k sample-base] (::sample-sizes dataset-conf)]
        (binding [*flush-on-newline* true]
          (printf "Benchmark %s, dataset %s, sample size %d (sample size key = %s, sample size base = %d)"
                  conf-name dataset-k (samples sample-base) sample-k sample-base)
          (println)
          (let [runner-fn (runner (-> ((::dataset-fn dataset-conf))
                                      (->> (take (samples sample-base)))
                                      doall))]
            (criterium/report-result
             (criterium/benchmark (runner-fn) opts)
             report-opts)
            (println "\n")))))))

(def ^:dynamic *default-report-opts* {:verbose true})

(defn run-benchmark!
  [config &
   {:keys [samples-multiplier report-opts]
    :as   opts}]
  ((config->runner (::benchmark (meta config))
                   (merge *default-report-opts* report-opts)
                   config)
   opts))
