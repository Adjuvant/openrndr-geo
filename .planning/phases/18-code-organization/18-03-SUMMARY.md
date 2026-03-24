---
phase: 18-code-organization
plan: 03
subsystem: infrastructure
tags: [kotlin, import-migration, geo-core, refactoring]

# Dependency graph
requires:
  - phase: 18-code-organization
    plan: 02
    provides: "13 geo root files moved to geo.core/ subdirectory with updated package declarations"
provides:
  - "All imports updated from geo.X to geo.core.X across entire codebase"
  - "Wildcard imports updated from geo.* to geo.core.*"
  - "Qualified references updated (geo.Type to geo.core.Type)"
affects:
  - Phase 19 (any feature work requiring compilation)
  - All future plans using geo.core types

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Hard break migration without backward compatibility shims"
    - "Batch import replacement using sed/perl regex"

key-files:
  modified:
    - "src/main/kotlin/geo/animation/*.kt - animation package imports"
    - "src/main/kotlin/geo/internal/**/*.kt - internal package imports"
    - "src/main/kotlin/geo/layer/*.kt - layer package imports"
    - "src/main/kotlin/geo/projection/*.kt - projection package imports"
    - "src/main/kotlin/geo/render/**/*.kt - render package imports"
    - "src/main/kotlin/geo/tools/SyntheticData.kt - tools package imports"
    - "examples/**/*.kt - example file imports"
    - "src/test/kotlin/geo/**/*.kt - test file imports"
    - "uat/*.kt - UAT file imports"

key-decisions:
  - "Extended migration scope beyond plan: updated wildcard imports (geo.* → geo.core.*) and qualified references (geo.Type → geo.core.Type) in addition to explicit imports"
  - "Preserved geo.projection package path (not moved to geo.core) - projection types remain in geo.projection"

patterns-established:
  - "After moving types to subpackage, update ALL import forms: explicit, wildcard, and qualified references"

requirements-completed:
  - ORG-03

# Metrics
duration: 15min
completed: 2026-03-24
---

# Phase 18-03: Import Migration to geo.core Summary

**Updated all imports from geo.X to geo.core.X across codebase after ORG-02 structural change**

## Performance

- **Duration:** 15 min
- **Started:** 2026-03-24T22:04:58Z
- **Completed:** 2026-03-24T22:20:00Z
- **Tasks:** 3 (import migration executed as batch operations)
- **Files modified:** 77

## Accomplishments
- Updated explicit imports: geo.Bounds → geo.core.Bounds, etc. for 13 core types
- Updated wildcard imports: geo.* → geo.core.* in 30 files
- Updated qualified references: geo.Point → geo.core.Point, etc. for geometry types
- Fixed mistaken geo.projection → geo.core.projection back to geo.projection
- Fixed geo.render extension function imports (geoSource, geoStack, forEachWithProjection)

## task Commits

Single commit for entire import migration:

1. **Import migration (18-03)** - `814aa6e` (refactor)
   - Updated 77 files across src/main, src/test, examples, uat
   - All forms of import references updated

## Files Created/Modified

**Core library (src/main/kotlin/geo/):**
- `geo/animation/FeatureAnimator.kt` - Feature import updated
- `geo/animation/ProceduralMotion.kt` - Feature import updated
- `geo/internal/OptimizedGeoSource.kt` - Bounds, Feature, GeoSource imports updated
- `geo/internal/cache/CacheKey.kt` - Geometry import updated
- `geo/internal/cache/ViewportCache.kt` - Geometry import updated
- `geo/internal/geometry/OptimizedGeometries.kt` - Bounds import updated
- `geo/layer/GeoLayer.kt` - Bounds, GeoSource imports updated
- `geo/layer/Graticule.kt` - Bounds, Feature, GeoSource imports updated
- `geo/projection/CRSExtensions.kt` - GeoSource import updated
- `geo/projection/ProjectionConfig.kt` - Bounds import updated
- `geo/projection/ProjectionFactory.kt` - Bounds import updated
- `geo/projection/ProjectionMercator.kt` - Bounds import updated
- `geo/projection/package.kt` - Bounds reference updated
- `geo/render/DrawerGeoExtensions.kt` - Bounds, Feature, GeoJSON, GeoSource, Geometry imports updated
- `geo/render/GeoRenderConfig.kt` - Feature import updated
- `geo/render/MultiRenderer.kt` - Geometry imports updated
- `geo/render/StyleDefaults.kt` - Geometry, Point, LineString, Polygon, etc. qualified refs updated
- `geo/render/geometry/GeometryNormalizer.kt` - Polygon, MultiPolygon imports updated
- `geo/render/geometry/RingValidator.kt` - Bounds import updated
- `geo/render/package.kt` - Feature reference updated
- `geo/render/render.kt` - Geometry imports updated
- `geo/tools/SyntheticData.kt` - geo.* wildcard import updated
- `geo/core/GeoStack.kt` - Geometry type references updated

