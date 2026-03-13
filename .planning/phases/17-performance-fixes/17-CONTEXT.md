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

### Cache Granularity (OpenCode's Discretion)
- **Key insight from discussion:** Cache actual OpenRNDR primitives (Shape, Contour) rather than just Array<Vector2>
- Current approach converts arrays to Shapes every frame - wasteful for static viewports
- Tradeoff: Higher memory usage vs zero per-frame conversion cost
- **Decision:** OpenCode to analyze and recommend based on:
  - Memory overhead of Shape objects vs coordinate arrays
  - Typical creative coding scene sizes (1000-10000 features)
  - Performance gain from eliminating per-frame Shape construction

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
- **Verify during planning:** Does current `styleByFeature` API already support the height → color fade use case? Don't build if it already exists.

### Cache Key Strategy
- Standard Geometry: Continue using object reference (identity) + viewport state
- Optimized geometries: Use OptimizedFeature reference + viewport state
- **Consistent with Phase 12 decisions:** Identity-based keys, no content hashing

### Memory Management
- **OpenCode's Discretion:** Size limits and eviction strategy
- Carry forward Phase 12 approach: Simple clear-on-change, no LRU/LFU
- Consider Shape memory overhead in MAX_CACHE_ENTRIES calculation
- Creative coding context: Performance > strict memory bounds

### Dirty Flag Pattern
- Standard Geometry: Continue using `isDirty` flag (Phase 12 pattern)
- Optimized geometries: **OpenCode's Discretion** — evaluate if OptimizedFeature should support dirty flags
- Consider: Are optimized geometries typically immutable after creation?

</decisions>

<specifics>
## Specific Ideas

### Performance Insight
User identified the real bottleneck: "It's all the shifting from data → project → arrays → shapes that is probably mashing performance"

**Solution:** Cache at the Shape/Contour level, not just projected coordinates.

### Use Case: Property-Based Rendering
Example: Contour lines with height data property, render color fade by height.

**Constraint:** Ensure this is possible through EXISTING API (styleByFeature) before building new access patterns. Creative coding workflow should use consistent patterns.

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

---

*Phase: 17-performance-fixes*
*Context gathered: 2026-03-13*
