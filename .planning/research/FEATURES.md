# Feature Landscape: Performance Optimization (Batch Projection & Geometry Caching)

**Project:** openrndr-geo v1.3.0 Performance  
**Domain:** Geospatial creative coding visualization  
**Researched:** 2026-03-05  
**Researcher Context:** Existing codebase analysis + geospatial visualization patterns

---

## Executive Summary

This research focuses on performance optimization features for an existing geospatial visualization library. The codebase currently performs per-point projection in the render loop, which becomes a bottleneck with large datasets (10K+ features, 100K+ coordinates). The goal is to add:

1. **Batch screen space projection** — transform all coordinates in bulk
2. **Geometry caching** — cache projected coordinates across animation frames
3. **Performance measurement** — quantify gains and identify bottlenecks

The existing architecture provides excellent foundation: `GeoProjection` interface, `ProjectedGeometry` sealed class, `GeoSource.withProjection()` method, and Sequence-based lazy iteration.

---

## Current State Analysis

### Existing Projection Pipeline

```
GeoSource.features
  → Sequence<Feature>
  → renderToDrawer() [PER FRAME]
    → geometry.renderToDrawer() [PER GEOMETRY]
      → projection.project(Vector2) [PER COORDINATE]
        → Mercator math (trig functions) [PER COORDINATE]
```

**Current implementation:**
- `ScreenTransform.kt` provides batch transforms: `toScreen(points: Sequence<Vector2>, projection)`
- `Geometry.kt` has per-geometry `toScreen()` methods (line 66-70, 115-127)
- `Feature.kt` defines `ProjectedGeometry` sealed class hierarchy (lines 112-139)
- `GeoSource.kt` provides `withProjection()` returning `Sequence<ProjectedFeature>` (lines 274-297)

**Performance bottleneck:** Every frame re-projects every coordinate, even when:
- Camera is static (no zoom/pan changes)
- Geometry hasn't changed
- Projection parameters are identical

---

## Table Stakes (Must Have)

Features users expect from a performance-oriented geospatial library.

### 1. Batch Coordinate Projection
**Why Expected:** Single-call projection of large coordinate arrays is standard in geospatial libs (D3, Mapbox, deck.gl). Per-point function call overhead accumulates.

| Aspect | Details |
|--------|---------|
| **What** | Transform arrays of geographic coordinates to screen space in one operation |
| **Complexity** | LOW — `toScreen()` variants already exist in `ScreenTransform.kt` (lines 33-47) |
| **Current Gap** | Renderers call `map { projection.project(it) }` per-geometry (DrawerGeoExtensions.kt lines 278-304) |
| **Implementation** | Extend existing `toScreen()` to use SIMD-friendly loops; integrate into render pipeline |

**Key insight from existing code:** `ScreenTransform.kt` already has batch variants:
```kotlin
fun toScreen(points: Sequence<Vector2>, projection: GeoProjection): List<Vector2>
fun toScreen(points: List<Vector2>, projection: GeoProjection): List<Vector2>
```

**What's missing:** These aren't used in the render pipeline; renderers still map individually.

### 2. Projection State Detection
**Why Expected:** Caching requires knowing when to invalidate. Users expect automatic cache invalidation when zoom/pan changes.

| Aspect | Details |
|--------|---------|
| **What** | Detect when projection parameters change (center, zoom, viewport size) |
| **Complexity** | MEDIUM — Requires `ProjectionConfig` equality checks |
| **Current Gap** | `ProjectionConfig` is a data class (good!) but no change detection mechanism |
| **Implementation** | Add `ProjectionState` wrapper with `hasChangedSince(lastConfig)` |

**Dependencies:** `ProjectionConfig` (ProjectionConfig.kt lines 39-80) already immutable and comparable.

### 3. Frame-Stable Caching
**Why Expected:** Static maps shouldn't recompute every frame. Animation libraries (Lottie, GSAP) cache computed values.

| Aspect | Details |
|--------|---------|
| **What** | Cache `ProjectedGeometry` per-feature when projection is stable |
| **Complexity** | MEDIUM — Cache keyed by `(featureId, projectionHash)` |
| **Current Gap** | `ProjectedGeometry` exists (Feature.kt lines 112-139) but not cached |
| **Implementation** | `ProjectedGeometryCache` with LRU eviction, keyed by projection state |

**Key insight:** `withProjection()` (GeoSource.kt lines 274-278) creates projected features lazily every call. This should cache results.

### 4. Performance Benchmarking
**Why Expected:** "Fast" is meaningless without metrics. Users need to measure before/after and identify bottlenecks.

