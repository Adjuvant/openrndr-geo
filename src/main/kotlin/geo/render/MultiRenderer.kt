package geo.render

import geo.MultiLineString
import geo.MultiPoint
import geo.MultiPolygon
import geo.Polygon
import geo.projection.GeoProjection
import geo.projection.MAX_MERCATOR_LAT
import geo.projection.ProjectionMercator
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import geo.render.writePolygonWithHoles

/**
 * Draw a MultiPoint geometry as a collection of points with consistent styling.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val multiPoint = MultiPoint(listOf(
 *         Point(0.0, 0.0),
 *         Point(100.0, 50.0),
 *         Point(200.0, 100.0)
 *     ))
 *
 *     // Draw all points with same style
 *     drawMultiPoint(drawer, multiPoint, Style {
 *         size = 8.0
 *         shape = Shape.Circle
 *         fill = ColorRGBa.RED
 *     })
 * }
 * ```
 *
 * ## Performance
 * For best performance with large MultiPoint collections, reuse a single Style
 * object and mutate properties:
 * ```kotlin
 * val style = Style { size = 5.0 }
 * data.features.forEach { feature ->
 *     val geom = feature.geometry
 *     if (geom is MultiPoint) {
 *         style.fill = featureColor(feature)
 *         drawMultiPoint(drawer, geom, style)
 *     }
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param multiPoint MultiPoint geometry containing points to render
 * @param projection Geographic projection to use for rendering
 * @param userStyle Style configuration (null = use defaultPointStyle)
 *
 * @see drawPoint Individual point rendering
 * @see MultiPoint Geometry class for point collections
 */
fun drawMultiPoint(
    drawer: Drawer,
    multiPoint: MultiPoint,
    projection: GeoProjection,
    userStyle: Style? = null
) {
    multiPoint.points.forEach { point ->
        drawPoint(drawer, point.toScreen(projection), userStyle)
    }
}

/**
 * Draw a MultiLineString geometry as a collection of lines with consistent styling.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val lines = listOf(
 *         LineString(listOf(Vector2(0.0, 0.0), Vector2(100.0, 50.0))),
 *         LineString(listOf(Vector2(100.0, 50.0), Vector2(200.0, 100.0)))
 *     )
 *     val multiLineString = MultiLineString(lines)
 *
 *     // Draw all line strings with same style
 *     drawMultiLineString(drawer, multiLineString, Style {
 *         stroke = ColorRGBa.BLUE
 *         strokeWeight = 2.0
 *         lineCap = LineCap.ROUND
 *     })
 * }
 * ```
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param multiLineString MultiLineString geometry containing line strings to render
 * @param projection Geographic projection to use for rendering
 * @param userStyle Style configuration (null = use defaultLineStyle)
 *
 * @see drawLineString Individual line string rendering
 * @see MultiLineString Geometry class for line string collections
 */
fun drawMultiLineString(
    drawer: Drawer,
    multiLineString: MultiLineString,
    projection: GeoProjection,
    userStyle: Style? = null
) {
    multiLineString.lineStrings.forEach { lineString ->
        drawLineString(drawer, lineString.toScreen(projection), userStyle)
    }
}

/**
 * Draw a MultiPolygon geometry as a collection of polygons with consistent styling.
 *
 * Handles geometries that may span beyond valid Mercator bounds (e.g., ocean data
 * covering the whole world) by automatically clamping coordinates to valid range.
 *
 * ## Usage
 * ```kotlin
 * extend {
 *     val polygons = listOf(
 *         Polygon(listOf(Vector2(0.0, 0.0), Vector2(100.0, 0.0), Vector2(50.0, 100.0))),
 *         Polygon(listOf(Vector2(150.0, 0.0), Vector2(250.0, 0.0), Vector2(200.0, 100.0)))
 *     )
 *     val multiPolygon = MultiPolygon(polygons)
 *
 *     // Draw all polygons with same style
 *     drawMultiPolygon(drawer, multiPolygon, Style {
 *         fill = ColorRGBa.GREEN.withAlpha(0.3)
 *         stroke = ColorRGBa.BLACK
 *         strokeWeight = 1.5
 *     })
 * }
 * ```
 *
 * ## Ocean/Whole-World Data
 * When rendering data that includes coordinates beyond ±85.05112878° latitude
 * (e.g., ocean polygons covering the poles), coordinates are automatically clamped
 * to prevent projection overflow artifacts.
 *
 * @param drawer OpenRNDR Drawer context for rendering
 * @param multiPolygon MultiPolygon geometry containing polygons to render
 * @param projection Geographic projection to use for rendering to screen space
 * @param userStyle Style configuration (null = use defaultPolygonStyle)
 * @param clampToMercatorBounds If true (default), automatically clamp coordinates
 *                              to valid Mercator range to prevent artifacts
 *
 * @see drawPolygon Individual polygon rendering
 * @see MultiPolygon Geometry class for polygon collections
 */
fun drawMultiPolygon(
    drawer: Drawer,
    multiPolygon: MultiPolygon,
    projection: GeoProjection,
    userStyle: Style? = null,
    clampToMercatorBounds: Boolean = true
) {
    val style = mergeStyles(StyleDefaults.defaultPolygonStyle, userStyle)

    // For Mercator projections, optionally clamp coordinates to valid range
    // This prevents artifacts when rendering ocean/whole-world data
    val polygonsToRender = if (clampToMercatorBounds && projection is ProjectionMercator) {
        multiPolygon.polygons.map { polygon ->
            // Clamp both exterior and interior coordinates to valid Mercator range
            val clampedExterior = polygon.exterior.map { coord ->
                Vector2(coord.x, coord.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            }
            val clampedInteriors = polygon.interiors.map { ring ->
                ring.map { coord ->
                    Vector2(coord.x, coord.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
                }
            }
            Polygon(clampedExterior, clampedInteriors)
        }
    } else {
        multiPolygon.polygons
    }

    polygonsToRender.forEach { polygon ->
        if (polygon.hasHoles()) {
            // Render polygon with holes
            val exterior = polygon.exteriorToScreen(projection)
            val interiors = polygon.interiorsToScreen(projection)
            writePolygonWithHoles(drawer, exterior, interiors, style)
        } else {
            // Simple polygon without holes
            val exterior = polygon.exteriorToScreen(projection)
            writePolygon(drawer, exterior, style)
        }
    }
}
