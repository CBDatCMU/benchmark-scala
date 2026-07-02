# Scala HPC Benchmark

A single-file Scala benchmark suite for evaluating CPU, memory, and compute
performance on HPC clusters. Developed at the
[Pittsburgh Supercomputing Center](https://www.psc.edu).

## Requirements

- Scala 2.x (loaded via Environment Modules on PSC systems)
- JVM with at least 24 GB of heap available (`-Xmx24g`)

## Usage

```bash
bash script.sh
```

The script loads the `scala` module, compiles `scala_benchmark.scala`, and
runs the benchmark with a default array size of 10,000,000 elements.

To run manually with a different array size:

```bash
module load scala
scalac scala_benchmark.scala
scala -J-Xmx24g ScalaBenchmark <N>
```

## Benchmarks

| # | Name | What it measures |
|---|------|-----------------|
| 1 | Allocate Random Array | Heap allocation + RNG fill time |
| 2 | Sum Array | Sequential reduction / scalar throughput |
| 3 | Map sqrt | Element-wise transformation (new array allocated) |
| 4 | Sort Array | In-place quicksort on a cloned copy |
| 5 | Monte Carlo Pi | RNG throughput + branch prediction (estimates π) |
| 6 | STREAM Copy | Memory bandwidth — `b[i] = a[i]` |
| 7 | STREAM Triad | Memory bandwidth — `c[i] = a[i] + scalar * b[i]` |
| 8 | Matrix Multiply 256×256 | Dense O(n³) compute kernel |
| 9 | Prime Sieve | Sieve of Eratosthenes to 5,000,000 |

## Sample Output

```
====================================================================
              Scala HPC Benchmark v1.0.0
              Pittsburgh Supercomputing Center
====================================================================
Hostname             : node001
CPU                  : Intel(R) Xeon(R) Gold 6248R CPU @ 3.00GHz
Memory               : 192.0 GB
Logical CPUs         : 48
...
====================================================================

===================== Benchmark Results =====================
Benchmark                           Seconds
-------------------------------------------------------------
Allocate Random Array                  0.4123
Sum Array                              0.0512
...
TOTAL                                  5.1234
=============================================================
```

## License

Copyright (c) 2026 Pittsburgh Supercomputing Center. All rights reserved.
