---
status: diagnosed
phase: 02-coordinate-systems
source: [02-01-SUMMARY.md, 02-02-SUMMARY.md, 02-03-SUMMARY.md]
started: 2026-02-21T17:00:00Z
updated: 2026-02-21T17:08:00Z
---

## Current Test

number: 1
name: Mercator projection transforms lat/lng to screen coordinates
expected: |
  Create a ProjectionMercator with width/height, call project(lat, lng), receive Vector2 screen coordinates. London (51.5, -0.1) maps to a reasonable screen position.
awaiting: user response

## Tests

### 1. Mercator Projection
expected: Create ProjectionMercator with width/height, project(51.5, -0.1) returns Vector2 screen coordinates for London.
result: issue
reported: "test script - ./gradlew test failed with compilation errors"
severity: blocker

### 2. Equirectangular Projection
expected: Create ProjectionEquirectangular with DSL syntax, project() works for basic world map coordinates.
result: pending

### 3. BNG Coordinate Transformation
expected: ProjectionBNG transforms WGS84 (lat/lng) to British National Grid (easting/northing) and back with ~3-5m accuracy.
result: pending

### 4. BNG Bounds Validation
expected: Projecting coordinates outside UK bounds (e.g., Paris) throws AccuracyWarningException with clear message.
result: pending

### 5. ProjectionFactory Presets
expected: ProjectionFactory.mercator(800, 600) creates a ready-to-use Mercator projection without manual configuration.
result: pending

### 6. fitWorld() Configuration
expected: Calling fitWorld() on a projection configures center/scale to show entire world within viewport.
result: pending

### 7. toScreen() Procedural Style
expected: toScreen(51.5, -0.1, projection) returns Vector2 screen coordinates for London.
result: pending

### 8. toScreen() Extension Style
expected: Vector2(51.5, -0.1).toScreen(projection) returns screen coordinates using extension method.
result: pending

### 9. fromScreen() Inverse Transform
expected: fromScreen(screenX, screenY, projection) returns geographic coordinates that round-trip correctly.
result: pending

### 10. Batch Coordinate Transformation
expected: toScreen(sequenceOf(points), projection) transforms multiple coordinates efficiently in one call.
result: pending

### 11. Latitude Clamping
expected: clampLatitude(89.0) returns 85.05112878 (Web Mercator limit), preventing pole overflow.
result: pending

### 12. Longitude Normalization
expected: normalizeLongitude(370.0) returns 10.0, handling Earth wraparound correctly.
result: pending

### 13. Screen Visibility Check
expected: isOnScreen(point, bounds) returns true for visible coordinates, false for off-screen.
result: pending

### 14. Coordinate Validation
expected: isValidCoordinate(-100.0, 200.0) returns false for invalid lat/lng ranges.
result: pending

## Summary

total: 14
passed: 0
issues: 1
pending: 13
skipped: 0

## Gaps

- truth: "Phase 2 code compiles and tests can run"
  status: failed
  reason: "User reported: test script - ./gradlew test failed with compilation errors"
  severity: blocker
  test: 1
  root_cause: "Multiple compilation errors: (1) UtilityFunctions.kt imports org.openrndr.shapes.Rectangle but correct path is org.openrndr.shape.Rectangle (singular); (2) ProjectionMercator.kt:50 and ProjectionEquirectangular.kt:48 cast GeoProjection to ProjectionConfig incorrectly in fitWorld(); (3) ProjectionFactory.kt:69,82 calls fitWorld() without required config parameter"
  artifacts:
    - path: "src/main/kotlin/geo/projection/UtilityFunctions.kt"
      issue: "Wrong import: org.openrndr.shapes.Rectangle should be org.openrndr.shape.Rectangle"
    - path: "src/main/kotlin/geo/projection/ProjectionMercator.kt"
      issue: "Line 50: fitWorld() casts GeoProjection to ProjectionConfig incorrectly"
    - path: "src/main/kotlin/geo/projection/ProjectionEquirectangular.kt"
      issue: "Line 48: fitWorld() casts GeoProjection to ProjectionConfig incorrectly"
    - path: "src/main/kotlin/geo/projection/ProjectionFactory.kt"
      issue: "Lines 69,82: fitWorld() called without required config parameter"
  missing:
    - "Fix import in UtilityFunctions.kt: change shapes to shape"
    - "Fix fitWorld() in ProjectionMercator.kt: return ProjectionMercator(config.copy(...)) directly"
    - "Fix fitWorld() in ProjectionEquirectangular.kt: return ProjectionEquirectangular(config.copy(...)) directly"
    - "Fix fitWorld() calls in ProjectionFactory.kt: pass config parameter"
  debug_session: "gsd-debugger session"
