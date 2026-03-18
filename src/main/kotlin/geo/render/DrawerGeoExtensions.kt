package geo.render

import geo.Bounds
import geo.Feature
import geo.GeoJSON
import geo.GeoJSONSource
import geo.GeoSource
import geo.Geometry
import geo.internal.OptimizedFeature
import geo.internal.OptimizedGeoSource
import geo.internal.cache.ViewportCache
import geo.internal.cache.ViewportState
import geo.internal.geometry.OptimizedLineString
import geo.internal.geometry.OptimizedMultiLineString
import geo.internal.geometry.OptimizedMultiPoint
import geo.internal.geometry.OptimizedMultiPolygon
import geo.internal.geometry.OptimizedPoint
import geo.internal.geometry.OptimizedPolygon
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import geo.render.StyleDefaults

/**
 * Viewport cache for Drawer.geo() extension function.
 * Shared across all calls to Drawer.geo() for performance optimization.
 */
private val drawerGeoCache = ViewportCache<Any, List<Shape>>()

// Helper to render list of shapes to drawer
private fun renderShapeList(drawer: Drawer, shapes: List<Shape>, style: Style?) {
    shapes.forEach {
        drawer.shape(it)
    }
}

/**
 * Extension for OptimizedFeature to provide screen coordinates.
 * Delegates to the underlying optimized geometry's projection methods.
 */
internal fun OptimizedFeature.toScreenCoordinates(projection: GeoProjection): List<Vector2> {
    val geom = this.optimizedGeometry
    return when (geom) {
        is OptimizedPoint -> geom.toScreenCoordinatesList(projection)
        is OptimizedLineString -> geom.toScreenCoordinatesList(projection)
        is OptimizedMultiLineString -> geom.toScreenCoordinatesList(projection).flatten()
        is OptimizedPolygon -> {
            val (exterior, interiors) = geom.toScreenCoordinates(projection)
            exterior.toList() + interiors.flatMap { it.toList() }
        }
        is OptimizedMultiPolygon -> {
            val result = mutableListOf<Vector2>()
            for ((exterior, interiors) in geom.toScreenCoordinates(projection)) {
                result.addAll(exterior.toList())
                for (interior in interiors) {
                    result.addAll(interior.toList())
                }
            }
            result
        }
        else -> emptyList()
    }
}

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
 * Uses viewport caching to avoid redundant coordinate projection
 * when rendering the same geometry with unchanged viewport state.
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

    val viewportState = ViewportState.fromProjection(proj)

    // Cache key for standard path is geometry
    val shapes: List<Shape> = drawerGeoCache.get(geometry as Any, viewportState) {
        // Build shapes from geometry exterior and interiors for caching
        val contours = mutableListOf<ShapeContour>()
        when (geometry) {
            is geo.Polygon -> {
                contours.add(ShapeContour.fromPoints(geometry.exterior, closed = true).clockwise)
                geometry.interiors.forEach { ring ->
                    if (ring.size >= 3) contours.add(ShapeContour.fromPoints(ring, closed = true).counterClockwise)
                }
            }
            is geo.MultiPolygon -> {
                geometry.polygons.forEach { poly ->
                    contours.add(ShapeContour.fromPoints(poly.exterior, closed = true).clockwise)
                    poly.interiors.forEach { ring ->
                        if (ring.size >= 3) contours.add(ShapeContour.fromPoints(ring, closed = true).counterClockwise)
                    }
                }
            }
            else -> {
                val projectedPoints = projectGeometryToArray(geometry, proj).toList()
                contours.add(ShapeContour.fromPoints(projectedPoints, closed = false))
            }
        }

        listOf(Shape(contours))
    }

    renderShapeList(this, shapes, style)
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
        padding = 0.0,
        projection = ProjectionType.MERCATOR
    )
    
    featureList.forEach { feature ->
        feature.geometry.renderToDrawer(this, proj, style)
    }
}

/**
 * Draw a sequence of features with automatic projection fitting and configuration block support.
 *
 * ## Tier 1: Beginner - one-liner
 * ```kotlin
 * val features = GeoJSON.load("data.json").features
 * drawer.geoFeatures(features)  // Auto-fit projection, default style
 * ```
 *
 * ## Tier 2: Professional - config block
 * ```kotlin
 * drawer.geoFeatures(features) {
 *     projection = ProjectionMercator { width = 800; height = 600 }
 *     style = Style { stroke = ColorRGBa.WHITE }
 *     styleByType = mapOf(
 *         "Polygon" to Style { fill = ColorRGBa.RED },
 *         "LineString" to Style { stroke = ColorRGBa.BLUE }
 *     )
 *     styleByFeature = { feature ->
 *         if (feature.doubleProperty("pop") > 1000000) {
 *             Style { stroke = ColorRGBa.YELLOW; strokeWeight = 2.0 }
 *         } else null
 *     }
 * }
 * ```
 *
 * @param features Features to render
 * @param block Optional configuration block for projection and styling
 */
