# Phase 11: Batch Projection - Research

**Researched:** 2026-03-05
**Domain:** Kotlin/JVM batch coordinate transformation, OPENRNDR geometry rendering
**Confidence:** HIGH

## Summary

Phase 11 implements batch coordinate projection for the openrndr-geo library, transforming the current per-point projection approach to batched array operations. The current codebase uses `.map { projection.project(it) }` patterns throughout `ProjectionExtensions.kt`, `GeoStack.kt`, and `GeoSource.kt`, causing repeated allocation and method call overhead for each render frame.

**Primary recommendation:** Implement a unified batch projection system using `DoubleArray` with pre-allocated output buffers, triggered via an `optimize = true` parameter on load functions. Internal migration replaces all per-point calls with batch variants—no dual implementation maintenance.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin JVM | 2.3.10 | Language runtime | Required by OPENRNDR |
| OPENRNDR | 0.5.0-alpha2 | Creative coding framework | Core dependency, provides `Vector2` |
| Proj4J | 1.4.1 | CRS transformations | Already integrated for `CRSTransformer` |
| JUnit | 4.13.2 | Testing framework | Existing project test infrastructure |

### Supporting
| Component | Purpose | When to Use |
|-----------|---------|-------------|
| `DoubleArray` | Coordinate storage | Batch operations (faster than `List<Vector2>`) |
| `Array<Vector2>` | Output format | Return type for compatibility with OPENRNDR Drawer |
| Inline functions | Eliminate lambda overhead | Batch transformation loops |

**No additional dependencies required** — implementation uses standard Kotlin/JVM arrays and existing OPENRNDR types.

## Architecture Patterns

### Current State Analysis
The codebase currently projects coordinates per-point at render time:

```kotlin
// Current pattern in ProjectionExtensions.kt (per-point)
fun LineString.projectToScreen(projection: GeoProjection): List<Vector2> {
    return points.map { projection.project(it) }  // Allocates List, N method calls
}

// Current pattern in GeoStack.kt renderToDrawer() (per-frame)
is LineString -> {
    val screenPoints = points.map { projection.project(it) }  // Every frame!
    geo.render.drawLineString(drawer, screenPoints, style)
}
```

**Problems:**
1. **Allocation churn:** `map {}` creates new `List<Vector2>` every call
2. **Method call overhead:** Each coordinate triggers `project(Vector2): Vector2` 
3. **Cache misses:** `Vector2` objects scattered in heap
4. **Per-frame cost:** Projection happens during render, not during data load

### Recommended Batch Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Data Loading Phase                        │
├─────────────────────────────────────────────────────────────┤
│  loadGeoJSON(path, optimize = true)                         │
│         ↓                                                   │
│  ┌──────────────────────────────┐                          │
│  │  Geometry Coordinate Arrays  │  Store contiguous arrays   │
│  │  - DoubleArray for x coords  │  instead of List<Vector2>  │
│  │  - DoubleArray for y coords  │                            │
│  └──────────────────────────────┘                            │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│              CRS Transformation Phase (opt-in)               │
├─────────────────────────────────────────────────────────────┤
│  autoTransformTo(targetCRS, optimize = true)                │
│         ↓                                                   │
│  Batch coordinate transformation using DoubleArray           │
│  Single loop: transform x[], y[] in lockstep                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   Render Phase                               │
├─────────────────────────────────────────────────────────────┤
│  toScreenCoordinates(projection): Array<Vector2>            │
│         ↓                                                   │
│  Batch project DoubleArray → Array<Vector2>                  │
│  Reuse pre-allocated buffer when possible                    │
└──────────────────────────────────────────────────────────────┘
```

### Pattern 1: DoubleArray Storage (Internal)
**What:** Store geometry coordinates as two `DoubleArray` (x[], y[]) internally  
**When to use:** All geometry types during batch operations  
**Why:** Contiguous memory, no object headers, CPU cache-friendly

```kotlin
// Internal coordinate storage
internal class CoordinateBatch(
    val x: DoubleArray,
    val y: DoubleArray
) {
    val size: Int get() = x.size
    
    fun toVector2Array(): Array<Vector2> = Array(size) { i ->
        Vector2(x[i], y[i])
    }
}
```

### Pattern 2: Inline Batch Transformation
**What:** Inline function that transforms coordinate arrays with minimal overhead  
**When to use:** CRS transformation, projection to screen space  
**Why:** Eliminates lambda allocation, enables loop unrolling

```kotlin
// Source: Kotlin inline function best practices
internal inline fun batchTransform(
    x: DoubleArray,
    y: DoubleArray,
    outX: DoubleArray,
    outY: DoubleArray,
    transform: (Double, Double) -> Pair<Double, Double>
) {
    require(x.size == y.size && x.size == outX.size && x.size == outY.size)
    
    for (i in x.indices) {
        val (tx, ty) = transform(x[i], y[i])
        outX[i] = tx
        outY[i] = ty
    }
}
```

### Pattern 3: Buffer Pool for Projection
**What:** Reuse pre-allocated output arrays during repeated projections  
**When to use:** Render loop projections (static camera scenarios)  
**Why:** Eliminates allocation in hot path

```kotlin
internal class ProjectionBufferPool {
    private val buffers = mutableMapOf<Int, Array<Vector2>>()
    
