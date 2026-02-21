package geo.projection

import geo.projection.internal.ProjectionMercatorInternal
import org.openrndr.math.Vector2

/**
 * Web Mercator projection for geographic coordinates.
 *
 * Suitable for world maps, Google Maps-style display.
 * Cannot represent poles (±90°), use clampLatitude() for extreme latitudes.
 *
 * DSL usage:
 * ```kotlin
 * val mercator = ProjectionMercator {
 *     width = 800
 *     height = 600
 *     center = Vector2(0.0, 51.5) // London
 *     scale = 1.0
 *     fitWorld = false
 * }
 * val screen = mercator.project(Vector2(-0.1, 51.5))
 * ```
 */
class ProjectionMercator(
    private val config: ProjectionConfig
) : GeoProjection {

    companion object {
        /**
         * Create ProjectionMercator with DSL configuration.
         */
        operator fun invoke(block: ProjectionConfigBuilder.() -> Unit): ProjectionMercator {
            val builder = ProjectionConfigBuilder()
            builder.block()
            return ProjectionMercator(builder.build())
        }
    }

    private val internal = ProjectionMercatorInternal(config)

    override fun project(latLng: Vector2): Vector2 = internal.project(latLng)

    override fun unproject(screen: Vector2): Vector2 = internal.unproject(screen)

    override fun configure(config: ProjectionConfig): GeoProjection {
        return ProjectionMercator(config)
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        return ProjectionMercator(internal.fitWorld(config).let { ProjectionMercatorInternal(it as ProjectionConfig) })
    }
}

/**
 * Builder DSL for ProjectionConfig.
 */
class ProjectionConfigBuilder {
    var width: Double = 800.0
    var height: Double = 600.0
    var center: Vector2? = null
    var scale: Double = 1.0
    var fitWorld: Boolean = false

    fun build(): ProjectionConfig {
        return if (fitWorld) {
            ProjectionConfig(width, height)
        } else {
            ProjectionConfig(width, height, center, scale, null)
        }
    }
}
