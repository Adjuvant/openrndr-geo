# Domain Pitfalls: Performance Optimization for Geospatial Visualization

**Domain:** Creative coding / Geospatial visualization library (openrndr-geo)  
**Milestone:** v1.3.0 — Adding batch projection and geometry caching  
**Researched:** 2026-03-05  
**Confidence:** HIGH (based on codebase analysis + domain expertise)

---

## Critical Pitfalls

Mistakes that cause rewrites, major performance regressions, or broken user workflows.

---

### Pitfall 1: Premature Optimization Without Profiling Baseline

**What goes wrong:** Implementing complex batch projection and caching systems without first establishing where time is actually spent. The optimization targets the wrong bottleneck.

**Why it happens:** 
- Assumption that "projection is expensive" without measurement
- Focus on algorithmic complexity (O(n) vs O(n²)) while ignoring constant factors
- Not accounting for OPENRNDR's rendering pipeline overhead

**Consequences:**
- Added complexity with no measurable benefit
- Cache maintenance overhead may actually *decrease* performance for typical use cases
- Code harder to maintain for prototyping workflows

**Prevention:**
1. **Establish baseline metrics first** - Use the benchmarking tools from PERF-03 to measure current frame times
2. **Profile before optimizing** - Identify whether projection, data loading, or rendering is the actual bottleneck
3. **Set performance targets** - Define "acceptable" vs "needs optimization" thresholds
4. **Test with real datasets** - Use the actual 12GB Ordnance Survey data mentioned in PROJECT.md, not toy examples

**Detection (Warning Signs):**
- "I think this will be faster" without numbers
- Optimizations added before any performance measurement
- Complex caching for data that changes every frame anyway

**Phase to Address:** Phase 1 (PERF-03 Benchmarking) - MUST complete before PERF-01/02

---

### Pitfall 2: Cache Invalidation Cascade with Projection Changes

**What goes wrong:** Cached projected geometries become stale when projection parameters change, but the invalidation logic misses edge cases, causing visual artifacts or crashes.

**Why it happens:**
- Current `GeoProjection` is an interface with multiple implementations (Mercator, Equirectangular, BNG)
- Projection can change per-frame via `configure()`, `fitWorld()`, or `fitBounds()`
- Animation layer (v1.1.0) enables tweening projection parameters over time

**Consequences:**
- Geometries render in wrong positions using stale cached coordinates
- "Ghost" artifacts from partially invalidated caches
- Memory corruption if cache keys don't capture all projection state

**Prevention:**
1. **Make projection state hashable** - `ProjectionConfig` should provide a cache key that captures all mutable state
2. **Version the projection** - Add `projectionVersion: Long` that increments on any parameter change
3. **Conservative invalidation** - When in doubt, invalidate. Better to reproject than show stale data
4. **Explicit cache lifecycle** - `ProjectionCache` should be tied to a specific projection instance, not global

```kotlin
// Anti-pattern: Global cache with implicit invalidation
object GlobalGeometryCache { ... }  // DON'T

// Pattern: Cache tied to projection lifecycle
class ProjectionWithCache(val projection: GeoProjection) {
    private val cacheVersion = projection.config.cacheKey
    private val geometryCache = mutableMapOf<...>()
    
    fun getCached(geometry: Geometry): List<Vector2> {
        if (projection.config.cacheKey != cacheVersion) {
            geometryCache.clear()  // Full invalidation on config change
        }
        // ... cache lookup
    }
}
```

**Detection (Warning Signs):**
- Geometries "stuck" in wrong positions after zoom/pan
- Flickering or jumping when projection animates
- Cache hit rates that seem too good to be true

**Phase to Address:** Phase 2 (PERF-02 Caching) - Core design decision

---

### Pitfall 3: Unbounded Cache Growth (Memory Leaks)

**What goes wrong:** Geometry caches grow without limit as users load different datasets or zoom through different regions, eventually causing OutOfMemoryError.

**Why it happens:**
- Multi-GB GeoPackage support means datasets can be massive
- Prototyping workflow encourages loading many different datasets rapidly
- No cache eviction strategy implemented

**Consequences:**
- Application crash after extended use
- Unpredictable performance degradation as GC struggles
- Poor experience for long-running creative sessions

