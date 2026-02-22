# openrndr-geo API Reference v1.0.0

openrndr-geo is a Kotlin library for creative geospatial visualization. It provides a unified API for loading GeoJSON and GeoPackage data, projecting coordinates, rendering geometries, and animating features.

---

## Quick Start

```kotlin
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import geo.GeoJSON
import geo.projection.ProjectionFactory
import geo.render.*
import geo.render.Shape

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load GeoJSON data
        val data = GeoJSON.load("data.geojson").features.toList()
        
        // Create a projection
        val projection = ProjectionFactory.mercator(width, height)

        extend {
            // Draw each feature
            data.forEach { feature ->
                when (val geom = feature.geometry) {
                    is geo.Point -> {
                        val screen = geom.toScreen(projection)
                        drawPoint(drawer, screen.x, screen.y, Style {
                            fill = ColorRGBa.RED
                            size = 10.0
                            shape = Shape.Circle
                        })
                    }
                    is geo.LineString -> {
                        drawLineString(drawer, geom.points, Style {
                            stroke = ColorRGBa.BLUE
                            strokeWeight = 2.0
                        })
                    }
                    is geo.Polygon -> {
                        drawPolygon(drawer, geom.exterior, Style {
                            fill = ColorRGBa.GREEN.withAlpha(0.5)
                        })
                    }
                    else -> { /* Handle Multi* types */ }
                }
            }
        }
    }
}
```

---

## Modules Overview

| Module | Package | Description |
|--------|---------|-------------|
| Data Layer | `geo` | Core data types: Geometry, Feature, Bounds, GeoSource |
| Coordinate Systems | `geo.projection` | Map projections and CRS transformations |
| Rendering | `geo.render` | Drawing primitives with styling |
| Layer System | `geo.layer` | Compositing layers and graticules |
| Animation | `geo.animation` | Tweening, easing, and procedural motion |

---

## 1. Data Layer

The data layer provides core types for representing geospatial data.

### Geometry Types

All geometry types inherit from the sealed class `Geometry`:

```kotlin
import geo.*

// Point - single location
val point = Point(-0.1276, 51.5074)  // longitude, latitude

// LineString - connected points (requires 2+ points)
val line = LineString(listOf(
    Vector2(0.0, 0.0),
    Vector2(100.0, 50.0),
    Vector2(200.0, 0.0)
))

// Polygon - closed shape (requires 3+ points)
val polygon = Polygon(
    exterior = listOf(
        Vector2(0.0, 0.0),
        Vector2(100.0, 0.0),
        Vector2(50.0, 100.0)
    ),
    interiors = emptyList()  // Optional holes
)

// MultiPoint, MultiLineString, MultiPolygon for collections
val multiPoint = MultiPoint(listOf(Point(0.0, 0.0), Point(10.0, 10.0)))
```

**Key Properties:**
- `boundingBox`: Lazy-computed bounding box for each geometry
- `size`: Number of elements (LineString, MultiPoint, etc.)

### Feature

Features combine geometry with attribute data:

```kotlin
import geo.*

val feature = Feature(
    geometry = Point(-0.1276, 51.5074),
    properties = mapOf(
        "name" to "London",
        "population" to 8982000,
        "country" to "UK"
    )
)

// Access properties with type safety
val name: String? = feature.stringProperty("name")
val population: Int? = feature.intProperty("population")

// Generic access
val value: Any? = feature.property("name")

// Check property existence
if (feature.hasProperty("population")) { /* ... */ }

// Helper for point features
val london = Feature.fromPoint(-0.1276, 51.5074, mapOf("name" to "London"))
```

### Bounds

Bounding box representation:

