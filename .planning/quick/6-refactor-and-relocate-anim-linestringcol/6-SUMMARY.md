---
phase: quick
plan: 6
name: refactor-and-relocate-anim-linestringcol
subsystem: examples
tags: [refactoring, examples, animation]
requires: []
provides: [REFACTOR-01]
affects:
  - examples/anim/06-linestring-color-anim.kt
  - examples/anim/README.md
tech-stack:
  added: []
  patterns:
    - "Modern API: drawer.geo() with GeoRenderConfig block"
    - "Property-based styling with styleByFeature"
    - "GeoSource and geoSource() for data loading"
key-files:
  created:
    - examples/anim/06-linestring-color-anim.kt
  modified:
    - examples/anim/README.md
  deleted:
    - src/main/kotlin/geo/examples/anim_LineStringColor.kt
decisions:
  - "Use drawer.geo(source) { } with projection and styleByFeature config instead of manual feature iteration"
  - "Keep simple global animator for color transition (not per-feature animators) to maintain original behavior"
  - "Use explicit Feature type annotation in lambda to help compiler with type inference"
  - "Line count reduced from 91 to 69 lines (24% reduction) while maintaining functionality"
---

# Quick Task 6: Refactor and Relocate anim_LineStringColor

**One-liner:** Migrated LineString color animation example to examples/anim/ with modern API patterns (24% code reduction).

## Summary

Successfully refactored and relocated the `anim_LineStringColor` example from `src/main/kotlin/geo/examples/` to `examples/anim/`. The refactored code uses modern API patterns:

- **Data loading:** `geoSource()` instead of `GeoJSON.load()`
- **Rendering:** `drawer.geo(source) { }` with `styleByFeature` instead of manual iteration
- **Configuration:** `GeoRenderConfig` block with `projection` and `styleByFeature` properties
- **Simplification:** Removed manual geometry type handling and coordinate projection loops

## Changes Made

### Created: `examples/anim/06-linestring-color-anim.kt`

- **Lines:** 69 (down from 91, 24% reduction)
- **Key patterns:**
  - `@file:JvmName("LineStringColorAnim")` annotation for valid class name
  - `package examples.anim` for consistency
  - `geoSource()` for data loading with relative path
  - `drawer.geo(data) { projection = ...; styleByFeature = { ... } }` for rendering
  - Property-based stroke weight mapping using `feature.doubleProperty()`

### Modified: `examples/anim/README.md`

- Added example entries for 04, 05, and 06 to the examples table
- Example 06 description: "Animate LineString colors with property-based styling"

### Deleted: `src/main/kotlin/geo/examples/anim_LineStringColor.kt`

Original 91-line file removed after migration.

## Verification

- [x] Original file deleted
- [x] New file created with modern API
- [x] README.md updated with example entries
- [x] Code compiles successfully (`./gradlew compileKotlin`)
- [x] Line count reduced (69 vs 91 lines)

## Deviations from Plan

None. Plan executed exactly as written.

## Commits

1. `e640101` - feat(quick-6): create refactored LineString color animation example
2. `32126a0` - feat(quick-6): delete original file and update README
3. `8725814` - fix(quick-6): correct API usage in linestring color example

## API Migration Comparison

### Old Pattern (91 lines)
```kotlin
val geojson = GeoJSON.load("data/geo/catchment-topo.geojson")
val features = geojson.listFeatures()
// Manual iteration with type checking and coordinate projection
features.forEach { feature ->
    when (val geom = feature.geometry) {
        is LineString -> {
            val screenCoords = geom.points.map { pt: Vector2 -> projection.project(pt) }
            drawLineString(drawer, screenCoords, Style(stroke = currentColor, strokeWeight = weight))
        }
        // ... more type cases
    }
}
```

### New Pattern (69 lines)
```kotlin
val data = geoSource("examples/data/geo/catchment-topo.geojson")
drawer.geo(data) {
    this.projection = projection
    styleByFeature = { feature: Feature ->
        val v = feature.doubleProperty("property_value") ?: minValue
        val t = if (range > 0.0) (v - minValue) / range else 0.0
        Style(stroke = currentColor, strokeWeight = 0.05 + t * 0.45)
    }
}
```

## Self-Check: PASSED

- [x] File exists: `examples/anim/06-linestring-color-anim.kt`
- [x] File exists: `examples/anim/README.md` (modified)
- [x] File deleted: `src/main/kotlin/geo/examples/anim_LineStringColor.kt`
- [x] Commits verified: e640101, 32126a0, 8725814
- [x] Compilation successful

---
*Created: 2026-02-28*
