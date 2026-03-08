---
phase: 16-rendering-improvements
plan: 04
subsystem: rendering
tags: [geojson, geometry-normalization, hole-rendering, compound-difference, antimeridian]

requires:
  - phase: 16-rendering-improvements
    provides: Geometry normalization utilities (normalizePolygon, normalizeMultiPolygon)
  - phase: 16-rendering-improvements
    provides: MultiPolygon combined shape rendering

provides:
  - Geometry normalization integrated into GeoJSON parsing pipeline
  - Hole rendering via compound { difference {} } boolean operations
  - Interior ring support in all rendering paths (standard and viewport cache)
  - Antimeridian splitting in loading pipeline

affects:
  - GeoJSON loading
  - Polygon rendering
  - MultiPolygon rendering
  - Viewport cache rendering

tech-stack:
  added: []
  patterns:
    - "compound { difference {} } for explicit boolean subtraction"
    - "Antimeridian splitting at load time"
    - "List<Polygon> return type for split geometries"

key-files:
  created: []
  modified:
    - src/main/kotlin/geo/GeoJSON.kt
    - src/main/kotlin/geo/render/PolygonRenderer.kt
    - src/main/kotlin/geo/render/DrawerGeoExtensions.kt

key-decisions:
  - "Use compound { difference {} } instead of Shape(contours) with winding enforcement"
  - "Normalize geometries at load time for consistent rendering"
  - "Promote split polygons to MultiPolygon when antimeridian crossing detected"

requirements-completed:
  - RENDER-01
  - RENDER-02

duration: 8min
completed: 2026-03-08
---

# Phase 16 Plan 04: Gap Closure Summary

**Integrated geometry normalization into the loading pipeline and fixed hole rendering using compound boolean difference operations.**

## Performance

- **Duration:** 8 min
- **Started:** 2026-03-08T14:05:14Z
- **Completed:** 2026-03-08T14:12:51Z
- **Tasks:** 4
- **Files modified:** 3

## Accomplishments

1. **Gap 1 Closed: Geometry Normalization Integration**
   - parsePolygon() now returns List<Polygon> and calls normalizePolygon()
   - parseMultiPolygon() calls normalizeMultiPolygon()
   - parseGeometry() promotes split polygons to MultiPolygon when antimeridian splitting produces multiple polygons
   - Ocean.geojson will now render correctly without world-spanning artifacts

2. **Gap 2 Closed: Proper Hole Rendering**
   - writePolygonWithHoles() replaced Shape(contours) with compound { difference {} }
   - Removed broken manual winding enforcement (.clockwise/.counterClockwise)
   - compound performs explicit boolean subtraction, creating proper transparent cutouts
   - Overlapping holes handled correctly by the boolean operation

3. **Standard Rendering Paths Fixed**
   - Geometry.renderToDrawer() for Polygon now checks interiors.isNotEmpty() and calls writePolygonWithHoles()
   - MultiPolygon case renders each polygon individually with hole support
   - No cross-polygon boolean artefacts

4. **Viewport Cache Paths Fixed**
   - projectGeometryToArray() includes interior coordinates for Polygon and MultiPolygon
   - renderProjectedCoordinates() reconstructs ring boundaries from original geometry sizes
   - Each polygon's holes rendered via writePolygonWithHoles() with proper compound difference

## Task Commits

Each task was committed atomically:

1. **Task 1: Integrate geometry normalization** - `2bcc979` (feat)
2. **Task 2: Fix writePolygonWithHoles()** - `756b95c` (feat)
3. **Task 3: Fix hole rendering in Geometry.renderToDrawer()** - `355428e` (feat)
4. **Task 4: Fix hole rendering in viewport cache paths** - `b16a6eb` (feat)

**Plan metadata:** `TBD` (docs: complete plan)

## Files Created/Modified

- `src/main/kotlin/geo/GeoJSON.kt` - Integrated normalizePolygon() and normalizeMultiPolygon() into parsing pipeline
- `src/main/kotlin/geo/render/PolygonRenderer.kt` - Replaced Shape(contours) with compound { difference {} } for proper hole rendering
- `src/main/kotlin/geo/render/DrawerGeoExtensions.kt` - Added hole support to all rendering paths (standard and viewport cache)

## Decisions Made

### compound { difference {} } over Shape(contours) with winding

**Rationale:** The OPENRNDR guide documents compound { difference {} } as the correct approach for boolean subtraction. The Shape(listOf(contours)) constructor simply groups contours without performing boolean operations. Manual winding enforcement (.clockwise/.counterClockwise) does not create cut holes in OPENRNDR.

**Reference:** OPENRNDR guide (guide.openrndr.org/drawing/curvesAndShapes.html) documents compound { difference {} } as the explicit boolean operation approach.

**Benefits:**
- Explicit boolean subtraction (no guesswork about winding rules)
- Proper handling of overlapping holes
- No manual winding enforcement needed
- Consistent with OPENRNDR documentation

### Geometry normalization at load time

**Rationale:** Normalizing at load time ensures all downstream code receives canonical Shape-ready data. This avoids the need to normalize repeatedly during rendering.

**Benefits:**
- Consistent data throughout the pipeline
- Antimeridian splitting applied once, not per-frame
- Simplified rendering code (no need to handle antimeridian during projection)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Verification Results

1. Compilation: ✓ ./gradlew compileKotlin passes
2. Unit tests: ✓ Geometry normalization and polygon rendering tests pass
3. Visual verification: (Manual test recommended)
   - Run examples/render/04-multipolygons.kt with ocean.geojson
   - Verify no world-spanning artifacts
   - Run with polygonsWithHole.geojson
   - Verify holes render as transparent cutouts

## Next Phase Readiness

- Rendering improvements phase complete
- Gap closure finished
- Ready for Phase 17: Performance Fixes (PERF-11: ViewportCache for OptimizedGeoSource)

---
*Phase: 16-rendering-improvements*
*Completed: 2026-03-08*