fun Drawer.geoFeatures(
    features: Sequence<Feature>,
    block: (GeoRenderConfig.() -> Unit)? = null
) {
    val config = block?.let { GeoRenderConfig().apply(it) } ?: GeoRenderConfig()

    val featureList = features.toList()
    if (featureList.isEmpty()) return

    // Calculate bounds from all features
    val bounds = featureList.fold(Bounds.empty()) { acc, f ->
        acc.expandToInclude(f.boundingBox)
    }

    // Auto-fit projection if not specified (beginner-friendly default)
    val proj = config.projection ?: ProjectionFactory.fitBounds(
        bounds = bounds,
        width = this.width.toDouble(),
        height = this.height.toDouble(),
        padding = 0.0,
        projection = ProjectionType.MERCATOR
    )

    // Snapshot config for safe iteration
    val resolved = config.snapshot()

    // Render each feature with style resolution
    featureList.forEach { feature ->
        val style = resolveStyle(feature, resolved)
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
    // Handle optimized sources specially since they don't support standard Feature access
when (source) {
    is OptimizedGeoSource -> {
        // For optimized sources, render directly without Feature conversion
        // Normalization (including antimeridian) happens at load time via GeometryNormalizer
        // 
        // NOTE: Shape caching is currently disabled for optimized sources pending
        // proper geometry type handling. The previous implementation incorrectly
        // treated all geometries as closed polygons.
        source.optimizedFeatureSequence.forEach { optFeature ->
            val style = resolveOptimizedStyle(optFeature, resolved)
            val geom = optFeature.optimizedGeometry
            
            when (geom) {
                is OptimizedPoint -> {
                    val screenPoint = geom.toScreenCoordinatesList(proj).firstOrNull()
                    if (screenPoint != null) {
                        drawPoint(this, screenPoint, style)
                    }
                }
                is OptimizedLineString -> {
                    val screenPoints = geom.toScreenCoordinatesList(proj)
                    if (screenPoints.size >= 2) {
                        drawLineString(this, screenPoints, style)
                    }
                }
                is OptimizedMultiLineString -> {
                    geom.toScreenCoordinatesList(proj).forEach { linePoints ->
                        if (linePoints.size >= 2) {
                            drawLineString(this, linePoints, style)
                        }
                    }
                }
                is OptimizedPolygon -> {
                    val (exterior, interiors) = geom.toScreenCoordinates(proj)
                    if (exterior.size >= 3) {
                        val screenExterior = exterior.toList()
                        val screenInteriors = interiors.map { it.toList() }
                        writePolygonWithHoles(this, screenExterior, screenInteriors, 
                            style ?: StyleDefaults.defaultPolygonStyle)
                    }
                }
                is OptimizedMultiPolygon -> {
                    geom.toScreenCoordinates(proj).forEach { (exterior, interiors) ->
                        if (exterior.size >= 3) {
                            val screenExterior = exterior.toList()
                            val screenInteriors = interiors.map { it.toList() }
                            writePolygonWithHoles(this, screenExterior, screenInteriors,
                                style ?: StyleDefaults.defaultPolygonStyle)
                        }
                    }
                }
                else -> {
                    // Fallback: skip unknown geometry types
                }
            }
        }
    }
    else -> {
        // Standard per-feature rendering
        source.features.forEach { feature ->
            val style = resolveStyle(feature, resolved)
            feature.geometry.renderToDrawer(this, proj, style)
        }
    }
}
}

// ============================================================================
// Inline Style DSL - Three-line workflow support
// ============================================================================

/**
 * Apply default values to style properties that weren't set.
 *
 * Defaults:
 * - stroke = WHITE if null
 * - fill = RED if null (for polygon/point types)
 * - strokeWeight = 1.5 if 0.0
 * - size = 5.0 if 0.0
 */
private fun applyStyleDefaults(style: Style, source: GeoSource): Style {
    return style.apply {
        // Default stroke: white
        if (stroke == null) {
            stroke = org.openrndr.color.ColorRGBa.WHITE
        }
        
        // Default fill: red (only for geometries that support fill)
        if (fill == null) {
            fill = org.openrndr.color.ColorRGBa.RED
        }
        
        // Default strokeWeight: 1.5
        if (strokeWeight == 0.0 || strokeWeight == 1.0) {
            strokeWeight = 1.5
        }
        
        // Default size: 5.0 (for points)
        if (size == 0.0 || size == 5.0) {
            size = 5.0
        }
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
            if (interiors.isNotEmpty()) {
                val screenExterior = exterior.map { projection.project(it) }
                val screenInteriors = interiors.map { ring ->
                    ring.map { projection.project(it) }
                }
                writePolygonWithHoles(drawer, screenExterior, screenInteriors,
                    style ?: StyleDefaults.defaultPolygonStyle)
            } else {
                val screenPoints = exterior.map { projection.project(it) }
                drawPolygon(drawer, screenPoints, style)
            }
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
                if (poly.interiors.isNotEmpty()) {
                    val screenExterior = poly.exterior.map { projection.project(it) }
                    val screenInteriors = poly.interiors.map { ring ->
                        ring.map { projection.project(it) }
                    }
                    writePolygonWithHoles(drawer, screenExterior, screenInteriors,
                        style ?: StyleDefaults.defaultPolygonStyle)
                } else {
                    val screenPoints = poly.exterior.map { projection.project(it) }
                    drawPolygon(drawer, screenPoints, style)
                }
            }
        }
    }
}

