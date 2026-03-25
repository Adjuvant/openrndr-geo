# Phase 20: Layer Features - Research

**Researched:** 2026-03-25
**Status:** Complete
**Phase:** 20-layer-features
**Requirement:** LAYER-01

---

## Domain Analysis

### What This Phase Delivers

Fix and enhance graticule layer for zoomed-in maps. The graticule provides geographic reference lines (latitude/longitude grid) for map visualizations.

### Current State

The existing `Graticule.kt` generates **Point features** at grid intersections. The context requires changing to **LineString features** organized as MultiLineStrings with separate `latLines` and `lngLines` properties on a GeoLayer.

### Key Technical Changes Required

1. **Feature Type Change**: Points → LineStrings
2. **Return Structure**: Single GeoSource → GeoLayer with latLines/lngLines
3. **Adaptive Spacing**: Power-of-10 grid (1°, 10°, 30°, 90°) based on viewport
4. **Label Generation**: Optional label positions + text for lat/lng values
5. **Density Management**: Auto-thinning when lines too dense, clip to viewport
6. **Antimeridian Handling**: Split lines at ±180° (reuse Phase 16 approach)

---

## Technical Approach

### 1. Line Generation Algorithm

**Grid Line Structure:**
```
latitudeLines: MultiLineString — all horizontal lines in one geometry
longitudeLines: MultiLineString — all vertical lines in one geometry
```

Each line is a LineString with two points (start, end). For longitude lines spanning the viewport, lines that cross antimeridian will be split.

**Adaptive Spacing Logic:**
```
visibleDegrees = max(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY)

spacing = when {
    visibleDegrees < 2.0 -> 1.0    // Very zoomed in
    visibleDegrees < 20.0 -> 10.0   // Zoomed in
    visibleDegrees < 60.0 -> 30.0   // Regional
    else -> 90.0                     // Continental
}
```

**Minimum floor:** 1° even when very zoomed in (prevents clutter).

### 2. Antimeridian Handling

Reuse `splitAtAntimeridian()` from `AntimeridianSplitter.kt`:
- Longitude lines crossing ±180° are split into separate LineStrings
- Each fragment stays on one side of antimeridian
- Interpolate intersection point at the boundary

### 3. Label Generation

**Data Structure:**
```kotlin
data class GraticuleLabels(
    val latitudeLabels: List<LabelPosition>,  // "45°N", "30°N", etc.
    val longitudeLabels: List<LabelPosition>  // "120°W", "90°E", etc.
)

data class LabelPosition(
    val text: String,
    val longitude: Double,  // for lat labels
    val latitude: Double,    // for lng labels  
    val projectedX: Double,  // screen position
    val projectedY: Double   // screen position
)
```

**Placement:**
- Latitude labels: left edge of viewport (constant longitude = bounds.minX)
- Longitude labels: bottom edge of viewport (constant latitude = bounds.minY)

**CRS Handling:**
- For EPSG:4326 (degrees): Format as "45°N", "120°W", "0°"
- For BNG (meters): Convert to lat/lng for display, or show meter values directly
- Labels generated in geographic coordinates, projection happens at render time

### 4. Viewport Density Management

**Auto-thinning:**
```kotlin
// Estimate projected spacing
val projectedSpacing = estimatePixelSpacing(line, projection, bounds)
if (projectedSpacing < MIN_PIXEL_SPACING) {
    spacing = increaseToNextPowerOf10(spacing)
}
```

**MIN_PIXEL_SPACING:** ~20px suggested (tunable)

**Clipping:**
- Only generate lines within viewport bounds (with small margin)
- Lines already generated only within bounds

### 5. API Design

