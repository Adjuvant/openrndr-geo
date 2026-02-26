# Technology Stack — v1.2.0 API Improvements

**Project:** openrndr-geo
**Milestone:** v1.2.0 - API improvements and examples
**Researched:** 2026-02-26
**Confidence:** HIGH (verified via OpenRNDR API docs, existing codebase, openrndr-examples)

---

## Executive Summary

For v1.2.0, **no new dependencies are required**. The existing stack (OpenRNDR 0.4.5, proj4j, kotlinx-serialization) already supports all planned features. The focus is on leveraging **OpenRNDR's `Shape` API** for proper polygon hole rendering and establishing **consistent example patterns**.

---

## Feature-Specific Stack Recommendations

### 1. GeoSource `summary()` Function

**No new dependencies needed.** Implement as an extension function using existing Kotlin stdlib.

**Recommended implementation pattern:**
```kotlin
fun GeoSource.summary(): GeoSourceSummary {
    val featureList = features.toList()  // Materialize for analysis
    val geometryTypes = featureList.map { it.geometry::class.simpleName }.distinct()
    val propertyKeys = featureList.flatMap { it.properties.keys }.distinct()
    
    return GeoSourceSummary(
        featureCount = featureList.size,
        geometryTypes = geometryTypes,
        propertyKeys = propertyKeys,
        crs = crs,
        bounds = totalBoundingBox()
    )
}

data class GeoSourceSummary(
    val featureCount: Int,
    val geometryTypes: List<String?>,
    val propertyKeys: List<String>,
    val crs: String,
    val bounds: Bounds
) {
    override fun toString(): String = buildString {
        appendLine("GeoSource Summary")
        appendLine("═".repeat(40))
        appendLine("Features:    $featureCount")
        appendLine("CRS:         $crs")
        appendLine("Bounds:      $bounds")
        appendLine("Geometry:    ${geometryTypes.joinToString()}")
        appendLine("Properties:  ${propertyKeys.take(5).joinToString()}${if (propertyKeys.size > 5) "..." else ""}")
    }
}
```

**Integration points:**
- Add to `GeoSource.kt` or create new `GeoSourceSummary.kt`
- Print-friendly `toString()` for console debugging

---

### 2. Polygon Interior/Exterior Ring Handling

**Key OpenRNDR APIs to use:**

| API | Purpose | Source |
|-----|---------|--------|
| `Shape(contours: List<ShapeContour>)` | Compound shape with multiple contours (holes) | `org.openrndr.shape.Shape` |
| `ShapeContour.fromPoints(points, closed, polarity)` | Create contour from point list | `org.openrndr.shape.ShapeContour.Companion` |
| `ShapeContour.clockwise` | Get contour with clockwise winding | `org.openrndr.shape.ShapeContour` |
| `ShapeContour.counterClockwise` | Get contour with counter-clockwise winding | `org.openrndr.shape.ShapeContour` |
| `ShapeContour.winding` | Inspect winding order | `org.openrndr.shape.Winding` |
| `drawer.shape(shape)` | Render compound shape with holes | `org.openrndr.draw.Drawer` |

**Winding order convention (OpenRNDR uses Y-down screen coordinates):**
- **CLOCKWISE** = Exterior ring (filled)
- **COUNTER_CLOCKWISE** = Interior ring (hole)

**Recommended implementation:**
```kotlin
// In PolygonRenderer.kt or new CompoundShapeRenderer.kt

import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour

/**
 * Convert a Polygon (with holes) to an OpenRNDR Shape.
 * 
 * Uses winding order convention:
 * - Exterior ring: CLOCKWISE
 * - Interior rings: COUNTER_CLOCKWISE
 */
fun Polygon.toShape(projection: GeoProjection): Shape {
    // Project exterior ring to screen space, ensure clockwise
    val exteriorContour = ShapeContour.fromPoints(
        points = exterior.map { projection.project(it) },
        closed = true
    ).clockwise
    
    // Project interior rings (holes), ensure counter-clockwise
    val holeContours = interiors.map { hole ->
        ShapeContour.fromPoints(
            points = hole.map { projection.project(it) },
            closed = true
        ).counterClockwise
    }
    
    return Shape(listOf(exteriorContour) + holeContours)
}

/**
 * Draw a Polygon with proper hole handling.
 */
fun drawPolygonWithHoles(
    drawer: Drawer,
    polygon: Polygon,
    projection: GeoProjection,
    style: Style? = null
) {
    val mergedStyle = mergeStyles(StyleDefaults.defaultPolygonStyle, style)
    applyStyle(drawer, mergedStyle)
    
    val shape = polygon.toShape(projection)
    drawer.shape(shape)
}
```

