# Phase 12: Viewport Caching - Research

**Researched:** 2026-03-06
**Domain:** Kotlin Caching Patterns, OpenRNDR Geometry Rendering
**Confidence:** HIGH

## Summary

Phase 12 implements viewport caching for projected geometries to achieve 10x+ performance improvement in static camera scenarios. The implementation follows a "clear-on-change" semantic where the entire cache invalidates when viewport parameters (zoom, pan, projection size) change. This approach is optimal for creative coding workflows where users explore with pan/zoom, then observe static views.

The cache stores projected screen coordinates (Array<Vector2>) keyed by viewport state + geometry object reference. By using identity equality for geometry objects (not content hash), we achieve O(1) cache lookups while maintaining predictable invalidation behavior. The dirty flag pattern on Geometry enables reactive cache invalidation when geometry content changes.

**Primary recommendation:** Implement ViewportCache as an internal MutableMap with ViewportState data class key, integrate into GeoStack.render(), and add isDirty property to Geometry sealed class hierarchy.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Cache Key Design**
- Cache key combines **viewport state** + **geometry object reference**
- Viewport state includes: zoom level, pan offset (x, y), projection target size (width, height)
- Window viewport size is fixed in OpenRNDR, so not part of key
- Geometry identified by object reference (identity equality), not content hash
- Data class with generated `equals()`/`hashCode()` for viewport state portion

**Invalidation Strategy**
- **Clear entire cache** when any viewport parameter changes (zoom, pan, projection size)
- No per-entry eviction (no LRU/LFU complexity as per requirements)
- Simple "clear-on-change" semantics — predictable behavior for creative coding

**Content Change Detection (Reactive)**
- Add `isDirty: Boolean` property to base `Geometry` interface
- All geometry types (Point, LineString, Polygon, Multi*) implement dirty flag
- Flag sets to `true` when geometry coordinates/modifying properties change
- Cache checks dirty flag before using cached projection
- Cache clears dirty flag after reading cached value
- **Trade-off:** Modifying existing geometry instance without setting dirty flag = stale cache (user responsibility)

**Internal Visibility**
- **Completely invisible** to users
- Zero API changes
- No opt-in parameters or configuration
- Library automatically caches when beneficial
- Users don't need to know caching exists — it just makes things faster

### OpenCode's Discretion

**Cache Bounds**
- Choose reasonable entry limit (suggested: 500-1000 entries)
- When limit exceeded, clear entire cache
- Memory-bounded by "one viewport worth of data" — no complex eviction algorithms

**Thread Safety**
- Analyze codebase threading model
- If single-threaded: Simple `MutableMap` sufficient
- If multi-threaded: Use `ConcurrentHashMap` or synchronization

### Deferred Ideas (OUT OF SCOPE)

- **LRU/LFU eviction algorithms:** Not needed for creative coding use case (web-style optimization unnecessary)
- **External caching libraries:** Caffeine, Aedile — explicitly out of scope per requirements
- **Per-geometry-type caches:** Unified cache is simpler and sufficient
- **Advanced GPU integration patterns:** Frame ID tracking, triple buffering — future phase if needed
- **Content versioning with Long counters:** Boolean isDirty sufficient for Phase 12, version counters could enhance in future
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| PERF-04 | Library caches projected geometries for current viewport state | ViewportCache class with MutableMap<CacheKey, Array<Vector2>>; CacheKey contains ViewportState + geometry reference |
| PERF-05 | Cache invalidates when viewport changes (zoom, pan, viewport size) | ViewportState data class with equals/hashCode; clearOnViewportChange() method called from GeoStack zoom/pan methods |
| PERF-06 | Cache bounded by simple size limit (clear-on-change, not LRU/LFU) | MAX_CACHE_ENTRIES constant (500-1000); check size() before put, clear() if exceeded |
| PERF-07 | Caching is transparent to existing code (no API changes required) | Internal visibility only; integrate into existing GeoStack.render() without changing signatures |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin Stdlib | 1.9+ | MutableMap, data classes | Built-in, zero overhead |
| OpenRNDR | 0.4.5 | Vector2, Drawer integration | Existing project dependency |

### Supporting
| Component | Purpose | When to Use |
|-----------|---------|-------------|
| ViewportState data class | Immutable viewport configuration | Cache key generation |
| CacheKey data class | Composite key (viewport + geometry) | Map lookup |
| isDirty Boolean | Reactive invalidation | Geometry modification tracking |