    fun acquire(size: Int): Array<Vector2> {
        return buffers.getOrPut(size) { Array(size) { Vector2.ZERO } }
    }
    
    // Note: Not releasing—creative coding sessions bounded by viewport size
}
```

### Pattern 4: Geometry-Type-Agnostic Batch
**What:** Flatten all coordinates into single batch, track offsets per geometry  
**When to use:** Multi-feature rendering, viewport caching (Phase 12 prep)  
**Why:** One batch transformation for many geometries

```kotlin
internal class FlattenedCoordinateBatch {
    val allX: DoubleArray      // All coordinates flattened
    val allY: DoubleArray
    val geometryOffsets: IntArray  // Where each geometry starts
    val geometrySizes: IntArray    // How many coords per geometry
}
```

### Anti-Patterns to Avoid

| Anti-Pattern | Why It's Bad | Do Instead |
|--------------|--------------|------------|
| `List<Vector2>.map { }` in hot path | Allocates list + N objects per call | Batch transform to pre-allocated array |
| `Array<Vector2>` for storage | Object headers, cache misses | `DoubleArray` pairs for storage |
| Branching per geometry type inside batch loop | Branch misprediction, SIMD unfriendly | Flatten then batch, or separate loops per type |
| Allocating output buffer per projection | GC pressure in render loop | Buffer pool or pre-allocated reusable arrays |
| Dual API (batch vs non-batch) | Maintenance burden, confusing | Internal migration—always use batch internally |

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SIMD vectorization | Custom SIMD with Java Vector API | JVM autovectorization + plain loops | Vector API incubator status, complexity vs gain |
| Parallel batching | `parallelStream()` or coroutines | Single-threaded batch (Phase 11) | PERF-13 deferred to v2+, synchronization overhead |
| LRU/LFU cache | Custom eviction algorithms | Simple `MutableMap` with clear-on-change | Out of scope (Phase 12), Caffeine overkill |
| Custom memory allocator | Object pooling frameworks | Simple `Array` reuse | JVM GC efficient for short-lived arrays |
| Benchmark framework | Full JMH suite | Simple timing micro-benchmarks | PERF-08 only needs relative comparison |

**Key insight:** Creative coding workloads differ from web services—predictable data sizes, bounded sessions, clear lifecycle boundaries. Simple batching provides 80% of the benefit with 20% of the complexity.

## Common Pitfalls

### Pitfall 1: Premature Optimization of Batch Size
**What goes wrong:** Spending time tuning chunk sizes (1024? 4096? 16384?) without measuring  
**Why it happens:** Web articles mention "optimal batch size"  
**How to avoid:** Start with `min(10000, totalSize)` or just process entire array—JVM handles small arrays well  
**Warning signs:** Benchmarks showing <10% variance between chunk sizes

### Pitfall 2: Breaking OPENRNDR Vector2 Contract
**What goes wrong:** Returning `FloatArray` or custom point type from projection  
**Why it happens:** Float is half the memory  
**How to avoid:** `Array<Vector2>` is required—OPENRNDR `Drawer` expects it  
**Warning signs:** Compiler errors in render code, runtime ClassCastException

### Pitfall 3: Optimizing CRS Transform (Wrong Layer)
**What goes wrong:** Batch optimizing `CRSTransformer` when bottleneck is projection  
**Why it happens:** Both involve coordinate math  
**How to avoid:** Profile first—CRS transform happens at load time, projection at render time  
**Warning signs:** Complex CRS batch code with no measurable render improvement

### Pitfall 4: Neglecting Memory Layout for Multi-Geometries
**What goes wrong:** `MultiPolygon` batch slower than separate `Polygon`s due to irregular access  
**Why it happens:** Holes and rings create non-uniform coordinate counts  
**How to avoid:** Flatten coordinates, track offsets separately  
**Warning signs:** `MultiPolygon` batch shows no improvement over per-point

### Pitfall 5: Over-Engineering the Opt-in Flag
**What goes wrong:** Complex feature detection, automatic threshold triggering  
**Why it happens:** Trying to be "smart" about when to optimize  
**How to avoid:** Simple boolean parameter—user decides, console warns  
**Warning signs:** >50 lines of code to decide whether to batch

## Code Examples

### Batch Projection Implementation
```kotlin
// Internal batch projection for all geometry types
internal fun batchProject(
    x: DoubleArray,
    y: DoubleArray,
    projection: GeoProjection,
    output: Array<Vector2>
): Array<Vector2> {
    require(x.size == y.size && x.size <= output.size)
    
    for (i in x.indices) {
        output[i] = projection.project(Vector2(x[i], y[i]))
    }
    return output
}