**Files to modify:**
- `src/main/kotlin/geo/render/PolygonRenderer.kt` — Add `writePolygonWithHoles()`
- `src/main/kotlin/geo/Geometry.kt` — Implement `interiorsToScreen()` (currently `TODO`)
- `src/main/kotlin/geo/render/MultiRenderer.kt` — Update `drawMultiPolygon()` to use holes

---

### 3. Reducing Rendering Boilerplate

**Current state:** `DrawerGeoExtensions.kt` already provides:
- `drawer.geoJSON(path)` — One-line render
- `drawer.geoSource(source)` — Load-once, draw-many
- `source.render(drawer, projection)` — GeoSource render method

**Recommended additions:**

```kotlin
// In GeoSourceConvenience.kt or new GeoSourceShortcuts.kt

/**
 * Load and render in one call with auto-fit.
 */
fun Drawer.drawGeo(path: String, style: Style? = null) {
    geoJSON(path, style = style)
}

/**
 * Create projection from data bounds, then render.
 */
fun GeoSource.drawFitted(drawer: Drawer, style: Style? = null, padding: Double = 0.9) {
    val bounds = totalBoundingBox()
    val projection = ProjectionFactory.fitBounds(
        bounds = bounds,
        width = drawer.width.toDouble(),
        height = drawer.height.toDouble(),
        padding = padding,
        projection = ProjectionType.MERCATOR
    )
    render(drawer, projection, style)
}
```

**Import consolidation:** Create `geo/api.kt` with star exports:
```kotlin
// src/main/kotlin/geo/api.kt
package geo

// Single import gets everything needed
// import geo.*

// Data types
typealias GeoPoint = Point
typealias GeoLineString = LineString
typealias GeoPolygon = Polygon

// Re-export commonly used functions
fun loadGeo(path: String) = geoSource(path)
```

---

### 4. MultiPolygon Outside Projection Bounds

**Current approach in `MultiRenderer.kt`:**
```kotlin
// Already clamps to MAX_MERCATOR_LAT
val polygonsToRender = if (clampToMercatorBounds && projection is ProjectionMercator) {
    multiPolygon.polygons.map { polygon ->
        polygon.exterior.map { coord ->
            Vector2(coord.x, coord.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
        }
    }
} else { ... }
```

**Issues with current approach:**
1. Only handles exterior rings (ignores holes)
2. Doesn't handle longitude wrapping (dateline crossing)
3. Doesn't split geometries at projection boundaries

**Recommended improvements:**

```kotlin
// In Geometry.kt - already exists, ensure it's used consistently
fun Geometry.clampAndNormalize(): Geometry { ... }

// In MultiRenderer.kt
fun drawMultiPolygon(
    drawer: Drawer,
    multiPolygon: MultiPolygon,
    projection: GeoProjection,
    userStyle: Style? = null,
    boundsHandling: BoundsHandling = BoundsHandling.CLAMP
) {
    val processedMultiPolygon = when (boundsHandling) {
        BoundsHandling.CLAMP -> multiPolygon.clampToMercator() as MultiPolygon
        BoundsHandling.NORMALIZE -> multiPolygon.clampAndNormalize() as MultiPolygon
        BoundsHandling.SKIP -> {
            // Filter out polygons with out-of-bounds coordinates
            MultiPolygon(multiPolygon.polygons.filter { it.validateMercatorBounds() })
        }
    }
    
    processedMultiPolygon.polygons.forEach { poly ->
        drawPolygonWithHoles(drawer, poly, projection, userStyle)
    }
}

enum class BoundsHandling {
    CLAMP,      // Clamp coordinates to valid range
    NORMALIZE,  // Clamp + normalize longitude
    SKIP        // Skip polygons outside bounds
}
```