/**
 * Resolve style for an optimized feature using precedence chain:
 * 1. Per-optimized-feature function (styleByOptimizedFeature) — Highest priority
 * 2. By-type map (styleByType) — Keyed by geometry type name
 * 3. Global style (style) — Applied to all features if specified
 * 4. Geometry-type default (StyleDefaults) — Fallback
 * 
 * ## Example
 * ```kotlin
 * drawer.geo(optimizedSource) {
 *     // Per-optimized-feature styling (highest priority)
 *     styleByOptimizedFeature = { optFeature ->
 *         val pop = optFeature.properties["population"] as? Double
 *         if (pop != null && pop > 1_000_000) {
 *             Style { stroke = ColorRGBa.YELLOW; strokeWeight = 3.0 }
 *         } else null  // Fall through to styleByType/style
 *     }
 *     
 *     // Type-based styling
 *     styleByType = mapOf(
 *         "Polygon" to Style { fill = ColorRGBa.RED }
 *     )
 *     
 *     // Global style (lowest priority)
 *     fill = ColorRGBa.WHITE
 * }
 * ```
 */
internal fun resolveOptimizedStyle(optFeature: OptimizedFeature, config: GeoRenderConfig): Style {
    // 1. Per-optimized-feature function
    config.styleByOptimizedFeature?.invoke(optFeature)?.let { return it }

    // 2. Determine geometry type from optimized geometry
    val typeName = when (val geom = optFeature.optimizedGeometry) {
        is OptimizedPoint -> "Point"
        is OptimizedLineString -> "LineString"
        is OptimizedPolygon -> "Polygon"
        is OptimizedMultiPoint -> "MultiPoint"
        is OptimizedMultiLineString -> "MultiLineString"
        is OptimizedMultiPolygon -> "MultiPolygon"
        else -> "Unknown"
    }

    // 3. By-type map
    config.styleByType[typeName]?.let { return it }

    // 4. Global style
    config.resolvedStyle()?.let { return it }

    // 5. Default fallback
    return StyleDefaults.defaultStyle
}/**
 * Render an optimized feature to the given Drawer using batch projection.
 */
internal fun OptimizedFeature.renderOptimizedToDrawer(
    drawer: Drawer,
    projection: GeoProjection,
    style: Style?
) {
    when (val geom = optimizedGeometry) {
        is OptimizedPoint -> {
            val screen = geom.toScreenCoordinates(projection).first()
            drawPoint(drawer, screen, style)
        }
        is OptimizedLineString -> {
            val screenPoints = geom.toScreenCoordinatesList(projection)
            drawLineString(drawer, screenPoints, style)
        }
        is OptimizedPolygon -> {
            val (exterior, interiors) = geom.toScreenCoordinates(projection)
            if (interiors.isEmpty()) {
                drawPolygon(drawer, exterior.toList(), style)
            } else {
                writePolygonWithHoles(
                    drawer,
                    exterior.toList(),
                    interiors.map { it.toList() },
                    style ?: StyleDefaults.defaultPolygonStyle
                )
            }
        }
        is OptimizedMultiPoint -> {
            geom.toScreenCoordinates(projection).forEach { pt ->
                drawPoint(drawer, pt, style)
            }
        }
        is OptimizedMultiLineString -> {
            geom.toScreenCoordinatesList(projection).forEach { line ->
                drawLineString(drawer, line, style)
            }
        }
        is OptimizedMultiPolygon -> {
            // Render as single Shape with all contours combined
            // This eliminates overdraw at shared boundaries and seams with transparency
            val allContours = mutableListOf<ShapeContour>()

            geom.toScreenCoordinates(projection).forEach { (exterior, interiors) ->
                // Add exterior contour with clockwise winding (positive fill)
                val extContour = ShapeContour.fromPoints(exterior.toList(), closed = true).clockwise
                allContours.add(extContour)

                // Add interior contours with counter-clockwise winding (negative fill = holes)
                interiors.forEach { ring ->
                    if (ring.size >= 3) {
                        val holeContour = ShapeContour.fromPoints(ring.toList(), closed = true).counterClockwise
                        allContours.add(holeContour)
                    }
                }
            }

            // Single draw call with all contours
            if (allContours.isNotEmpty()) {
                drawer.shape(Shape(allContours))
            }
        }
    }
}

