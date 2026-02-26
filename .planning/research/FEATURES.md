# Feature Landscape

**Domain:** Geospatial visualization library (Kotlin/OPENRNDR)
**Researched:** 2026-02-26
**Focus:** v1.2.0 API improvements and examples

---

## Scope Note

This document focuses **only** on features proposed for v1.2.0. For the full v1.0 feature landscape, see git history.

---

## Table Stakes

Features users expect in geospatial libraries. Missing = product feels incomplete.

| Feature | Why Expected | Complexity | Dependencies | Notes |
|---------|--------------|------------|--------------|-------|
| **GeoSource summary/inspection** | Users need to understand data before rendering. Standard in Turf.js (`meta`), Shapely (`geom_type`, `bounds`), GeoPandas (`info()`). | Low | Existing: `features`, `totalBoundingBox()`, `crs` | Add: geometry type counts, property schema extraction, coordinate range stats |
| **Polygon interior ring rendering** | GeoJSON RFC 7946 mandates holes (interior rings). Complex polygons (countries with lakes, buildings with courtyards) require this. | Medium | Existing: `Polygon.interiors` | `interiorsToScreen()` is TODO. Need to render as CompoundShape or subtractive mask |
| **MultiPolygon bounds handling** | Large MultiPolygons may span dateline or exceed Mercator limits. Standard approach: clamp coordinates, not clip geometry. | Low | Existing: `clampToMercator()`, `clampAndNormalize()` | Current implementation is table stakes quality |
| **Example file organization** | Educational libraries need discoverable examples. Pattern: `category_FeatureName.kt` is established in creative coding (Processing, p5.js, OPENRNDR). | Low | None | Current pattern is excellent: `anim_`, `render_`, `layer_`, `proj_`, `core_` prefixes |

## Differentiators

Features that set openrndr-geo apart from other geospatial libraries. Not expected, but valued.

| Feature | Value Proposition | Complexity | Dependencies | Notes |
|---------|-------------------|------------|--------------|-------|
| **Rendering boilerplate reduction** | Creative coding UX: one-liner rendering with smart defaults. D3.js, p5.js compete on this. | Medium | Existing: Style DSL, drawer extensions | Consider: `source.drawQuick(drawer)`, `polygon.withHoles()` helper, projection presets |
| **Batch coordinate projection** | Performance optimization for large datasets. Project once, render many times (animation). Most libs project per-frame. | Medium | Existing: `GeoProjection.project()` | Add: `projectAll(points): List<Vector2>`, cached projection on GeoSource, lazy+memoized pattern |

## Anti-Features

Features to explicitly NOT build.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| **Geometry clipping (vs clamping)** | Clipping (actual polygon cutting at dateline/Mercator bounds) is complex, changes topology, rarely needed for visualization. | Use clamping (`clampToMercator()`) which preserves geometry structure |
| **Full GeoJSON validation** | Validation is slow, most data is valid, errors are edge cases. | Provide `validate()` as opt-in, not automatic |
| **Property-based styling DSL** | Overly complex for creative coding use case. Declarative styling is better handled by user code. | Keep simple Style class, let users write `when` expressions on properties |

---

## Feature Dependencies

```
Polygon interior rendering → MultiPolygon rendering (both need hole support)
Batch projection → Animation performance (cache projected coords per-frame)
GeoSource summary → Debug/inspection workflows (understand data before rendering)
```

---

## Feature Details

### 1. GeoSource Summary/Inspection Function

**What libraries provide:**

| Library | Function | Returns |
|---------|----------|---------|
| Turf.js | `turf.meta.coordAll()` | All coordinates as flat array |
| Turf.js | `turf.meta.featureEach()` | Iterator with index |
| Shapely | `geom.geom_type` | String: "Point", "Polygon", etc. |
| Shapely | `geom.bounds` | `(minx, miny, maxx, maxy)` |
| GeoPandas | `gdf.info()` | Schema, row count, memory usage |
| OpenLayers | `source.getFeatures()` | Feature array with `.getGeometry().getType()` |

**Recommended API for openrndr-geo:**

