package geo.projection

import geo.core.Bounds
import geo.projection.internal.ProjectionMercatorInternal
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan
import kotlin.math.min
import kotlin.math.log2

/**
 * Mercator projection with fitBounds support.
 *
 * Implements the Web Mercator projection with proper zoom/scale semantics
 * and bounding box fitting functionality.
 *
 * ## Key Formulas
 * - Scale to zoom: `zoom = -log2(scale / baseScale)`
 * - Zoom to scale: `scale = baseScale * 2^(-zoom)`
 * - Mercator y: `ln(tan(π/4 + φ/2))` where φ is latitude
 *
 * ## Three-Variant API
 * - [fit] - Mutates this projection in-place to fit a bounding box
 * - [fitted] - Returns a new projection fitted to a bounding box
 * - [fitParameters] - Returns parameters for animation/custom implementation
 */
class ProjectionMercator(
    private val _config: ProjectionConfig
) : GeoProjection {
    
    /**
     * The projection configuration.
     */
    val config: ProjectionConfig
        get() = _config

    companion object {
        operator fun invoke(block: ProjectionConfigBuilder.() -> Unit): ProjectionMercator {
            val builder = ProjectionConfigBuilder()
            builder.block()
            return ProjectionMercator(builder.build())
        }

        /**
         * Standard Web Mercator world bounds (full world in Mercator coordinates).
         */
        val WORLD_BOUNDS: Bounds = Bounds(
            minX = -Math.PI,
            minY = -MAX_MERCATOR_LAT,
            maxX = Math.PI,
            maxY = MAX_MERCATOR_LAT
        )
    }

    private val internal = ProjectionMercatorInternal(_config)

    override fun project(latLng: Vector2): Vector2 = internal.project(latLng)

    override fun unproject(screen: Vector2): Vector2 = internal.unproject(screen)

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionMercator(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        // zoom=0 now means world fits viewport - no calculation needed
        // baseScale is automatically calculated from dimensions in ProjectionConfig
        return ProjectionMercator(config.copy(
            center = Vector2(0.0, 0.0),
            zoomLevel = 0.0  // world fits in viewport at zoom=0
        ))
    }

    // ============================================================================
    // fitBounds API - Three Variants
    // ============================================================================

    /**
     * Fits the projection to contain the given geographic bounding box.
     * Mutates this projection in-place.
     *
     * Uses "contain" strategy: the entire bbox fits within the viewport,
     * potentially leaving empty space, but never cropping content.
     *
     * @param bbox The geographic bounding box to fit (x=longitude, y=latitude)
     * @param padding Optional padding in pixels (default 0)
     * @return This projection for chaining
     */
    fun fit(bbox: Bounds, padding: Double = 0.0): ProjectionMercator {
        val params = fitParameters(bbox, padding)
        return ProjectionMercator(_config.copy(
            center = params.center,
            zoomLevel = params.zoomLevel
        ))
    }

    /**
     * Returns a new projection fitted to contain the given geographic bounding box.
     *
     * Uses "contain" strategy: the entire bbox fits within the viewport,
     * potentially leaving empty space, but never cropping content.
     *
     * @param bbox The geographic bounding box to fit (x=longitude, y=latitude)
     * @param padding Optional padding in pixels (default 0)
     * @return A new ProjectionMercator configured to fit the bbox
     */
    fun fitted(bbox: Bounds, padding: Double = 0.0): ProjectionMercator {
        val params = fitParameters(bbox, padding)
        return ProjectionMercator(_config.copy(
            center = params.center,
            zoomLevel = params.zoomLevel
        ))
    }

    /**
     * Calculates fit parameters for a bounding box.
     * Useful for animation or custom projection implementations.
     *
     * Algorithm (7-step process):
     * 1. Calculate padded viewport size
     * 2. Project bbox corners (SW, NE) to determine projected bounds
     * 3. Calculate scale factors for both dimensions
     * 4. Use minimum scale (contain strategy) to ensure entire bbox fits
     * 5. Calculate center point in projected coordinates
     * 6. Calculate viewport center in screen coordinates
     * 7. Calculate translation to center the projection
     *
     * @param bbox The geographic bounding box to fit
     * @param padding Padding in pixels around the viewport
     * @return FitParameters containing center and zoomLevel
     */
    fun fitParameters(bbox: Bounds, padding: Double = 0.0): FitParameters {
        // Step 1: Calculate padded viewport size
        val paddedWidth = _config.width - 2 * padding
        val paddedHeight = _config.height - 2 * padding

        require(paddedWidth > 0) { "Viewport width minus padding must be positive" }
        require(paddedHeight > 0) { "Viewport height minus padding must be positive" }

        // Step 2: Project bbox corners to Mercator coordinates
        // Handle edge case: bbox spanning dateline or at poles
        val swLat = bbox.minY.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        val neLat = bbox.maxY.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        val swLng = bbox.minX
        val neLng = bbox.maxX

        // Calculate Mercator y coordinates for latitudes
        val swY = ln(tan(PI / 4 + Math.toRadians(swLat) / 2))
        val neY = ln(tan(PI / 4 + Math.toRadians(neLat) / 2))

        // Step 3: Calculate scale factors for both dimensions
        val bboxWidth = neLng - swLng
        val bboxHeight = neY - swY

        // Handle edge case: degenerate bbox (single point or line)
        val effectiveWidth = if (bboxWidth > 0) bboxWidth else 1e-10
        val effectiveHeight = if (bboxHeight > 0) bboxHeight else 1e-10

        val scaleX = paddedWidth / effectiveWidth
        val scaleY = paddedHeight / effectiveHeight

        // Step 4: Use minimum scale (contain strategy - never crop)
        val projectionScale = min(scaleX, scaleY)

        // Convert scale to zoom: zoom = -log2(scale / baseScale) where baseScale fits world
        // Higher scale = more zoomed out, so we invert the formula
        val worldWidth = 2 * PI
        val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_MERCATOR_LAT) / 2))
        val baseScale = minOf(_config.width / worldWidth, _config.height / worldHeight)
        val zoom = -log2(projectionScale / baseScale)

        // Step 5 & 6 & 7: Calculate center for projection
        val centerLng = (swLng + neLng) / 2
        val centerLat = (swLat + neLat) / 2

        return FitParameters(
            center = Vector2(centerLng, centerLat),
            zoomLevel = zoom
        )
    }
}