**Prevention:**
1. **Bounded caches with LRU eviction** - Use `LinkedHashMap` with `removeEldestEntry` or Caffeine library
2. **Size-aware caching** - Track approximate memory usage, evict when threshold exceeded
3. **Explicit cache clearing API** - Allow users to `clearCache()` between dataset loads
4. **Soft/Weak references for large geometries** - Allow GC to reclaim under memory pressure

```kotlin
// Pattern: Bounded LRU cache for projected geometries
class BoundedGeometryCache(maxEntries: Int = 1000) {
    private val cache = object : LinkedHashMap<CacheKey, List<Vector2>>(
        16, 0.75f, true  // access-order for LRU
    ) {
        override fun removeEldestEntry(eldest: MutableMap.Entry<CacheKey, List<Vector2>>): Boolean {
            return size > maxEntries
        }
    }
}
```

**Detection (Warning Signs):**
- Memory usage grows monotonically during session
- `OutOfMemoryError` after loading multiple datasets
- Slowing performance over time (GC thrashing)

**Phase to Address:** Phase 2 (PERF-02 Caching) - Implementation detail

---

### Pitfall 4: Breaking the Two-Tier API Contract

**What goes wrong:** Batch projection and caching optimizations change method signatures or behavior in ways that break existing user code using the beginner/professional tier APIs.

**Why it happens:**
- v1.2.0 established a two-tier API: `drawer.geo(source)` (beginner) and `drawer.geo(source) { }` (professional)
- Current `renderToDrawer()` in DrawerGeoExtensions.kt processes geometry per-frame
- Optimizations may require pre-computation or different calling patterns

**Consequences:**
- User code from v1.2.0 examples breaks
- "Quick start" promise of library is violated
- Forces users to understand implementation details

**Prevention:**
1. **Preserve all existing entry points** - `drawer.geoJSON()`, `drawer.geoSource()`, `drawer.geo()` must work unchanged
2. **Transparent optimization** - Caching should happen internally, not require API changes
3. **Opt-in for advanced features** - If batching requires different setup, make it an optional config, not required
4. **Test all 16 examples** - Ensure every example from v1.2.0 still works

**Anti-pattern to avoid:**
```kotlin
// DON'T: Force users to manage cache explicitly
val cache = GeometryCache()  // User shouldn't need this
drawer.geo(source, cache)

// DO: Internal optimization, same API
drawer.geo(source)  // Internally uses cache, user doesn't care
```

**Detection (Warning Signs):**
- Example code needs modification to work
- Unit tests require updates
- Documentation examples become invalid

**Phase to Address:** Phase 2 (PERF-01/02) - Integration testing

---

### Pitfall 5: Over-Engineering for Prototyping Use Case

**What goes wrong:** Implementing enterprise-grade caching (distributed, persistent, multi-level) when the use case is rapid creative prototyping with frequently changing data.

**Why it happens:**
- "While we're optimizing, let's do it right" thinking
- Applying patterns from web services (Redis, memcached) to a desktop creative tool
- Not respecting the PROJECT.md constraint: "focus is prototyping/exploration, not end-user apps"

**Consequences:**
- Complex code that's hard to modify for experiments
- Optimization overhead exceeds benefit for typical datasets
- Violates the library's core value: "fluid and creative" exploration

**Prevention:**
1. **YAGNI principle** - Don't implement features not needed for v1.3.0 scope
2. **Start simple** - In-memory cache only, no persistence
3. **Measure overhead** - Ensure cache management < 5% of frame time
4. **Respect the domain** - Creative coding values flexibility over raw performance

**Appropriate scope for v1.3.0:**
- ✓ In-memory geometry cache for current session
- ✓ Per-projection cache (cleared when projection changes)
- ✓ Batch projection for visible geometries only
- ✗ Persistent disk cache
- ✗ Distributed/multi-level caching
- ✗ Predictive pre-computation

**Phase to Address:** Phase 1 (Design) - Scope definition

---

### Pitfall 6: Ignoring Data Locality in Batch Processing

**What goes wrong:** Batch projection improves throughput but causes cache thrashing due to poor memory layout, negating the benefit.

**Why it happens:**
- Current `Geometry` sealed class with `List<Vector2>` creates pointer-chasing patterns
- `Sequence<Feature>` iteration scatters memory access
- Projection math on scattered data causes CPU cache misses

