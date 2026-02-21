package geo.projection

import org.openrndr.math.Vector2

/**
 * Factory for creating preset map projections.
 *
 * Provides convenient access to common projections without manual configuration.
 */
object ProjectionFactory {

    /**
     * Create a Mercator projection for the given viewport.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param center Center coordinates (lat/lng), defaults to (0, 0)
     * @param scale Zoom scale factor, defaults to 1.0
     * @return Configured Mercator projection
     */
    fun mercator(
        width: Double = 800.0,
        height: Double = 600.0,
        center: Vector2? = null,
        scale: Double = 1.0
    ): ProjectionMercator {
        return ProjectionMercator(ProjectionConfig(width, height, center, scale, null))
    }

    /**
     * Create an Equirectangular projection for the given viewport.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param center Center coordinates (lat/lng), defaults to (0, 0)
     * @param scale Zoom scale factor, defaults to 1.0
     * @return Configured Equirectangular projection
     */
    fun equirectangular(
        width: Double = 800.0,
        height: Double = 600.0,
        center: Vector2? = null,
        scale: Double = 1.0
    ): ProjectionEquirectangular {
        return ProjectionEquirectangular(ProjectionConfig(width, height, center, scale, null))
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
        return ProjectionBNG(ProjectionConfig(width, height, null, 1.0, null))
    }

    /**
     * Create a Mercator projection fitted to show the entire world.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @return Mercator projection with world bounds
     */
    fun fitWorldMercator(
        width: Double = 800.0,
        height: Double = 600.0
    ): GeoProjection {
        val config = ProjectionConfig(width, height, Vector2(0.0, 0.0), 1.0, null)
        return ProjectionMercator(config)
    }

    /**
     * Create an Equirectangular projection fitted to show the entire world.
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @return Equirectangular projection with world bounds
     */
    fun fitWorldEquirectangular(
        width: Double = 800.0,
        height: Double = 600.0
    ): GeoProjection {
        val config = ProjectionConfig(width, height, Vector2(0.0, 0.0), 1.0, null)
        return ProjectionEquirectangular(config)
    }
}
