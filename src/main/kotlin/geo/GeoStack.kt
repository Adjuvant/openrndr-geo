package geo

import geo.crs.CRS
import geo.internal.OptimizedFeature
import geo.internal.OptimizedGeoSource
import geo.internal.geometry.OptimizedLineString
import geo.internal.geometry.OptimizedMultiLineString
import geo.internal.geometry.OptimizedMultiPoint
import geo.internal.geometry.OptimizedMultiPolygon
import geo.internal.geometry.OptimizedPoint
import geo.internal.geometry.OptimizedPolygon
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2

/**
 * GeoStack - A stack of multiple GeoSources for multi-dataset overlay rendering.
 *
 * Automatically unifies CRS across all sources in the stack, using the first
 * source's CRS as the target. All subsequent sources are transformed to match.
 *
 * ## Usage
 * ```kotlin
 * // Load multiple datasets with potentially different CRS
 * val coastline = geoSource("coastline.json")  // WGS84
 * val cities = geoSource("cities.json")       // WGS84
 * val rivers = geoSource("rivers.json")        // Could be different
 *
 * // Create a stack - automatically unifies to first source's CRS
 * val map = geoStack(coastline, cities, rivers)
 *
 * // Fit to view and render
 * map.fit(viewBounds)
 * map.render(drawer)
 * ```
 *
 * ## Interactive Exploration
 *
 * GeoStack supports iterative exploration with zoom and pan operations:
 * ```kotlin
 * // Reset to show all data
 * stack.reset()
 *
 * // Zoom in/out by factor
 * stack.zoom(1.5)  // Zoom in 1.5x
 * stack.zoom(0.75) // Zoom out
 *
 * // Pan the view
 * stack.pan(-10.0, 0.0)  // Pan left
 * stack.pan(10.0, 5.0)   // Pan right and up
 *
 * // Center on specific coordinates
 * stack.centerOn(-73.9, 40.7)  // Center on New York
 *
 * // Zoom to specific bounds
 * stack.zoomTo(Bounds(minX=-74.5, minY=40.5, maxX=-73.5, maxY=40.9))
 *
 * // Get current view bounds
 * val viewBounds = stack.getCurrentViewBounds()
 * ```
 */