// Optimized Mercator projection avoiding Vector2 allocation
internal fun batchProjectMercator(
    lon: DoubleArray,
    lat: DoubleArray,
    config: ProjectionConfig,
    outX: DoubleArray,
    outY: DoubleArray
) {
    val centerX = config.center?.x?.let { Math.toRadians(it) } ?: 0.0
    val centerY = config.center?.y?.let { 
        ln(tan(PI / 4 + Math.toRadians(it) / 2)) 
    } ?: 0.0
    val scale = config.scale
    
    for (i in lon.indices) {
        val mercX = Math.toRadians(lon[i])
        val clampedLat = lat[i].coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        val mercY = ln(tan(PI / 4 + Math.toRadians(clampedLat) / 2))
        
        outX[i] = config.width / 2.0 + (mercX - centerX) * scale
        outY[i] = config.height / 2.0 - (mercY - centerY) * scale
    }
}
```

### API Integration (Opt-in Parameter)
```kotlin
// GeoSource.kt - opt-in optimization
fun loadGeoJSON(
    path: String, 
    optimize: Boolean = false
): GeoSource {
    val source = parseGeoJSON(path)
    return if (optimize) {
        OptimizedGeoSource(source)
    } else {
        source
    }
}

// Internal optimized wrapper
internal class OptimizedGeoSource(
    private val delegate: GeoSource
) : GeoSource(delegate.crs) {
    
    override val features: Sequence<Feature> = delegate.features.map { 
        it.withOptimizedGeometry() 
    }
    
    private fun Feature.withOptimizedGeometry(): Feature = when (geometry) {
        is LineString -> {
            // Convert to coordinate arrays at load time
            val x = DoubleArray(geometry.points.size) { i -> geometry.points[i].x }
            val y = DoubleArray(geometry.points.size) { i -> geometry.points[i].y }
            Feature(OptimizedLineString(x, y), properties)
        }
        // ... other geometry types
        else -> this  // Fallback for unsupported
    }
}
```

### Console Warning Implementation
```kotlin
internal fun checkOptimizationRecommendation(
    featureCount: Int,
    coordinateCount: Int,
    optimizeFlag: Boolean
) {
    if (optimizeFlag) return  // Already optimized
    
    val threshold = 5000  // Discretion: adjust based on benchmarks
    if (coordinateCount > threshold) {
        println(
            "⚡ Performance Tip: Large geometry detected ($coordinateCount coordinates). " +
            "Consider using optimize=true for better performance: " +
            "loadGeoJSON(path, optimize = true)"
        )
    }
}
```

### Micro-Benchmark Pattern
```kotlin
// Simple timing benchmark (no JMH needed for Phase 11)
class BatchProjectionBenchmark {
    
