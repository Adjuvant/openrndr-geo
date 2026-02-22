# Rendering Documentation

This guide covers the rendering API for visualizing geo primitives with OpenRNDR.

## Overview

The rendering module provides a consistent API for drawing Point, LineString, Polygon, and Multi* geometries with configurable styling. All drawing functions follow the same pattern:

1. Accept a `Drawer` context and geometry data
2. Accept an optional `Style` parameter (uses sensible defaults if not provided)
3. Apply style properties to the drawer before drawing

## Basic Usage: Creating Styles

### Creating Styles with DSL

Use the `Style` class with its invoke operator for clean DSL syntax:

```kotlin
import geo.render.Style
import org.openrndr.color.ColorRGBa

// Create a style with the DSL
val pointStyle = Style {
    fill = ColorRGBa.RED
    stroke = ColorRGBa.BLACK
    size = 10.0
    shape = Shape.Circle
}

val lineStyle = Style {
    stroke = ColorRGBa.BLUE
    strokeWeight = 2.0
    lineCap = LineCap.ROUND
    lineJoin = LineJoin.ROUND
}

val polygonStyle = Style {
    fill = ColorRGBa.GREEN.withAlpha(0.5)  // 50% transparent
    stroke = ColorRGBa.BLACK
    strokeWeight = 1.5
}
```

### Using Defaults (No Style Parameter)

All drawing functions have optional style parameters, so you can call them without specifying a style:

```kotlin
extend {
    // Uses defaultPointStyle: white circle, 5px diameter
    drawPoint(drawer, 100.0, 100.0)

    // Uses defaultLineStyle: white line, 1px weight
    drawLineString(drawer, points)

    // Uses defaultPolygonStyle: outline only (transparent fill)
    drawPolygon(drawer, polygonPoints)
}
```

## Rendering Geometry Types

### Point Geometries

Render single points or use shapes for emphasis:

```kotlin
import geo.render.drawPoint
import geo.render.Shape

extend {
    // Basic point
    drawPoint(drawer, screenX, screenY)

    // With custom style
    drawPoint(drawer, screenX, screenY, Style {
        size = 12.0
        shape = Shape.Square
        fill = ColorRGBa.BLUE
    })

    // Triangle marker
    drawPoint(drawer, screenX, screenY, Style {
        size = 8.0
        shape = Shape.Triangle
        fill = ColorRGBa.RED
        stroke = ColorRGBa.BLACK
    })
}
```

### LineString Geometries

Draw connected line segments with various cap and join styles:

```kotlin
import geo.render.drawLineString
import org.openrndr.math.Vector2

extend {
    val points = listOf(
        Vector2(0.0, 0.0),
        Vector2(100.0, 50.0),
        Vector2(200.0, 0.0)
    )

    // Basic line string
    drawLineString(drawer, points)

    // Styled with round caps and joins
    drawLineString(drawer, points, Style {
        stroke = ColorRGBa.RED
        strokeWeight = 3.0
        lineCap = LineCap.ROUND
        lineJoin = LineJoin.ROUND
    })
}
```

### Polygon Geometries

Fill polygons with color or render as outlines:

```kotlin
import geo.render.drawPolygon
import org.openrndr.math.Vector2

extend {
    val exterior = listOf(
        Vector2(100.0, 100.0),
        Vector2(200.0, 100.0),
        Vector2(150.0, 200.0)
    )

    // Outline only (default)
    drawPolygon(drawer, exterior)

    // With semi-transparent fill
    drawPolygon(drawer, exterior, Style {
        fill = ColorRGBa.BLUE.withAlpha(0.3)  // 30% opacity
        stroke = ColorRGBa.BLACK
        strokeWeight = 2.0
    })
}
```

### MultiPoint Geometries

Render collections of points with consistent styling:

```kotlin
import geo.render.drawMultiPoint
import geo.Point
import geo.MultiPoint

extend {
    val multiPoint = MultiPoint(listOf(
        Point(0.0, 0.0),
        Point(100.0, 50.0),
        Point(200.0, 100.0)
    ))

    // Draw all points with same style
    drawMultiPoint(drawer, multiPoint, Style {
        size = 8.0
        shape = Shape.Circle
        fill = ColorRGBa.RED
    })
}
```

### MultiLineString Geometries

Render multiple line strings efficiently:

```kotlin
import geo.render.drawMultiLineString
import geo.LineString
import geo.MultiLineString

extend {
    val lines = listOf(
        LineString(listOf(Vector2(0.0, 0.0), Vector2(100.0, 50.0))),
        LineString(listOf(Vector2(100.0, 50.0), Vector2(200.0, 100.0)))
    )
    val multiLineString = MultiLineString(lines)

    // Draw all line strings with same style
    drawMultiLineString(drawer, multiLineString, Style {
        stroke = ColorRGBa.BLUE
        strokeWeight = 2.0
        lineCap = LineCap.ROUND
    })
}
```

### MultiPolygon Geometries

Render polygon collections (exterior rings only in v1):

