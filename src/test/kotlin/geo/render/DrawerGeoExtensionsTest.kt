package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.color.ColorRGBa
import geo.GeoSource
import geo.Feature
import geo.Point
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig

/**
 * Test for API-02: Two-tier API with config block.
 * 
 * Tests drawer.geo() extension function with and without config block.
 */
class DrawerGeoExtensionsTest {

    /**
     * Test that config-less call creates default config.
     */
    @Test
    fun testSimpleGeoCall() {
        val config = GeoRenderConfig()
        assertNull("Default config should have no projection", config.projection)
        assertNull("Default config should have no style", config.style)
        assertTrue("Default config should have empty styleByType", config.styleByType.isEmpty())
        assertNull("Default config should have no styleByFeature", config.styleByFeature)
    }
    
    /**
     * Test that config block is applied.
     */
    @Test
    fun testGeoWithConfigBlock() {
        val config = GeoRenderConfig()
        config.style = Style { stroke = ColorRGBa.RED }
        
        assertNotNull("Config should have style", config.style)
        assertEquals("Style should have red stroke", ColorRGBa.RED, config.style?.stroke)
    }
    
    /**
     * Test that projection from config block is stored.
     */
    @Test
    fun testConfigBlockProjection() {
        val proj = createTestProjection()
        val config = GeoRenderConfig()
        config.projection = proj
        
        assertSame("Config should store projection", proj, config.projection)
    }
    
    /**
     * Test that style from config block is applied.
     */
    @Test
    fun testConfigBlockStyle() {
        val customStyle = Style { fill = ColorRGBa.BLUE; stroke = ColorRGBa.GREEN }
        val config = GeoRenderConfig()
        config.style = customStyle
        
        assertEquals("Style should have blue fill", ColorRGBa.BLUE, config.style?.fill)
        assertEquals("Style should have green stroke", ColorRGBa.GREEN, config.style?.stroke)
    }
    
    /**
     * Test auto-fit when projection not specified.
     */
    @Test
    fun testAutoFitWhenNoProjection() {
        val config = GeoRenderConfig()
        assertNull("Default config should have no projection", config.projection)
    }
    
    /**
     * Test GeoRenderConfig snapshot creates immutable copy.
     */
    @Test
    fun testConfigSnapshot() {
        val original = GeoRenderConfig()
        original.style = Style { stroke = ColorRGBa.RED }
        
        val snapshot = original.snapshot()
        
        // Modify original
        original.style = Style { stroke = ColorRGBa.BLUE }
        
        // Snapshot should be unchanged
        assertEquals("Snapshot should have original style", ColorRGBa.RED, snapshot.style?.stroke)
    }
    
    /**
     * Test styleByType map is copied in snapshot.
     */
    @Test
    fun testStyleByTypeSnapshot() {
        val pointStyle = Style { size = 10.0 }
        val original = GeoRenderConfig()
        original.styleByType = mapOf("Point" to pointStyle)
        
        val snapshot = original.snapshot()
        
        // Modify original map
        original.styleByType = mapOf("Point" to Style { size = 20.0 })
        
        // Snapshot should be unchanged
        assertEquals("Snapshot should have original styleByType", 10.0, snapshot.styleByType["Point"]?.size)
    }

    // ============================================================================
    // Test Helpers
    // ============================================================================

    private fun createTestGeoSource(): GeoSource {
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = sequenceOf(
                Feature(
                    geometry = Point(0.0, 0.0),
                    properties = mapOf("name" to "test1")
                ),
                Feature(
                    geometry = Point(1.0, 1.0),
                    properties = mapOf("name" to "test2")
                )
            )
        }
    }

    private fun createTestProjection(): GeoProjection {
        return object : GeoProjection {
            override fun project(latLng: Vector2): Vector2 {
                // Simple scaling for testing
                return latLng * 100.0
            }

            override fun unproject(screen: Vector2): Vector2 {
                return screen / 100.0
            }

            override fun configure(config: ProjectionConfig): GeoProjection {
                return this
            }

            override fun fitWorld(config: ProjectionConfig): GeoProjection {
                return this
            }
        }
    }
}
