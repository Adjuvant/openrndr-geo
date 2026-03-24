package geo.render

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import geo.core.Polygon
import geo.projection.GeoProjection

/**
 * Public API for drawing LineString and Polygon geometries.
 *
 * This module provides convenient functions for rendering geo primitives with
 * configurable styling. For Point rendering, see [drawPoint] in PointRenderer.kt.
 *
 * ## Quick Start
 * ```kotlin
 * extend {
 *     // Draw a line with custom style
 *     val points = listOf(Vector2(0.0, 0.0), Vector2(100.0, 100.0))
 *     drawLineString(drawer, points, Style {
 *         stroke = ColorRGBa.RED
 *         strokeWeight = 2.0
 *     })
 *
 *     // Draw a polygon with semi-transparent fill
 *     val poly = listOf(Vector2(100.0, 100.0), Vector2(200.0, 100.0), Vector2(150.0, 200.0))
 *     drawPolygon(drawer, poly, Style {
 *         fill = ColorRGBa.BLUE.withAlpha(0.5)
 *     })
 * }
 * ```
 *
 * ## Style Merging
 * All drawing functions use style merging:
 * 1. System defaults provide base values (see [StyleDefaults])
 * 2. User style overrides specific properties
 * 3. Result is applied to the drawer before drawing
 *
 * @see Style Style configuration with mutable properties
 * @see StyleDefaults System defaults per geometry type
 * @see mergeStyles Style merging with user override precedence
 * @see drawPoint Point rendering (in PointRenderer.kt)
 */

/**
 * Draw a LineString geometry as a connected sequence of line segments.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val points = listOf(Vector2(0.0, 0.0), Vector2(100.0, 50.0), Vector2(200.0, 0.0))
 *
 *     // Use defaults
 *     drawLineString(drawer, points)
 *
 *     // Override style
 *     drawLineString(drawer, points, Style {
 *         stroke = ColorRGBa.RED
 *         strokeWeight = 2.0
 *         lineCap = LineCap.ROUND
 *         lineJoin = LineJoin.ROUND
 *     })
 * }
 * ```
 *
 * ## Line Caps and Joins
 * - **BUTT cap**: Line ends flush with endpoint
 * - **ROUND cap**: Line ends with semicircle
 * - **SQUARE cap**: Line ends with square extension
 * - **MITER join**: Sharp corners (clipped at miterLimit)
 * - **ROUND join**: Rounded corners
 * - **BEVEL join**: Chamfered corners
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param points List of Vector2 points defining the line string
 * @param userStyle Style configuration (null = use defaultLineStyle)
 *
 * @see writeLineString Internal implementation
 * @see StyleDefaults.defaultLineStyle Default line style
 */
fun drawLineString(
    drawer: Drawer,
    points: List<Vector2>,
    userStyle: Style? = null
) {
    val style = mergeStyles(StyleDefaults.defaultLineStyle, userStyle)
    writeLineString(drawer, points, style)
}

/**
 * Draw a Polygon geometry with fill and stroke.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val points = listOf(
 *         Vector2(100.0, 100.0),
 *         Vector2(200.0, 100.0),
 *         Vector2(150.0, 200.0)
 *     )
 *
 *     // Use defaults (outline only)
 *     drawPolygon(drawer, points)
 *
 *     // With semi-transparent fill
 *     drawPolygon(drawer, points, Style {
 *         fill = ColorRGBa.BLUE.withAlpha(0.5)
 *         stroke = ColorRGBa.BLACK
 *         strokeWeight = 2.0
 *     })
 * }
 * ```
 *
 * ## Fill Opacity
 * Use ColorRGBa.withAlpha() to control fill transparency:
 * ```kotlin
 * fill = ColorRGBa.RED.withAlpha(0.3)  // 30% opacity
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param points List of Vector2 points defining the polygon exterior ring
 * @param userStyle Style configuration (null = use defaultPolygonStyle)
 *
 * @see writePolygon Internal implementation
 * @see StyleDefaults.defaultPolygonStyle Default polygon style
 */
fun drawPolygon(
    drawer: Drawer,
    points: List<Vector2>,
    userStyle: Style? = null
) {
    val style = mergeStyles(StyleDefaults.defaultPolygonStyle, userStyle)
    writePolygon(drawer, points, style)
}

/**
 * Draw a Polygon geometry with automatic hole detection and rendering.
 *
 * If the polygon has interior rings (holes), they are rendered as transparent
 * cutouts. If the polygon has no holes, delegates to the simple polygon renderer.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     // Polygon with holes (holes render automatically)
 *     val polygon = geo.core.Polygon(
 *         exterior = listOf(Vector2(0.0, 0.0), Vector2(100.0, 0.0), Vector2(50.0, 100.0)),
 *         interiors = listOf(
 *             listOf(Vector2(20.0, 20.0), Vector2(40.0, 20.0), Vector2(30.0, 40.0))
 *         )
 *     )
 *     drawPolygon(drawer, polygon, projection, Style {
 *         fill = ColorRGBa.BLUE.withAlpha(0.5)
 *     })
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param polygon Polygon geometry (may have interior rings/holes)
 * @param projection Geographic projection to use for coordinate transformation
 * @param userStyle Style configuration (null = use defaultPolygonStyle)
 *
 * @see writePolygonWithHoles Internal implementation for polygons with holes
 * @see geo.core.Polygon Geometry class with exterior and interior rings
 */
fun drawPolygon(
    drawer: Drawer,
    polygon: Polygon,
    projection: GeoProjection,
    userStyle: Style? = null
) {
    val style = mergeStyles(StyleDefaults.defaultPolygonStyle, userStyle)

    if (polygon.hasHoles()) {
        // Render with holes using Shape API
        val exterior = polygon.exteriorToScreen(projection)
        val interiors = polygon.interiorsToScreen(projection)
        writePolygonWithHoles(drawer, exterior, interiors, style)
    } else {
        // Simple polygon without holes
        val exterior = polygon.exteriorToScreen(projection)
        writePolygon(drawer, exterior, style)
    }
}
