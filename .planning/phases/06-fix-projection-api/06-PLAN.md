---
phase: 6
name: Fix projection errors and API design
goal: Fix projection scaling/fitBounds API and simplify data overlay workflows for confidence in multi-dataset alignment
depends_on: v1.0.0 foundation
plans: 5
---

# Phase 6: Fix projection errors and API design

## Overview

This phase addresses critical friction discovered during real-world v1.0.0 usage. The API has rough edges that make common workflows unnecessarily difficult, particularly for users who want to quickly "load data → put it on screen → muck around with it visually."

## Phase Goal

Fix projection scaling/fitBounds API and simplify data overlay workflows for confidence in multi-dataset alignment.

## Plans

### Plan 1: Fix projection scaling and fitBounds API

**Objective:** Make projection fitting intuitive and reliable for creative coding workflows

**Key Problems:**
- `scale` parameter is confusing (defaults to 10.0, acts like zoom level, not 0-1 scale)
- `fitWorldMercator()` marked as "TODO Broken"
- `fitBounds()` at 50% zoom breaks — "stuff in right scale but lines fuck up at poles"
- Data clamping in Mercator causes issues for whole-world visualization

**Research-Based Implementation:**

From 06-RESEARCH.md, the fitBounds algorithm follows this 7-step process:
1. Calculate padded viewport size
2. Project bbox corners (SW, NE) to determine projected bounds
3. Calculate scale factors for both dimensions
4. Use **minimum scale** (contain strategy) to ensure entire bbox fits
5. Calculate center point in projected coordinates
6. Calculate viewport center in screen coordinates
7. Calculate translation to center the projection

**Critical Constants:**
- Mercator latitude limit: ±85.0511287798066° (MAX_MERCATOR_LAT)
- Formula: `tan(π/4 + φ/2)` where φ = 85.05112878° ≈ π
- Zoom/scale conversion: `scale = 256 * 2^zoom` (standard tile pyramid math)

**Deliverables:**
1. **Three-variant API** (per research Section 6.1):
   - `projection.fit(bbox, padding=20)` — mutates in-place
   - `projection.fitted(bbox, padding=20)` — returns new instance
   - `projection.fitParameters(bbox, padding=20)` — returns TransformParameters for animation

2. **Replace confusing scale parameter** with clear semantics:
   - Option A: `zoomLevel` (0=world, 5=country, 10=city scale)
   - Option B: `metersPerPixel` (physical scale)
   - Remove arbitrary `scale = 10.0` default

3. **Implement fitBounds() with contain strategy** (per research Section 5.1):
   ```kotlin
   val scaleX = paddedWidth / (ne.x - sw.x)
   val scaleY = paddedHeight / (ne.y - sw.y)
   val scale = min(scaleX, scaleY)  // Contain: never crop
   ```

4. **Add latitude clamping** to Mercator projection (per research Section 3.2):
   ```kotlin
   val clampedLat = lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
   ```

5. **Support pixel-based padding**: `fit(bbox, padding=20)`

6. **Add site vs data bounds distinction** (per research Section 4):
   - `fitTo(siteBounds)` — fit to user's area of interest
   - `fit(dataBounds)` — fit to data container bounds

**Files to Modify:**
- `src/main/kotlin/geo/projection/ProjectionConfig.kt` — refactor scale parameter
- `src/main/kotlin/geo/projection/ProjectionFactory.kt` — add fitBounds variants
- `src/main/kotlin/geo/projection/ProjectionMercator.kt` — add latitude clamping
- `src/main/kotlin/geo/Bounds.kt` — add worldMercator() helper

**Tests:**
- Unit tests for fitBounds with various bbox sizes
- Unit tests for zoom/scale conversion formulas
- Integration test with whole-world ocean data
- Edge case: data at Mercator pole limits (±85.05112878°)
- Edge case: degenerate bbox (zero area)
- Edge case: dateline crossing (170° to -170°)

---

### Plan 2: Fix MultiPolygon rendering for ocean/whole-world data

**Objective:** Handle geometries that span projection bounds gracefully

**Key Problems:**
- MultiPolygon fails when data spans projection bounds (e.g., ocean data covers beyond min/max lat/lng)
- User comment: "TODO fails on ocean as it covers beyond min max lat longs"
- Current rendering approach doesn't handle coordinate wrapping or projection overflow
- "Lines fuck up at poles" — artifacts when projecting coordinates near ±90°

**Research-Based Implementation:**

