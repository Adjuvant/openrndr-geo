package geo.projection

import org.openrndr.math.Vector2

/**
 * Core abstraction for map projections.
 *
 * Allows mixing coordinate systems (lat/lng, BNG) in visualizations.
 * Each projection manages its own coordinate transformations and configuration.
 */
interface GeoProjection {
    /**
     * Project geographic coordinates to screen coordinates.
     * @param latLng Geographic coordinates as Vector2 (x=longitude, y=latitude)
     * @return Screen coordinates as Vector2
     */
    fun project(latLng: Vector2): Vector2

    /**
     * Inverse project screen coordinates to geographic coordinates.
     * @param screen Screen coordinates as Vector2
     * @return Geographic coordinates as Vector2 (x=longitude, y=latitude)
     */
    fun unproject(screen: Vector2): Vector2

    /**
     * Create a configured instance of this projection.
     * @param config Projection configuration (width, height, center, scale, bounds)
     * @return Configured projection instance
     */
    fun configure(config: ProjectionConfig): GeoProjection

    /**
     * Fit the projection to display the entire world.
     * Automatically computes center and scale to show full [ -180, 180 ] x [ -90, 90 ].
     * @param config Base configuration (width, height)
     * @return Configured projection with world bounds
     */
    fun fitWorld(config: ProjectionConfig): GeoProjection
}
