---
phase: 02-coordinate-systems
plan: 02
subsystem: projection
tags: [kotlin, proj4j, mercator, equirectangular, bng, crs, dsl, factory]

# Dependency graph
requires:
  - phase: 02-01
    provides: GeoProjection interface, ProjectionConfig, internal implementations
provides:
  - Public projection classes with DSL configuration
  - ProjectionFactory for convenient presets
  - BNG projection with proj4j CRS transformation
  - DSL builder syntax for projections
affects:
  - Phase 3 (Core Rendering - will use projections for screen mapping)
  - Phase 4 (Layer System - projections used in layer composition)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - DSL builder pattern with invoke() operator
    - Factory pattern for preset creation
    - Delegation to internal implementations
    - Companion object static methods for utilities

key-files:
  created:
    - src/main/kotlin/geo/projection/ProjectionMercator.kt
    - src/main/kotlin/geo/projection/ProjectionEquirectangular.kt
    - src/main/kotlin/geo/projection/ProjectionBNG.kt
    - src/main/kotlin/geo/projection/ProjectionFactory.kt
  modified: []

key-decisions:
  - "DSL configuration with invoke() operator enables clean syntax: ProjectionMercator { width = 800 }"
  - "ProjectionBNG uses proj4j with Helmert transformation (~3-5m accuracy) for simplicity"
  - "BNG validates UK bounds (0-700km easting, 0-1300km northing) and throws AccuracyWarningException"
  - "Factory pattern provides convenient preset access without manual ProjectionConfig construction"

patterns-established:
  - "DSL Builder: operator fun invoke(block: Builder.() -> Unit) for clean configuration"
  - "Delegation Pattern: Public class delegates to Internal class for mathematics"
  - "Factory Object: Singleton object with preset methods for common use cases"
  - "Static Utilities: Companion object methods for coordinate transformations"

# Metrics
duration: 8min
completed: 2026-02-21
---

# Phase 2 Plan 2: Public Projection Implementations Summary

**Public projection classes with DSL configuration, proj4j CRS transformation for BNG, and factory for convenient presets**

## Performance

- **Duration:** 8 min
- **Started:** 2026-02-21T16:10:00Z
- **Completed:** 2026-02-21T16:18:00Z
- **Tasks:** 4
- **Files modified:** 4 created

## Accomplishments

- Created ProjectionMercator with DSL configuration and delegation to internal implementation
- Created ProjectionEquirectangular following same DSL pattern for basic world maps
- Created ProjectionBNG with proj4j CRS transformation (EPSG:4326 ↔ EPSG:27700)
- Created ProjectionFactory with preset methods for Mercator, Equirectangular, BNG
- Implemented Helmert transformation for BNG (~3-5m accuracy)
- Added UK bounds validation with rich error messages

## Task Commits

Each task was committed atomically:

1. **Task 1: Create public Mercator projection with DSL configuration** - `b965df8` (feat)
2. **Task 2: Create public Equirectangular projection with DSL configuration** - `4e6ce9c` (feat)
3. **Task 3: Create British National Grid projection with CRS transformation** - `b6461e7` (feat)
4. **Task 4: Create ProjectionFactory for convenient preset access** - `ec9b3a2` (feat)

**Plan metadata:** [pending - will be committed with STATE.md update]

## Files Created/Modified

- `src/main/kotlin/geo/projection/ProjectionMercator.kt` - Web Mercator projection with DSL configuration
- `src/main/kotlin/geo/projection/ProjectionEquirectangular.kt` - Equirectangular projection with DSL
- `src/main/kotlin/geo/projection/ProjectionBNG.kt` - British National Grid with proj4j CRS transformation
- `src/main/kotlin/geo/projection/ProjectionFactory.kt` - Factory for preset projections

## Decisions Made

- **DSL syntax with invoke() operator:** Enables clean configuration syntax like `ProjectionMercator { width = 800 }`
- **ProjectionConfigBuilder in same file:** Keeps builder with its related class for discoverability
- **BNG uses Helmert transformation (~3-5m):** Chose simpler implementation over OSTN15 (~1cm) which requires grid interpolation
- **Factory with default parameters:** 800x600 viewport, scale=1.0 covers common use cases
- **Companion object for static utilities:** latLngToBNG and bngToLatLng available without instance

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None - all implementations followed established patterns from 02-01.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All public projection classes ready for use in rendering
- proj4j dependency already added in 02-01
- DSL syntax established and ready for user-facing API
- Factory provides convenient entry points for common use cases

**Ready for:** 02-03 (Screen transformation utilities and helper functions)

---
*Phase: 02-coordinate-systems*
*Completed: 2026-02-21*
