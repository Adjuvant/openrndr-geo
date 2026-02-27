package geo.render

import geo.Bounds
import geo.Feature
import geo.GeoJSON
import geo.GeoJSONSource
import geo.GeoSource
import geo.Geometry
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2

/**
 * Extension functions for Drawer providing simplified GeoJSON rendering.
 * 
 * ## Tier 1: One-line rendering
 * ```kotlin
 * extend {
 *     drawer.geoJSON("world.json")  // Auto-load, auto-fit, auto-render
 * }
 * ```
 * 
 * ## Tier 2: Load once, draw many
 * ```kotlin
 * val source = geoSource("data.json")
 * extend {
 *     source.render(drawer)
 * }
 * ```
 * 
 * ## Tier 3: Full control
 * ```kotlin
 * val features = GeoJSON.load("data.json")
 * val projection = ProjectionMercator { width = 800; height = 600 }
 * extend {
 *     features.forEach { drawer.draw(it.geometry, projection) }
 * }
 * ```
 */

/**
 * Draw a GeoJSON file with automatic loading, projection fitting, and rendering.
 * 
 * This is the simplest possible workflow: provide a file path and get on-screen geometry.
 * 
 * ## Usage
 * ```kotlin
 * extend {
 *     drawer.geoJSON("world.json")
 * }
 * ```
 * 
 * @param path Path to GeoJSON file
 * @param projection Optional projection (defaults to Mercator fitted to data bounds)
 * @param style Optional rendering style (null = use defaults)
 * @throws FileNotFoundException if the file doesn't exist
 */
fun Drawer.geoJSON(
    path: String,
    projection: GeoProjection? = null,
    style: Style? = null
) {
    val source = GeoJSON.load(path)
    val proj = projection ?: ProjectionFactory.fitBounds(
        bounds = source.boundingBox(),
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.9,
        projection = ProjectionType.MERCATOR
    )
    source.render(this, proj, style)
}

/**
 * Draw a GeoJSONSource with automatic projection fitting.
 * 
 * ## Usage
 * ```kotlin
 * val source = GeoJSON.load("world.json")
 * extend {
 *     drawer.geoSource(source)
 * }
 * ```
 * 
 * @param source The GeoJSONSource to render
 * @param projection Optional projection (defaults to Mercator fitted to source bounds)
 * @param style Optional rendering style
 */
fun Drawer.geoSource(
    source: GeoJSONSource,
    projection: GeoProjection? = null,
    style: Style? = null
) {
    val proj = projection ?: ProjectionFactory.fitBounds(
        bounds = source.boundingBox(),
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.9,
        projection = ProjectionType.MERCATOR
    )
    source.render(this, proj, style)
}

/**
 * Draw a geometry with automatic projection creation.
 * 
 * @param geometry The geometry to render
 * @param projection Optional projection (defaults to Mercator fitted to geometry bounds)
 * @param style Optional rendering style
 */
fun Drawer.geo(
    geometry: Geometry,
    projection: GeoProjection? = null,
    style: Style? = null
) {
    val bounds = geometry.boundingBox
    val proj = projection ?: ProjectionFactory.fitBounds(
        bounds = bounds,
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.9,
        projection = ProjectionType.MERCATOR
    )
    geometry.renderToDrawer(this, proj, style)
}

/**
 * Draw a sequence of features with automatic projection fitting.
 * 
 * @param features Features to render
 * @param projection Optional projection (defaults to Mercator fitted to features bounds)
 * @param style Optional rendering style
 */
fun Drawer.geoFeatures(
    features: Sequence<Feature>,
    projection: GeoProjection? = null,
    style: Style? = null
) {
    val featureList = features.toList()
    if (featureList.isEmpty()) return
    
    val bounds = featureList.fold(Bounds.empty()) { acc, f ->
        acc.expandToInclude(f.boundingBox)
    }
    
    val proj = projection ?: ProjectionFactory.fitBounds(
        bounds = bounds,
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.9,
        projection = ProjectionType.MERCATOR
    )
    
    featureList.forEach { feature ->
        feature.geometry.renderToDrawer(this, proj, style)
    }
}

/**
 * Draw a GeoSource with optional configuration block.
 *
 * ## Tier 1: Beginner - one-liner
 * ```kotlin
 * drawer.geo(source)  // Auto-fit projection, default style
 * ```
 *
 * ## Tier 2: Professional - config block
 * ```kotlin
 * drawer.geo(source) {
 *     projection = ProjectionMercator { width = 800; height = 600 }
 *     style = Style { fill = ColorRGBa.RED }
 *     styleByType = mapOf("Polygon" to polygonStyle)
 *     styleByFeature = { feature -> 
 *         if (feature.doubleProperty("pop") > 1000000) Style { stroke = ColorRGBa.RED }
 *         else null
 *     }
 * }
 * ```
 *
 * @param source The GeoSource to render (any implementation: GeoJSON, GeoPackage, etc.)
 * @param block Optional configuration block
 */
fun Drawer.geo(source: GeoSource, block: (GeoRenderConfig.() -> Unit)? = null) {
    val config = block?.let { GeoRenderConfig().apply(it) } ?: GeoRenderConfig()
    
    // Auto-fit projection if not specified (beginner-friendly default)
    val proj = config.projection ?: ProjectionFactory.fitBounds(
        bounds = source.totalBoundingBox(),
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.9,
        projection = ProjectionType.MERCATOR
    )
    
    // Snapshot config for safe iteration
    val resolved = config.snapshot()
    
    // Render each feature with style resolution
    source.features.forEach { feature ->
        val style = resolveStyle(feature, resolved)
        feature.geometry.renderToDrawer(this, proj, style)
    }
}

/**
 * Render this geometry to the given Drawer.
 */
private fun Geometry.renderToDrawer(drawer: Drawer, projection: GeoProjection, style: Style?) {
    when (this) {
        is geo.Point -> {
            val screen = projection.project(Vector2(x, y))
            drawPoint(drawer, screen, style)
        }
        is geo.LineString -> {
            val screenPoints = points.map { projection.project(it) }
            drawLineString(drawer, screenPoints, style)
        }
        is geo.Polygon -> {
            val screenPoints = exterior.map { projection.project(it) }
            drawPolygon(drawer, screenPoints, style)
        }
        is geo.MultiPoint -> {
            points.forEach { pt ->
                val screen = projection.project(Vector2(pt.x, pt.y))
                drawPoint(drawer, screen, style)
            }
        }
        is geo.MultiLineString -> {
            lineStrings.forEach { line ->
                val screenPoints = line.points.map { projection.project(it) }
                drawLineString(drawer, screenPoints, style)
            }
        }
        is geo.MultiPolygon -> {
            polygons.forEach { poly ->
                val screenPoints = poly.exterior.map { projection.project(it) }
                drawPolygon(drawer, screenPoints, style)
            }
        }
    }
}