| Aspect | Details |
|--------|---------|
| **What** | Frame time tracking, projection time breakdown, memory profiling |
| **Complexity** | LOW-MEDIUM — Simple nanosecond timers, optional JMH for micro-benchmarks |
| **Current Gap** | No performance measurement exists |
| **Implementation** | `PerformanceMonitor` with frame times, `ProjectionProfiler` for transform costs |

---

## Differentiators (Competitive Advantage)

Features that set this library apart. Not expected, but valued.

### 1. Lazy vs Eager Projection Strategy
**Value Proposition:** Users choose memory vs compute tradeoff per-dataset. Large datasets use lazy streaming; small datasets use eager caching.

| Aspect | Details |
|--------|---------|
| **What** | `ProjectionStrategy.LAZY` (Sequence-based, per-frame) vs `ProjectionStrategy.EAGER` (pre-computed cache) |
| **Complexity** | MEDIUM — Requires strategy pattern in render pipeline |
| **Dependency** | Builds on existing `GeoSource.materialize()` pattern (GeoSource.kt lines 127-133) |

**Existing foundation:** `GeoSource.materialize()` shows the library already thinks about lazy vs eager tradeoffs:
```kotlin
fun materialize(): GeoSource {
    val materializedFeatures = listFeatures()  // Eager
    return object : GeoSource(crs) {
        override val features: Sequence<Feature> = materializedFeatures.asSequence()
    }
}
```

### 2. Adaptive Level-of-Detail (LOD)
**Value Proposition:** Simplify geometry when zoomed out (e.g., drop every Nth point). Standard in game engines, rare in geospatial libs.

| Aspect | Details |
|--------|---------|
| **What** | Simplify geometries based on zoom level using Ramer-Douglas-Peucker or point decimation |
| **Complexity** | HIGH — Requires simplification algorithm, zoom threshold config |
| **Current Gap** | No simplification exists |
| **Trade-off** | Pre-processing vs runtime simplification |

### 3. Spatial Partitioning for Visibility
**Value Proposition:** Skip projection entirely for off-screen geometries. Existing quadtree infrastructure enables this.

| Aspect | Details |
|--------|---------|
| **What** | Cull geometries outside viewport before projection using spatial index |
| **Complexity** | LOW-MEDIUM — `SpatialIndex` exists, integrate into render loop |
| **Current Gap** | `featuresInBounds()` exists (GeoSource.kt lines 60-62) but not used in renderers |
| **Implementation** | `GeoRenderer` checks bounds intersection before projection |

**Existing foundation:**
- `SpatialIndex` already implemented for efficient spatial queries
- `featuresInBounds(bounds)` (GeoSource.kt line 60) filters by bounding box

### 4. Multi-threaded Batch Projection
**Value Proposition:** Leverage all CPU cores for large coordinate arrays. Kotlin coroutines make this idiomatic.

| Aspect | Details |
|--------|---------|
| **What** | Parallel projection using `Dispatchers.Default` for coordinate batches |
| **Complexity** | MEDIUM-HIGH — Thread-safety, batch sizing, coroutine overhead |
| **Current Gap** | All projection is single-threaded |
| **Implementation** | `asyncBatchProject()` splitting coordinates into chunks |

---

## Anti-Features (Explicitly NOT Building)

Features to explicitly avoid. These are traps in geospatial perf optimization.

### 1. Automatic Geometry Simplification (Always-On)
**Why Avoid:** Silent data loss. Users expect the geometry they loaded to render exactly. Auto-simplification causes confusion when features "disappear" at certain zooms.

**What to do instead:**
- Explicit `simplify(epsilon)` API that users call intentionally
- Visual indicator when simplification is active
- Preserves original geometry, creates simplified copy

### 2. GPU-Based Projection (Compute Shaders)
**Why Avoid:** Massive complexity, limited platform support, overkill for creative coding use case. Current bottleneck is single-threaded CPU projection, not GPU throughput.

**What to do instead:**
- CPU multi-threading (simpler, portable)
- Batch processing (amortizes function call overhead)
- Caching (eliminates redundant work)

### 3. Tile-Based Rendering Pyramid
**Why Avoid:** Premature optimization for creative coding workflow. Adds complexity (tile generation, storage, LOD management) that conflicts with "rapid prototyping" goal.

**What to do instead:**
- Batch projection with caching
- Spatial culling (existing quadtree)
- Defer to v2 if needed for massive datasets

### 4. Projected Geometry Persistence to Disk
**Why Avoid:** Cache invalidation complexity (CRS changes, projection parameter changes). Disk I/O often slower than recomputing for typical dataset sizes.

**What to do instead:**
- In-memory LRU cache only
- Application-lifetime cache (clear on projection change)
- No persistence across app restarts

---

## Feature Dependencies

