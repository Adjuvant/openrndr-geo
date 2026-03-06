---
phase: 12-viewport-caching
plan: 01
subsystem: caching

# Dependency graph
requires:
  - phase: 11-batch-projection
    provides: Batch projection infrastructure, OptimizedGeometries
provides:
  - ViewportState immutable viewport configuration for cache keys
  - CacheKey with identity-based equality for geometry references
  - ViewportCache with clear-on-change semantics and size limit
  - Geometry.isDirty property for reactive cache invalidation
affects:
  - GeoStack.render() (will integrate in future plans)
  - All Geometry types (via isDirty property)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Identity equality for cache keys (Unity-style)"
    - "Clear-on-change cache invalidation"
    - "Dirty flag pattern for reactive invalidation"
    - "Simple MutableMap caching (no external libraries)"

key-files:
  created:
    - src/main/kotlin/geo/internal/cache/ViewportState.kt
    - src/main/kotlin/geo/internal/cache/CacheKey.kt
    - src/main/kotlin/geo/internal/cache/ViewportCache.kt
  modified:
    - src/main/kotlin/geo/Geometry.kt (added isDirty property)

key-decisions:
  - "Used identity equality (===) for geometry references instead of content hash - avoids expensive hashing for large geometries"
  - "Set MAX_CACHE_ENTRIES = 1000 per user discretion (within 500-1000 range)"
  - "Added isDirty to Geometry sealed class now to support ViewportCache compilation - referenced in Plan 02"

patterns-established:
  - "ViewportState.fromProjection(): Factory method using when-expression for projection type extraction"
  - "CacheKey identity equality: viewportState == other.viewportState && geometryRef === other.geometryRef"
  - "Clear-on-change semantics: cache.clear() when viewportState changes"
  - "Dirty flag pattern: geometry.isDirty triggers entry invalidation before cache lookup"

requirements-completed: [PERF-04, PERF-05, PERF-06]

# Metrics
duration: 2min
completed: 2026-03-06T23:04:54Z
---

# Phase 12 Plan 01: Viewport Caching Infrastructure Summary

**Core viewport caching infrastructure with ViewportState, CacheKey, and ViewportCache classes. Provides foundation for 10x+ performance improvement in static camera scenarios using clear-on-change semantics with MutableMap storage.**

## Performance

- **Duration:** 2 min
- **Started:** 2026-03-06T23:02:38Z
- **Completed:** 2026-03-06T23:04:54Z
- **Tasks:** 3
- **Files modified:** 4 (3 created, 1 modified)

## Accomplishments

- Created ViewportState data class capturing all viewport parameters that affect projection (zoom, center, dimensions)
- Implemented CacheKey with identity-based equality using System.identityHashCode for geometry references
- Built ViewportCache with clear-on-viewport-change semantics and MAX_CACHE_ENTRIES = 1000 limit
- Added isDirty property to Geometry sealed class for reactive cache invalidation
- All components marked internal for implementation hiding

## Task Commits

Each task was committed atomically:

1. **Task 1: Create ViewportState data class** - `dca09d6` (feat)
2. **Task 2: Create CacheKey with identity equality** - `f8dc6c4` (feat)
3. **Task 3: Implement ViewportCache** - `c01965e` (feat)

**Plan metadata:** [pending final commit]

## Files Created/Modified

- `src/main/kotlin/geo/internal/cache/ViewportState.kt` - Immutable viewport configuration with fromProjection factory
- `src/main/kotlin/geo/internal/cache/CacheKey.kt` - Composite key with identity equality for geometry references
- `src/main/kotlin/geo/internal/cache/ViewportCache.kt` - MutableMap cache with clear-on-change and size limit enforcement
- `src/main/kotlin/geo/Geometry.kt` - Added isDirty property to sealed class for reactive invalidation

## Decisions Made

1. **Identity equality for geometry**: Using `===` and `System.identityHashCode()` instead of content hashing avoids O(n) hash computation for large geometries. This follows the Unity-style caching pattern from research.

2. **MAX_CACHE_ENTRIES = 1000**: Selected the upper end of the suggested 500-1000 range. For creative coding scenarios with static camera views, this accommodates typical scene complexity while bounding memory.

3. **Added isDirty now rather than Plan 02**: Task 3 required `geometry.isDirty` to compile the ViewportCache. Rather than leaving a placeholder, implemented the property in Geometry sealed class. This is a minor deviation that enables clean compilation.

4. **Internal visibility**: All cache components marked `internal` per requirement PERF-07 (transparent caching, no API changes).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added isDirty property to Geometry sealed class**
- **Found during:** Task 3 (ViewportCache implementation)
- **Issue:** ViewportCache references `geometry.isDirty` but Geometry class didn't have this property yet (planned for Plan 02)
- **Fix:** Added `isDirty: Boolean = true` property to Geometry sealed class with `internal set` visibility
- **Files modified:** `src/main/kotlin/geo/Geometry.kt`
- **Verification:** Compilation successful with `./gradlew compileKotlin`
- **Committed in:** `c01965e` (part of task 3 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Minor deviation that enables compilation. Property was required for ViewportCache logic and was already planned for Plan 02.

## Issues Encountered

None - all compilation checks passed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Viewport caching infrastructure complete and compiling
- Ready for Plan 02: Integration into GeoStack rendering pipeline
- Plan 02 will add integration tests and benchmark the 10x+ improvement target

---
*Phase: 12-viewport-caching*
*Completed: 2026-03-06*
