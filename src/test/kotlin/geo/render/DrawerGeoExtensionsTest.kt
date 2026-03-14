package geo.render

import geo.internal.OptimizedFeature
import geo.internal.geometry.*
import geo.projection.GeoProjection
import org.junit.Test
import org.openrndr.math.Vector2
import org.junit.Assert.*

class DrawerGeoExtensionsTest {

    @Test
    fun testToScreenCoordinatesDelegatesPoint() {
        // Identity projection
        val identityProjection = object : GeoProjection {
            override fun project(latLng: Vector2) = latLng
            override fun unproject(screen: Vector2) = screen
            override fun configure(config: geo.projection.ProjectionConfig) = this
            override fun fitWorld(config: geo.projection.ProjectionConfig) = this
        }

        val points = listOf(Vector2(1.0, 2.0), Vector2(3.0, 4.0))
        val batch = CoordinateBatch(points)
        val optimizedPoint = OptimizedPoint(batch)

        val feature = object : OptimizedFeature() {
            override val optimizedGeometry = optimizedPoint
        }

        val result = feature.toScreenCoordinates(identityProjection)

        assertEquals(points, result)
    }

    @Test
    fun testToScreenCoordinatesPolygonWithHoles() {
        val identityProjection = object : GeoProjection {
            override fun project(latLng: Vector2) = latLng
            override fun unproject(screen: Vector2) = screen
            override fun configure(config: geo.projection.ProjectionConfig) = this
            override fun fitWorld(config: geo.projection.ProjectionConfig) = this
        }

        val exterior = listOf(
            Vector2(0.0, 0.0),
            Vector2(4.0, 0.0),
            Vector2(4.0, 4.0),
            Vector2(0.0, 4.0),
            Vector2(0.0, 0.0)
        )
        val hole = listOf(
            Vector2(1.0, 1.0),
            Vector2(3.0, 1.0),
            Vector2(3.0, 3.0),
            Vector2(1.0, 3.0),
            Vector2(1.0, 1.0)
        )
        
        val polygon = OptimizedPolygon(
            listOf(CoordinateBatch(exterior), CoordinateBatch(hole))
        )
        
        val feature = object : OptimizedFeature() {
            override val optimizedGeometry = polygon
        }

        val result = feature.toScreenCoordinates(identityProjection)

        val expected = exterior + hole

        assertEquals(expected.size, result.size)
        assertEquals(expected, result)
    }
}
