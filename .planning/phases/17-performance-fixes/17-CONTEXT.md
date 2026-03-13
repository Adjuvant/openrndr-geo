# Phase 17: Performance Fixes - Context

**Gathered:** 2026-03-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Extend ViewportCache to work with OptimizedGeoSource rendering path (PERF-11). Currently, optimized geometries bypass the cache entirely because they don't extend the `Geometry` sealed class.

**Key Insight:** The real performance win isn't just extending cache coverage - it's caching actual OpenRNDR Shape/Contour objects instead of coordinate arrays. This eliminates the per-frame "data → project → arrays → shapes" transformation overhead entirely.

**Requirements:** PERF-11 - ViewportCache integration for OptimizedGeoSource

</domain>

<decisions>
## Implementation Decisions

### Cache Value Type: Replacement, Not Extension
- **Critical realization:** This is NOT an incremental extension of Phase 12 — it replaces the entire cache value type and render-from-cache pathway
- Phase 12 built a chain around `Array<Vector2>`: `projectGeometryToArray()` → `renderWithCache()` → `renderProjectedCoordinates()`
- New flow: Check cache → on miss, project AND construct Shape/Contour → cache Shape → on hit, render Shape directly (skip both projection and construction)
- **Decision:** Replace the chain entirely — single cache value type (Shape), clean implementation. Maintaining parallel array+Shape paths defeats the "unified implementation" goal.

### Memory Overhead: Recalculate MAX_CACHE_ENTRIES
- Phase 12 set `MAX_CACHE_ENTRIES = 1000` assuming `Array<Vector2>` values
- ShapeContour stores ~4× memory per vertex (Segments with degenerate linear cubics)
- Back-of-envelope: 1000 entries × 50 vertices = ~60-80 MB (vs ~15-20 MB for arrays)
- Still within creative coding budget, but limit must be recalculated for new value type
- **Decision:** Recalculate early in planning — consider 500 limit or make configurable

### Scope — Both Render Paths
- Apply shape caching to **both** standard Geometry path AND OptimizedGeoSource path
- Standard path currently caches Array<Vector2>, should upgrade to Shape/Contour
- Optimized path currently has no caching - this is the gap closure
- **Rationale:** Consistent performance across all source types, unified implementation

### API Access — Creative Coding Friendly
- **Internal-only cache "feels bad for creative coding"** — users need access to cached data
- Extend existing patterns rather than create new API surface:
  - `styleByFeature` for property-based styling (height → color fade example)
  - Feature properties/data attributes for accessing cached shapes
- **Avoid:** New API patterns that duplicate existing functionality
- **Verify before planning:** Does `resolveOptimizedStyle()` invoke the `styleByFeature` callback? 
  - Standard path: `resolveStyle()` 
  - Optimized path: `resolveOptimizedStyle()`
  - If `resolveOptimizedStyle()` does NOT invoke `styleByFeature`, property-based styling silently fails on optimized path
  - **In scope:** Ensure style resolution parity (not new API, just fixing missing integration)

### Critical: Sequence Stability for Identity-Based Cache Keys
- Phase 12 validated identity equality (`===`) against `Geometry` objects in **materialised lists** — worked because stable references
- Optimized path exposes `optimizedFeatureSequence: Sequence<OptimizedFeature>` — often lazy pipelines
- **Risk:** If sequence re-evaluates per frame, every iteration produces fresh `OptimizedFeature` instances with new identity hashes → cache becomes write-only (0% hit rate)
- **Must verify before planning:**
  1. Is `optimizedFeatureSequence` backed by materialised collection (e.g., `List.asSequence()`)?
  2. Or is it a lazy pipeline producing new instances per iteration?
- **Resolution options:**
  - If materialised: Identity keys work as-is
  - If lazy: Either materialise on first access and hold references, OR switch to index-based keys (index within source + viewport state)

### Cache Unification: Single Generic Implementation
- **Don't duplicate `ViewportCache`** — use single generic cache `ViewportCache<K, V>` parameterized on key and value types
- Standard path: `ViewportCache<Geometry, Shape>` (keys on Geometry identity + viewport state)
- Optimized path: `ViewportCache<OptimizedFeature, Shape>` (keys on OptimizedFeature identity + viewport state)
- Same eviction semantics, same clear-on-change, one implementation
- **Avoid:** Duplicating cache class — looks harmless now, diverges silently later

### Memory Management
- **OpenCode's Discretion:** Size limits and eviction strategy
- Carry forward Phase 12 approach: Simple clear-on-change, no LRU/LFU
- **Already addressed:** Shape memory overhead in MAX_CACHE_ENTRIES calculation above
- Creative coding context: Performance > strict memory bounds

### Render Path: Clarify Canonical Location
- **Ambiguity:** Phase 12-03 integrated cache into `GeoStack.render()`, but Phase 17 context identifies `DrawerGeoExtensions.kt` (`Drawer.geo()`) as render dispatch point
- **Question:** Which path is canonical going forward? `GeoStack.render()` or `Drawer.geo()`?
- **Impact:** If `GeoStack` is being phased out in favor of `Drawer.geo()` extension pattern (v1.4 DSL refactor suggests this), then Phase 12 `GeoStack` integration may be dead code
- **Must clarify before planning:** Build Shape cache into canonical path only