**No External Dependencies Required**
- Caffeine/Aedile explicitly out of scope per requirements
- Simple Kotlin MutableMap sufficient for creative coding use case

## Architecture Patterns

### Recommended Project Structure
```
src/main/kotlin/geo/internal/cache/
├── ViewportCache.kt           # Main cache implementation
├── ViewportState.kt           # Immutable viewport configuration
└── CacheKey.kt                # Composite cache key

src/main/kotlin/geo/
├── Geometry.kt                # Add isDirty property to sealed class
└── GeoStack.kt                # Integrate cache into render()
```

### Pattern 1: ViewportState as Immutable Cache Key
**What:** Data class capturing all viewport parameters that affect projection
**When to use:** Whenever viewport configuration changes require cache invalidation
**Example:**
```kotlin
// Source: Based on ProjectionConfig pattern in codebase
data class ViewportState(
    val zoomLevel: Double,
    val centerX: Double,
    val centerY: Double,
    val projectionWidth: Double,
    val projectionHeight: Double
) {
    companion object {
        fun fromProjection(projection: GeoProjection): ViewportState {
            return when (projection) {
                is ProjectionMercator -> ViewportState(
                    zoomLevel = projection.config.zoomLevel,
                    centerX = projection.config.center?.x ?: 0.0,
                    centerY = projection.config.center?.y ?: 0.0,
                    projectionWidth = projection.config.width,
                    projectionHeight = projection.config.height
                )
                // ... other projection types
            }
        }
    }
}
```

### Pattern 2: Object Reference Identity for Geometry Key
**What:** Using geometry object reference (===) instead of content hash for cache key
**When to use:** When geometry instances are stable and content changes are tracked via dirty flag
**Example:**
```kotlin
// Source: Research finding - Unity-style caching pattern
data class CacheKey(
    val viewportState: ViewportState,
    val geometryRef: Geometry  // Uses identity equality via data class
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CacheKey) return false
        return viewportState == other.viewportState && 
               geometryRef === other.geometryRef  // Identity equality
    }
    
    override fun hashCode(): Int {
        return 31 * viewportState.hashCode() + System.identityHashCode(geometryRef)
    }
}
```

### Pattern 3: Dirty Flag Reactive Invalidation
**What:** Boolean flag on Geometry that indicates content modification
**When to use:** When geometry content can change after initial creation
**Example:**
```kotlin
// Source: Game engine pattern from CONTEXT.md
sealed class Geometry {
    abstract val boundingBox: Bounds
    
    // Dirty flag for reactive cache invalidation
    var isDirty: Boolean = true
        internal set
    
    // Call this when modifying coordinates
    protected fun markDirty() {
        isDirty = true
    }
}

// Usage in cache lookup
fun getCachedProjection(geometry: Geometry, projection: GeoProjection): Array<Vector2> {
    val key = CacheKey(viewportState, geometry)
    
    // Check dirty flag - if set, invalidate this entry
    if (geometry.isDirty) {
        cache.remove(key)
        geometry.isDirty = false  // Clear flag after invalidation
    }
    
    return cache.getOrPut(key) {
        projectGeometry(geometry, projection)
    }
}
```

### Pattern 4: Clear-on-Change Cache Semantics
**What:** Simple invalidation strategy - clear entire cache when viewport changes
**When to use:** When viewport changes are infrequent relative to rendering, and memory should be bounded
**Example:**
```kotlin
// Source: CONTEXT.md simplification decision
class ViewportCache(private val maxEntries: Int = 1000) {
    private val cache = mutableMapOf<CacheKey, Array<Vector2>>()
    private var currentViewportState: ViewportState? = null
    
    fun onViewportChange(newState: ViewportState) {
        if (newState != currentViewportState) {
            cache.clear()  // Clear entire cache on viewport change
            currentViewportState = newState
        }
    }
    
    fun get(key: CacheKey, compute: () -> Array<Vector2>): Array<Vector2> {
        // Check size limit
        if (cache.size >= maxEntries) {
            cache.clear()
        }
        return cache.getOrPut(key, compute)
    }
}
```

### Anti-Patterns to Avoid

