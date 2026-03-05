# Technology Stack: v1.3.0 Performance Additions

**Project:** openrndr-geo  
**Milestone:** v1.3.0 Performance  
**Researched:** 2026-03-05  
**Confidence:** HIGH (verified with official sources)

## Executive Summary

For the v1.3.0 performance milestone focusing on batch projection and geometry caching, the stack requires minimal additions to the existing Kotlin/JVM + OPENRNDR foundation. The core additions are: **Caffeine** for geometry caching, **JMH** for benchmarking, and **kotlinx.coroutines** for parallel batch processing. All choices prioritize compatibility with OPENRNDR's single-threaded OpenGL context constraint and the creative coding use case.

## Existing Stack (Validated)

| Technology | Version | Purpose | Status |
|------------|---------|---------|--------|
| Kotlin/JVM | 2.2.10 | Core language | ✓ Existing |
| OPENRNDR | 0.4.5 | Creative coding framework | ✓ Existing |
| JVM | 17 | Runtime | ✓ Existing |
| proj4j | Latest | CRS transformations | ✓ Existing |
| Gradle (KTS) | 8.x | Build system | ✓ Existing |

## New Additions for v1.3.0

### Core Caching

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **Caffeine** | 3.2.3 | High-performance geometry cache | Near-optimal caching with TinyLFU eviction. Used by Kafka, Cassandra, Neo4j. 6x faster than Guava Cache. |
| **Aedile** | 3.0.2 | Kotlin coroutine wrapper for Caffeine | Provides `suspend` functions, Kotlin Duration support, and idiomatic Kotlin API. Eliminates Java Future boilerplate. |

**Rationale:** Caffeine is the industry standard for JVM caching with peer-reviewed eviction algorithms (TinyLFU). Aedile provides a thin, idiomatic Kotlin wrapper that integrates with coroutines—critical for non-blocking cache operations in animation loops.

**When to use Caffeine directly vs Aedile:**
- Use **Aedile** for async cache loading with coroutines (animation frame cache misses)
- Use **Caffeine directly** for simple synchronous caches (projected geometry store)

### Benchmarking

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **JMH (Java Microbenchmark Harness)** | 1.37 | Microbenchmark framework | Official OpenJDK tool. Eliminates JVM warmup, JIT, and GC noise for accurate measurements. |
| **JMH Gradle Plugin** | 0.7.3 | Gradle integration | Community-standard plugin. Isolates benchmarks in `src/jmh` source set. |

**Rationale:** JMH is the only reliable way to benchmark JVM code. Hand-rolled timing loops are unreliable due to JIT optimization, dead code elimination, and JVM warmup effects. The Gradle plugin provides clean separation from main/test sources.

### Parallel Processing

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **kotlinx.coroutines** | 1.10.2 | Structured concurrency | Already available via OPENRNDR. Use `Dispatchers.Default` for CPU-bound batch projection work. |

**Rationale:** Coroutines are already in the classpath via OPENRNDR. For batch coordinate projection (CPU-bound, not I/O-bound), `Dispatchers.Default` provides optimal thread pool sizing (cores + 2). No additional threading libraries needed.

**Critical constraint:** OPENRNDR's OpenGL context is single-threaded. All `Drawer` operations must happen on the main thread. Parallel processing is ONLY for data transformation (coordinate projection), not rendering.

## Integration Architecture

### Geometry Caching Pipeline

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Raw Geometry   │────▶│  Projection Cache│────▶│  Screen Geometry│
│  (Lat/Lng)      │     │  (Caffeine)      │     │  (Vector2)      │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  Cache Key:      │
                       │  geometryId +    │
                       │  viewportHash    │
                       └──────────────────┘
```

### Batch Projection Flow

```kotlin
// CPU-bound work on background threads
val projected = withContext(Dispatchers.Default) {
    geometries.parMap { geo ->
        projectToScreen(geo, viewport)
    }
}

