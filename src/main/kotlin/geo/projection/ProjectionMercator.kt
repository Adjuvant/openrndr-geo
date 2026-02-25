package geo.projection

import geo.projection.internal.ProjectionMercatorInternal
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.tan

private const val MAX_LATITUDE = 85.05112878

class ProjectionMercator(
    private val config: ProjectionConfig
) : GeoProjection {

    companion object {
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
        val worldWidth = 2 * PI
        val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_LATITUDE) / 2))

        // TODO This does not make sense
        val scaleX = config.width / worldWidth
        val scaleY = config.height / worldHeight
        val scale = minOf(scaleX, scaleY)
        
        return ProjectionMercator(config.copy(
            center = Vector2(0.0, 0.0),
            scale = scale
        ))
    }
}

class ProjectionConfigBuilder {
    var width: Double = 800.0
    var height: Double = 600.0
    var center: Vector2? = null
    var scale: Double? = null
    var fitWorld: Boolean = false

    fun build(): ProjectionConfig {
        return if (fitWorld) {
            val worldWidth = 2 * PI
            val worldHeight = 2 * ln(tan(PI / 4 + Math.toRadians(MAX_LATITUDE) / 2))
            val scaleX = width / worldWidth
            val scaleY = height / worldHeight
            val scale = minOf(scaleX, scaleY)
            ProjectionConfig(width, height, Vector2(0.0, 0.0), scale, null)
        } else {
            ProjectionConfig(width, height, center, scale ?: 1.0, null)
        }
    }
}