```kotlin
import geo.*

val bounds = Bounds(minX = -10.0, minY = 40.0, maxX = 30.0, maxY = 60.0)

// Check intersection
val intersects = bounds.intersects(otherBounds)

// Check if point is inside
val contains = bounds.contains(x, y)

// Expand to include point or bounds
val expanded = bounds.expandToInclude(100.0, 200.0)

// Properties
val width = bounds.width
val height = bounds.height
val (cx, cy) = bounds.center

// Empty bounds (contains NaN)
val empty = Bounds.empty()
```

### GeoSource

Abstract data source with lazy iteration:

```kotlin
import geo.*

// Filter features
val filtered = source.filterFeatures { it.intProperty("population")!! > 1000000 }

// Spatial query (if backed by quadtree)
val nearby = (source as? GeoPackageSource)?.queryByBounds(bounds)

// Get all features as list (loads into memory)
val all = source.listFeatures()

// Get count efficiently
val count = source.countFeatures()

// Get total bounding box
val dataBounds = source.totalBoundingBox()

// Check if empty
if (source.isEmpty()) { /* no data */ }
```

---

### Loading Data

#### GeoJSON

```kotlin
import geo.GeoJSON

// Load from file
val source = GeoJSON.load("data.geojson")

// Convenience: get features directly
val features = GeoJSON.features("data.geojson")

// Parse from string
val sourceFromString = GeoJSON.loadString(geoJsonString)

// Features from string
val featuresFromString = GeoJSON.featuresString(geoJsonString)

// Supported geometries: Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon
// Supports both FeatureCollection and single Feature
```

#### GeoPackage

```kotlin
import geo.GeoPackage

// Load GeoPackage with spatial indexing
val source = GeoPackage.load("data.gpkg")

// Limit features to prevent OOM
val limited = GeoPackage.load("data.gpkg", maxFeatures = 10000)

// Convenience: get features directly
val features = GeoPackage.features("data.gpkg")

// GeoPackageSource provides efficient spatial queries
val gpkgSource = GeoPackage.load("data.gpkg")
val results = gpkgSource.queryByBounds(bounds)
```

---

## 2. Coordinate Systems

### GeoProjection Interface

All projections implement the common interface:

```kotlin
import geo.projection.GeoProjection
import org.openrndr.math.Vector2

val projection: GeoProjection = // ...

// Forward projection: geographic to screen
val screen: Vector2 = projection.project(Vector2(longitude, latitude))

// Inverse projection: screen to geographic
val geo: Vector2 = projection.unproject(screen)
```

### ProjectionFactory

Create preset projections:

```kotlin
import geo.projection.ProjectionFactory

// Web Mercator (default for web maps)
val mercator = ProjectionFactory.mercator(
    width = 800.0,
    height = 600.0,
    center = Vector2(0.0, 0.0),  // longitude, latitude
    scale = 1.0
)

// Equirectangular (simple lat/lng to xy)
val equirectangular = ProjectionFactory.equirectangular(width, height)

// British National Grid (EPSG:27700)
val bng = ProjectionFactory.bng(width, height)

// World-fitted projections
val worldMercator = ProjectionFactory.fitWorldMercator(width, height)
val worldEquirectangular = ProjectionFactory.fitWorldEquirectangular(width, height)
```

### CRSTransformer

Transform coordinates between CRS codes:

```kotlin
import geo.projection.CRSTransformer

// Create transformer (reusable for many coordinates)
val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")

// Transform coordinates
val (lon, lat) = transformer.transform(easting, northing)

// Common transformations
val toWgs84 = CRSTransformer("EPSG:27700", "EPSG:4326")
val toWebMercator = CRSTransformer("EPSG:4326", "EPSG:3857")

// Throws CRSTransformationException for invalid CRS codes
```

### CRS Extensions

Fluent API for common transformations:

```kotlin
import geo.projection.*

// Transform source to WGS84 (EPSG:4326)
val wgs84Source = source.toWGS84()

// Transform to Web Mercator (EPSG:3857)
val webMercatorSource = source.toWebMercator()

// Materialize lazy sequences to in-memory list
// Important for render loops
val cachedSource = source.materialize()

// Chained transformations
val result = GeoPackage.load("data.gpkg")
    .toWGS84()
    .materialize()
```