// Back to main thread for OpenGL rendering
withContext(Dispatchers.Main) {
    drawer.contours(projected)
}
```

## Installation

### build.gradle.kts

```kotlin
dependencies {
    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.3")
    implementation("com.sksamuel.aedile:aedile-core:3.0.2")
    
    // Coroutines (usually already provided by OPENRNDR)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

plugins {
    // Benchmarking
    id("me.champeau.jmh") version "0.7.3"
}

jmh {
    jmhVersion = "1.37"
    iterations = 5
    warmupIterations = 3
    fork = 2
    benchmarkMode = listOf("avgt") // Average time
    timeUnit = "ms"
    includes = listOf(".*ProjectionBenchmark.*")
}
```

## Usage Patterns

### 1. Frame-Stable Geometry Cache

Cache projected geometries keyed by viewport state. Invalidates when zoom/center changes.

```kotlin
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

class GeometryCache {
    private val cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .recordStats() // For performance monitoring
        .build<CacheKey, List<Vector2>>()
    
    fun getProjected(geometry: Geometry, viewport: Viewport): List<Vector2> {
        val key = CacheKey(geometry.id, viewport.hash())
        return cache.get(key) { _ ->
            projectGeometry(geometry, viewport)
        }
    }
}
```

### 2. Async Batch Projection with Aedile

For expensive projections that shouldn't block the render thread:

```kotlin
import com.sksamuel.aedile.core.asCache
import kotlin.time.Duration.Companion.seconds

class AsyncProjectionCache {
    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(10.seconds)
        .asCache<CacheKey, List<Vector2>>()
    
    suspend fun getProjectedAsync(geometry: Geometry, viewport: Viewport): List<Vector2> {
        return cache.get(CacheKey(geometry.id, viewport.hash())) {
            // Runs on caller's coroutine context
            withContext(Dispatchers.Default) {
                expensiveProjection(geometry, viewport)
            }
        }
    }
}
```

### 3. Benchmarking Projection Performance

```kotlin
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
class ProjectionBenchmark {
    
    @Benchmark
    fun batchProject1000(): List<Vector2> {
        return batchProject(geometries1000, viewport)
    }
    
    @Benchmark
    fun cachedProject1000(): List<Vector2> {
        return cache.getProjected(geometries1000[0], viewport)
    }
}
```

### 4. Parallel Batch Processing

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers

suspend fun batchProjectParallel(
    geometries: List<Geometry>, 
    viewport: Viewport
): List<List<Vector2>> = coroutineScope {
    // Chunk geometries for optimal parallelism
    val chunkSize = maxOf(1, geometries.size / Runtime.getRuntime().availableProcessors())
    
    geometries.chunked(chunkSize).map { chunk ->
        async(Dispatchers.Default) {
            chunk.map { projectGeometry(it, viewport) }
        }
    }.awaitAll().flatten()
}
```

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Caching | Caffeine + Aedile | Guava Cache | Guava is slower, less flexible, no coroutine support |
| Caching | Caffeine | Custom map + synchronized | Cache eviction, statistics, expiration are non-trivial to implement correctly |
| Benchmarking | JMH | Hand-rolled timing | JVM warmup, JIT dead code elimination make DIY unreliable |
| Parallelism | Coroutines | Java parallelStream() | Less control, harder to integrate with suspend functions |
| Parallelism | Coroutines | RxJava | Overkill for simple batch operations; coroutines already in classpath |

## What NOT to Add

### Overkill for Creative Coding

| Technology | Why Not |
|------------|---------|
| **GraalVM Native Image** | Breaks OPENRNDR's dynamic classloading; adds 5+ min build times for minimal runtime benefit |
| **Ehcache** | Distributed/clustering features irrelevant; Caffeine is simpler and faster |
| **Redis/Memcached** | Network overhead exceeds benefit for single-process creative coding |
| **Project Reactor/RxJava** | Coroutines handle all async needs; additional reactive streams complexity unnecessary |
| ** Chronicle Queue/Map** | Persistence features not needed; creative coding is ephemeral |
| **HPPC/Eclipse Collections** | Primitive collections add dependency weight; standard Kotlin collections + caching sufficient |

### Incompatible with OPENRNDR

| Technology | Problem |
|------------|---------|
| **Background thread drawing** | OpenGL context is bound to main thread; background rendering crashes JVM |
| **GLFW/Vulkan threading** | Would require rewriting OPENRNDR's render loop |
| **Compute shaders for projection** | proj4j is CPU-bound; moving to GPU requires rewriting CRS logic in GLSL |

## Memory Optimization Techniques

No additional libraries needed—use JVM features:

### 1. Object Pooling for Vector2

```kotlin
// Reuse Vector2 arrays across frames to reduce GC pressure
class Vector2Pool {
    private val pool = ArrayDeque<List<Vector2>>()
    
    fun acquire(size: Int): List<Vector2> {
        return pool.removeFirstOrNull()?.takeIf { it.size >= size }
            ?: List(size) { Vector2.ZERO }
    }
    
    fun release(vectors: List<Vector2>) {
        if (pool.size < MAX_POOL_SIZE) {
            pool.addLast(vectors)
        }
    }
}
```

### 2. Lazy Sequence for Large Datasets

Already used in v1.0-v1.2. Continue using `Sequence<Geometry>` to avoid loading full datasets:

```kotlin
// Good: Processes one geometry at a time
geoSource.geometries()
    .filter { it.intersects(viewport.boundingBox) }
    .map { cache.getProjected(it, viewport) }
    .forEach { drawer.contour(it) }
```

### 3. Weak Reference Caching

For memory-constrained scenarios:

```kotlin
Caffeine.newBuilder()
    .weakValues() // Allow GC when memory pressure
    .maximumSize(10_000)
    .build()
```

## Version Compatibility Matrix

| Component | Minimum | Recommended | Latest | Notes |
|-----------|---------|-------------|--------|-------|
| JVM | 11 | 17 | 23 | Caffeine 3.x requires Java 11+ |
| Kotlin | 1.9.0 | 2.2.10 | 2.2.10 | Aedile 3.x requires Kotlin 2.0+ |
| Caffeine | 3.0.0 | 3.2.3 | 3.2.3 | Use 2.x only if stuck on Java 8 |
| kotlinx.coroutines | 1.7.0 | 1.10.2 | 1.10.2 | Match Kotlin version |
| JMH | 1.35 | 1.37 | 1.37 | Plugin 0.7.3 uses 1.37 |
| OPENRNDR | 0.4.5 | 0.4.5 | 0.4.5 | Verify before upgrading |

## Risk Assessment

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Cache memory leaks | Low | Use size-based eviction + weak values |
| Thread-safety bugs | Medium | Only access Drawer on main thread; use `Dispatchers.Default` only for math |
| Benchmark inaccuracy | Low | Follow JMH best practices; isolate in `src/jmh` |
| Coroutine context confusion | Medium | Document which dispatcher to use; provide examples |

## Sources

- **Caffeine**: https://github.com/ben-manes/caffeine (v3.2.3, Oct 2025) — HIGH confidence
- **Aedile**: https://github.com/sksamuel/aedile (v3.0.2, Dec 2025) — HIGH confidence
- **JMH**: https://github.com/openjdk/jmh — HIGH confidence
- **JMH Gradle Plugin**: https://github.com/melix/jmh-gradle-plugin (v0.7.3, Apr 2025) — HIGH confidence
- **kotlinx.coroutines**: https://github.com/Kotlin/kotlinx.coroutines (v1.10.2, Apr 2025) — HIGH confidence
- **OPENRNDR Batched Drawing**: https://guide.openrndr.org/drawing/drawingPrimitivesBatched.html — HIGH confidence
- **OPENRNDR Concurrency**: https://guide.openrndr.org/drawing/concurrencyAndMultithreading.html — HIGH confidence
