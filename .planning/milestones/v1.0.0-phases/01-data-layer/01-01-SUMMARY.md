---
phase: 01-data-layer
plan: 01
subsystem: data-model

tags:
  - kotlin
  - sealed-class
  - geospatial
  - openrndr
  - junit4

requires:
  - phase: none
    provides: foundational types

provides:
  - Bounds: rectangular bounding box with spatial operations
  - Geometry: sealed class hierarchy (Point, LineString, Polygon, Multi*)
  - Feature: geometry + properties with type-safe access
  - GeoSource: abstract base for geo data sources

affects:
  - phase: 01-data-layer
    needs: GeoJSON and GeoPackage readers will use these types
  - phase: 02-coordinate-systems
    needs: CRS transformation will work with Geometry types
  - phase: 03-core-rendering
    needs: Renderers will use sealed class for type-safe drawing

tech-stack:
  added:
    - OpenRNDR Vector2 (org.openrndr.math.Vector2)
  patterns:
    - Sealed class hierarchy for exhaustive when expressions
    - Lazy property delegation for expensive calculations
    - Reified generics for type-safe property access
    - Sequence-based lazy iteration for memory efficiency

key-files:
  created:
    - src/main/kotlin/geo/Bounds.kt
    - src/main/kotlin/geo/Geometry.kt
    - src/main/kotlin/geo/Feature.kt
    - src/main/kotlin/geo/GeoSource.kt
    - src/test/kotlin/geo/BoundsTest.kt
    - src/test/kotlin/geo/GeometryTest.kt
    - src/test/kotlin/geo/FeatureTest.kt
    - src/test/kotlin/geo/GeoSourceTest.kt
  modified: []

key-decisions:
  - "Use OpenRNDR Vector2 for points instead of custom Point class - integrates with drawing"
  - "Sealed class hierarchy enables exhaustive when expressions for type-safe rendering"
  - "Lazy bounding box calculation (computed once, cached)"
  - "Sequence<Feature> in GeoSource for memory-efficient large dataset processing"
  - "Bounds.empty() with NaN values for type-safe empty state handling"

patterns-established:
  - "Geometry validation in init blocks - fail fast on invalid data"
  - "Type-safe property access via reified generics (propertyAs<T>())"
  - "Spatial queries via Bounds.intersects() and expandToInclude()"
  - "Abstract base classes with default implementations for common operations"

duration: 12min
completed: 2026-02-21
---

# Phase 01 Plan 01: Core Data Model Summary

**Sealed class hierarchy for geospatial types with OpenRNDR Vector2 integration, lazy bounding boxes, and type-safe property access.**

## Performance

- **Duration:** 12 min
- **Started:** 2026-02-21T12:47:39Z
- **Completed:** 2026-02-21T12:59:59Z
- **Tasks:** 4
- **Files created:** 8 (4 source + 4 test)

## Accomplishments

1. **Bounds data class** - Rectangular bounding box with spatial operations (intersects, contains, expandToInclude), empty state handling, and convenience properties (width, height, area, center)

2. **Geometry sealed class hierarchy** - Six concrete types (Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon) with OpenRNDR Vector2 integration, lazy bounding box calculation, and validation in init blocks

3. **Feature class** - Combines geometry with properties map, provides type-safe property access via propertyAs<T>(), includes convenience methods for common types (stringProperty, doubleProperty, etc.)

4. **GeoSource abstract base** - Unified interface for geo data access with lazy Sequence<Feature> iteration, spatial queries (featuresInBounds), and CRS tracking (default WGS84)

## Task Commits

Each task was committed atomically:

1. **Task 1: Create Bounds type** - `f53984e` (feat)
2. **Task 2: Create Geometry sealed class hierarchy** - `ec576b9` (feat)
3. **Task 3: Create Feature class** - `bc882c6` (feat)
4. **Task 4: Create GeoSource abstract class** - `088b12d` (feat)

**Plan metadata:** [to be committed]

## Files Created

**Source files:**
- `src/main/kotlin/geo/Bounds.kt` - Spatial bounding box with intersection, containment, expansion
- `src/main/kotlin/geo/Geometry.kt` - Sealed class hierarchy for all geometry types
- `src/main/kotlin/geo/Feature.kt` - Feature with geometry + properties
- `src/main/kotlin/geo/GeoSource.kt` - Abstract base class for geo data sources

**Test files:**
- `src/test/kotlin/geo/BoundsTest.kt` - 10 test cases covering all Bounds operations
- `src/test/kotlin/geo/GeometryTest.kt` - 18 test cases for all geometry types
- `src/test/kotlin/geo/FeatureTest.kt` - 18 test cases for property access
- `src/test/kotlin/geo/GeoSourceTest.kt` - 14 test cases for data source interface

**Total test coverage:** 60 passing tests

## Decisions Made

1. **OpenRNDR Vector2 for points** - Instead of creating a custom Point class with x/y, we use OpenRNDR's Vector2. This provides immediate integration with OpenRNDR's drawing operations and math utilities.

2. **Sealed class for Geometry** - Using `sealed class Geometry` enables exhaustive `when` expressions. Renderers can handle all geometry types with compiler-checked completeness.

3. **Lazy bounding boxes** - Bounding boxes are expensive to calculate for complex geometries. Using `by lazy` delegates ensures they're computed once and cached.

4. **NaN for empty Bounds** - Using `Double.NaN` for empty bounds coordinates allows type-safe empty state handling. All operations check `isEmpty()` first.

5. **Sequence<Feature> for GeoSource** - Sequences provide lazy iteration, essential for processing large geo datasets without loading everything into memory.

6. **Reified generics for propertyAs<T>()** - Kotlin's `inline fun <reified T>` enables type-safe property access without explicit Class<T> parameters.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed Bounds.expandToInclude() for empty bounds**

- **Found during:** Task 2 (MultiPoint bounding box calculation)
- **Issue:** `Bounds.expandToInclude(x, y)` used `minOf(minX, x)` which returns NaN when minX is NaN (empty bounds). MultiPoint bounding box starts with empty bounds and expands to include each point, but NaN propagated through all calculations.
- **Fix:** Added check `if (isEmpty()) return Bounds(x, y, x, y)` at start of `expandToInclude(x, y)`
- **Files modified:** `src/main/kotlin/geo/Bounds.kt`
- **Verification:** MultiPointBoundingBox test now passes (was failing with AssertionError: expected:<0.0> but was:<NaN>)
- **Committed in:** `ec576b9` (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Bug fix was necessary for correct MultiPoint/Multi* bounding box calculation. No scope creep.

## Issues Encountered

1. **JUnit version mismatch** - Project uses JUnit 4 (4.13.2), not JUnit 5. Initially wrote tests with JUnit 5 imports (`org.junit.jupiter.api`). Fixed by using JUnit 4 imports (`org.junit.Test`, `org.junit.Assert.*`).

2. **Kotlin nullable Double with JUnit assertEquals** - JUnit 4's `assertEquals(expected, actual, delta)` requires non-null doubles. Tests using `propertyAs<Double>()` or `doubleProperty()` returned `Double?`. Fixed by using non-null assertion operator (`!!`) or destructuring.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

**Ready for Phase 01 Plan 02:** GeoJSON reader implementation

The core data model is complete and tested:
- ✅ Bounds supports spatial operations needed for filtering
- ✅ Geometry hierarchy handles all GeoJSON geometry types
- ✅ Feature combines geometry with properties
- ✅ GeoSource provides the interface that GeoJsonSource will implement

**Potential concerns:** None. All foundational types are solid.

---
*Phase: 01-data-layer*
*Plan: 01-01*
*Completed: 2026-02-21*
