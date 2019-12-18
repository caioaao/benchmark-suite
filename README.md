# benchmark-suite [![Clojars Project](https://img.shields.io/clojars/v/com.caioaao/benchmark-suite.svg)](https://clojars.org/com.caioaao/benchmark-suite)

A **very** simple benchmark suite, built on top of [criterium](https://github.com/hugoduncan/criterium).

## Usage

First define the benchmark configuration:

```clojure
(require '[com.caioaao.benchmark :refer [defbenchmark run-benchmark!]])
(defn fetch-dataset [] (range))

(defn runner-fn [vals]
  (fn []
    (Thread/sleep 1000)
    (doall (map inc vals))))

(defbenchmark my-fn
  #::benchmark{:runner   runner-fn
               :datasets {:simple-list #::benchmark {:dataset-fn   fetch-dataset
                                                     :sample-sizes {:small 1
                                                                    :large 10}}}})
```

The config requires two keys:
- ``::benchmark/runner``: a function that receives the sampled data and results in a 0-arity function, which will be used to run the benchmark. This function will be re-generated at every benchmark configuration
- ``::benchmark/datasets``: a map of dataset configurations. The key will be the dataset id (used in the reports) and the value has the dataset function and the sample sizes that can be used with this dataset

To run a config, simply call:

```clojure
(run-benchmark! my-fn)
```

Check the code for `run-benchmark!` to see the other options available. This example outputs the following report:

```
Benchmark user/my-fn, dataset :simple-list, sample size 1 (sample size key = :small, sample size base = 1)
Evaluation count : 60 in 60 samples of 1 calls.
             Execution time mean : 1.000145 sec
    Execution time std-deviation : 101.343525 µs
   Execution time lower quantile : 1.000071 sec ( 2.5%)
   Execution time upper quantile : 1.000476 sec (97.5%)
                   Overhead used : 2.008060 ns

Found 8 outliers in 60 samples (13.3333 %)
	low-severe	 2 (3.3333 %)
	low-mild	 6 (10.0000 %)
 Variance from outliers : 1.6389 % Variance is slightly inflated by outliers


Benchmark user/my-fn, dataset :simple-list, sample size 10 (sample size key = :large, sample size base = 10)
Evaluation count : 60 in 60 samples of 1 calls.
             Execution time mean : 1.000151 sec
    Execution time std-deviation : 86.481547 µs
   Execution time lower quantile : 1.000073 sec ( 2.5%)
   Execution time upper quantile : 1.000436 sec (97.5%)
                   Overhead used : 2.008060 ns

Found 6 outliers in 60 samples (10.0000 %)
	low-severe	 3 (5.0000 %)
	low-mild	 3 (5.0000 %)
 Variance from outliers : 1.6389 % Variance is slightly inflated by outliers

```

# TODO

- [ ] Discover and run benchmarks like a test runner
- [ ] Support benchmarks that don't need datasets
- [ ] Benchmark tests (comparing results to a baseline - goal is to protect projects from performance regressions)
- [ ] API to run only a subset of benchmarks
- [ ] kaocha runner?
- [ ] Option to run [quick-benchmark](http://hugoduncan.org/criterium/0.4/api/criterium.core.html#var-quick-benchmark) instead of the more rigorous [benchmark](http://hugoduncan.org/criterium/0.4/api/criterium.core.html#var-benchmark)

