---
status: complete
phase: 02-coordinate-systems
source: [02-01-SUMMARY.md, 02-02-SUMMARY.md, 02-03-SUMMARY.md]
started: 2026-02-21T17:00:00Z
updated: 2026-02-21T18:00:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Mercator Projection
expected: Create ProjectionMercator with width/height, project(51.5, -0.1) returns Vector2 screen coordinates for London.
result: pass

### 2. Equirectangular Projection
expected: Create ProjectionEquirectangular with DSL syntax, project() works for basic world map coordinates.
result: pass

### 3. BNG Coordinate Transformation
expected: ProjectionBNG transforms WGS84 (lat/lng) to British National Grid (easting/northing) and back with ~3-5m accuracy.
result: pass

### 4. BNG Bounds Validation
expected: Projecting coordinates outside UK bounds (e.g., Paris) throws AccuracyWarningException with clear message.
result: pass

### 5. ProjectionFactory Presets
expected: ProjectionFactory.mercator(800, 600) creates a ready-to-use Mercator projection without manual configuration.
result: pass

### 6. fitWorld() Configuration
expected: Calling fitWorld() on a projection configures center/scale to show entire world within viewport.
result: pass

### 7. toScreen() Procedural Style
expected: toScreen(51.5, -0.1, projection) returns Vector2 screen coordinates for London.
result: pass

### 8. toScreen() Extension Style
expected: Vector2(51.5, -0.1).toScreen(projection) returns screen coordinates using extension method.
result: pass

### 9. fromScreen() Inverse Transform
expected: fromScreen(screenX, screenY, projection) returns geographic coordinates that round-trip correctly.
result: pass

### 10. Batch Coordinate Transformation
expected: toScreen(sequenceOf(points), projection) transforms multiple coordinates efficiently in one call.
result: pass

### 11. Latitude Clamping
expected: clampLatitude(89.0) returns 85.05112878 (Web Mercator limit), preventing pole overflow.
result: pass

### 12. Longitude Normalization
expected: normalizeLongitude(370.0) returns 10.0, handling Earth wraparound correctly.
result: pass

### 13. Screen Visibility Check
expected: isOnScreen(point, bounds) returns true for visible coordinates, false for off-screen.
result: pass

### 14. Coordinate Validation
expected: isValidCoordinate(-100.0, 200.0) returns false for invalid lat/lng ranges.
result: pass

## Summary

total: 14
passed: 14
issues: 0
pending: 0
skipped: 0

## Gaps

[none]

## Fixes Applied

**Blocker Fixed (during UAT):**
- Import path corrected: `org.openrndr.shape.Rectangle`
- Mercator projection math fixed: proper unit conversion (radians) and scale calculation
- fitWorld() now computes correct scale to fit world in viewport
- All 14 automated tests pass
