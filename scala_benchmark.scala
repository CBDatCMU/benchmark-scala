/*
 * Scala HPC Benchmark
 *
 * Copyright (c) 2026 Pittsburgh Supercomputing Center (icaoberg)
 * All rights reserved.
 *
 * Author:
 *   Ivan Cao-Berg (icaoberg)
 */

import scala.util.Random
import scala.collection.mutable.ArrayBuffer
import java.net.InetAddress

object ScalaBenchmark {

  val Version = "1.0.0"

  /** Elapsed-time result for a single benchmark task. */
  case class Result(name: String, seconds: Double)

  /**
   * Times a named block of code using wall-clock nanoseconds.
   * The block is executed exactly once; no warm-up is performed.
   */
  def benchmark(name: String)(block: => Unit): Result = {
    val start = System.nanoTime()
    block
    val end = System.nanoTime()
    Result(name, (end - start) / 1e9)
  }

  /** Reads the first CPU model name from /proc/cpuinfo (Linux only). */
  def cpuModel(): String = {
    try {
      val src = scala.io.Source.fromFile("/proc/cpuinfo")
      val model = src.getLines()
        .find(_.startsWith("model name"))
        .map(_.split(":", 2)(1).trim)
        .getOrElse("Unknown")
      src.close()
      model
    } catch {
      case _: Throwable => "Unknown"
    }
  }

  /** Returns total installed RAM in GiB from /proc/meminfo (Linux only). */
  def totalMemoryGB(): Double = {
    try {
      val src = scala.io.Source.fromFile("/proc/meminfo")
      val line = src.getLines().find(_.startsWith("MemTotal")).get
      src.close()
      val kb = line.split("\\s+")(1).toLong
      kb / 1024.0 / 1024.0
    } catch {
      case _: Throwable => 0.0
    }
  }