From 06-RESEARCH.md Section 7.3, pole artifacts occur when:
1. Geometry includes latitudes > ±85.05112878°
2. Projection attempts to project these to infinity
3. Line segments to/from infinity create visual artifacts

**Solution: Pre-projection coordinate clamping**

```kotlin
fun clampGeometryToMercator(geometry: Geometry): Geometry {
    return when (geometry) {
        is Point -> Point(
            lng = geometry.lng,
            lat = geometry.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        )
        is Polygon -> Polygon(
            exterior = geometry.exterior.map { 
                Point(it.lng, it.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            },
            holes = geometry.holes.map { ring ->
                ring.map { Point(it.lng, it.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)) }
            }
        )
        // ... etc for other geometry types
    }
}
```

**Deliverables:**
1. **Add coordinate validation** (per research Section 7.3):
   ```kotlin
   fun validateMercatorBounds(geometry: Geometry): Boolean {
       return geometry.allCoordinates { coord ->
           coord.latitude in -85.05112878..85.05112878
       }
   }
   ```

2. **Implement safe projection** with automatic clamping:
   ```kotlin
   fun projectSafe(geometry: Geometry, projection: MercatorProjection): Geometry {
       return if (validateMercatorBounds(geometry)) {
           projection.project(geometry)
       } else {
           projection.project(clampGeometryToMercator(geometry))
       }
   }
   ```

3. **Fix MultiPolygon rendering** in MultiRenderer.kt:
   - Current: `polygon.exteriorToScreen(projection)` doesn't handle clamping
   - Fix: Apply clamping before projection for all coordinates
   - Add support for interior rings (holes)

4. **Handle dateline wrapping** (per research Section 7.2):
   ```kotlin
   fun normalizeLongitude(lng: Double): Double {
       return ((lng + 180) % 360 + 360) % 360 - 180
   }
   ```

5. **Geometry clipping for edge cases**:
   - Split geometries that span >180° longitude
   - Handle Pacific Ocean data properly

**Files to Modify:**
- `src/main/kotlin/geo/render/MultiRenderer.kt` (lines 132-141) — add clamping
- `src/main/kotlin/geo/projection/ProjectionMercator.kt` — add safeProject methods
- `src/main/kotlin/geo/Geometry.kt` — add clampGeometryToMercator() extension

**Tests:**
- Render ocean MultiPolygon without crash
- Verify coordinates >85.05112878° are clamped, not crashed
- Verify hole rendering in complex polygons
- Test edge cases: dateline crossing (170° to -170°)
- Test edge cases: pole touching (latitude ±90°)
- Test edge cases: whole-world bbox (-180,-85 to 180,85)

---

### Plan 3: Reduce API boilerplate for common rendering workflows

**Objective:** Enable "load → visualize" in minimal lines of code

**Key Problems:**
- Too much setup code before seeing visual output
- Recent partial fix: "feeding projection config into draw functions" needs completion
- Users want: `GeoJSON.load().render(projection, drawer)` simplicity

**Deliverables:**
1. Drawer extension: `drawer.geoJSON(data, projection)` for one-line rendering
2. Auto-create default projection if none provided (fit to view)
3. Single-import convenience: `import geo.*` gets everything
4. `readyToDraw` mode for GeoSource (auto-fit, auto-project)
5. Cached screen-space coordinates to avoid per-frame reprojection

**API Design:**
```kotlin
// Tier 1: Absolute simplest
extend {
    drawer.geoJSON("world.json")  // Auto-load, auto-fit, auto-render
}

// Tier 2: Load once, draw many
val source = geoSource("data.json")  // With auto-detection
extend {
    source.render(drawer)
}

// Tier 3: Full control (existing API preserved)
val features = GeoJSON.load(File("data.json"))
val projection = ProjectionMercator { width = 800; height = 600 }
extend {
    features.forEach { drawer.draw(it.geometry, projection) }
}
```

**Files to Modify:**
- `src/main/kotlin/geo/render/` (new convenience extensions)
- `src/main/kotlin/geo/GeoSource.kt`
- `src/main/kotlin/geo/ProjectionExtensions.kt` (new)

**Tests:**
- Test all three API tiers work
- Verify caching improves performance (>50% reduction for 1000+ features)

---

### Plan 4: Simplify CRS handling API

**Objective:** Make coordinate system work invisible for common cases

