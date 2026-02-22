---
created: 2026-02-22T16:20
title: Simplify CRS handling API
area: api
files:
  - src/main/kotlin/geo/examples/LiveRendering.kt
  - src/main/kotlin/geo/projection/ProjectionBNG.kt
  - src/main/kotlin/geo/projection/ScreenTransform.kt
  - src/main/kotlin/geo/GeoPackage.kt
  - src/main/kotlin/geo/render/
---

## Problem

Working with BNG (British National Grid) coordinates through the current API is difficult and error-prone. Users must manually:
1. Detect data CRS (e.g., check `data.crs == "EPSG:27700"`)
2. Convert bounds to lat/lon before creating projection
3. Manually convert each geometry coordinate before projecting
4. Write helper functions like `toLatLonIfBNG()` that are brittle

Current code in `LiveRendering.kt` shows the pain:
- Lines 53-54: Manual CRS detection (`val isBNG = data.crs == "EPSG:27700"`)
- Lines 85-91: Convert BNG bounds to lat/lon before projection
- Lines 135-144: Helper function for coordinate conversion
- Lines 166-167, 180-181: Manual conversion per geometry point
- Comment (line 40): "All GeoProjection implementations expect lat/lng (WGS84) coordinates"

This violates the creative coding intention: "An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative." Users shouldn't need to know about CRS internals when prototyping visual ideas.

## Solution

Create a simplified API where CRS handling is automatic. Options:

**Option 1: CRS-aware Geometry**
- Add CRS metadata to `GeoSource` / `Geometry`
- Add `render(projection, context)` method that auto-converts coordinates
- Projections consume CRS info directly

**Option 2: Unified Scene Object**
```kotlin
val scene = RenderScene {
  data = GeoPackage.load("file.gpkg")
  projection = ProjectionFactory.mercator()
}
scene.render(drawer)  // Auto-handles CRS conversion
```

**Option 3: Projection Factory with CRS Awareness**
```kotlin
// Factory reads data.crs and configures projection automatically
val projection = ProjectionFactory.from(
  data = GeoPackage.load("file.gpkg"),
  viewport = Vector2(width, height)
)
```

Goal: Users want to "load data, use projection, render" without thinking about CRS. New projections can be added as needed, but the discovery/setup cost should be near-zero.