```
┌─────────────────────────────────────────────────────────────┐
│                    DEPENDENCY GRAPH                          │
└─────────────────────────────────────────────────────────────┘

Batch Projection (Table Stakes #1)
    ├─→ Uses: ScreenTransform.toScreen() [exists]
    ├─→ Uses: GeoProjection.project() [exists]
    └─→ Integrates with: renderToDrawer() [exists]

Projection State Detection (Table Stakes #2)
    ├─→ Uses: ProjectionConfig [exists, immutable data class]
    ├─→ Uses: ProjectionConfig.equals() [auto-generated]
    └─→ Enables: Cache invalidation

Frame-Stable Caching (Table Stakes #3)
    ├─→ Depends on: Projection State Detection
    ├─→ Uses: ProjectedGeometry [exists]
    ├─→ Uses: withProjection() [exists]
    └─→ New: Cache keyed by (feature, projectionHash)

Performance Benchmarking (Table Stakes #4)
    ├─→ Independent
    └─→ Integrates with: All render paths

Lazy vs Eager Strategy (Differentiator #1)
    ├─→ Depends on: Frame-Stable Caching
    ├─→ Uses: materialize() pattern [exists]
    └─→ New: ProjectionStrategy enum

Spatial Culling (Differentiator #3)
    ├─→ Uses: SpatialIndex [exists]
    ├─→ Uses: featuresInBounds() [exists]
    └─→ Integrates with: Render pipeline

Multi-threaded Projection (Differentiator #4)
    ├─→ Depends on: Batch Projection
    └─→ New: Parallel coordinate transformation
```

---

## Implementation Recommendations

### Phase 1: Foundation (Week 1)

1. **Batch Projection Integration**
   - Modify `DrawerGeoExtensions.kt` renderers to use `toScreen(points: List, projection)`
   - Single change eliminates per-point lambda allocation overhead
   - **Estimated improvement:** 10-20% reduction in projection time

2. **Projection State Tracking**
   - Add `ProjectionState` class wrapping `ProjectionConfig` with generation counter
   - Hash code based on config values
   - **Complexity:** 1 day

### Phase 2: Caching (Week 1-2)

1. **ProjectedGeometryCache**
   ```kotlin
   class ProjectedGeometryCache {
       private val cache = ConcurrentHashMap<CacheKey, ProjectedGeometry>()
       private var currentGeneration: Long = 0
       
       fun getOrProject(feature: Feature, projection: GeoProjection): ProjectedGeometry
       fun invalidateOnProjectionChange(newConfig: ProjectionConfig)
   }
   ```

2. **Integration Points**
   - `GeoSource.withProjection()` → use cache
   - `renderToDrawer()` → check cache before projecting
   - **Estimated improvement:** 80-95% reduction for static camera

### Phase 3: Visibility & Metrics (Week 2)

1. **Spatial Culling**
   - In `DrawerGeoExtensions.kt`, filter `features` with `featuresInBounds(viewportBounds)`
   - Apply before projection
   - **Estimated improvement:** Variable (depends on viewport coverage)

2. **Performance Monitor**
   - Frame time histogram
   - Projection time breakdown
   - Cache hit rate

---

## Complexity Summary

| Feature | Complexity | Risk | Estimated Effort |
|---------|------------|------|------------------|
| Batch Projection Integration | LOW | Low | 2 days |
| Projection State Detection | LOW | Low | 1 day |
| Frame-Stable Caching | MEDIUM | Medium | 3-4 days |
| Performance Benchmarking | LOW | Low | 2 days |
| Lazy/Eager Strategy | MEDIUM | Low | 2 days |
| Spatial Culling | LOW | Low | 1-2 days |
| Multi-threaded Projection | MEDIUM-HIGH | Medium | 4-5 days |
| Adaptive LOD | HIGH | High | 1-2 weeks |

---

## Sources

- **Existing codebase analysis:**
  - `ScreenTransform.kt` — batch transformation functions
  - `ProjectionConfig.kt` — immutable configuration
  - `Feature.kt` — `ProjectedGeometry` sealed class
  - `GeoSource.kt` — `withProjection()` method, `materialize()` pattern
  - `DrawerGeoExtensions.kt` — render pipeline
  - `SpatialIndex.kt` — spatial query infrastructure

- **Geospatial visualization patterns:**
  - D3.js projection caching strategies
  - MapLibre/deck.gl batch rendering approaches
  - Game engine LOD systems (Unity, Unreal)

- **Confidence:** HIGH — Based on direct codebase analysis and established patterns in geospatial visualization domain.

---

## Open Questions for Phase Planning

1. **Cache size limits:** What's the memory budget? (Need to support 12GB GeoPackage → partial caching only)
2. **Multi-threading priority:** Is single-threaded caching sufficient for v1.3.0?
3. **LOD scope:** Should adaptive simplification be deferred to v2?
4. **Benchmarking depth:** Built-in metrics or external profiler integration?