```kotlin
// New function signature
fun generateGraticuleLayer(
    bounds: Bounds,
    projection: Projection,
    includeLabels: Boolean = false,
    minPixelSpacing: Double = 20.0
): GeoLayer

// With latLines and lngLines properties
data class GeoLayer(
    var source: GeoSource? = null,  // Combined source for backward compat
    var style: Style? = null,
    var latLines: GeoSource? = null,   // NEW: latitude lines
    var lngLines: GeoSource? = null,   // NEW: longitude lines
    var labels: GraticuleLabels? = null // NEW: label data (if includeLabels=true)
)

// Alternative: separate functions
fun generateGraticuleSource(bounds: Bounds, spacing: Double): GeoSource
fun generateGraticuleLabels(bounds: Bounds, spacing: Double, projection: Projection): GraticuleLabels
```

**Usage:**
```kotlin
val graticule = generateGraticuleLayer(viewportBounds, projection, includeLabels = true)

// Render lines
drawer.geo(graticule.latLines)
drawer.geo(graticule.lngLines)

// Render labels
graticule.labels?.latitudeLabels?.forEach { label ->
    drawer.text(label.text, label.projectedX, label.projectedY)
}
```

---

## Validation Architecture

### Test Infrastructure

- **Framework:** JUnit 5 (existing test infrastructure)
- **Config:** Standard Gradle test configuration
- **Quick command:** `./gradlew test --tests "geo.layer.*" -q`
- **Full suite:** `./gradlew test -q`
- **Estimated runtime:** ~30 seconds for layer tests

### Unit Test Coverage

| Test | Description | File |
|------|-------------|------|
| Grid spacing calculation | Power-of-10 logic at different zoom levels | GraticuleSpacingTest.kt |
| Line generation | Correct coordinates for lat/lng lines | GraticuleLineGenerationTest.kt |
| Antimeridian splitting | Lines correctly split at ±180° | GraticuleAntimeridianTest.kt |
| Label positioning | Correct placement at viewport edges | GraticuleLabelPositionTest.kt |
| Label formatting | Correct degree notation (N/S/E/W) | GraticuleLabelFormatTest.kt |
| Density management | Auto-thinning when zoomed out | GraticuleDensityTest.kt |

### Integration Test

- **UAT-style verification:** Render graticule at various zoom levels and verify visual output
- **Antimeridian case:** Verify grid lines render correctly across ±180°

---

## Reusable Assets from Phase 16

| Asset | Location | Usage |
|-------|----------|-------|
| `splitAtAntimeridian()` | `geo/render/geometry/AntimeridianSplitter.kt` | Split longitude lines crossing antimeridian |
| `Bounds` utilities | `geo/core/Bounds.kt` | viewport calculations |
| `GeoLayer` DSL | `geo/layer/GeoLayer.kt` | API pattern to follow |
| Existing `generateGraticuleSource()` | `geo/layer/Graticule.kt` | Current implementation to replace |

---

## Potential Pitfalls

1. **Performance:** Generating many LineString features could be slower than Points
   - **Mitigation:** Only generate lines within viewport bounds, use power-of-10 spacing

2. **Label positioning accuracy:** Screen coordinates depend on projection
   - **Mitigation:** Labels generated from projected coordinates, not recalculated at render

3. **BNG CRS:** Uses meters, not degrees — label formatting needs special handling
   - **Mitigation:** Research BNG coordinate format; may need separate label format path

4. **Very large bounds:** Could generate excessive lines
   - **Mitigation:** 1° minimum floor, auto-thinning, viewport clipping

5. **Floating-point at antimeridian:** Edge cases near ±180°
   - **Mitigation:** Use same interpolation approach as Phase 16

---

## Implementation Notes

- **Backward compatibility:** `generateGraticuleSource()` can remain as deprecated function returning combined source
- **MultiLineString handling:** OPENRNDR renderer supports MultiLineString natively via `drawer.lineStrip()`
- **Style inheritance:** Lines use same stroke style, labels need separate text style
- **Documentation:** Update KDoc for new API, add usage examples

---

*Research completed for Phase 20: layer-features*
*Requirement: LAYER-01 (Graticule layer for zoomed-in maps)*