```kotlin
data class GeoSourceSummary(
    val featureCount: Long,
    val bounds: Bounds,
    val crs: String,
    val geometryTypes: Map<String, Int>,  // "Point" -> 5, "Polygon" -> 3
    val propertyKeys: Set<String>,         // All unique property names
    val coordinateCount: Long              // Total vertices
)

fun GeoSource.summarize(): GeoSourceSummary
```

**Complexity:** Low - iterate features once, aggregate stats.

---

### 2. Polygon Interior/Exterior Ring Handling

**GeoJSON RFC 7946 specification:**

> For Polygons with more than one ring, the first MUST be the exterior ring, and any others MUST be interior rings. The exterior ring bounds the surface, and interior rings bound holes.

**Winding order (right-hand rule):**
- Exterior: counterclockwise (positive area)
- Interior: clockwise (negative area)
- Note: RFC says parsers SHOULD NOT reject non-compliant winding for backwards compatibility

**How libraries render holes:**

| Library | Approach |
|---------|----------|
| D3.js | `d3-path` with `moveTo`/`lineTo` + even-odd fill rule |
| Canvas 2D | `context.fill('evenodd')` |
| SVG | `fill-rule="evenodd"` |
| OPENRNDR | `ShapeContour` + `CompositionNode` with subtractive blend, or `Shape` with holes |

**Recommended implementation:**

```kotlin
// Option A: CompoundShape (multiple contours, even-odd fill)
fun Polygon.toShape(projection: GeoProjection): Shape {
    val exteriorContour = ShapeContour.fromPoints(
        exterior.map { projection.project(it) }, 
        closed = true
    )
    val holeContours = interiors.map { ring ->
        ShapeContour.fromPoints(
            ring.map { projection.project(it) },
            closed = true
        )
    }
    return Shape(listOf(exteriorContour) + holeContours)
}

// Option B: Keep current approach, add hole support to writePolygon
fun writePolygonWithHoles(
    drawer: Drawer,
    exterior: List<Vector2>,
    holes: List<List<Vector2>>,
    style: Style
) {
    // Use OPENRNDR Shape with multiple contours
}
```

**Complexity:** Medium - need to handle Shape/Composition API, test with complex polygons.

---

### 3. Rendering Boilerplate Reduction

**Current pattern (already good):**

```kotlin
val source = geoSource("data.geojson")
val projection = ProjectionMercator { width = 800; height = 600 }
extend {
    source.render(drawer, projection)
}
```

**Potential improvements:**

| Improvement | Example | Value |
|-------------|---------|-------|
| Quick render (auto-fit) | `source.renderQuick(drawer)` | Eliminates projection setup for simple cases |
| Style presets | `Style.redOutline`, `Style.blueFill(alpha=0.5)` | Common styles without DSL |
| Geometry helpers | `polygon.draw(drawer, projection)` | Method on geometry, not just GeoSource |

**Recommended additions:**

```kotlin
// Already exists but could enhance
fun GeoSource.render(drawer: Drawer, style: Style? = null)  // Auto-fits

// New: style presets
object StylePresets {
    val redOutline get() = Style { stroke = ColorRGBa.RED; fill = null }
    val blueFill get() = Style { fill = ColorRGBa.BLUE.withAlpha(0.5) }
}

// New: geometry extension
fun Geometry.draw(drawer: Drawer, projection: GeoProjection, style: Style? = null)
```

**Complexity:** Low - mostly convenience extensions.

---

### 4. MultiPolygon Bounds Handling

**Clipping vs Clamping:**

| Approach | What it does | When to use |
|----------|--------------|-------------|
| **Clamping** | Constrains coordinates to valid range, preserves topology | Visualization (almost always) |
| **Clipping** | Cuts geometry at boundary, creates new polygons | GIS analysis, dateline crossing |

**Current implementation (already correct):**

```kotlin
fun Geometry.clampToMercator(): Geometry
fun Geometry.clampAndNormalize(): Geometry
```

**Recommendation:** Keep clamping approach. Add documentation that clipping is out of scope.