**Consequences:**
- Batch projection 50x slower than expected (per Game Programming Patterns Data Locality chapter)
- No improvement over per-geometry projection
- Wasted effort implementing batching

**Prevention:**
1. **Structure of Arrays (SoA) for batch operations** - Convert to contiguous arrays before batch projection
2. **Process in tiles/chunks** - Work on spatially coherent subsets to improve cache locality
3. **Avoid pointer chasing** - Flatten `List<List<Vector2>>` to primitive arrays for projection math
4. **Profile with Cachegrind** - Verify cache hit rates improve

```kotlin
// Anti-pattern: Pointer chasing through nested lists
features.forEach { feature ->  // Cache miss
    feature.geometry.points.forEach { point ->  // Cache miss
        projection.project(point)  // Cache miss on point data
    }
}

// Pattern: Flatten to contiguous arrays first
val flatCoords = FloatArray(totalPoints * 2)  // All coords contiguous
// ... fill array ...
batchProject(flatCoords)  // Sequential access, cache-friendly
```

**Detection (Warning Signs):**
- Batch projection no faster than sequential
- Performance doesn't scale with geometry count
- CPU profiling shows high cache miss rates

**Phase to Address:** Phase 2 (PERF-01 Batch Projection) - Implementation

---

### Pitfall 7: Cache Key Collisions with Geometry Identity

**What goes wrong:** Different geometries with same bounds or similar properties generate identical cache keys, causing cross-contamination of cached results.

**Why it happens:**
- Using only bounding box as cache key misses internal structure differences
- `List<Vector2>` hash codes may collide for different point arrangements
- Multi-level geometries (Polygon with holes) have complex identity

**Consequences:**
- Geometry A renders with Geometry B's projected coordinates
- Subtle visual corruption that's hard to debug
- Non-deterministic behavior based on hash collisions

**Prevention:**
1. **Use structural hash** - Include all point coordinates in cache key computation
2. **IdentityHashMap for object identity** - If objects are stable, use reference equality
3. **Content-based addressing** - Hash the actual coordinate data, not metadata
4. **Validate on retrieval** - Spot-check that cached result matches expected structure

```kotlin
// Pattern: Content-based cache key
private fun geometryCacheKey(geometry: Geometry): Long {
    var hash = geometry::class.java.name.hashCode().toLong()
    when (geometry) {
        is LineString -> {
            hash = hash * 31 + geometry.points.size
            geometry.points.forEach { pt ->
                hash = hash * 31 + pt.x.hashCode()
                hash = hash * 31 + pt.y.hashCode()
            }
        }
        // ... other types
    }
    return hash
}
```

**Detection (Warning Signs):**
- Wrong geometry rendered at wrong location
- Cache hit rate suspiciously high
- Visual artifacts that change on restart

**Phase to Address:** Phase 2 (PERF-02 Caching) - Implementation

---

### Pitfall 8: Neglecting Multi-Geometry Sealed Class Exhaustiveness

**What goes wrong:** Batch projection or caching optimization handles common geometry types (Point, LineString, Polygon) but misses Multi* variants, causing compiler errors or runtime failures.

**Why it happens:**
- `Geometry` is a sealed class with 6 types: Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
- Current `renderToDrawer()` uses exhaustive `when` expression
- Optimizations may only target the "common" cases

**Consequences:**
- Kotlin compiler error: "'when' expression must be exhaustive"
- Runtime `ClassCastException` or missing geometries
- Incomplete feature implementation

**Prevention:**
1. **Compiler-enforced exhaustiveness** - Let Kotlin's sealed class checking catch missing cases
2. **Test all geometry types** - Include Multi* variants in test suite
3. **Refactor, don't bypass** - If `when` becomes complex, extract strategies but keep exhaustiveness
4. **Use exhaustive when return** - `val result = when(geometry) { ... }` forces all branches

```kotlin
// Pattern: Exhaustive when with compiler checking
fun projectGeometry(geometry: Geometry): ProjectedGeometry {
    return when (geometry) {  // Compiler errors if any type missing
        is Point -> ...
        is LineString -> ...
        is Polygon -> ...
        is MultiPoint -> ...
        is MultiLineString -> ...
        is MultiPolygon -> ...
    }
}
```

