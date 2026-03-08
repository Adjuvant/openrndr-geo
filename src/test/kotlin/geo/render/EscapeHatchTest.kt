package geo.render

import geo.*
import geo.projection.RawProjection
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

/**
 * Test escape hatches for advanced rendering:
 * - RawProjection type for bypassing projections
 * - Style resolution precedence chain
 * - Feature.geometry direct access
 */
class EscapeHatchTest {

    @Test
    fun testRawProjectionIdentity() {
        val input = Vector2(100.0, 200.0)
        val result = RawProjection.project(input)
        assertEquals(input, result)
    }
    
    @Test
    fun testRawProjectionBypass() {
        // Verify RawProjection returns input unchanged (no transformation)
        val geoCoord = Vector2(-122.0, 47.0)  // Seattle coordinates
        val result = RawProjection.project(geoCoord)
        assertEquals(geoCoord, result)
        
        // Unproject should also be identity
        val unprojected = RawProjection.unproject(result)
        assertEquals(geoCoord, unprojected)
    }
    
    @Test
    fun testStyleByFeatureFunction() {
        val feature = Feature(Point(0.0, 0.0), mapOf("pop" to 2_000_000))
        
        val config = GeoRenderConfig()
        config.styleByFeature = { f ->
            val pop = f.property("pop") as? Number
            if (pop != null && pop.toDouble() > 1_000_000) {
                Style { stroke = ColorRGBa.RED }
            } else null
        }
        
        val style = resolveStyle(feature, config)
        assertEquals(ColorRGBa.RED, style.stroke)
    }
    
    @Test
    fun testStyleByTypeMap() {
        val polygonFeature = Feature(
            Polygon(listOf(Vector2(0.0, 0.0), Vector2(1.0, 0.0), Vector2(0.5, 1.0), Vector2(0.0, 0.0)))
        )
        
        val polygonStyle = Style { fill = ColorRGBa.BLUE }
        val config = GeoRenderConfig()
        config.styleByType = mapOf("Polygon" to polygonStyle)
        
        val style = resolveStyle(polygonFeature, config)
        assertEquals(ColorRGBa.BLUE, style.fill)
    }
    
    @Test
    fun testStyleResolutionPrecedence() {
        // Feature with property that triggers per-feature style
        val feature = Feature(Point(0.0, 0.0), mapOf("priority" to "high"))
        
        val perFeatureStyle = Style { stroke = ColorRGBa.RED; strokeWeight = 5.0 }
        val typeStyle = Style { stroke = ColorRGBa.GREEN; strokeWeight = 3.0 }

        val config = GeoRenderConfig()
        config.stroke = ColorRGBa.BLUE
        config.strokeWeight = 1.0
        config.styleByType = mapOf("Point" to typeStyle)
        config.styleByFeature = { f ->
            val priority = f.property("priority") as? String
            if (priority == "high") perFeatureStyle
            else null
        }
        
        // Per-feature should win
        val style = resolveStyle(feature, config)
        assertEquals(ColorRGBa.RED, style.stroke)
        assertEquals(5.0, style.strokeWeight, 0.001)
        
        // Verify type-style wins when per-feature returns null
        val normalFeature = Feature(Point(0.0, 0.0), mapOf("priority" to "low"))
        val typeResult = resolveStyle(normalFeature, config)
        assertEquals(ColorRGBa.GREEN, typeResult.stroke)
    }
    
    @Test
    fun testFeatureGeometryDirectAccess() {
        // Verify feature.geometry is accessible (escape hatch for custom rendering)
        val point = Point(10.0, 20.0)
        val feature = Feature(point, mapOf("name" to "test"))
        
        // Direct access should work
        assertSame(point, feature.geometry)
        assertEquals(10.0, (feature.geometry as Point).x, 0.001)
        assertEquals(20.0, (feature.geometry as Point).y, 0.001)
    }
}