**Complexity:** Already implemented. No new work needed.

---

### 5. Batch Coordinate Projection

**Current approach (per-point):**

```kotlin
features.forEach { feature ->
    val screenPoints = feature.geometry.points.map { projection.project(it) }
    // render
}
```

**Problem:** In animation loops (60fps), projection is recalculated every frame for static data.

**Optimization strategies:**

| Strategy | Implementation | Tradeoff |
|----------|----------------|----------|
| **Pre-project GeoSource** | `source.projected(projection)` returns new GeoSource with cached screen coords | Memory for speed |
| **Batch project function** | `projection.projectAll(points): List<Vector2>` | Single pass, no cache |
| **Lazy + memoized** | Project on first access, cache result | Memory only for used coords |

**Recommended API:**

```kotlin
// Batch projection (simple)
fun GeoProjection.projectAll(points: List<Vector2>): List<Vector2> =
    points.map { project(it) }

// Cached projection on GeoSource
fun GeoSource.withProjection(projection: GeoProjection): ProjectedGeoSource {
    val projectedFeatures = features.map { it.project(projection) }.toList()
    return ProjectedGeoSource(projectedFeatures)
}

// Or: extension on Geometry
fun Geometry.projected(projection: GeoProjection): Geometry = when (this) {
    is Point -> Point(projection.project(Vector2(x, y)).let { it.x to it.y })
    is LineString -> LineString(points.map { projection.project(it) })
    // ... etc
}
```

**Complexity:** Medium - need new `ProjectedGeoSource` type or cache mechanism.

---

### 6. Example File Organization

**Current pattern (excellent):**

```
src/main/kotlin/geo/examples/
├── anim_BasicAnimation.kt
├── anim_ChainDemo.kt
├── anim_TimelineDemo.kt
├── anim_RippleDemo.kt
├── core_CRSTransformTest.kt
├── core_DataLoadingTest.kt
├── layer_BlendModes.kt
├── layer_Composition.kt
├── layer_Graticule.kt
├── proj_HaversineDemo.kt
├── proj_ProjectionTest.kt
├── render_BasicRendering.kt
├── render_LiveRendering.kt
```

**Best practices from creative coding:**

| Practice | Example | Reason |
|----------|---------|--------|
| Prefix by category | `anim_`, `render_`, `layer_` | Groups related examples |
| Descriptive suffix | `BasicAnimation`, not `Demo1` | Self-documenting |
| Single concept per file | One feature per example | Educational clarity |
| Runnable standalone | `fun main() = application { }` | Copy-paste friendly |

**Recommendation:** Keep current pattern. Add new examples following same convention:
- `render_PolygonWithHoles.kt` - interior ring rendering
- `data_SourceSummary.kt` - inspection API
- `perf_BatchProjection.kt` - performance optimization demo

**Complexity:** Low - organizational, not technical.

---

## MVP Recommendation

**Implement in v1.2.0:**

| Priority | Feature | Rationale |
|----------|---------|-----------|
| P1 | GeoSource summary | Low complexity, high value for debugging/understanding data |
| P1 | Polygon interior ring rendering | Table stakes, closes feature gap, TODO already exists |
| P2 | Batch projection | Differentiator, enables animation performance |

**Defer to later versions:**

| Feature | Reason |
|---------|--------|
| Rendering boilerplate reduction | Current API is good enough |
| Additional examples | Add organically as features land |

---

## Sources

- GeoJSON RFC 7946: https://tools.ietf.org/html/rfc7946 (HIGH confidence - official spec)
- Shapely documentation: https://shapely.readthedocs.io/en/stable/manual.html (HIGH confidence - authoritative Python GIS)
- D3-geo: https://github.com/d3/d3-geo (HIGH confidence - industry standard)
- OPENRNDR guide: https://guide.openrndr.org (HIGH confidence - official docs)
- Existing codebase analysis: GeoSource.kt, Geometry.kt, render.kt, PolygonRenderer.kt (HIGH confidence - primary source)

---

*Feature research for: openrndr-geo v1.2.0*
*Researched: 2026-02-26*
