package geo

import geo.crs.CRS
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
     */
    fun totalBoundingBox(): Bounds {
        return features.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox)
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
        features.forEach { feature ->
            feature.geometry.renderToDrawer(drawer, projection, null)
        }
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
 * Render this geometry to the given Drawer.
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
