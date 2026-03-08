---
status: resolved
trigger: "Geometry normalization utilities not plumbed into load>project>render pipeline"
created: 2026-03-08T00:00:00Z
updated: 2026-03-08T00:00:00Z
---

## Current Focus

hypothesis: Geometry normalization utilities exist but are never called in the rendering pipeline
test: Search for normalization function calls in main source code
expecting: No calls found outside of test files
next_action: Document root cause and integration points

## Symptoms

expected: Geometry normalization runs (antimeridian splitting, winding normalization, ring validation) when loading/rendering ocean.geojson
actual: Normalization utilities exist but are never invoked; ocean data fails to render due to unnormalized geometries crossing antimeridian and extending beyond Mercator latitude bounds
errors: No errors in utilities themselves - they're just not being called
reproduction: Run examples/render/04-multipolygons.kt - ocean data will not render correctly
started: Utilities implemented in plans 16-01 through 16-03, but integration was never completed

## Eliminated

- hypothesis: Normalization is happening at render time in DrawerGeoExtensions.kt
  evidence: Searched DrawerGeoExtensions.kt - no calls to normalizePolygon, normalizeMultiPolygon, or .normalized() extension functions
  timestamp: 2026-03-08

- hypothesis: Normalization is happening at load time in GeoJSON.load()
  evidence: Searched GeoJSON.kt - parsePolygon, parseMultiPolygon return raw parsed geometries without normalization
  timestamp: 2026-03-08

- hypothesis: Normalization is happening in loadGeo()
  evidence: loadGeo.kt simply wraps GeoJSON.loadString() and creates CachedGeoSource - no normalization step
  timestamp: 2026-03-08

- hypothesis: Normalization happens in MultiRenderer.kt
  evidence: drawMultiPolygon() directly renders polygons without normalization - only clamps coordinates to Mercator bounds
  timestamp: 2026-03-08

## Evidence

- timestamp: 2026-03-08
  checked: src/main/kotlin/geo/render/geometry/GeometryNormalizer.kt
  found: normalizePolygon() and normalizeMultiPolygon() functions defined but only called from within same file
  implication: These functions are never invoked from the actual rendering pipeline

- timestamp: 2026-03-08
  checked: grep for "normalizePolygon|normalizeMultiPolygon|\.normalized(" in src/main/
  found: Only matches are in GeometryNormalizer.kt itself (lines 20, 52, 65, 72)
  implication: ZERO invocations from rendering or loading code paths

- timestamp: 2026-03-08
  checked: examples/render/04-multipolygons.kt comments
  found: Line 44 has TODO: "Ocean will not render as when projection applied the latitudes in ocean data cause it to explode"
  implication: Known issue that normalization was supposed to fix

- timestamp: 2026-03-08
  checked: .planning/phases/16-rendering-improvements/16-CONTEXT.md
  found: Explicit decision on lines 25-30: "Normalize at load time, not render time"
  implication: Design decision was made but implementation was never completed

- timestamp: 2026-03-08
  checked: GeoJSON.kt parsePolygon() and parseMultiPolygon() functions (lines 259-279, 318-341)
  found: Raw coordinate parsing without any normalization step
  implication: Integration point at load time is missing

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt renderToDrawer() function (lines 356-389)
  found: Direct rendering without normalization
  implication: Alternative integration point at render time exists but is also missing

## Resolution

root_cause: Geometry normalization utilities were fully implemented (AntimeridianSplitter, WindingNormalizer, RingValidator, GeometryNormalizer) in plans 16-01 through 16-03, but were never integrated into the actual data loading or rendering pipeline. The CONTEXT.md document explicitly decided "Normalize at load time, not render time" (lines 25-30), but this integration was never completed.

fix: Not yet applied - requires integration at one of the identified points

verification: Not yet verified

files_changed: []

## Integration Points Identified

### Primary Integration Point (Per CONTEXT.md Decision)
File: `src/main/kotlin/geo/GeoJSON.kt`
Functions: `parsePolygon()` (line 259) and `parseMultiPolygon()` (line 318)
Action: After parsing raw coordinates, call `normalizePolygon()` and return the normalized result
Code needed:
  - Import: `import geo.render.geometry.normalizePolygon`
  - In parsePolygon(): Return `normalizePolygon(Polygon(exterior, interiors)).firstOrNull() ?: Polygon(exterior, interiors)`
  - In parseMultiPolygon(): Return `MultiPolygon(polygons.flatMap { normalizePolygon(it) })`

### Alternative Integration Point
File: `src/main/kotlin/geo/loadGeo.kt`
Function: `loadGeoJSONWithCRS()` (line 77)
Action: Normalize geometries after loading but before creating CachedGeoSource
Code needed:
  - Import normalization functions
  - Transform the GeoSource features to normalize Polygon and MultiPolygon geometries

### Secondary Integration Point
File: `src/main/kotlin/geo/render/DrawerGeoExtensions.kt`
Function: `Geometry.renderToDrawer()` (line 356)
Action: Normalize before rendering (though CONTEXT.md decided against render-time normalization)

### Render Path for MultiPolygon
File: `src/main/kotlin/geo/render/MultiRenderer.kt`
Function: `drawMultiPolygon()` (line 148)
Note: Already handles Mercator bounds clamping but doesn't call the full normalization pipeline

## Specific Issues with Ocean Data

The ocean.geojson test data has two characteristics that require normalization:
1. Crosses antimeridian (±180° longitude) - requires AntimeridianSplitter
2. Extends to ±90° latitude - requires coordinate clamping or special handling

Without normalization:
- Mercator projection fails with latitudes outside ±85.05112878° range
- Antimeridian crossing causes coordinates to jump from +180 to -180
- Ring winding order may be inconsistent

## Files That Need Changes

1. **src/main/kotlin/geo/GeoJSON.kt** - Primary integration point (load-time normalization)
   - Add imports for normalization functions
   - Modify parsePolygon() to normalize
   - Modify parseMultiPolygon() to normalize

2. **src/main/kotlin/geo/render/MultiRenderer.kt** - Already has partial handling
   - Currently only clamps coordinates to Mercator bounds
   - Should optionally call full normalization if not done at load time

3. **examples/render/04-multipolygons.kt** - Can remove TODO once fixed
   - Line 44 TODO can be resolved once normalization is integrated
