package geo.render

import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2

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