  /**
   * Entry point.
   *
   * Args:
   *   args(0) — optional array size N (default 10,000,000)
   *
   * Benchmarks executed (in order):
   *   1. Allocate Random Array  — heap allocation + RNG fill
   *   2. Sum Array              — sequential reduction
   *   3. Map sqrt               — element-wise transformation (new array)
   *   4. Sort Array             — in-place quicksort on a cloned copy
   *   5. Monte Carlo Pi         — random-sampling floating-point throughput
   *   6. STREAM Copy            — memory bandwidth (read + write)
   *   7. STREAM Triad           — memory bandwidth (fused multiply-add)
   *   8. Matrix Multiply 256x256 — dense O(n³) compute kernel
   *   9. Prime Sieve            — Sieve of Eratosthenes up to 5,000,000
   */
  def main(args: Array[String]): Unit = {

    val n =
      if (args.nonEmpty) args(0).toInt
      else 10000000

    val runtime = Runtime.getRuntime()

    val hostname =
      try InetAddress.getLocalHost.getHostName
      catch {
        case _: Throwable => "Unknown"
      }

    println()
    println("====================================================================")
    println(s"              Scala HPC Benchmark v$Version")
    println("              Pittsburgh Supercomputing Center")
    println("====================================================================")

    printf("%-20s : %s%n", "Hostname", hostname)
    printf("%-20s : %s%n", "CPU", cpuModel())
    printf("%-20s : %.1f GB%n", "Memory", totalMemoryGB())
    printf("%-20s : %d%n", "Logical CPUs", runtime.availableProcessors())
    printf("%-20s : %s%n", "Java", System.getProperty("java.version"))
    printf("%-20s : %s%n", "Scala", util.Properties.versionNumberString)
    printf("%-20s : %s%n", "OS", System.getProperty("os.name"))
    printf("%-20s : %s%n", "Architecture", System.getProperty("os.arch"))
    printf("%-20s : %,d%n", "Array Size", n)

    println("====================================================================")

    val results = ArrayBuffer[Result]()

    // Benchmark 1: measure time to allocate and fill an N-element Double array
    val allocStart = System.nanoTime()
    val data = Array.fill(n)(Random.nextDouble())
    val allocEnd = System.nanoTime()

    results += Result(
      "Allocate Random Array",
      (allocEnd - allocStart) / 1e9
    )

    // Benchmark 2: sequential reduction — exercises cache and scalar throughput
    results += benchmark("Sum Array") {
      val s = data.sum
      assert(s > 0)
    }

    // Benchmark 3: element-wise sqrt — allocates a new output array
    results += benchmark("Map sqrt") {
      val out = data.map(math.sqrt)
      assert(out.nonEmpty)
    }

    // Benchmark 4: sort a clone so the original array stays unsorted for later tasks
    results += benchmark("Sort Array") {
      val copy = data.clone()
      scala.util.Sorting.quickSort(copy)
    }

    // Benchmark 5: estimate π via Monte Carlo sampling — stresses RNG + branch prediction
    results += benchmark("Monte Carlo Pi") {
      val rng = new Random(42)
      var inside = 0
      var i = 0

      while (i < n) {
        val x = rng.nextDouble()
        val y = rng.nextDouble()

        if (x * x + y * y <= 1.0)
          inside += 1

        i += 1
      }

      val pi = 4.0 * inside / n
      assert(pi > 3.0 && pi < 3.3)
    }

    // Benchmark 6: STREAM Copy — measures memory read+write bandwidth (b[i] = a[i])
    results += benchmark("STREAM Copy") {
      val a = Array.fill(n)(1.0)
      val b = new Array[Double](n)

      var i = 0
      while (i < n) {
        b(i) = a(i)
        i += 1
      }

      assert(b(n - 1) == 1.0)
    }

    // Benchmark 7: STREAM Triad — fused multiply-add bandwidth (c[i] = a[i] + scalar*b[i])
    results += benchmark("STREAM Triad") {
      val a = Array.fill(n)(1.0)
      val b = Array.fill(n)(2.0)
      val c = new Array[Double](n)
      val scalar = 3.0

      var i = 0
      while (i < n) {
        c(i) = a(i) + scalar * b(i)
        i += 1
      }

      assert(c(n - 1) == 7.0)
    }

    // Benchmark 8: naive O(n³) matrix multiply on 256×256 matrices — compute-bound kernel
    results += benchmark("Matrix Multiply 256x256") {
      val size = 256

      val a = Array.fill(size, size)(Random.nextDouble())
      val b = Array.fill(size, size)(Random.nextDouble())
      val c = Array.ofDim[Double](size, size)

      var i = 0
      while (i < size) {
        var j = 0
        while (j < size) {
          var sum = 0.0
          var k = 0

          while (k < size) {
            sum += a(i)(k) * b(k)(j)
            k += 1
          }

          c(i)(j) = sum
          j += 1
        }
        i += 1
      }

      assert(c(0)(0) >= 0.0)
    }

    // Benchmark 9: Sieve of Eratosthenes up to 5,000,000 — tests boolean array thrashing
    results += benchmark("Prime Sieve") {
      val limit = 5000000
      val isPrime = Array.fill(limit + 1)(true)

      isPrime(0) = false
      isPrime(1) = false

      var p = 2

      while (p * p <= limit) {
        if (isPrime(p)) {
          var m = p * p

          while (m <= limit) {
            isPrime(m) = false
            m += p
          }
        }

        p += 1
      }

      val count = isPrime.count(identity)
      assert(count > 0)
    }

    println()
    println("===================== Benchmark Results =====================")

    printf("%-32s %12s%n", "Benchmark", "Seconds")
    println("-------------------------------------------------------------")

    results.foreach { r =>
      printf("%-32s %12.4f%n", r.name, r.seconds)
    }

    println("-------------------------------------------------------------")

    val total = results.map(_.seconds).sum

    printf("%-32s %12.4f%n", "TOTAL", total)

    println("=============================================================")
  }
}
