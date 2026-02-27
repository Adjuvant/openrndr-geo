package geo.render

import org.junit.Test
import org.junit.Assert.*
import org.junit.Ignore
import org.openrndr.math.Vector2
import geo.GeoSource
import geo.Feature
import geo.Point
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig

/**
 * Test scaffold for API-02: Two-tier API with config block.
 * 
 * Tests drawer.geo() extension function with and without config block.
 * 
 * @see <a href="https://github.com/openrndr/openrndr-geo/issues/XX">API-02</a>
 */
@Ignore("Implementation pending in 09-02")
class DrawerGeoExtensionsTest {

    /**
     * Test that drawer.geo(source) works without config block (simple API).
     */
    @Test
    fun testSimpleGeoCall() {
        // Placeholder test - drawer.geo() extension not yet implemented
        // When implemented, should verify:
        // - drawer.geo(source) renders all features with defaults
        // - Uses sensible defaults: minimal, black background, thin white lines
        
        val simpleApiPlanned = true
        assertTrue("Simple API should be planned", simpleApiPlanned)
    }

    /**
     * Test that drawer.geo(source) { projection = ... } works (config block API).
     */
    @Test
    fun testGeoWithConfigBlock() {
        // Placeholder test - drawer.geo() with config block not yet implemented
        // When implemented, should verify:
        // - Config block syntax works: drawer.geo(source) { ... }
        // - projection parameter accessible in config block
        
        val configBlockPlanned = true
        assertTrue("Config block API should be planned", configBlockPlanned)
    }

    /**
     * Test that projection is applied from config block.
     */
    @Test
    fun testConfigBlockProjection() {
        // Placeholder test - projection from config block not yet implemented
        // When implemented, should verify:
        // - projection = ... sets the projection for rendering
        
        val projectionConfigPlanned = true
        assertTrue("Projection config should be planned", projectionConfigPlanned)
    }

    /**
     * Test that style is applied from config block.
     */
    @Test
    fun testConfigBlockStyle() {
        // Placeholder test - style from config block not yet implemented
        // When implemented, should verify:
        // - style = ... applies the style to rendered features
        
        val styleConfigPlanned = true
        assertTrue("Style config should be planned", styleConfigPlanned)
    }

    /**
     * Test auto-fit when projection not specified in config block.
     */
    @Test
    fun testAutoFitWhenNoProjection() {
        // Placeholder test - auto-fit not yet implemented
        // When implemented, should verify:
        // - When projection not specified, auto-fits to viewport bounds
        // - Uses ProjectionFactory.fitBounds() internally
        
        val autoFitPlanned = true
        assertTrue("Auto-fit should be planned", autoFitPlanned)
    }

    // ============================================================================
    // Test Helpers - These exist and can be used
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