---

## 3. Rendering

The rendering module provides functions for drawing geometries with configurable styles. See [docs/rendering.md](rendering.md) for detailed documentation.

### Style Configuration

```kotlin
import geo.render.*
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin

// DSL-style style creation
val style = Style {
    fill = ColorRGBa.RED                    // Fill color (null = no fill)
    stroke = ColorRGBa.BLACK                 // Stroke color
    strokeWeight = 2.0                      // Line thickness
    size = 10.0                             // Point marker size
    shape = Shape.Circle                    // Point shape (Circle, Square, Triangle)
    lineCap = LineCap.ROUND                 // Line end style
    lineJoin = LineJoin.ROUND               // Corner style
    miterLimit = 4.0                        // Miter limit for MITER join
}

// Transparent style
val invisible = Style.transparent(alpha = 0.0)
```

### Drawing Functions

```kotlin
import geo.render.*

// Point rendering
drawPoint(drawer, x, y, Style { fill = ColorRGBa.RED })

// LineString rendering  
drawLineString(drawer, points, Style { stroke = ColorRGBa.BLUE })

// Polygon rendering
drawPolygon(drawer, exteriorPoints, Style { fill = ColorRGBa.GREEN })

// Multi geometries
drawMultiPoint(drawer, multiPoint, Style { size = 5.0 })
drawMultiLineString(drawer, multiLineString, Style { strokeWeight = 1.0 })
drawMultiPolygon(drawer, multiPolygon, Style { fill = ColorRGBa.YELLOW })
```

### Shape Options

```kotlin
// Circle (default) - renders as filled circle with diameter = size
Style { shape = Shape.Circle }

// Square - renders as filled square with side = size
Style { shape = Shape.Square }

// Triangle - renders as filled equilateral triangle with side = size
Style { shape = Shape.Triangle }
```

---

## 4. Layer System

### GeoLayer

Composable layer for use with orx-compositor:

```kotlin
import geo.layer.*
import geo.render.*
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.fx.blend.Multiply

// Create layer with DSL
val layer = GeoLayer {
    source = geoDataSource
    style = Style {
        fill = ColorRGBa.BLUE.withAlpha(0.5)
        stroke = ColorRGBa.BLACK
    }
}

// Convenience function
val layer2 = layer {
    source = otherSource
    style = Style { stroke = ColorRGBa.RED }
}
```

### Graticule

Generate coordinate grid for reference:

```kotlin
import geo.layer.*
import geo.Bounds
import geo.projection.ProjectionFactory

// Generate graticule points
val graticule = generateGraticule(
    spacing = 5.0,  // degrees between lines (min: 1.0)
    bounds = Bounds(-10.0, 40.0, 30.0, 60.0)
)

// Generate as GeoSource
val graticuleSource = generateGraticuleSource(5.0, bounds)

// Common spacing values:
//   1.0 - Detailed view
//   5.0 - Regional view (recommended)
//  10.0 - Continental view

// Use in layer
val graticuleLayer = layer {
    source = graticuleSource
    style = Style {
        stroke = ColorRGBa.WHITE.withAlpha(0.3)
        strokeWeight = 0.5
    }
}
```

---

## 5. Animation

### GeoAnimator

Global animation controller integrating with OpenRNDR's Animatable:

```kotlin
import geo.animation.*
import org.openrndr.application

fun main() = application {
    program {
        // Get animator singleton
        val animator = animator()

        // Configure animations using property references
        animator.apply {
            ::x.animate(300.0, 2000, Easing.CubicInOut)
            ::y.animate(200.0, 2000, Easing.CubicOut)
            ::progress.animate(1.0, 3000)
        }

        extend {
            // CRITICAL: Update animations each frame
            animator.updateAnimation()

            // Read animated values
            drawer.circle(animator.x, animator.y, 100.0)
        }
    }
}
```

