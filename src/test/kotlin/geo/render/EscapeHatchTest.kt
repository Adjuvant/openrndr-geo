package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.junit.Ignore
import org.openrndr.math.Vector2
import org.openrndr.color.ColorRGBa
import geo.GeoSource
import geo.Feature
import geo.Point
import geo.Polygon
import geo.LineString
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig

/**
 * Test scaffold for API-03: Escape hatches - RawProjection and style resolution.
 * 
 * Tests RawProjection type for bypassing projections and style resolution precedence.
 * 
 * @see <a href="https://github.com/openrndr/openrndr-geo/issues/XX">API-03</a>
 */
@Ignore("Implementation pending in 09-03")
class EscapeHatchTest {

    /**
     * Test that RawProjection returns input coordinates unchanged.
     */
    @Test
    fun testRawProjectionIdentity() {
        val rawProjection = RawProjection
        
        val input = Vector2(100.0, 200.0)
        val output = rawProjection.project(input)
        
        assertEquals("RawProjection should return input unchanged", input, output)
    }

    /**
     * Test that RawProjection bypasses projection math entirely.
     */
    @Test
    fun testRawProjectionBypass() {
        val rawProjection = RawProjection
        
        // Even with extreme coordinates, RawProjection should not transform
        val extreme = Vector2(999999.0, 888888.0)
        val result = rawProjection.project(extreme)
        
        assertEquals("RawProjection should bypass all projection math", extreme, result)
    }

    /**
     * Test that styleByFeature function is called per feature.
     */
    @Test
    fun testStyleByFeatureFunction() {
        val source = createTestGeoSource()
        var callCount = 0
        
        val styleFunction: (Feature) -> Style = { feature ->
            callCount++
            Style {
                fill = ColorRGBa.RED
            }
        }
        
        // Apply style function to source
        // This should call the function for each feature
        applyStyleByFeature(source, styleFunction)
        
        assertTrue("Style function should be called for each feature", callCount > 0)
    }

    /**
     * Test that styleByType map applies style by geometry type.
     */
    @Test
    fun testStyleByTypeMap() {
        val styleMap = mapOf(
            "Point" to Style { fill = ColorRGBa.RED },
            "LineString" to Style { stroke = ColorRGBa.BLUE },
            "Polygon" to Style { fill = ColorRGBa.GREEN }
        )
        
        // Point should get Point style
        val pointStyle = resolveStyleByType("Point", styleMap)
        assertNotNull("Point should have style", pointStyle)
        
        // LineString should get LineString style
        val lineStyle = resolveStyleByType("LineString", styleMap)
        assertNotNull("LineString should have style", lineStyle)
        
        // Polygon should get Polygon style
        val polygonStyle = resolveStyleByType("Polygon", styleMap)
        assertNotNull("Polygon should have style", polygonStyle)
    }

    /**
     * Test style resolution precedence: per-feature > by-type > global > default.
     */
    @Test
    fun testStyleResolutionPrecedence() {
        // Highest priority: per-feature style
        val perFeatureStyle = Style { fill = ColorRGBa.RED }
        val resolvedStyle1 = resolveStyle(
            perFeature = perFeatureStyle,
            byType = null,
            global = null
        )
        assertEquals("Per-feature should have highest precedence", perFeatureStyle.fill, resolvedStyle1.fill)
        
        // Second priority: by-type style
        val byTypeStyle = Style { fill = ColorRGBa.BLUE }
        val resolvedStyle2 = resolveStyle(
            perFeature = null,
            byType = byTypeStyle,
            global = null
        )
        assertEquals("By-type should have second precedence", byTypeStyle.fill, resolvedStyle2.fill)
        
        // Third priority: global style
        val globalStyle = Style { fill = ColorRGBa.GREEN }
        val resolvedStyle3 = resolveStyle(
            perFeature = null,
            byType = null,
            global = globalStyle
        )
        assertEquals("Global should have third precedence", globalStyle.fill, resolvedStyle3.fill)
    }

    /**
     * Test that feature.geometry is accessible for custom rendering.
     */
    @Test
    fun testFeatureGeometryDirectAccess() {
        val feature = Feature(
            geometry = Point(100.0, 200.0),
            properties = mapOf("name" to "test")
        )
        
        // Direct geometry access should work
        val geometry = feature.geometry
        assertNotNull("Feature geometry should be accessible", geometry)
        
        // Should be able to access geometry-specific properties
        when (geometry) {
            is Point -> {
                assertEquals("Point x should be accessible", 100.0, geometry.x, 0.0001)
                assertEquals("Point y should be accessible", 200.0, geometry.y, 0.0001)
            }
            else -> fail("Expected Point geometry")
        }
    }

    // ============================================================================
    // Test Helpers / Placeholder Functions
    // ============================================================================

    private fun createTestGeoSource(): GeoSource {
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = sequenceOf(
                Feature(
                    geometry = Point(0.0, 0.0),
                    properties = mapOf("name" to "point1")
                ),
                Feature(
                    geometry = LineString(listOf(
                        Vector2(0.0, 0.0),
                        Vector2(1.0, 1.0),
                        Vector2(2.0, 0.0)
                    )),
                    properties = mapOf("name" to "line1")
                ),
                Feature(
                    geometry = Polygon(listOf(
                        Vector2(0.0, 0.0),
                        Vector2(1.0, 0.0),
                        Vector2(1.0, 1.0),
                        Vector2(0.0, 1.0)
                    )),
                    properties = mapOf("name" to "polygon1")
                )
            )
        }
    }

    // Placeholder functions - would be implemented in actual API
    private fun applyStyleByFeature(source: GeoSource, styleFunction: (Feature) -> Style) {
        // Placeholder: actual implementation would apply style function
    }

    private fun resolveStyleByType(geometryType: String, styleMap: Map<String, Style>): Style? {
        return styleMap[geometryType]
    }

    private fun resolveStyle(
        perFeature: Style?,
        byType: Style?,
        global: Style?
    ): Style {
        // Precedence: perFeature > byType > global > default
        return perFeature ?: byType ?: global ?: Style()
    }
}

/**
 * RawProjection - bypasses all projection calculations.
 * Use this when you want to render in raw coordinates without transformation.
 */
object RawProjection : GeoProjection {
    override fun project(latLng: Vector2): Vector2 {
        // Identity - return input unchanged
        return latLng
    }

    override fun unproject(screen: Vector2): Vector2 {
        // Identity - return input unchanged
        return screen
    }

    override fun configure(config: ProjectionConfig): GeoProjection {
        return this
    }

    override fun fitWorld(config: ProjectionConfig): GeoProjection {
        return this
    }
}