class GeoStack(
    private val sources: List<GeoSource>
) {
    init {
        require(sources.isNotEmpty()) { "GeoStack requires at least one source" }
    }

    /** Current view bounds, or null to show all data */
    private var viewBounds: Bounds? = null
    
    /** Viewport cache for projected geometry coordinates */
    private val viewportCache = geo.internal.cache.ViewportCache()
    
    /**
     * The unified CRS used by this stack.
     * This is the CRS of the first source - all others are transformed to match.
     */
    val crs: String = sources.first().crs
    
    /**
     * Get all features from all sources, with CRS unification.
     * The first source's CRS is used as the target - all other sources
     * are transformed to match.
     */
    val features: Sequence<Feature> by lazy {
        if (sources.size == 1) {
            sources.first().features
        } else {
            // First source: no transformation needed
            val firstSource = sources.first()
            
            // Rest of sources: transform to first source's CRS
            val restSources = sources.drop(1)
            
            // Combine all features
            firstSource.features + restSources.asSequence().flatMap { source ->
                if (source.crs == crs) {
                    source.features
                } else {
                    println("Warning: Transforming source from ${source.crs} to $crs")
                    source.autoTransformTo(crs).features
                }
            }
        }
    }
    
    /**
     * Get the unified bounding box of all features in the stack.
     * Handles both standard and optimized GeoSources.
     */
    fun totalBoundingBox(): Bounds {
        return sources.fold(Bounds.empty()) { acc, source ->
            when (source) {
                is OptimizedGeoSource -> {
                    // For optimized sources, compute bounds directly from optimized geometries
                    val sourceBounds = source.optimizedFeatureSequence.fold(Bounds.empty()) { bounds, feature ->
                        bounds.expandToInclude(feature.boundingBox())
                    }
                    acc.expandToInclude(sourceBounds)
                }
                else -> {
                    // For standard sources, use the features sequence
                    source.features.fold(acc) { bounds, feature ->
                        bounds.expandToInclude(feature.boundingBox)
                    }
                }
            }
        }
    }
    
    /**
     * Get the current view bounds.
     * Returns viewBounds if set, otherwise returns total bounding box of all data.
     */
    fun getCurrentViewBounds(): Bounds = viewBounds ?: totalBoundingBox()

    /**
     * Zoom to fit specific geographic bounds.
     * @param bounds Target bounds to show
     */
    fun zoomTo(bounds: Bounds) {
        viewBounds = bounds
    }

    /**
     * Center the view on a specific point.
     * @param x Longitude/lat in current CRS
     * @param y Latitude/lng in current CRS
     */
    fun centerOn(x: Double, y: Double) {
        val current = getCurrentViewBounds()
        val halfWidth = current.width / 2.0
        val halfHeight = current.height / 2.0
        viewBounds = Bounds(
            minX = x - halfWidth,
            minY = y - halfHeight,
            maxX = x + halfWidth,
            maxY = y + halfHeight
        )
    }

    /**
     * Zoom in/out by a factor.
     * @param factor Zoom factor (< 1 = zoom out, > 1 = zoom in)
     */
    fun zoom(factor: Double) {
        val current = getCurrentViewBounds()
        val centerX = current.center.first
        val centerY = current.center.second
        val newWidth = current.width / factor
        val newHeight = current.height / factor
        viewBounds = Bounds(
            minX = centerX - newWidth / 2.0,
            minY = centerY - newHeight / 2.0,
            maxX = centerX + newWidth / 2.0,
            maxY = centerY + newHeight / 2.0
        )
    }

    /**
     * Pan the view by offset.
     * @param dx Offset in X (negative = left, positive = right)
     * @param dy Offset in Y (negative = down, positive = up)
     */
    fun pan(dx: Double, dy: Double) {
        val current = getCurrentViewBounds()
        viewBounds = Bounds(
            minX = current.minX + dx,
            minY = current.minY + dy,
            maxX = current.maxX + dx,
            maxY = current.maxY + dy
        )
    }

    /**
     * Reset view to show all data.
     */
    fun reset() {
        viewBounds = null
    }

    /**
     * Render all features in the stack to the given drawer.
     * Uses auto-fit projection based on current view bounds.
     */
    fun render(drawer: Drawer) {
        val bounds = viewBounds ?: totalBoundingBox()  // Use viewBounds if set
        val projection = geo.projection.ProjectionFactory.fitBounds(
            bounds = bounds,
            width = drawer.width.toDouble(),
            height = drawer.height.toDouble(),
            padding = 0.9,
            projection = geo.projection.ProjectionType.MERCATOR
        )
        render(drawer, projection)
    }
    
    /**
     * Render all features in the stack with the given projection.
     */
    fun render(drawer: Drawer, projection: geo.projection.GeoProjection) {
        val viewportState = geo.internal.cache.ViewportState.fromProjection(projection)

        sources.forEach { source ->
            when (source) {
                is OptimizedGeoSource -> {
                    // Use batch projection for optimized geometries
                    source.optimizedFeatureSequence.forEach { optFeature ->
                        optFeature.renderOptimizedToDrawer(drawer, projection, null)
                    }
                }
                else -> {
                    // Standard per-point projection with viewport caching
                    source.features.forEach { feature ->
                        renderWithCache(feature.geometry, drawer, projection, viewportState)
                    }
                }
            }
        }
    }
    
    /**
     * Render geometry with viewport caching.
     * Uses cache to avoid redundant projection calculations.
     */
    private fun renderWithCache(
        geometry: Geometry,
        drawer: Drawer,
        projection: geo.projection.GeoProjection,
        viewportState: geo.internal.cache.ViewportState
    ) {
        val projectedCoords = viewportCache.getProjectedCoordinates(
            geometry = geometry,
            viewportState = viewportState
        ) {
            // Projector lambda - only called on cache miss
            projectGeometryToArray(geometry, projection)
        }

        // Render using cached coordinates
        renderProjectedCoordinates(geometry, projectedCoords, drawer)
    }
    
    /**
     * Project a geometry to screen coordinates as an Array<Vector2>.
     * Used by the viewport cache to store projected coordinates.
     */
    private fun projectGeometryToArray(
        geometry: Geometry,
        projection: geo.projection.GeoProjection
    ): Array<Vector2> = when (geometry) {
        is geo.Point -> arrayOf(projection.project(Vector2(geometry.x, geometry.y)))
        is geo.LineString -> geometry.points.map { projection.project(it) }.toTypedArray()
        is geo.Polygon -> geometry.exterior.map { projection.project(it) }.toTypedArray()
        is geo.MultiPoint -> geometry.points.map { projection.project(Vector2(it.x, it.y)) }.toTypedArray()
        is geo.MultiLineString -> geometry.lineStrings.flatMap { it.points.map { pt -> projection.project(pt) } }.toTypedArray()
        is geo.MultiPolygon -> geometry.polygons.flatMap { it.exterior.map { pt -> projection.project(pt) } }.toTypedArray()
    }
    
    /**
     * Get the number of sources in this stack.
     */
    fun sourceCount(): Int = sources.size
    
    /**
     * Get the source at the given index.
     */
    operator fun get(index: Int): GeoSource = sources[index]
    
    companion object {
        /**
         * Create a GeoStack from multiple GeoSources.
         * 
         * @param sources Variable number of GeoSources
         * @return GeoStack with auto-CRS unification
         */
        @JvmStatic
        fun create(vararg sources: GeoSource): GeoStack {
            return GeoStack(sources.toList())
        }
        
        /**
         * Create a GeoStack from a list of GeoSources.
         */
        @JvmStatic
        fun fromList(sources: List<GeoSource>): GeoStack {
            return GeoStack(sources)
        }
    }
}

/**
 * Extension function to create a GeoStack from multiple GeoSources.
 * 
 * ## Usage
 * ```kotlin
 * val map = geoStack(
 *     geoSource("coastline.json"),
 *     geoSource("cities.json"),
 *     geoSource("rivers.json")
 * )
 * ```
 */