### Easing Functions

15 convenience functions for common easing curves:

```kotlin
import geo.animation.*

// Most common
easeInOut()  // CubicInOut - default for most animations
easeOut()    // CubicOut - natural deceleration
easeIn()     // CubicIn - building momentum

// Linear
linear()     // Constant velocity

// Sine variants (gentle curves)
sineIn()
sineOut()
sineInOut()

// Quadratic variants (subtle)
quadIn()
quadOut()
quadInOut()

// Quartic variants (emphatic)
quartIn()
quartInOut()

// Explicit cubic (same as easeIn/Out)
cubicIn()
cubicOut()
cubicInOut()
```

### Procedural Motion

#### Stagger by Index

Sequential animation delays based on position:

```kotlin
import geo.animation.*

// Each feature starts 50ms after the previous
val staggered = features.staggerByIndex(delayMs = 50)

staggered.forEach { wrapper ->
    // wrapper.feature - the geo feature
    // wrapper.delay - computed delay in ms
    delay(wrapper.delay)
    animateFeature(wrapper.feature)
}
```

#### Stagger by Distance

Spatial ripple effect from origin:

```kotlin
import geo.animation.*
import org.openrndr.math.Vector2

// Ripple from origin point (earthquake, explosion effect)
val origin = Vector2(0.0, 0.0)
val ripple = features.staggerByDistance(origin, factor = 10.0)

ripple.forEach { wrapper ->
    // Delay proportional to distance from origin
    val delay = wrapper.delay
    // Animate feature arriving at wrapper.delay
}
```

### Composition

#### GeoTimeline

Explicit timing control for coordinated animations:

```kotlin
import geo.animation.composition.*
import geo.animation.*

val timeline = GeoTimeline {
    // First animation starts immediately
    val anim1 = GeoAnimator().apply {
        ::x.animate(100.0, 1000)
    }
    add(anim1)

    // Second starts 500ms after first
    val anim2 = GeoAnimator().apply {
        ::y.animate(100.0, 1000)
    }
    add(anim2, offset = 500)
}

extend {
    timeline.update()
}
```

#### ChainedAnimation

Fluent sequential composition:

```kotlin
import geo.animation.composition.*

// Build animation chain
val chain = ChainedAnimationBuilder()
    .then(animateFeature1)
    .then(animateFeature2)
    .then(animateFeature3)

// Execute
extend {
    chain.update()
}
```

---

## Type Conversions

### Point Conversions

```kotlin
import geo.Point
import org.openrndr.math.Vector2

// Point to Vector2
val vector: Vector2 = point.toVector2()

// Vector2 to Point
val point2: Point = Point.fromVector2(vector)

// Point to screen coordinates
val screen: Vector2 = point.toScreen(projection)
```

---

## Error Handling

```kotlin
import geo.GeoJSON
import geo.exception.ProjectionOverflowException
import geo.projection.CRSTransformationException

// GeoJSON loading
try {
    val data = GeoJSON.load("data.geojson")
} catch (e: FileNotFoundException) {
    // File doesn't exist
} catch (e: IllegalArgumentException) {
    // Invalid GeoJSON format
}

// CRS transformations
try {
    val transformed = source.autoTransformTo("EPSG:4326")
} catch (e: CRSTransformationException) {
    // Invalid CRS code or transformation failure
}

// Projection overflow (Mercator poles)
try {
    val projected = projection.project(latLng)
} catch (e: ProjectionOverflowException) {
    // Coordinate outside projectable range
    // Use clampLat() to normalize
}
```

---

## Dependencies

Key library dependencies used by openrndr-geo:

| Library | Purpose |
|---------|---------|
| openrndr | Drawing and animation framework |
| kotlinx.serialization | GeoJSON parsing |
| mil.nga:geopackage | GeoPackage file handling |
| proj4j | CRS transformations |
