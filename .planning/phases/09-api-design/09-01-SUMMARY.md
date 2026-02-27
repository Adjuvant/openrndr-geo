---
phase: 09-api-design
plan: 01
subsystem: api
tags: [geo, projection, feature-iteration, chaining]

# Dependency graph
requires:
  - phase: 09-00
    provides: test scaffolds for API-01
provides:
  - ProjectedFeature wrapper with projected geometry
  - GeoSource.withProjection() for lazy projection
  - GeoSource.filter() and map() chainable operations
  - forEachWithProjection() extension function
affects: [API-02, API-03, API-04]

# Tech tracking
tech-stack:
  added: []
  patterns: [sealed class for type-safe geometry, Sequence-based lazy evaluation, extension functions for DSL]

key-files:
  created: []
  modified:
    - src/main/kotlin/geo/Feature.kt - Added ProjectedFeature and ProjectedGeometry classes
    - src/main/kotlin/geo/GeoSource.kt - Added withProjection, filter, map, forEachWithProjection
    - src/test/kotlin/geo/GeoSourceChainingTest.kt - Implemented 5 tests

key-decisions:
  - "Used sealed class for ProjectedGeometry to preserve type information"
  - "Sequence-based lazy evaluation for memory efficiency"
  - "filter/map return new GeoSource to preserve CRS context"

patterns-established:
  - "ProjectedGeometry sealed class with concrete types for each geometry type"
  - "GeoSource extension functions for chainable operations"

requirements-completed: [API-01]

# Metrics
duration: 5 min
completed: 2026-02-27
---

# Phase 9 Plan 1: Feature Iteration with Projected Coordinates Summary

**ProjectedFeature wrapper with lazy projection iteration, enabling filter().map().forEach() chains**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-27T13:05:25Z
- **Completed:** 2026-02-27T13:10:00Z
- **Tasks:** 3
- **Files modified:** 3

## Accomplishments
- Implemented ProjectedFeature data class wrapping Feature with projected geometry
- Implemented ProjectedGeometry sealed class with all geometry types (Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon)
- Added GeoSource.withProjection() returning lazy Sequence<ProjectedFeature>
- Added GeoSource.filter() and map() for chainable operations
- Added forEachWithProjection() extension for clean iteration syntax
- Enabled and implemented 5 tests in GeoSourceChainingTest

## Task Commits

Each task was committed atomically:

1. **task 1: Add ProjectedFeature wrapper and withProjection extension** - `310de75` (feat)
2. **task 2: Add chainable filter/map extensions to GeoSource** - `310de75` (feat)
3. **task 3: Implement and verify GeoSourceChainingTest** - `310de75` (feat)

**Plan metadata:** `310de75` (docs: complete plan)

## Files Created/Modified
- `src/main/kotlin/geo/Feature.kt` - Added ProjectedFeature and ProjectedGeometry sealed class hierarchy
- `src/main/kotlin/geo/GeoSource.kt` - Added withProjection(), filter(), map(), forEachWithProjection()
- `src/test/kotlin/geo/GeoSourceChainingTest.kt` - Implemented 5 tests (all pass)

## Decisions Made
- Used sealed class for ProjectedGeometry to preserve geometry type through projection pipeline
- Sequence-based lazy evaluation to handle large datasets efficiently
- filter/map return new GeoSource to preserve CRS through transformations

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Pre-existing broken test in DrawerGeoExtensionsTest.kt (unrelated to this plan, restored original file)

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- API-01 complete, ready for API-02 (two-tier API with drawer.geo())
- filter/map chain ready for integration with projection context

---
*Phase: 09-api-design*
*Completed: 2026-02-27*