- **LRU/LFU Implementation:** Overkill for creative coding; clear-on-change is sufficient and predictable
- **Content-Based Geometry Hash:** Expensive for large geometries; use identity equality + dirty flag instead
- **Per-Geometry Caches:** Unified cache is simpler and avoids fragmentation
- **WeakReference Keys:** Unnecessary complexity; cache bounded by entry count is sufficient
- **Async Cache Loading:** Synchronous access required for rendering pipeline; no benefit to async here

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Concurrent cache | Custom locking | MutableMap + simple synchronization | Single-threaded rendering; overkill |
| LRU eviction | LinkedHashMap override | Clear-on-change | Creative coding use case |
| Content hashing | Deep hash of coordinates | Identity equality + dirty flag | Performance + simplicity |
| Cache metrics | Custom counters | None (deferred to v2+) | PERF-14 deferred per requirements |

**Key insight:** The creative coding use case favors simplicity and predictability over sophisticated caching strategies. A static camera view should be fast; when the user pans/zooms, clearing the cache is acceptable behavior.

## Common Pitfalls

### Pitfall 1: Memory Leak from Unbounded Cache
**What goes wrong:** Cache grows indefinitely as user loads more geometries, causing OOM
**Why it happens:** No size limit or eviction strategy
**How to avoid:** Implement MAX_CACHE_ENTRIES check before put; clear entire cache when exceeded
**Warning signs:** Memory usage grows linearly with geometry count in profiler

### Pitfall 2: Stale Cache from Dirty Flag Not Set
**What goes wrong:** Geometry coordinates modified but isDirty not set; cache returns stale projection
**Why it happens:** User modifies geometry internals directly (e.g., mutable list operations)
**How to avoid:** Document that geometry modifications must go through API methods that set dirty flag; make coordinate collections immutable where possible
**Warning signs:** Visual artifacts where geometry appears in wrong position after modification

### Pitfall 3: Thread Safety Issues
**What goes wrong:** Concurrent access to MutableMap from render thread and animation thread causes ConcurrentModificationException
**Why it happens:** OpenRNDR programs may use coroutines or multiple threads
**How to avoid:** Use synchronized blocks or ConcurrentHashMap if multi-threading detected; single-threaded programs can use plain MutableMap
**Warning signs:** Intermittent crashes during rendering with stack trace pointing to cache access

### Pitfall 4: Cache Key Collisions
**What goes wrong:** Different geometries with same reference or different viewports with same state hash cause cache collisions
**Why it happens:** Improper equals/hashCode implementation in CacheKey
**How to avoid:** Use identity hash code for geometry (System.identityHashCode); include all viewport state fields in equals/hashCode
**Warning signs:** Wrong geometry rendered or viewport state ignored

### Pitfall 5: Breaking API Compatibility
**What goes wrong:** Adding cache parameter to public methods changes API surface
**Why it happens:** Cache integrated at wrong layer in architecture
**How to avoid:** Keep cache internal to render pipeline; never expose in public API
**Warning signs:** Compilation errors in user code or examples after update

## Code Examples

### ViewportCache Implementation
```kotlin
// Source: Based on codebase patterns and CONTEXT.md decisions
package geo.internal.cache

import geo.Geometry
import geo.projection.GeoProjection
import geo.projection.ProjectionMercator
import org.openrndr.math.Vector2

internal class ViewportCache(
    private val maxEntries: Int = 1000
) {
    private val cache = mutableMapOf<CacheKey, Array<Vector2>>()
    private var currentViewportState: ViewportState? = null
    
    fun getProjectedCoordinates(
        geometry: Geometry,
        projection: GeoProjection,
        projector: (Geometry) -> Array<Vector2>
    ): Array<Vector2> {
        val viewportState = ViewportState.fromProjection(projection)
        
        // Clear cache on viewport change
        if (viewportState != currentViewportState) {
            cache.clear()
            currentViewportState = viewportState
        }
        
        // Check dirty flag and invalidate if needed
        if (geometry.isDirty) {
            cache.remove(CacheKey(viewportState, geometry))
            geometry.isDirty = false
        }
        
        // Check size limit
        if (cache.size >= maxEntries) {
            cache.clear()
        }
        
        val key = CacheKey(viewportState, geometry)
        return cache.getOrPut(key) { projector(geometry) }
    }
    
    fun clear() {
        cache.clear()
        currentViewportState = null
    }
    
    val size: Int get() = cache.size
}
```

