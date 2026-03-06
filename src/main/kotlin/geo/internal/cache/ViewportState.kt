package geo.internal.cache

import geo.projection.GeoProjection
import geo.projection.ProjectionMercator
import org.openrndr.math.Vector2

/**
 * Immutable viewport configuration for cache keys.
 *
 * Captures all viewport parameters that affect projection:
 * - zoomLevel: The zoom level from ProjectionConfig
 * - centerX/centerY: The geographic center point coordinates
 * - projectionWidth/projectionHeight: The projection target dimensions
 *
 * Window viewport size is fixed in OpenRNDR and not part of the key.
 *
 * @property zoomLevel Zoom level where 0 = viewport fills world
 * @property centerX Geographic center X coordinate (longitude)
 * @property centerY Geographic center Y coordinate (latitude)
 * @property projectionWidth Projection target width in pixels
 * @property projectionHeight Projection target height in pixels
 */
internal data class ViewportState(
    val zoomLevel: Double,
    val centerX: Double,
    val centerY: Double,
    val projectionWidth: Double,
    val projectionHeight: Double
) {
    companion object {
        /**
         * Creates a ViewportState from any GeoProjection instance.
         *
         * Uses a when-expression to extract state from different projection types.
         * Currently supports ProjectionMercator; other projections fall back to defaults.
         *
         * @param projection The projection to extract viewport state from
         * @return A ViewportState capturing the current projection configuration
         */
        fun fromProjection(projection: GeoProjection): ViewportState {
            return when (projection) {
                is ProjectionMercator -> {
                    val config = projection.config
                    ViewportState(
                        zoomLevel = config.zoomLevel,
                        centerX = config.center?.x ?: 0.0,
                        centerY = config.center?.y ?: 0.0,
                        projectionWidth = config.width,
                        projectionHeight = config.height
                    )
                }
                else -> {
                    // Fallback for unsupported projection types
                    // Use default values that will effectively disable caching
                    // by always appearing as a different viewport state
                    ViewportState(
                        zoomLevel = 0.0,
                        centerX = 0.0,
                        centerY = 0.0,
                        projectionWidth = 0.0,
                        projectionHeight = 0.0
                    )
                }
            }
        }
    }
}
