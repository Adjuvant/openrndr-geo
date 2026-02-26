package geo.projection

import geo.Bounds
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

/**
 * Maximum valid latitude for Web Mercator projection.
 * This is the theoretical limit where tan(π/4 + φ/2) approaches infinity.
 * Value: ±85.0511287798066 degrees
 */
const val MAX_MERCATOR_LAT = 85.0511287798066

/**
 * Configuration for map projections.
 *
 * Provides camera-like control over projections including viewport dimensions,
 * center point, zoom level, and optional bounding box.
 *
 * @param width Viewport width in pixels
 * @param height Viewport height in pixels
 * @param center Geographic center point in degrees (x=longitude, y=latitude), null means (0, 0)
 * @param zoomLevel Zoom level where 0 = whole world, 1 = 2x zoomed in, etc.
 *                  Standard tile pyramid: scale = 256 * 2^zoom
 * @param bounds Optional bounding box for clipping/visibility checks
 */
data class ProjectionConfig(
    val width: Double,
    val height: Double,
    val center: Vector2? = null,
    val zoomLevel: Double = 0.0,
    val bounds: Bounds? = null
) {
    /**
     * Converts zoom level to scale factor.
     * Standard tile pyramid math: scale = 256 * 2^zoom
     *
     * @return Scale factor for projection calculations
     */
    val scale: Double
        get() = 256.0 * Math.pow(2.0, zoomLevel)

    companion object {
        /**
         * Creates a default world view configuration.
         */
        fun world(width: Double, height: Double) = ProjectionConfig(width, height)
    }
}

/**
 * Builder for creating ProjectionConfig instances with a DSL.
 *
 * ## Usage
 * ```kotlin
 * val config = ProjectionConfigBuilder().apply {
 *     width = 800.0
 *     height = 600.0
 *     zoomLevel = 5.0
 * }.build()
 * ```
 *
 * Or with the invoke pattern:
 * ```kotlin
 * val config = ProjectionConfig {
 *     width = 800.0
 *     height = 600.0
 *     zoomLevel = 5.0
 * }
 * ```
 */
class ProjectionConfigBuilder {
    var width: Double = 800.0
    var height: Double = 600.0
    var center: Vector2? = null
    var zoomLevel: Double? = null
    var fitWorld: Boolean = false

    fun build(): ProjectionConfig {
        return if (fitWorld) {
            val worldWidth = 2 * PI
            val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_MERCATOR_LAT) / 2))
            val scaleX = width / worldWidth
            val scaleY = height / worldHeight
            val scale = minOf(scaleX, scaleY)
            // Reverse: scale = 256 * 2^zoom -> zoom = log2(scale / 256)
            val zoom = kotlin.math.log2(scale / 256.0)
            ProjectionConfig(width, height, Vector2(0.0, 0.0), zoom, null)
        } else {
            ProjectionConfig(width, height, center, zoomLevel ?: 0.0, null)
        }
    }
}

/**
 * Result of fitBounds calculation, useful for animation.
 *
 * @param center The geographic center point to center the projection on
 * @param zoomLevel The calculated zoom level to fit the bounding box
 */
data class FitParameters(
    val center: Vector2,
    val zoomLevel: Double
)