---

### 5. Batch Screen Space Projection

**Problem:** Current API re-projects every frame. For static data, this is wasteful.

**Recommended approach: Cached projection**

```kotlin
// In Geometry.kt or new ScreenSpace.kt

/**
 * Projected geometry ready for rendering (no further projection needed).
 */
sealed class ScreenGeometry {
    abstract val screenPoints: List<Vector2>
}

data class ScreenPoint(val screen: Vector2) : ScreenGeometry() {
    override val screenPoints = listOf(screen)
}

data class ScreenLineString(override val screenPoints: List<Vector2>) : ScreenGeometry()

data class ScreenPolygon(
    val exterior: List<Vector2>,
    val interiors: List<List<Vector2>> = emptyList()
) : ScreenGeometry() {
    override val screenPoints = exterior
    
    fun toShape(): Shape {
        val exteriorContour = ShapeContour.fromPoints(exterior, true).clockwise
        val holeContours = interiors.map { 
            ShapeContour.fromPoints(it, true).counterClockwise 
        }
        return Shape(listOf(exteriorContour) + holeContours)
    }
}

/**
 * Project geometry to screen space once, cache for repeated rendering.
 */
fun Geometry.projectToScreen(projection: GeoProjection): ScreenGeometry = when (this) {
    is Point -> ScreenPoint(projection.project(Vector2(x, y)))
    is LineString -> ScreenLineString(points.map { projection.project(it) })
    is Polygon -> ScreenPolygon(
        exterior = exterior.map { projection.project(it) },
        interiors = interiors.map { ring -> ring.map { projection.project(it) } }
    )
    is MultiPoint -> TODO("MultiPoint batch projection")
    is MultiLineString -> TODO("MultiLineString batch projection")
    is MultiPolygon -> TODO("MultiPolygon batch projection")
}

/**
 * Pre-projected feature for efficient render loops.
 */
data class ScreenFeature(
    val geometry: ScreenGeometry,
    val properties: Map<String, Any?>
)

/**
 * Pre-project entire source for render loop efficiency.
 */
fun GeoSource.projectToScreen(projection: GeoProjection): Sequence<ScreenFeature> {
    return features.map { feature ->
        ScreenFeature(
            geometry = feature.geometry.projectToScreen(projection),
            properties = feature.properties
        )
    }
}
```

**Usage in render loop:**
```kotlin
// In program setup
val data = geoSource("world.json")
val projection = ProjectionFactory.fitWorldMercator(width, height)
val screenData = data.projectToScreen(projection).toList()  // Project once

extend {
    // No per-frame projection cost
    screenData.forEach { feature ->
        when (val geom = feature.geometry) {
            is ScreenPoint -> drawPoint(drawer, geom.screen, style)
            is ScreenLineString -> drawLineString(drawer, geom.screenPoints, style)
            is ScreenPolygon -> {
                drawer.shape(geom.toShape())  // Uses pre-projected points
            }
        }
    }
}
```

---

### 6. Example File Structure

**Pattern from openrndr-examples:**
- Location: `src/main/kotlin/examples/`
- Naming: `{category}_{description}.kt` (e.g., `render_BasicRendering.kt`)
- Run command: `./gradlew run -Popenrndr.application=geo.examples.BasicRenderingKt`

**Recommended structure for openrndr-geo:**

