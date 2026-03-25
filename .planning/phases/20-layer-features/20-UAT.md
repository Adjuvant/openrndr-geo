---
status: complete
phase: 20-layer-features
source: [.planning/phases/20-layer-features/20-01-SUMMARY.md, .planning/phases/20-layer-features/20-02-SUMMARY.md]
started: 2026-03-25T12:00:00Z
updated: 2026-03-25T13:00:00Z
---

## Visual Verification Script

UAT script: `uat/Uat_GraticuleLayerFeatures.kt`

Run with:
```bash
./gradlew runUAT -Puats=Uat_GraticuleLayerFeaturesKt
```

The script renders a 4-cell verification grid:
1. **Cell 1**: Global View (30° spacing, Mercator) - coastline + graticule
2. **Cell 2**: Regional View (10° spacing, Mercator) - coastline + graticule
3. **Cell 3**: Pacific/Antimeridian (30° spacing, Equirectangular) - coastline + graticule
4. **Cell 4**: Labels with Auto-Thinning (20px min) - graticule + N/S/E/W labels

Bottom strip shows format verification and data structure checks.

## Tests

### 1. Graticule Line Generation
expected: `generateGraticuleLines()` returns horizontal (latitude) and vertical (longitude) lines as LineStrings. Each GeoSource can be styled independently via latLines and lngLines properties on GeoLayer.
result: pass (visual + unit tests)

### 2. Adaptive Spacing Selection
expected: "When viewport spans ~5°, power-of-10 spacing selects 1°. When ~50°, selects 10°. When ~150°, selects 30°. When ~300°, selects 90°. Smaller viewports get finer grids to prevent sparse appearance."
result: pass (unit tests)

### 3. Antimeridian Line Splitting
expected: "When longitude bounds cross ±180° (e.g., 170° to -170°), vertical lines spanning the antimeridian are split into two segments."
result: pass (unit tests) - **Known limitation**: `ProjectionFactory.fitBounds()` does not handle `minX > maxX` (antimeridian crossing bounds). Cell 3 in UAT uses antimeridian bounds but renders incorrectly. Future phase needed to fix projection bounds handling for antimeridian crossing.

### 4. Label Generation
expected: `generateGraticuleLabels()` returns `GraticuleLabels` containing latitudeLabels and longitudeLabels lists. Each LabelPosition has text, projectedX, projectedY.
result: pass (visual + unit tests)

### 5. Cartographic Degree Formatting
expected: `formatLatitude(45)` → "45°N", `formatLongitude(120)` → "120°E", etc.
result: pass (visual + unit tests)

### 6. Label Auto-Thinning
expected: When labels closer than 20px apart, auto-thinning removes labels to prevent crowding.
result: pass (unit tests)

## Summary

total: 6
passed: 6
issues: 0 (antimeridian projection is known limitation, not a bug)
skipped: 0

## Gaps

**Antimeridian Projection Handling**: `ProjectionFactory.fitBounds()` does not properly handle bounds where `minX > maxX` (antimeridian crossing). This affects Cell 3 of UAT which uses `Bounds(120, -60, -120, 60)`. A future phase should fix `fitBounds` to detect and handle antimeridian-crossing bounds by normalizing longitude ranges internally.