### Integrating into GeoStack
```kotlin
// Source: Extension of existing GeoStack.render() pattern
class GeoStack(private val sources: List<GeoSource>) {
    private val viewportCache = ViewportCache(maxEntries = 1000)
    
    // ... existing methods ...
    
    fun zoom(factor: Double) {
        // ... existing zoom logic ...
        // Cache automatically clears on next render due to viewport state change
    }
    
    fun pan(dx: Double, dy: Double) {
        // ... existing pan logic ...
        // Cache automatically clears on next render due to viewport state change
    }
    
    fun render(drawer: Drawer, projection: GeoProjection) {
        sources.forEach { source ->
            when (source) {
                is OptimizedGeoSource -> {
                    source.optimizedFeatureSequence.forEach { optFeature ->
                        renderOptimizedWithCache(optFeature, drawer, projection)
                    }
                }
                else -> {
                    source.features.forEach { feature ->
                        renderStandardWithCache(feature.geometry, drawer, projection)
                    }
                }
            }
        }
    }
    
    private fun renderStandardWithCache(
        geometry: Geometry,
        drawer: Drawer,
        projection: GeoProjection
    ) {
        val coords = viewportCache.getProjectedCoordinates(geometry, projection) { geom ->
            // Project geometry coordinates
            when (geom) {
                is Point -> arrayOf(projection.project(Vector2(geom.x, geom.y)))
                is LineString -> geom.points.map { projection.project(it) }.toTypedArray()
                // ... other geometry types
            }
        }
        
        // Render using cached coordinates
        when (geometry) {
            is Point -> geo.render.drawPoint(drawer, coords.first(), null)
            is LineString -> geo.render.drawLineString(drawer, coords.toList(), null)
            // ... other geometry types
        }
    }
}
```