```
src/main/kotlin/geo/examples/
├── core/                          # Core functionality
│   ├── DataLoading.kt             # GeoJSON/GeoPackage loading
│   ├── CRSHandling.kt             # CRS detection and transformation
│   └── GeometryTypes.kt           # All geometry types demo
│
├── render/                        # Rendering examples
│   ├── BasicRendering.kt          # Simple load-and-render
│   ├── PolygonWithHoles.kt        # Compound shape rendering
│   ├── MultiGeometries.kt         # MultiPoint, MultiLineString, MultiPolygon
│   ├── Styling.kt                 # Fill, stroke, styles
│   └── OceanData.kt               # Large MultiPolygon handling
│
├── proj/                          # Projection examples
│   ├── Projections.kt             # Mercator, Equirectangular
│   ├── FitBounds.kt               # Auto-fit to data
│   └── Haversine.kt               # Distance calculations
│
├── layer/                         # Layer system examples
│   ├── Layers.kt                  # Basic layer usage
│   ├── BlendModes.kt              # Layer compositing
│   └── Graticule.kt               # Grid overlay
│
└── anim/                          # Animation examples
    ├── BasicAnimation.kt          # Simple property animation
    ├── Timeline.kt                # Keyframe sequences
    └── GeoAnimation.kt            # Animating geo properties
```

**Example file template:**
```kotlin
package geo.examples.render

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.geoSource
import geo.projection.ProjectionFactory
import geo.render.Style

/**
 * Polygon with Holes Example
 * 
 * Demonstrates rendering polygons with interior rings (holes) using
 * OpenRNDR's compound Shape API.
 * 
 * Run: ./gradlew run -Popenrndr.application=geo.examples.render.PolygonWithHolesKt
 */
fun main() = application {
    configure {
        width = 800
        height = 600
        title = "openrndr-geo: Polygon with Holes"
    }
    
    program {
        val data = geoSource("data/polygons-with-holes.geojson")
        val projection = ProjectionFactory.fitBounds(
            bounds = data.totalBoundingBox(),
            width = width.toDouble(),
            height = height.toDouble()
        )
        
        extend {
            drawer.clear(ColorRGBa.WHITE)
            data.render(drawer, projection, Style {
                fill = ColorRGBa.BLUE.withAlpha(0.3)
                stroke = ColorRGBa.BLACK
                strokeWeight = 1.0
            })
        }
    }
}
```

---

## Dependency Summary

**No new dependencies for v1.2.0.** Existing stack is sufficient:

```kotlin
// build.gradle.kts - no changes needed

// Already have:
// - OpenRNDR 0.4.5 (includes Shape, ShapeContour APIs)
// - orx-shapes (for advanced shape utilities if needed)
// - kotlinx-serialization (for GeoJSON parsing)
// - proj4j (for CRS transformations)
// - mil.nga.geopackage (for GeoPackage reading)
```

---

## Integration Points

| New Feature | File(s) to Modify | New File(s) |
|-------------|-------------------|-------------|
| `summary()` | `GeoSource.kt` | `GeoSourceSummary.kt` |
| Polygon holes | `PolygonRenderer.kt`, `Geometry.kt` | — |
| Boilerplate reduction | `GeoSourceConvenience.kt` | `geo/api.kt` |
| Bounds handling | `MultiRenderer.kt`, `Geometry.kt` | — |
| Batch projection | `Geometry.kt` | `ScreenSpace.kt` |
| Examples | — | `geo/examples/**/*.kt` |

---

## Confidence Assessment

| Area | Confidence | Source |
|------|------------|--------|
| OpenRNDR Shape API | HIGH | api.openrndr.org documentation |
| Winding order for holes | HIGH | OpenRNDR ShapeContour.winding docs |
| Example structure | HIGH | openrndr-examples repository |
| No new deps needed | HIGH | Existing build.gradle.kts review |
| Batch projection approach | MEDIUM | Standard pattern, needs performance testing |

---

## Sources

- **OpenRNDR Shape API:** https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape/index.html
- **OpenRNDR ShapeContour:** https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-shape-contour/index.html
- **OpenRNDR Winding:** https://api.openrndr.org/openrndr-shape/org.openrndr.shape/-winding/index.html
- **Drawer.shape():** https://api.openrndr.org/openrndr-draw/org.openrndr.draw/-drawer/shape.html
- **openrndr-examples:** https://github.com/openrndr/openrndr-examples
- **Existing codebase:** src/main/kotlin/geo/**/*.kt
