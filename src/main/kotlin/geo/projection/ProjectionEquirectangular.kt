package geo.projection

import geo.projection.internal.ProjectionEquirectangularInternal
import org.openrndr.math.Vector2

/**
 * Equirectangular projection for geographic coordinates.
 *
 * Simple linear mapping, suitable for basic world maps.
 * Automatically normalizes longitude to [-180, 180].
 *
 * DSL usage:
 * ```kotlin
 * val equirect = ProjectionEquirectangular {
 *     width = 800
 *     height = 600
 *     fitWorld = true
 * }
 * val screen = equirect.project(Vector2(0.0, 51.5))
 * ```
 */
class ProjectionEquirectangular(
    private val config: ProjectionConfig
) : GeoProjection {

    companion object {
        /**
         * Create ProjectionEquirectangular with DSL configuration.
         */
        operator fun invoke(block: ProjectionConfigBuilder.() -> Unit): ProjectionEquirectangular {
            val builder = ProjectionConfigBuilder()
            builder.block()
            return ProjectionEquirectangular(builder.build())
        }
    }

    private val internal = ProjectionEquirectangularInternal(config)

    override fun project(latLng: Vector2): Vector2 = internal.project(latLng)

    override fun unproject(screen: Vector2): Vector2 = internal.unproject(screen)

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionEquirectangular(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        return ProjectionEquirectangular(config.copy(center = Vector2(0.0, 0.0), scale = 1.0))
    }
}