/**
 * Project a geometry to screen coordinates as an Array<Vector2>.
 * Used by the viewport cache to store projected coordinates.
 */
private fun projectGeometryToArray(
    geometry: Geometry,
    projection: GeoProjection
): Array<Vector2> = when (geometry) {
    is geo.Point -> arrayOf(projection.project(Vector2(geometry.x, geometry.y)))
    is geo.LineString -> geometry.points.map { projection.project(it) }.toTypedArray()
    is geo.Polygon -> {
        val exteriorProjected = geometry.exterior.map { projection.project(it) }
        val interiorProjected = geometry.interiors.flatMap { ring ->
            ring.map { projection.project(it) }
        }
        (exteriorProjected + interiorProjected).toTypedArray()
    }
    is geo.MultiPoint -> geometry.points.map { projection.project(Vector2(it.x, it.y)) }.toTypedArray()
    is geo.MultiLineString -> geometry.lineStrings.flatMap { it.points.map { pt -> projection.project(pt) } }.toTypedArray()
    is geo.MultiPolygon -> {
        geometry.polygons.flatMap { poly ->
            val ext = poly.exterior.map { projection.project(it) }
            val ints = poly.interiors.flatMap { ring ->
                ring.map { projection.project(it) }
            }
            ext + ints
        }.toTypedArray()
    }
}

/**
 * Render pre-projected coordinates for a geometry.
 */
private fun renderProjectedCoordinates(
    geometry: Geometry,
    projectedCoords: Array<Vector2>,
    drawer: Drawer,
    style: Style?
) {
    when (geometry) {
        is geo.Point -> {
            drawPoint(drawer, projectedCoords[0], style)
        }
        is geo.LineString -> {
            drawLineString(drawer, projectedCoords.toList(), style)
        }
        is geo.Polygon -> {
            if (geometry.interiors.isNotEmpty()) {
                val exteriorSize = geometry.exterior.size
                val screenExterior = projectedCoords.slice(0 until exteriorSize)

                val screenInteriors = mutableListOf<List<Vector2>>()
                var idx = exteriorSize
                geometry.interiors.forEach { ring ->
                    screenInteriors.add(projectedCoords.slice(idx until idx + ring.size))
                    idx += ring.size
                }

                writePolygonWithHoles(drawer, screenExterior, screenInteriors,
                    style ?: StyleDefaults.defaultPolygonStyle)
            } else {
                drawPolygon(drawer, projectedCoords.toList(), style)
            }
        }
        is geo.MultiPoint -> {
            projectedCoords.forEach { pt ->
                drawPoint(drawer, pt, style)
            }
        }
        is geo.MultiLineString -> {
            // MultiLineString flattens all points, need to reconstruct line segments
            var idx = 0
            geometry.lineStrings.forEach { line ->
                val linePoints = Array(line.points.size) { projectedCoords[idx++] }
                drawLineString(drawer, linePoints.toList(), style)
            }
        }
        is geo.MultiPolygon -> {
            // MultiPolygon flattens all exterior and interior points
            var idx = 0
            geometry.polygons.forEach { poly ->
                val extSize = poly.exterior.size
                val screenExterior = projectedCoords.slice(idx until idx + extSize)
                idx += extSize

                if (poly.interiors.isNotEmpty()) {
                    val screenInteriors = mutableListOf<List<Vector2>>()
                    poly.interiors.forEach { ring ->
                        screenInteriors.add(projectedCoords.slice(idx until idx + ring.size))
                        idx += ring.size
                    }
                    writePolygonWithHoles(drawer, screenExterior, screenInteriors,
                        style ?: StyleDefaults.defaultPolygonStyle)
                } else {
                    drawPolygon(drawer, screenExterior, style)
                }
            }
        }
    }
}