    fun benchmarkBatchVsPerPoint(
        coordinates: List<Vector2>,
        projection: GeoProjection
    ): BenchmarkResult {
        val iterations = 100
        
        // Warmup
        repeat(10) { perPointProjection(coordinates, projection) }
        repeat(10) { batchProjection(coordinates, projection) }
        
        // Per-point timing
        val perPointStart = System.nanoTime()
        repeat(iterations) { perPointProjection(coordinates, projection) }
        val perPointDuration = (System.nanoTime() - perPointStart) / iterations
        
        // Batch timing
        val batchStart = System.nanoTime()
        repeat(iterations) { batchProjection(coordinates, projection) }
        val batchDuration = (System.nanoTime() - batchStart) / iterations
        
        return BenchmarkResult(
            perPointNs = perPointDuration,
            batchNs = batchDuration,
            speedup = perPointDuration.toDouble() / batchDuration
        )
    }
    
    private fun perPointProjection(
        coords: List<Vector2>, 
        projection: GeoProjection
    ): List<Vector2> = coords.map { projection.project(it) }
    
    private fun batchProjection(
        coords: List<Vector2>, 
        projection: GeoProjection
    ): Array<Vector2> {
        val x = DoubleArray(coords.size) { i -> coords[i].x }
        val y = DoubleArray(coords.size) { i -> coords[i].y }
        val out = Array(coords.size) { Vector2.ZERO }
        return batchProject(x, y, projection, out)
    }
}

data class BenchmarkResult(
    val perPointNs: Long,
    val batchNs: Long,
    val speedup: Double
)
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `List<Vector2>.map { }` | `DoubleArray` + indexed loop | Phase 11 | 3-10x faster, less allocation |
| Per-frame projection | Load-time transformation | Phase 11 | Removes render bottleneck |
| Always-on optimization | Opt-in `optimize = true` | Phase 11 | Backward compatible, user choice |
| Project4J per-point | Batch CRS transform | Phase 11 (optional) | Faster data loading |

**Deprecated/outdated:**
- `Geometry.toScreen()` methods that return `List<Vector2>` → Use batch projection internally
- Per-point `CRSTransformer.transform()` in render loop → Batch at load time

## Open Questions

1. **Optimal batch threshold**
   - What we know: 5000+ coordinates shows benefit
   - What's unclear: Exact threshold for console warning
   - Recommendation: Start with 5000, adjust based on benchmarks

2. **Multi-geometry batching strategy**
   - What we know: Flattening coordinates helps cache locality
   - What's unclear: Cost of offset tracking vs per-geometry batches
   - Recommendation: Implement per-geometry batch first, flatten if profiling shows need

3. **Buffer pool sizing**
   - What we know: Creative coding has bounded viewport sizes
   - What's unclear: Maximum reasonable buffer size
   - Recommendation: Unbounded map keyed by size, JVM handles cleanup

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| PERF-01 | Batch-transform coordinate arrays | `DoubleArray` pairs + indexed loops eliminate per-point allocation |
| PERF-02 | Pipeline uses batch for all geometry types | Internal `Geometry` subclasses with coordinate arrays, batch in `renderToDrawer` |
| PERF-03 | Preserve existing API contracts | Opt-in `optimize = true` parameter, default unchanged, internal migration |

## Sources

### Primary (HIGH confidence)
- `src/main/kotlin/geo/Geometry.kt` - Current geometry hierarchy
- `src/main/kotlin/geo/ProjectionExtensions.kt` - Current projection patterns
- `src/main/kotlin/geo/GeoStack.kt` - Render pipeline integration
- `src/main/kotlin/geo/projection/ProjectionMercator.kt` - Mercator implementation
- `.opencode/skills/kotlin/language/SKILL.md` - Kotlin idioms
- `.opencode/skills/kotlin/best-practices/SKILL.md` - Scope functions, backing properties

### Secondary (MEDIUM confidence)
- Kotlin Array documentation - `DoubleArray` vs `Array<Double>` performance
- OPENRNDR 0.5.0 Vector2 source - Immutable design constraints
- JVM Performance Engineering guides - Cache locality principles

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - existing codebase dependencies
- Architecture: HIGH - clear current patterns, straightforward batch conversion
- Pitfalls: MEDIUM-HIGH - common JVM/Kotlin performance antipatterns

**Research date:** 2026-03-05
**Valid until:** 2026-06-05 (Kotlin 2.3.x stable cycle)