### Geometry with Dirty Flag
```kotlin
// Source: Extension of existing Geometry sealed class
sealed class Geometry {
    abstract val boundingBox: Bounds
    
    /**
     * Dirty flag for viewport cache invalidation.
     * Set to true when geometry coordinates change.
     * Cleared by viewport cache after reading.
     */
    var isDirty: Boolean = true
        internal set
}

data class Point(val x: Double, val y: Double) : Geometry() {
    override val boundingBox: Bounds = Bounds(x, y, x, y)
    
    // Point is immutable - no setters needed
    // isDirty remains true from initialization, cleared by cache
}

data class LineString(val points: List<Vector2>) : Geometry() {
    init {
        require(points.size >= 2) { "LineString must have at least 2 points" }
    }

    override val boundingBox: Bounds by lazy {
        val xs = points.map { it.x }
        val ys = points.map { it.y }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }
    
    // LineString is immutable via data class
    // To modify, create new instance (sets isDirty=true by default)
}

// For mutable geometry variants (if needed in future):
class MutableLineString : Geometry() {
    private val _points = mutableListOf<Vector2>()
    val points: List<Vector2> get() = _points.toList()
    
    fun addPoint(point: Vector2) {
        _points.add(point)
        isDirty = true  // Mark cache entry as stale
    }
    
    // ... other modification methods ...
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| No caching | Viewport-based cache | Phase 12 | 10x+ performance for static cameras |
| Per-point projection | Batch projection + caching | Phase 11+12 | Combined optimization strategy |
| LRU eviction | Clear-on-change | Phase 12 simplification | Predictable behavior, simpler code |
| External caching libs | Kotlin MutableMap | Phase 12 simplification | Zero dependencies, faster build |

**Deprecated/outdated:**
- Caffeine/Aedile libraries: Explicitly out of scope per requirements
- Content hash for geometry keys: Replaced by identity equality + dirty flag
- Per-geometry-type caches: Unified cache is simpler

## Open Questions

1. **Thread Safety Model**
   - What we know: OpenRNDR programs typically run single-threaded for rendering
   - What's unclear: Whether coroutines/animations run on separate threads
   - Recommendation: Start with MutableMap; add synchronization if crashes occur

2. **Optimized Geometry Caching**
   - What we know: OptimizedGeometries use CoordinateBatch internally
   - What's unclear: Whether to cache at CoordinateBatch level or Vector2 array level
   - Recommendation: Cache Array<Vector2> result for consistency with standard geometries

3. **Cache Entry Size Estimation**
   - What we know: 1000 entry limit suggested
   - What's unclear: Average geometry size in typical use cases
   - Recommendation: Make limit configurable via constructor; default to 1000

4. **Viewport State Extraction**
   - What we know: ProjectionConfig has zoomLevel, center, width, height
   - What's unclear: Whether GeoProjection interface exposes all needed state
   - Recommendation: Add extension function or when-expression to extract state from any projection type

## Validation Architecture

> Nyquist validation is enabled in config (workflow.nyquist_validation: true)

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 |
| Config file | build.gradle.kts (testImplementation) |
| Quick run command | `./gradlew test --tests "geo.cache.*"` |
| Full suite command | `./gradlew test` |
| Estimated runtime | ~30 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| PERF-04 | Cache stores projected coordinates | unit | `./gradlew test --tests "geo.cache.ViewportCacheTest.testCacheStoresProjection"` | ❌ Wave 0 gap |
| PERF-05 | Cache clears on viewport change | unit | `./gradlew test --tests "geo.cache.ViewportCacheTest.testCacheClearsOnViewportChange"` | ❌ Wave 0 gap |
| PERF-06 | Cache bounded by size limit | unit | `./gradlew test --tests "geo.cache.ViewportCacheTest.testCacheSizeLimit"` | ❌ Wave 0 gap |
| PERF-07 | No API changes (transparent) | integration | `./gradlew test --tests "geo.GeoStackTest.testRenderingUnchanged"` | ❌ Wave 0 gap |
| PERF-04 | Dirty flag invalidates cache | unit | `./gradlew test --tests "geo.GeometryTest.testDirtyFlagInvalidatesCache"` | ❌ Wave 0 gap |

### Nyquist Sampling Rate
- **Minimum sample interval:** After every committed task → run: `./gradlew test --tests "geo.cache.*"`
- **Full suite trigger:** Before merging final task of any plan wave
- **Phase-complete gate:** Full suite green before `/gsd-verify-work` runs
- **Estimated feedback latency per task:** ~15 seconds

### Wave 0 Gaps (must be created before implementation)
- [ ] `src/test/kotlin/geo/cache/ViewportCacheTest.kt` — covers PERF-04, PERF-05, PERF-06
- [ ] `src/test/kotlin/geo/cache/CacheKeyTest.kt` — covers cache key equality/hashing
- [ ] `src/test/kotlin/geo/GeometryDirtyFlagTest.kt` — covers dirty flag behavior
- [ ] `src/main/kotlin/geo/internal/cache/ViewportCache.kt` — framework to test
- [ ] `src/main/kotlin/geo/internal/cache/CacheKey.kt` — cache key implementation

## Sources

### Primary (HIGH confidence)
- `12-CONTEXT.md` - Locked decisions and constraints
- `REQUIREMENTS.md` - PERF-04 through PERF-07 requirements
- `STATE.md` - Project architecture and existing patterns
- `Geometry.kt` - Existing Geometry sealed class hierarchy
- `GeoStack.kt` - Rendering pipeline integration point
- `OptimizedGeometries.kt` - Batch projection patterns

### Secondary (MEDIUM confidence)
- OpenRNDR 0.4.5 documentation and patterns
- Kotlin data class best practices for cache keys
- Game engine dirty flag patterns (Unity, Unreal)

### Tertiary (LOW confidence)
- Web-based LRU cache implementations (not applicable per requirements)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - No external dependencies required, Kotlin stdlib sufficient
- Architecture: HIGH - Clear integration points in GeoStack and Geometry
- Pitfalls: MEDIUM - Thread safety requires runtime validation

**Research date:** 2026-03-06
**Valid until:** 2026-04-06 (30 days for stable Kotlin patterns)

---

## Appendix: Existing Codebase Patterns

### Current Rendering Pipeline
```
GeoStack.render(drawer, projection)
├── For each source in sources
│   ├── If OptimizedGeoSource
│   │   └── For each OptimizedFeature
│   │       └── renderOptimizedToDrawer() // Uses batch projection
│   └── If standard GeoSource
│       └── For each Feature
│           └── renderToDrawer() // Per-point projection
```

### Integration Point
ViewportCache integrates at the `renderToDrawer()` and `renderOptimizedToDrawer()` level:
- Before projecting, check cache
- If cache miss, project and store
- If cache hit and not dirty, use cached coordinates

### Geometry Type Coverage
| Geometry Type | Standard | Optimized | Cache Support |
|--------------|----------|-----------|---------------|
| Point | ✓ | ✓ | ✓ |
| LineString | ✓ | ✓ | ✓ |
| Polygon | ✓ | ✓ | ✓ |
| MultiPoint | ✓ | ✓ | ✓ |
| MultiLineString | ✓ | ✓ | ✓ |
| MultiPolygon | ✓ | ✓ | ✓ |
