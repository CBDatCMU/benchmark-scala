#!/bin/bash
#
# Scala HPC Benchmark Runner
#
# Copyright (c) 2026 Pittsburgh Supercomputing Center (icaoberg)
# All rights reserved.
#
# Author:
#   Ivan Cao-Berg (icaoberg)
#
# Usage:
#   bash script.sh
#
# Description:
#   Loads the Scala environment module, compiles the benchmark source,
#   and runs ScalaBenchmark with a 10,000,000-element array and a
#   24 GB JVM heap limit.

# Load the Scala environment (requires Environment Modules)
module load scala

# Compile the benchmark
scalac scala_benchmark.scala

# Run the benchmark; -J-Xmx24g raises the JVM heap ceiling to 24 GB
# to accommodate large array allocations without GC pressure
scala -J-Xmx24g ScalaBenchmark 10000000
