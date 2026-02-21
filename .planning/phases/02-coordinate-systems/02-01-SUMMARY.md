---
phase: 02-coordinate-systems
plan: 01
subsystem: projection

# Dependency graph
requires:
  - phase: 01-data-layer
    provides: OpenRNDR Vector2 integration, sealed class patterns from data model
provides:
  - Core projection interface with project/unproject/configure/fitWorld methods
  - Projection configuration system with viewport, center, scale control
  - Four custom exception types for coordinate transformation errors
  - Internal Mercator implementation with Web Mercator formula and pole handling
  - Internal Equirectangular implementation with automatic longitude normalization
affects:
  - 02-02 (public projections with DSL configuration)
  - 02-03 (screen transformation utilities)
  - 03-xx (rendering adapters will use projections)

# Tech tracking
tech-stack:
  added:
    - "org.locationtech.proj4j:proj4j:1.4.1" (CRS transformations)
    - "org.locationtech.proj4j:proj4j-epsg:1.4.1" (EPSG code database)
  patterns:
    - Interface-based abstraction for mixing coordinate systems
    - Internal package pattern for complexity isolation
    - Exception-based error handling with rich feedback messages

key-files:
  created:
    - src/main/kotlin/geo/projection/GeoProjection.kt (Core abstraction)
    - src/main/kotlin/geo/projection/ProjectionConfig.kt (Configuration)
    - src/main/kotlin/geo/exception/ProjectionExceptions.kt (Exception types)
    - src/main/kotlin/geo/projection/internal/ProjectionMercatorInternal.kt (Web Mercator)
    - src/main/kotlin/geo/projection/internal/ProjectionEquirectangularInternal.kt (Equirectangular)
  modified:
    - build.gradle.kts (added proj4j dependencies)

key-decisions:
  - "Use OpenRNDR Vector2 for screen coordinates" (integrates with drawing)
  - "Interface-based design allows mixing coordinate systems in visualizations"
  - "Internal package separates complex math from public API"
  - "Throw ProjectionOverflowException for Mercator poles, include clamp recommendation"
  - "Normalize longitudes automatically (CONTEXT.md decision)"

patterns-established:
  - "GeoProjection interface: contract for all projection types with project/unproject/configure/fitWorld"
  - "Internal implementations: complex mathematics isolated in internal package"
  - "Exception hierarchy: specific types for overflow, accuracy warnings, unrepresentable, and coordinate system errors"
  - "Screen offset calculation: center-based with Y-flip for screen coordinates"

# Metrics
duration: 6min
completed: 2026-02-21
---

# Phase 2 Plan 1: Projection Infrastructure Summary

**Core projection infrastructure with interface abstraction, configuration system, error handling, and internal Mercator/Equirectangular implementations using proj4j for CRS transformations**

## Performance

- **Duration:** 6 min
- **Started:** 2026-02-21T16:00:09Z
- **Completed:** 2026-02-21T16:05:48Z
- **Tasks:** 4
- **Files modified:** 6 (5 new, 1 modified)

## Accomplishments
- Added proj4j 1.4.1 dependencies for coordinate reference system transformations (EPSG:4326, EPSG:27700)
- Created GeoProjection interface with project(), unproject(), configure(), fitWorld() methods
- Designed ProjectionConfig data class for viewport, center, scale, and bounds configuration
- Implemented four custom exception types: ProjectionOverflowException, AccuracyWarningException, ProjectionUnrepresentableException, CoordinateSystemException
- Built internal Mercator implementation with Web Mercator formula and pole handling (MAX_LATITUDE=85.05112878°)
- Built internal Equirectangular implementation with automatic longitude normalization

## Task Commits

Each task was committed atomically:

1. **Task 1: Add proj4j dependency** - `19f34de` (chore)
2. **Task 2: Create GeoProjection interface** - `7dafb43` (feat)
3. **Task 3: Create projection exceptions** - `6d34312` (feat)
4. **Task 4: Create internal projections** - `a8f6eda` (feat)

**Plan metadata:** [pending]

## Files Created/Modified

- `build.gradle.kts` - Added proj4j and proj4j-epsg dependencies
- `src/main/kotlin/geo/projection/GeoProjection.kt` - Core projection interface abstraction
- `src/main/kotlin/geo/projection/ProjectionConfig.kt` - Configuration data class for projections
- `src/main/kotlin/geo/exception/ProjectionExceptions.kt` - Four custom exception types for projection errors
- `src/main/kotlin/geo/projection/internal/ProjectionMercatorInternal.kt` - Internal Mercator with pole handling
- `src/main/kotlin/geo/projection/internal/ProjectionEquirectangularInternal.kt` - Internal Equirectangular with normalization

## Decisions Made

- **Use OpenRNDR Vector2 for screen coordinates** (integrates with drawing operations)
- **Interface-based abstraction allows mixing coordinate systems** (enables lat/lng + BNG in one visualization)
- **Internal package separates complex math from public API** (complexity isolation)
- **Throw ProjectionOverflowException for exact Mercator poles** with clamp recommendation (CONTEXT.md)
- **Normalize longitudes automatically** in Equirectangular projection (CONTEXT.md)
- **Use proj4j for CRS transformations** - proven Java library with EPSG code support

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Next Phase Readiness

- Projection infrastructure complete, ready for 02-02 (public projections with DSL configuration)
- proj4j dependencies in place for EPSG:27700 (BNG) implementation
- Exception types ready for OSTN15 accuracy warnings
- Foundation established for all projection types

---
*Phase: 02-coordinate-systems*
*Completed: 2026-02-21*
