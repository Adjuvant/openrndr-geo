package geo.projection

import geo.core.Bounds
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan
import kotlin.math.min
import kotlin.math.log2

enum class ProjectionType { EQUIRECTANGULAR, MERCATOR }

object ProjectionFactory {

    /**
     * Create a Mercator projection for the given viewport.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param center Center coordinates (lat/lng), defaults to (0, 0)
     * @param zoomLevel Zoom level (0 = whole world, higher = more zoomed in)
     * @return Configured Mercator projection
     */
    fun mercator(
        width: Double = 800.0,
        height: Double = 600.0,
        center: Vector2? = null,
        zoomLevel: Double = 0.0,
        bounds: Bounds? = null
    ): ProjectionMercator {
        return ProjectionMercator(ProjectionConfig(width, height, center, zoomLevel, null))
    }

    /**
     * Create a Mercator projection for the provided projection configuration.
     * @param config Complete projection configuration
     * @return Configured Mercator projection
     */
    fun mercator(config: ProjectionConfig) : ProjectionMercator {
        return ProjectionMercator(config)
    }

    /**
     * Create an Equirectangular projection for the given viewport.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param center Center coordinates (lat/lng), defaults to (0, 0)
     * @param zoomLevel Zoom level (0 = whole world, higher = more zoomed in)
     * @return Configured Equirectangular projection
     */
    fun equirectangular(
        width: Double = 800.0,
        height: Double = 600.0,
        center: Vector2? = null,
        zoomLevel: Double = 0.0
    ): ProjectionEquirectangular {
        return ProjectionEquirectangular(ProjectionConfig(width, height, center, zoomLevel, null))
    }

    /**
     * Create a British National Grid projection for the UK.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @return Configured BNG projection
     */
    fun bng(
        width: Double = 800.0,
        height: Double = 600.0
    ): ProjectionBNG {
        return ProjectionBNG(ProjectionConfig(width, height, null, 0.0, null))
    }

    /**
     * Create a Mercator projection fitted to show the entire world.
     * At zoom=0, the world bounds fit exactly in the viewport.
     *
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @return Mercator projection with world bounds (zoom=0)
     */
    fun fitWorldMercator(
        width: Double = 800.0,
        height: Double = 600.0
    ): ProjectionMercator {
        // zoom=0 now means world fits in viewport (baseScale calculated from dimensions)
        val config = ProjectionConfig(width, height, Vector2(0.0, 0.0), 0.0, null)
        return ProjectionMercator(config)
    }

    /**
     * Create an Equirectangular projection fitted to show the entire world.
     * At zoom=0, the world bounds fit exactly in the viewport.
     *
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @return Equirectangular projection with world bounds (zoom=0)
     */
    fun fitWorldEquirectangular(
        width: Double = 800.0,
        height: Double = 600.0
    ): ProjectionEquirectangular {
        // zoom=0 now means world fits in viewport
        val config = ProjectionConfig(width, height, Vector2(0.0, 0.0), 0.0, null)
        return ProjectionEquirectangular(config)
    }

    /**
     * Create a projection fitted to the specified geographic bounds with pixel-based padding.
     *
     * @param bounds Geographic bounds to fit (minX, minY, maxX, maxY in degrees)
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param padding Padding in pixels around the viewport (default 20.0). Applied to all sides.
     * @param projection Projection type to use (EQUIRECTANGULAR or MERCATOR)
     * @return Configured projection fitted to bounds with padding
     */
    fun fitBounds(
        bounds: geo.core.Bounds,
        width: Double,
        height: Double,
        padding: Double = 20.0,
        projection: ProjectionType = ProjectionType.EQUIRECTANGULAR
    ): GeoProjection {
        val center = Vector2(bounds.center.first, bounds.center.second)

        val config = when (projection) {
            ProjectionType.EQUIRECTANGULAR -> {
                // Pixel-based padding: reduce effective viewport
                val paddedWidth = width - (padding * 2)
                val paddedHeight = height - (padding * 2)

                // Calculate scale for equirectangular: 360 degrees width, 180 degrees height
                val scaleX = 360.0 / bounds.width
                val scaleY = 180.0 / bounds.height
                val scale = min(scaleX, scaleY) * (min(paddedWidth, paddedHeight) / min(width, height))
                // Convert scale to zoomLevel: zoom = -log2(scale / baseScale) where baseScale fits world
                // For equirectangular, baseScale = min(width/360, height/180)
                val worldBaseScale = min(width / 360.0, height / 180.0)
                val zoomLevel = -log2(scale / worldBaseScale)
                ProjectionConfig(width, height, center, zoomLevel, null)
            }
            ProjectionType.MERCATOR -> {
                // Pixel-based padding: reduce effective viewport
                val paddedWidth = width - (padding * 2)
                val paddedHeight = height - (padding * 2)

                val clampedMinY = bounds.minY.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
                val clampedMaxY = bounds.maxY.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)

                val projMinX = Math.toRadians(bounds.minX)
                val projMaxX = Math.toRadians(bounds.maxX)
                val projMinY = ln(tan(PI / 4 + Math.toRadians(clampedMinY) / 2))
                val projMaxY = ln(tan(PI / 4 + Math.toRadians(clampedMaxY) / 2))

                val projWidth = projMaxX - projMinX
                val projHeight = kotlin.math.abs(projMaxY - projMinY)

                val scaleX = paddedWidth / projWidth
                val scaleY = paddedHeight / projHeight
                val scale = min(scaleX, scaleY)

                // Convert scale to zoom: zoom = -log2(scale / baseScale) where baseScale fits world
                // Higher scale = zoomed out (show more), so we invert
                val worldWidth = 2 * PI
                val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_MERCATOR_LAT) / 2))
                val baseScale = min(width / worldWidth, height / worldHeight)
                val zoomLevel = -log2(scale / baseScale)

                ProjectionConfig(width, height, center, zoomLevel, null)
            }
        }

        return when (projection) {
            ProjectionType.EQUIRECTANGULAR -> ProjectionEquirectangular(config)
            ProjectionType.MERCATOR -> ProjectionMercator(config)
        }
    }
}