**Test files:**
- 16 test files updated with corrected imports

**Example files:**
- 19 example files updated with corrected imports

## Decisions Made

- **Extended migration scope:** Plan specified only explicit imports, but found wildcard imports (`import geo.*`) and qualified references (`geo.Type`) also needed updating. Handled automatically under Rule 2 (missing critical functionality).
- **Preserved geo.projection:** The projection package types (GeoProjection, ProjectionFactory, etc.) remain in `geo.projection` - they were never moved to `geo.core`. Fixed mistaken replacements.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Wildcard imports not covered by explicit import list**
- **Found during:** task 1 (initial import migration)
- **Issue:** 30 files used `import geo.*` which doesn't include `geo.core.*` types
- **Fix:** Updated all `import geo.*` to `import geo.core.*`
- **Files modified:** 30 files across src/main, src/test, examples, uat
- **Verification:** No `import geo.*` patterns remain
- **Committed in:** 814aa6e (task commit)

**2. [Rule 2 - Missing Critical] Qualified references also needed updating**
- **Found during:** task 1 (verification after import fix)
- **Issue:** Code using qualified names like `geo.Point`, `geo.Geometry` instead of imports
- **Fix:** Updated all qualified references to `geo.core.Point`, `geo.core.Geometry`, etc.
- **Files modified:** Multiple files in geo.render, geo.core, tests
- **Verification:** Compilation errors reduced significantly
- **Committed in:** 814aa6e (task commit)

**3. [Rule 3 - Blocking] Mistaken replacement of geo.projection**
- **Found during:** task 1 (compilation check)
- **Issue:** sed replaced `geo.projection` with `geo.core.projection` which is wrong
- **Fix:** Reverted `geo.core.projection.*` back to `geo.projection.*`
- **Files modified:** 54 files in geo.render, tests, examples
- **Verification:** Projection package imports correct
- **Committed in:** 814aa6e (task commit)

**4. [Rule 2 - Missing Critical] Extension function imports incorrect**
- **Found during:** task 1 (compilation check)
- **Issue:** geoSource, geoStack, forEachWithProjection are in geo.render package, not geo package directly
- **Fix:** Updated imports to `geo.render.geoSource`, `geo.render.forEachWithProjection`, etc.
- **Files modified:** TemplateProgram.kt, 01-synthetic-data-gen.kt, 06-quick-geo.kt
- **Verification:** Extension function imports now correct
- **Committed in:** 814aa6e (task commit)

---

**Total deviations:** 4 auto-fixed (3 missing critical, 1 blocking)
**Impact on plan:** All auto-fixes necessary for compilation. Extended scope to handle wildcard and qualified references which were implied by the hard break decision in ORG-02.

## Issues Encountered

**Pre-existing API issues in TemplateProgram.kt and examples:**
- TemplateProgram.kt uses functions that don't exist or have wrong signatures: `geoStack`, `totalBoundingBox`, `forEachWithProjection` (on Drawer), `withProjection`, `filter`, `propertyKeys`, `property`
- examples/tools/01-synthetic-data-gen.kt calls `geoSource()` as standalone function instead of `drawer.geoSource()`
- These are pre-existing issues NOT caused by import migration - the APIs used don't exist in current codebase

**Core library status:**
- src/main/kotlin/geo/ compiles without errors
- src/test/kotlin/geo/ compiles without errors
- Only TemplateProgram.kt and example files have compilation issues

## Next Phase Readiness

- Core library compiles successfully - ready for feature development
- ORG-03 requirement complete: All imports updated from geo.X to geo.core.X
- Note: TemplateProgram.kt and some examples have pre-existing API usage issues that may need fixing before use

---
*Phase: 18-code-organization*
*Completed: 2026-03-24*