### Dirty Flag Pattern
- **Standard Geometry:** Continue using `isDirty` flag (Phase 12 pattern) — geometries are mutable
- **Optimized geometries:** **DO NOT add dirty flag to `OptimizedFeature`**
  - `optimizedGeometry: Any` is opaque, type-erased blob from preprocessing
  - Not intended for mutation — adding dirty flag adds ceremony without utility
  - Cache invalidation: **source-level**, not per-feature
  - If `OptimizedGeoSource` changes (replaced or sequence changes), clear entire cache
- Aligns with Phase 12 pattern: clear-on-change semantics applied at source level

</decisions>

<specifics>
## Specific Ideas

### Performance Insight
User identified the real bottleneck: "It's all the shifting from data → project → arrays → shapes that is probably mashing performance"

**Solution:** Cache at the Shape/Contour level, not just projected coordinates.

### Use Case: Property-Based Rendering
Example: Contour lines with height data property, render color fade by height.

**Constraint:** Ensure this is possible through EXISTING API (styleByFeature) before building new access patterns. Creative coding workflow should use consistent patterns.

### Antimeridian Interaction (Phase 17.1)
- Phase 17.1 handles antimeridian fixes — antimeridian-split geometries produce **multiple Shapes** from single Feature
- Current Phase 12 cache assumes one-to-one mapping (`CacheKey` → `Array<Vector2>`)
- **Design constraint:** Cache must handle one-to-many (one feature key → multiple cached Shapes)
- **Options:**
  - Cache value type is `List<Shape>` rather than `Shape` (always a list, usually length 1) — **recommended**
  - Cache key includes split index — more complex, fragments the cache
- **Decision:** Use `List<Shape>` as cache value type for forward compatibility with 17.1

### Current Architecture Context
- `ViewportCache` stores `Array<Vector2>` for standard geometries
- `OptimizedGeoSource` contains `OptimizedFeature(optimizedGeometry: Any)` — not Geometry type
- Optimized render path: Iterates `optimizedFeatureSequence`, renders directly with `renderOptimizedToDrawer()`
- The gap: No caching in optimized path at all

</specifics>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ViewportCache` (src/main/kotlin/geo/internal/cache/ViewportCache.kt):
  - MutableMap-based storage with CacheKey (viewport state + geometry)
  - Clear-on-change semantics already implemented
  - MAX_CACHE_ENTRIES = 1000 constant
  - Can be extended or duplicated for optimized path

- `OptimizedGeoSource` (src/main/kotlin/geo/internal/OptimizedGeoSource.kt):
  - Provides `optimizedFeatureSequence: Sequence<OptimizedFeature>`
  - `OptimizedFeature` has `optimizedGeometry: Any` and `properties: Map<String, Any?>`
  - Already has `boundingBox()` calculation for optimized geometries

- `DrawerGeoExtensions.kt` (render logic):
  - Lines 298-305: Optimized path — iterates and renders without caching
  - Lines 306-313: Standard path — uses per-feature rendering
  - Both paths need shape caching integration

### Established Patterns
- **Cache keys:** `data class CacheKey(val viewportState: ViewportState, val geometry: Geometry)`
- **Viewport state:** Zoom, pan offset, projection target size
- **Dirty flag:** `Geometry.isDirty` boolean for change detection
- **Shape construction:** `ShapeContour.fromPoints()` then wind (`.clockwise`/`.counterClockwise`)

### Integration Points
- **Drawer extension:** `Drawer.geo(source, projection, config)` is the entry point
- **Render dispatch:** `when (source)` branching at line 297
- **Style resolution:** `resolveStyle()` for standard, `resolveOptimizedStyle()` for optimized
- **Geometry rendering:** `Geometry.renderToDrawer()` extension function
- **Optimized rendering:** `OptimizedFeature.renderOptimizedToDrawer()` extension function

</code_context>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. Key insight (cache Shapes not arrays) is within PERF-11 boundary.

**Note for planning:** Phase 17.1 (antimeridian fixes) exists with its own plan. Coordinate handling for antimeridian-split geometries should be considered when designing cached shape storage.

</deferred>

## Pre-Planning Checklist (Must Resolve Before Task Breakdown)

- [ ] **Sequence stability:** Verify whether `optimizedFeatureSequence` is backed by materialised collection or lazy pipeline
- [ ] **Canonical render path:** Confirm whether `GeoStack.render()` or `Drawer.geo()` is target for Shape caching
- [ ] **Style resolution parity:** Check whether `resolveOptimizedStyle()` invokes `styleByFeature`

**Once these three are resolved, the plan is ready for task breakdown.** Everything else — cache unification, memory recalculation, dirty flag decision, antimeridian one-to-many — can be addressed during planning with information already available.

---

*Phase: 17-performance-fixes*
*Context gathered: 2026-03-13*
*Updated: 2026-03-13 with technical review feedback*