**Key Problems:**
- Manual CRS detection/conversion is painful for multi-dataset overlays
- Users shouldn't need to know EPSG codes for standard datasets
- Transform chains are verbose: `features.transform(to=CRS.WebMercator).materialize()`

**Deliverables:**
1. Strongly typed CRS enum: `CRS.WGS84`, `CRS.WebMercator`, `CRS.BritishNationalGrid`
2. Auto-detect CRS at load time with `geoSource()` function
3. Single transform method: `geoSource.transform(to = CRS.WebMercator)`
4. Auto-unify CRS for `geoStack()` (multi-dataset overlays)
5. Fallback to WGS84 with warning when detection fails

**API Design:**
```kotlin
// Auto-detection
val source = geoSource("file.json")  // Detects CRS from file

// Explicit transform
val transformed = source.transform(to = CRS.WebMercator)

// Multi-dataset stack with auto-unification
val map = geoStack(coastline, cities, rivers)  // All unified to first CRS
map.fit(viewBounds)
map.render(drawer)
```

**Caching Strategy:**
- < 1000 points: Cache screen coordinates, recompute on projection change
- > 1000 points: Spatial indexing + view culling
- `materialize()` for eager evaluation when needed

**Files to Modify:**
- `src/main/kotlin/geo/crs/CRS.kt` (new enum)
- `src/main/kotlin/geo/crs/CRSTransformer.kt`
- `src/main/kotlin/geo/GeoSource.kt`
- `src/main/kotlin/geo/GeoStack.kt` (new)

**Tests:**
- CRS detection from various file formats
- Transform accuracy (within 1m for same CRS)
- Stack unification works across different CRS

---

### Plan 5: Integration and regression testing

**Objective:** Ensure all fixes work together and don't break existing functionality

**Deliverables:**
1. Comprehensive integration test with real datasets:
   - UK coastline (BNG)
   - Global cities (WGS84)
   - Ocean data (whole-world MultiPolygon)
2. Performance benchmarks: verify caching improves rendering speed
3. Example programs demonstrating each API tier
4. Migration guide for v1.0.0 → v1.1.0 API changes

**Test Scenarios:**
- Load UK coastline + global cities + ocean data
- Apply fitBounds to specific site (not whole UK)
- Render with unified CRS and cached coordinates
- Verify no clamping artifacts at poles

**Example Programs:**
- `examples/api_tier1_simple.kt` — One-liner rendering
- `examples/api_tier2_source.kt` — Load-once, draw-many
- `examples/api_tier3_full_control.kt` — Existing API preserved
- `examples/multi_dataset_overlay.kt` — CRS unification demo
- `examples/ocean_multipolygon.kt` — Whole-world data handling

**Files to Create/Modify:**
- `src/test/kotlin/geo/integration/Phase6IntegrationTest.kt`
- `src/main/kotlin/geo/examples/` (new examples)
- `MIGRATION-v1.0-to-v1.1.md`

---

## Wave Structure

| Wave | Plans | Focus |
|------|-------|-------|
| 1 | 1, 2 | Core projection fixes (scaling, fitBounds, MultiPolygon) |
| 2 | 3 | API boilerplate reduction |
| 3 | 4 | CRS simplification |
| 4 | 5 | Integration, testing, examples |

## Dependencies

- **Wave 1** can start immediately (no dependencies)
- **Wave 2** depends on Wave 1 completion (projection fixes needed for caching)
- **Wave 3** depends on Wave 1 (CRS needs working projection)
- **Wave 4** depends on all prior waves

## Success Criteria

- [ ] All 5 plans completed
- [ ] 150+ tests passing (including 20+ new Phase 6 tests)
- [ ] Example programs run without modification
- [ ] No breaking changes to existing v1.0.0 APIs (only additions)
- [ ] Performance: 50%+ faster rendering for repeated draws of same dataset
- [ ] Documentation: API tier guide + migration guide complete

## Notes

**Design Philosophy:**
- Tiered API: Simple for beginners, powerful for experts
- "A little magic, with a trace of warning" — auto-detection with logging
- Preserve all v1.0.0 APIs — only additive changes

**Performance Goals:**
- Cached screen coordinates: O(1) per geometry after initial projection
- Spatial indexing: O(log n) for view culling with large datasets

**Deferred to v1.2.0:**
- Real-time pan/zoom (requires different architecture)
- Level-of-detail rendering (zoom-based symbology)
- Advanced spatial operations (joins, overlays)

---

*Phase planned: 2026-02-25*
*Target completion: v1.1.0 milestone*