fun geoStack(vararg sources: GeoSource): GeoStack {
    return GeoStack.create(*sources)
}

/**
 * Extension function to create a GeoStack from a list of GeoSources.
 */
fun geoStack(sources: List<GeoSource>): GeoStack {
    return GeoStack.fromList(sources)
}

/**
 * Render pre-projected coordinates for a geometry.
 */
private fun renderProjectedCoordinates(
    geometry: Geometry,
    projectedCoords: Array<Vector2>,
    drawer: Drawer
) {
    when (geometry) {
        is geo.Point -> {
            geo.render.drawPoint(drawer, projectedCoords[0], null)
        }
        is geo.LineString -> {
            geo.render.drawLineString(drawer, projectedCoords.toList(), null)
        }
        is geo.Polygon -> {
            geo.render.drawPolygon(drawer, projectedCoords.toList(), null)
        }
        is geo.MultiPoint -> {
            projectedCoords.forEach { pt ->
                geo.render.drawPoint(drawer, pt, null)
            }
        }
        is geo.MultiLineString -> {
            // MultiLineString flattens all points, need to reconstruct line segments
            var idx = 0
            geometry.lineStrings.forEach { line ->
                val linePoints = Array(line.points.size) { projectedCoords[idx++] }
                geo.render.drawLineString(drawer, linePoints.toList(), null)
            }
        }
        is geo.MultiPolygon -> {
            // MultiPolygon flattens all exterior points
            var idx = 0
            geometry.polygons.forEach { poly ->
                val polyPoints = Array(poly.exterior.size) { projectedCoords[idx++] }
                geo.render.drawPolygon(drawer, polyPoints.toList(), null)
            }
        }
    }
}

/**
 * Render this geometry to the given Drawer (legacy method, used when cache is not needed).
 */
private fun Geometry.renderToDrawer(drawer: Drawer, projection: geo.projection.GeoProjection, style: geo.render.Style?) {
    when (this) {
        is geo.Point -> {
            val screen = projection.project(Vector2(x, y))
            geo.render.drawPoint(drawer, screen, style)
        }
        is geo.LineString -> {
            val screenPoints = points.map { projection.project(it) }
            geo.render.drawLineString(drawer, screenPoints, style)
        }
        is geo.Polygon -> {
            val screenPoints = exterior.map { projection.project(it) }
            geo.render.drawPolygon(drawer, screenPoints, style)
        }
        is geo.MultiPoint -> {
            points.forEach { pt ->
                val screen = projection.project(Vector2(pt.x, pt.y))
                geo.render.drawPoint(drawer, screen, style)
            }
        }
        is geo.MultiLineString -> {
            lineStrings.forEach { line ->
                val screenPoints = line.points.map { projection.project(it) }
                geo.render.drawLineString(drawer, screenPoints, style)
            }
        }
        is geo.MultiPolygon -> {
            polygons.forEach { poly ->
                val screenPoints = poly.exterior.map { projection.project(it) }
                geo.render.drawPolygon(drawer, screenPoints, style)
            }
        }
    }
}

/**
 * Render an optimized feature to the given Drawer using batch projection.
 */
private fun OptimizedFeature.renderOptimizedToDrawer(
    drawer: Drawer,
    projection: geo.projection.GeoProjection,
    style: geo.render.Style?
) {
    when (val geom = optimizedGeometry) {
        is OptimizedPoint -> {
            val screen = geom.toScreenCoordinates(projection).first()
            geo.render.drawPoint(drawer, screen, style)
        }
        is OptimizedLineString -> {
            val screenPoints = geom.toScreenCoordinatesList(projection)
            geo.render.drawLineString(drawer, screenPoints, style)
        }
        is OptimizedPolygon -> {
            val (exterior, interiors) = geom.toScreenCoordinates(projection)
            if (interiors.isEmpty()) {
                geo.render.drawPolygon(drawer, exterior.toList(), style)
            } else {
                geo.render.writePolygonWithHoles(
                    drawer,
                    exterior.toList(),
                    interiors.map { it.toList() },
                    style ?: geo.render.StyleDefaults.defaultPolygonStyle
                )
            }
        }
        is OptimizedMultiPoint -> {
            geom.toScreenCoordinates(projection).forEach { pt ->
                geo.render.drawPoint(drawer, pt, style)
            }
        }
        is OptimizedMultiLineString -> {
            geom.toScreenCoordinatesList(projection).forEach { line ->
                geo.render.drawLineString(drawer, line, style)
            }
        }
        is OptimizedMultiPolygon -> {
            geom.toScreenCoordinates(projection).forEach { (exterior, interiors) ->
                if (interiors.isEmpty()) {
                    geo.render.drawPolygon(drawer, exterior.toList(), style)
                } else {
                    geo.render.writePolygonWithHoles(
                        drawer,
                        exterior.toList(),
                        interiors.map { it.toList() },
                        style ?: geo.render.StyleDefaults.defaultPolygonStyle
                    )
                }
            }
        }
    }
}
