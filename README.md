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

## Results

```
====================================================================
              Scala HPC Benchmark v1.0.0
              Pittsburgh Supercomputing Center
====================================================================
Hostname             : lanec2.compbio.cs.cmu.edu
CPU                  : Intel(R) Xeon(R) Silver 4314 CPU @ 2.40GHz
Memory               : 62.1 GB
Logical CPUs         : 64
Java                 : 17.0.19
Scala                : 2.13.16
OS                   : Linux
Architecture         : amd64
Array Size           : 10,000,000
====================================================================

===================== Benchmark Results =====================
Benchmark                             Seconds
-------------------------------------------------------------
Allocate Random Array                  0.3065
Sum Array                              0.0692
Map sqrt                               0.0383
Sort Array                             1.1826
Monte Carlo Pi                         0.5650
STREAM Copy                            0.0564
STREAM Triad                           0.2583
Matrix Multiply 256x256                0.0408
Prime Sieve                            0.0696
-------------------------------------------------------------
TOTAL                                  2.5867
=============================================================
```

## License

Copyright (c) 2026 Pittsburgh Supercomputing Center. All rights reserved.
