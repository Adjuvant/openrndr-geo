package geo.projection

import org.openrndr.math.Vector2

/**
 * Identity projection that bypasses all coordinate transformation.
 * Use when data is already in screen coordinates.
 *
 * ## Usage
 * ```kotlin
 * drawer.geo(source) {
 *     projection = RawProjection  // No transformation applied
 * }
 * ```
 *
 * ## Use Cases
 * - Pre-projected data already in screen coordinates
 * - Raster overlay alignment
 * - Direct pixel-space rendering
 * - Custom coordinate systems handled externally
 */
object RawProjection : GeoProjection {
    
    override fun project(latLng: Vector2): Vector2 = latLng
    
    override fun unproject(screen: Vector2): Vector2 = screen
    
    override fun configure(config: ProjectionConfig): GeoProjection {
        // No configuration needed for raw projection - return self
        return this
    }
    
    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        // No fitting needed for raw projection - return self
        return this
    }
    
    /**
     * Returns string representation for debugging.
     */
    override fun toString(): String = "RawProjection (identity)"
}
