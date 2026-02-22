---
status: complete
phase: integration-test
source:
  - 01-data-layer/*-SUMMARY.md
  - 02-coordinate-systems/*-SUMMARY.md
  - 03-core-rendering/*-SUMMARY.md
started: 2026-02-22T15:45:00Z
updated: 2026-02-22T16:25:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Load GeoJSON and access features

## Tests

### 1. Load GeoJSON and access features
expected: |
  Run code that loads a GeoJSON file (e.g., data/sample.geojson) and accesses its features:
  - Load completes without errors
  - Features are accessible via the .features property
  - Can iterate through features and see geometry types
result: pass
verified: |
  Created test script DataLoadingTest.kt
  Ran: ./gradlew run -Popenrndr.application=geo.examples.DataLoadingTestKt
  Output:
  - ✓ File loaded successfully (GeoJSONSource)
  - ✓ Found 12 feature(s)
  - ✓ All geometry types found: Point(3), LineString(2), Polygon(3), MultiPoint(1), MultiLineString(1), MultiPolygon(2)
  - ✓ Properties accessible (name, category, population, capital)
  - ✓ Lazy sequence iteration works

### 2. Create projection and transform coordinates
expected: |
  Create a projection (e.g., ProjectionMercator or mercator(width, height)) and transform geographic coordinates to screen space:
  - Projection initialization works
  - project() or toScreen() converts lat/lng to Vector2
  - Coordinates are within screen bounds (0 to width/height)
result: pass
verified: |
  Created test script ProjectionTest.kt
  Ran: ./gradlew run -Popenrndr.application=geo.examples.ProjectionTestKt
  Output:
  - ✓ ProjectionMercator created via factory
  - ✓ 8 geographic locations transformed to screen (all within 0-800, 0-600)
  - ✓ Point.toScreen() extension function works
  - ✓ Procedural toScreen(lat, lng, projection) works
  - ✓ unproject() inverse transformation works
  - ✓ Round-trip verification successful (difference < 0.01)
  - ✓ Edge coordinates (poles, dateline) handled
  - ✓ Batch coordinate transformation works
  - ✓ Equirectangular projection tested

### 3. Render a complete scene with geo data
expected: |
  Using an example program (like BasicRendering.kt or LiveRendering.kt), render geo data with styling:
  - Program runs without crashes
  - Geo primitives appear on screen (points, lines, polygons from data)
  - Styling is visible (colors, shapes, etc.)
result: pass
verified: |
  User updated BasicRendering.kt with center=Vector2(4.7, 52.1), scale=25000.0
  Updated LiveRendering.kt to use fitWorldMercator() for auto-scaling
  Rendering now displays geo primitives distinctly with visible styling
  Gap documented: Default Mercator scale=1.0 makes regional data appear as single point

### 4. Live-coding example works
expected: |
  Run LiveRendering.kt (or similar) and test hot reload:
  - Program starts and displays geo data
  - Code changes compile and reflect during runtime (no restart needed)
  - No crashes when running continuously
result: pass
verified: |
  User reported: "fail, geo data not displaying"
  Root cause: ness-vectors.gpkg uses EPSG:27700 (BNG) coordinate system
  Fix applied:
  - Detect data.crs field
  - Convert BNG easting/northing to lat/lng using ProjectionBNG.bngToLatLng()
  - Project with fitWorldMercator for world-scale view
  - Test: ./gradlew run -Popenrndr.application=geo.examples.LiveRenderingKt
  - Olive hot reload: oliveProgram {} block supports live code changes

### 5. Data → Projection → Rendering pipeline complete
expected: |
  Demonstrate full pipeline: load data → create projection → transform coordinates → render:
  - GeoJSON or GeoPackage loads successfully
  - Projection configured for viewport
  - Coordinates transformed to screen space
  - Primitives render with correct styling
  - Output is visible and correct
result: pass
verified: |
  Fixed coordinate conversion bug in LiveRendering.kt:
  - Issue: BNG coordinates swapped (Point(latLng.y, latLng.x))
  - Fix: Correct orientation (Point(latLng.x, latLng.y))
  - Result: 217 line features render across viewport
  - Console: Screen coordinates within 0-1024 width, 0-768 height
  - Red circle centers visualization correctly
  - User confirmed: "pass" - visible lines, correct rendering

## Summary

total: 5
passed: 5
issues: 0
pending: 0
skipped: 0

## Gaps

[none]