---
status: resolved
trigger: "Polygon holes not rendering"
created: 2026-03-08T00:00:00Z
updated: 2026-03-08T00:00:00Z
---

## Current Focus

hypothesis: Standard geometry rendering paths ignore polygon interiors - only optimized path handles holes
test: Read DrawerGeoExtensions.kt and trace renderToDrawer, projectGeometryToArray, renderProjectedCoordinates
expecting: Confirm that Polygon and MultiPolygon only render exteriors, not holes
next_action: Provide structured diagnosis with fix requirements

## Symptoms

expected: Polygon exterior fills with color, interior rings (holes) render as transparent cutouts
actual: Holes are not visible - polygons appear solid
errors: None
reproduction: Load polygonsWithHole.geojson and render using drawer.geo() or Geometry.renderToDrawer()
started: Unknown, likely since 16-rendering-improvements phase

## Eliminated

- hypothesis: PolygonRenderer.writePolygonWithHoles has incorrect winding order
  evidence: Code is correct - exterior.clockwise, interiors.counterClockwise, combined into Shape
  timestamp: 2026-03-08

- hypothesis: MultiRenderer.drawMultiPolygon has incorrect contour ordering
  evidence: Code is correct - collects all contours (exteriors + holes) into single Shape
  timestamp: 2026-03-08

## Evidence

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt Geometry.renderToDrawer() lines 366-368
  found: Polygon case only renders exterior: `val screenPoints = exterior.map { projection.project(it) }; drawPolygon(drawer, screenPoints, style)`
  implication: Interiors are completely ignored in this code path

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt Geometry.renderToDrawer() lines 382-386
  found: MultiPolygon case only renders exteriors: `polygons.forEach { poly -> val screenPoints = poly.exterior.map...; drawPolygon(drawer, screenPoints, style) }`
  implication: MultiPolygon holes are also completely ignored

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt projectGeometryToArray() lines 504-507
  found: Polygon and MultiPolygon cases only project exterior coordinates
  implication: Viewport cache path also cannot render holes

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt renderProjectedCoordinates() lines 526-548
  found: Both Polygon and MultiPolygon only use exterior points from cached coordinates
  implication: Cached rendering path also ignores holes

- timestamp: 2026-03-08
  checked: DrawerGeoExtensions.kt OptimizedPolygon.renderOptimizedToDrawer() lines 444-455
  found: Correctly checks for holes and calls writePolygonWithHoles() when interiors exist
  implication: The optimized rendering path works correctly - only standard paths are broken

## Resolution

root_cause: The standard Geometry.renderToDrawer() method (lines 356-389) and the viewport cache paths (projectGeometryToArray, renderProjectedCoordinates) only render polygon exteriors, completely ignoring interior rings (holes). Only the OptimizedFeature.renderOptimizedToDrawer() path handles holes correctly by calling writePolygonWithHoles().

fix: Four fixes needed:
1. Update Geometry.renderToDrawer() Polygon case to check for interiors and call writePolygonWithHoles()
2. Update Geometry.renderToDrawer() MultiPolygon case to collect all contours (exteriors + holes) into Shape
3. Update projectGeometryToArray() to project both exterior and interior coordinates
4. Update renderProjectedCoordinates() to use writePolygonWithHoles() when holes exist

verification: Load examples/data/geo/polygonsWithHole.geojson and render - holes should be visible as transparent cutouts
files_changed:
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
