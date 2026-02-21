package geo.projection

import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle

/**
 * Configuration for map projections.
 *
 * Provides camera-like control over projections including viewport dimensions,
 * center point, scale, and optional bounding box.
 *
 * @param width Viewport width in pixels
 * @param height Viewport height in pixels
 * @param center Geographic center point in degrees (x=longitude, y=latitude), null means (0, 0)
 * @param scale Zoom scale factor, 1.0 = default world view
 * @param bounds Optional bounding box for clipping/visibility checks
 */
data class ProjectionConfig(
    val width: Double,
    val height: Double,
    val center: Vector2? = null,
    val scale: Double = 1.0,
    val bounds: IntRectangle? = null
)