```kotlin
import geo.render.drawMultiPolygon
import geo.Polygon
import geo.MultiPolygon

extend {
    val polygons = listOf(
        Polygon(listOf(Vector2(0.0, 0.0), Vector2(100.0, 0.0), Vector2(50.0, 100.0))),
        Polygon(listOf(Vector2(150.0, 0.0), Vector2(250.0, 0.0), Vector2(200.0, 100.0)))
    )
    val multiPolygon = MultiPolygon(polygons)

    // Draw all polygons with same style
    drawMultiPolygon(drawer, multiPolygon, Style {
        fill = ColorRGBa.GREEN.withAlpha(0.3)
        stroke = ColorRGBa.BLACK
        strokeWeight = 1.5
    })
}
```

## Shape Options for Points

Available shapes for point rendering:

```kotlin
// Circle (default) - diameter = size
Style { shape = Shape.Circle }

// Square - side length = size
Style { shape = Shape.Square }

// Triangle - equilateral triangle with side = size
Style { shape = Shape.Triangle }
```

## Line Caps and Joins

Control line appearance with caps and joins:

### Line Caps (how line ends are drawn)

```kotlin
// BUTT cap - line ends flush with endpoint (default)
Style { lineCap = LineCap.BUTT }

// ROUND cap - line ends with semicircle
Style { lineCap = LineCap.ROUND }

// SQUARE cap - line ends with square extension
Style { lineCap = LineCap.SQUARE }
```

### Line Joins (how line segments connect)

```kotlin
// MITER join - sharp corners (default)
// Clipped at miterLimit to prevent infinite points
Style {
    lineJoin = LineJoin.MITER
    miterLimit = 4.0  // Default
}

// ROUND join - rounded corners
Style { lineJoin = LineJoin.ROUND }

// BEVEL join - chamfered corners
Style { lineJoin = LineJoin.BEVEL }
```

## Fill Opacity

Control transparency using ColorRGBa's `withAlpha()` method:

```kotlin
// Fully opaque
Style { fill = ColorRGBa.RED }

// 50% transparent
Style { fill = ColorRGBa.RED.withAlpha(0.5) }

// 25% transparent (subtle overlay)
Style { fill = ColorRGBa.BLUE.withAlpha(0.25) }

// Fully transparent (invisible fill, outline only)
Style { fill = ColorRGBa.WHITE.withAlpha(0.0) }
```

## Performance: Reusing Style Objects

For animation and rendering many features, reuse Style objects to avoid allocation:

```kotlin
// GOOD: Zero allocation - mutate existing style
val style = Style { size = 5.0 }

extend {
    animate {
        // Mutate properties (no allocation)
        style.size += 0.1
        style.fill = currentColor

        points.forEach { point ->
            drawPoint(drawer, point.x, point.y, style)
        }
    }
}

// BAD: Creates new Style every frame (causes GC pressure)
extend {
    animate {
        points.forEach { point ->
            // Allocation in hot path - avoid!
            val style = Style {
                size = 5.0 + animationValue
                fill = currentColor
            }
            drawPoint(drawer, point.x, point.y, style)
        }
    }
}
```

## Working with Projections

Use `Point.toScreen()` to convert geographic coordinates to screen coordinates:

```kotlin
import geo.projection.ProjectionFactory
import geo.Point

val projection = ProjectionFactory.mercator(width = 800, height = 600)

extend {
    val point = Point(-0.1276, 51.5074)  // London
    val screenPos = point.toScreen(projection)

    drawPoint(drawer, screenPos.x, screenPos.y, Style {
        size = 8.0
        fill = ColorRGBa.RED
    })
}
```

## Complete Example

```kotlin
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.math.Vector2
import geo.projection.ProjectionFactory
import geo.render.*
import geo.Point
import geo.LineString
import geo.Polygon

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        val projection = ProjectionFactory.mercator(width, height)

        // Sample data
        val london = Point(-0.1276, 51.5074)
        val paris = Point(2.3522, 48.8566)
        val berlin = Point(13.4050, 52.5200)

        val cityPoints = listOf(london, paris, berlin)

        val route = LineString(listOf(
            Vector2(100.0, 100.0),
            Vector2(200.0, 150.0),
            Vector2(300.0, 200.0)
        ))

        val region = listOf(
            Vector2(400.0, 400.0),
            Vector2(500.0, 400.0),
            Vector2(550.0, 500.0),
            Vector2(450.0, 550.0),
            Vector2(350.0, 500.0)
        )

        // Reusable styles
        val cityStyle = Style {
            size = 8.0
            fill = ColorRGBa.RED
            stroke = ColorRGBa.BLACK
            shape = Shape.Circle
        }

        val routeStyle = Style {
            stroke = ColorRGBa.BLUE
            strokeWeight = 2.0
            lineCap = LineCap.ROUND
            lineJoin = LineJoin.ROUND
        }

        val regionStyle = Style {
            fill = ColorRGBa.GREEN.withAlpha(0.3)
            stroke = ColorRGBa.BLACK
            strokeWeight = 1.5
        }

        extend {
            // Draw cities
            cityPoints.forEach { city ->
                val screen = city.toScreen(projection)
                drawPoint(drawer, screen.x, screen.y, cityStyle)
            }

            // Draw route
            drawLineString(drawer, route.points, routeStyle)

            // Draw region
            drawPolygon(drawer, region, regionStyle)
        }
    }
}
```
