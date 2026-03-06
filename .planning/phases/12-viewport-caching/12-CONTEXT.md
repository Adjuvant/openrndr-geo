# Phase 12: Viewport Caching - Context

**Gathered:** 2026-03-06
**Status:** Ready for planning

<domain>
## Phase Boundary

Cache projected geometries for the current viewport state with clear-on-change semantics. When viewport changes (zoom, pan, projection target size), the entire cache clears. Implementation uses simple Kotlin `MutableMap` with no external dependencies (no Caffeine/Aedile). Caching is completely transparent to users — zero API changes required.

**Target:** 10x+ performance improvement for static camera scenarios by avoiding redundant projection calculations.

</domain>

<decisions>
## Implementation Decisions

### Cache Key Design
- Cache key combines **viewport state** + **geometry object reference**
- Viewport state includes: zoom level, pan offset (x, y), projection target size (width, height)
- Window viewport size is fixed in OpenRNDR, so not part of key
- Geometry identified by object reference (identity equality), not content hash
- Data class with generated `equals()`/`hashCode()` for viewport state portion

### Invalidation Strategy
- **Clear entire cache** when any viewport parameter changes (zoom, pan, projection size)
- No per-entry eviction (no LRU/LFU complexity as per requirements)
- Simple "clear-on-change" semantics — predictable behavior for creative coding

### Content Change Detection (Reactive)
- Add `isDirty: Boolean` property to base `Geometry` interface
- All geometry types (Point, LineString, Polygon, Multi*) implement dirty flag
- Flag sets to `true` when geometry coordinates/modifying properties change
- Cache checks dirty flag before using cached projection
- Cache clears dirty flag after reading cached value
- **Trade-off:** Modifying existing geometry instance without setting dirty flag = stale cache (user responsibility)

### Cache Bounds
- **OpenCode's Discretion:** Choose reasonable entry limit (suggested: 500-1000 entries)
- When limit exceeded, clear entire cache
- Memory-bounded by "one viewport worth of data" — no complex eviction algorithms

### Internal Visibility
- **Completely invisible** to users
- Zero API changes
- No opt-in parameters or configuration
- Library automatically caches when beneficial
- Users don't need to know caching exists — it just makes things faster

### Thread Safety
- **OpenCode's Discretion:** Analyze codebase threading model
- If single-threaded: Simple `MutableMap` sufficient
- If multi-threaded: Use `ConcurrentHashMap` or synchronization

</decisions>

<specifics>
## Specific Ideas

- **Unity-style caching:** Cache key based on object reference (like Unity's mesh instance ID + camera state)
- **Game engine pattern:** Boolean dirty flags for change detection (simpler than version counters for this use case)
- **Creative coding use case:** User explores with pan/zoom, then observes static view. When camera moves, everything becomes stale — simple clear-on-change is sufficient and predictable.

</specifics>

<deferred>
## Deferred Ideas

- **LRU/LFU eviction algorithms:** Not needed for creative coding use case (web-style optimization unnecessary)
- **External caching libraries:** Caffeine, Aedile — explicitly out of scope per requirements
- **Per-geometry-type caches:** Unified cache is simpler and sufficient
- **Advanced GPU integration patterns:** Frame ID tracking, triple buffering — future phase if needed
- **Content versioning with Long counters:** Boolean isDirty sufficient for Phase 12, version counters could enhance in future

</deferred>

---

*Phase: 12-viewport-caching*
*Context gathered: 2026-03-06*