**Detection (Warning Signs):**
- Kotlin compiler errors about non-exhaustive when
- Missing Multi* geometries in rendered output
- Tests pass individually but fail in batch

**Phase to Address:** Phase 2 (PERF-01/02) - Implementation

---

### Pitfall 9: Assuming Projection Thread-Safety

**What goes wrong:** Implementing parallel batch projection assuming `GeoProjection` implementations are thread-safe, causing race conditions or incorrect results.

**Why it happens:**
- `ProjectionConfig` contains mutable state (center, scale, bounds)
- `proj4j` library used for CRS transforms may have internal state
- Kotlin coroutines make parallelization tempting

**Consequences:**
- Non-deterministic projection results
- Race conditions in concurrent batch processing
- Subtle coordinate drift or corruption

**Prevention:**
1. **Treat projections as stateful** - Assume NOT thread-safe unless proven otherwise
2. **Create projection per thread** - If parallelizing, give each thread its own projection instance
3. **Document thread-safety** - Clearly mark which classes are safe for concurrent use
4. **Test concurrency** - Use stress tests with multiple threads projecting same data

```kotlin
// Pattern: Thread-local projections for parallel batch processing
class ParallelProjector(private val config: ProjectionConfig) {
    private val threadLocalProjection = ThreadLocal.withInitial {
        ProjectionFactory.create(config)
    }
    
    fun projectBatch(points: List<Vector2>): List<Vector2> = 
        points.parallelStream().map { pt ->
            threadLocalProjection.get().project(pt)
        }.toList()
}
```

**Detection (Warning Signs):**
- Flaky tests that pass/fail randomly
- Coordinate values that change between runs
- `ConcurrentModificationException` or similar

**Phase to Address:** Phase 2 (PERF-01 Batch Projection) - If implementing parallelism

---

### Pitfall 10: Breaking Spatial Index Consistency

**What goes wrong:** The Quadtree spatial index (v1.0.0) becomes inconsistent with cached geometries, causing incorrect spatial queries or missed features.

**Why it happens:**
- v1.0.0 implemented Quadtree for efficient spatial queries on large datasets
- If geometries are cached in transformed state, spatial index may reference wrong coordinates
- `GeoSource` abstraction allows swappable sources with different indexing strategies

**Consequences:**
- Spatial queries return wrong results
- Features visible on screen not rendered (or vice versa)
- Inconsistent behavior between query and render paths

**Prevention:**
1. **Index in source CRS** - Spatial index should always use original geographic coordinates
2. **Separate concerns** - Spatial index for querying, cache for rendering
3. **Consistent coordinate spaces** - Clear documentation of when coordinates are geo vs screen
4. **Test index consistency** - Verify spatial queries work correctly with cached rendering

**Detection (Warning Signs):**
- `GeoSource.featuresInBounds()` returns different results than visible features
- Missing or extra features when panning/zooming
- Spatial query results don't match visual inspection

**Phase to Address:** Phase 2 (PERF-02 Caching) - Integration with existing features

---

## Moderate Pitfalls

Issues that cause bugs or performance problems but are recoverable.

### Pitfall 11: Inefficient Cache Key Generation

**What goes wrong:** Computing cache keys (hash codes for geometry + projection) takes significant time, negating the benefit of caching.

**Prevention:**
- Cache the hash code in Geometry classes (immutable, so safe)
- Use incremental hashing for large geometries
- Consider identity-based caching for stable object graphs

---

### Pitfall 12: Not Handling CRS Transformations in Cache

**What goes wrong:** Cache doesn't account for `CRSTransformer` usage (v1.1.0 feature), causing stale data when coordinate systems change.

**Prevention:**
- Include source and target CRS in cache key
- Invalidate cache when `autoTransformTo()` changes CRS
- Document interaction between CRS transforms and projection cache

---

### Pitfall 13: Ignoring Animation Frame Coherence

**What goes wrong:** Cache invalidates completely between frames even when projection is static, missing opportunity for frame-to-frame coherence.

**Prevention:**
- Track projection "generation" - only invalidate when config changes
- Cache survives across frames by default
- Clear only on explicit `extend { }` re-initialization

---

## Minor Pitfalls

Annoyances or code quality issues.

### Pitfall 14: Verbosity in Cache Configuration

**What goes wrong:** Users need to configure cache size, eviction policy, etc., adding friction to simple use cases.

**Prevention:**
- Sensible defaults (1000 entries, LRU eviction)
- Global config override for advanced users
- Beginner API should not expose cache configuration

---

### Pitfall 15: Missing Cache Statistics

**What goes wrong:** No visibility into cache performance (hit rate, memory usage), making optimization debugging difficult.

**Prevention:**
- Optional `CacheStats` exposed via `GeoRenderConfig`
- Debug logging for cache operations
- Performance dashboard in debug builds

---

## Phase-Specific Warnings

| Phase | Likely Pitfall | Mitigation |
|-------|---------------|------------|
| PERF-03 (Benchmarking) | Measuring wrong things | Profile end-to-end frame time, not just projection |
| PERF-01 (Batch Projection) | Data locality issues | Use SoA pattern, profile cache behavior |
| PERF-02 (Caching) | Invalidation bugs | Version projection state, conservative invalidation |
| Integration | Breaking existing APIs | Test all 16 examples, preserve entry points |
| Testing | Not testing with real data | Use Ordnance Survey datasets, not toy examples |

---

## Integration-Specific Pitfalls Summary

When adding batch projection and caching to the existing projection pipeline:

1. **Projection Interface Compatibility** - `GeoProjection.project()` must remain the primary API; batch operations are optimizations, not replacements
2. **Drawer Extension Transparency** - `drawer.geo()`, `drawer.geoSource()`, `drawer.geoJSON()` must work unchanged
3. **Geometry Sealed Class Exhaustiveness** - All 6 geometry types must be handled in any new when expressions
4. **Spatial Index Decoupling** - Don't tie spatial index updates to projection cache; keep concerns separate
5. **CRS Transformation Ordering** - Cache should store post-CRS-transform, pre-projection data, not raw source data

---

## Prevention Checklist by Feature

### PERF-03: Benchmarking & Measurement
- [ ] Profile end-to-end frame time before any optimization
- [ ] Test with real datasets (Ordnance Survey 12GB GeoPackage)
- [ ] Establish performance budgets (target: 60fps with 100k features)
- [ ] Document current bottlenecks with measurements

### PERF-01: Batch Screen Space Projection
- [ ] Implement SoA (Structure of Arrays) for coordinate batches
- [ ] Profile cache hit rates with Cachegrind or similar
- [ ] Test all 6 geometry types (including Multi* variants)
- [ ] Verify thread-safety if implementing parallel projection
- [ ] Measure memory layout improvements

### PERF-02: Geometry Caching
- [ ] Implement bounded LRU cache with configurable size
- [ ] Create projection-versioned cache keys
- [ ] Test cache invalidation on projection parameter changes
- [ ] Verify no memory leaks with long-running sessions
- [ ] Ensure cache is transparent to existing APIs
- [ ] Document cache statistics for debugging

### Integration Testing
- [ ] All 16 v1.2.0 examples run without modification
- [ ] Unit tests pass without changes to public API
- [ ] Spatial index queries return correct results with cached rendering
- [ ] CRS transformations work correctly with cache
- [ ] Animation layer (projection tweening) works with cache invalidation

---

## Sources

- [Game Programming Patterns - Data Locality](https://gameprogrammingpatterns.com/data-locality.html) - Cache-friendly data structures, pointer chasing pitfalls
- [Game Programming Patterns - Spatial Partition](https://gameprogrammingpatterns.com/spatial-partition.html) - Efficient spatial queries, data structure tradeoffs
- [Game Programming Patterns - Flyweight](https://gameprogrammingpatterns.com/flyweight.html) - Shared state patterns, immutability requirements
- [Martin Fowler - Two Hard Things](https://martinfowler.com/bliki/TwoHardThings.html) - Cache invalidation complexity
- openrndr-geo PROJECT.md - Project context, constraints, existing architecture
- openrndr-geo v1.2.0 codebase - Current projection pipeline, two-tier API, sealed class Geometry hierarchy
- Previous PITFALLS.md (v1.2.0) - Related pitfalls for API design and coordinate processing

---

*Pitfalls research for: openrndr-geo v1.3.0 Performance Optimization*  
*Researched: 2026-03-05*  
*Previous: v1.2.0 PITFALLS.md (API Improvements)*
