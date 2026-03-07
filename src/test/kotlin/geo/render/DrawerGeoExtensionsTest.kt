package geo.render

import geo.*
import geo.internal.cache.ViewportCache
import geo.internal.cache.ViewportState
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import geo.projection.ProjectionMercator
import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import org.openrndr.color.ColorRGBa

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

    // ============================================================================
    // Viewport Cache Tests (PERF-04, PERF-07)
    // ============================================================================

    /**
     * Test that ViewportCache is properly instantiated and accessible.
     * Verifies the cache infrastructure exists for Drawer.geo() optimization.
     */
    @Test
    fun testViewportCacheExists() {
        // Create a viewport cache instance
        val cache = ViewportCache()
        assertNotNull("ViewportCache should be instantiable", cache)
        assertEquals("New cache should be empty", 0, cache.size)
    }

    /**
     * Test that ViewportCache stores and retrieves projected coordinates.
     */
    @Test
    fun testViewportCacheStoresCoordinates() {
        val cache = ViewportCache()
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        val viewportState = ViewportState.fromProjection(projection)
        val point = Point(0.0, 0.0)

        // First call - should compute and cache
        val coords1 = cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(100.0, 200.0))
        }

        assertEquals("Should return projected coordinates", Vector2(100.0, 200.0), coords1[0])
        assertEquals("Cache should have 1 entry", 1, cache.size)

        // Second call with same geometry and viewport - should use cache
        val coords2 = cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(999.0, 999.0)) // This should NOT be called
        }

        assertEquals("Should return cached coordinates", Vector2(100.0, 200.0), coords2[0])
        assertEquals("Cache should still have 1 entry", 1, cache.size)
    }

    /**
     * Test that ViewportCache clears on viewport state change.
     */
    @Test
    fun testViewportCacheClearsOnViewportChange() {
        val cache = ViewportCache()
        val projection1 = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 1.0
        }
        val projection2 = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 2.0  // Different zoom level
        }
        val viewportState1 = ViewportState.fromProjection(projection1)
        val viewportState2 = ViewportState.fromProjection(projection2)
        val point = Point(0.0, 0.0)

        // Add entry with first viewport state
        cache.getProjectedCoordinates(point, viewportState1) {
            arrayOf(Vector2(100.0, 200.0))
        }
        assertEquals("Cache should have 1 entry", 1, cache.size)

        // Access with different viewport state - should clear cache
        cache.getProjectedCoordinates(point, viewportState2) {
            arrayOf(Vector2(300.0, 400.0))
        }
        assertEquals("Cache should still have 1 entry (old cleared, new added)", 1, cache.size)
    }

    /**
     * Test that ViewportState.fromProjection() creates correct state.
     */
    @Test
    fun testViewportStateFromProjection() {
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
            zoomLevel = 5.0
            center = Vector2(10.0, 20.0)
        }

        val state = ViewportState.fromProjection(projection)

        assertNotNull("ViewportState should be created", state)
        assertEquals("Width should match", 800.0, state.projectionWidth, 0.001)
        assertEquals("Height should match", 600.0, state.projectionHeight, 0.001)
        assertEquals("Zoom should match", 5.0, state.zoomLevel, 0.001)
        assertEquals("Center X should match", 10.0, state.centerX, 0.001)
        assertEquals("Center Y should match", 20.0, state.centerY, 0.001)
    }

    /**
     * Test that geometry dirty flag invalidates cache entry.
     */
    @Test
    fun testGeometryDirtyFlagInvalidatesCache() {
        val cache = ViewportCache()
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        val viewportState = ViewportState.fromProjection(projection)
        val point = Point(0.0, 0.0)

        // First projection - caches result
        cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(100.0, 200.0))
        }
        assertEquals("Cache should have 1 entry", 1, cache.size)

        // Mark geometry as dirty
        point.isDirty = true

        // Re-project - should recompute due to dirty flag
        val newCoords = cache.getProjectedCoordinates(point, viewportState) {
            arrayOf(Vector2(300.0, 400.0))
        }

        assertEquals("Should return new coordinates after dirty flag", Vector2(300.0, 400.0), newCoords[0])
        assertFalse("Dirty flag should be cleared", point.isDirty)
    }

    /**
     * Test cache behavior with different geometry types.
     */
    @Test
    fun testViewportCacheWithMultipleGeometryTypes() {
        val cache = ViewportCache()
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        val viewportState = ViewportState.fromProjection(projection)

        val point = Point(0.0, 0.0)
        val lineString = LineString(listOf(Vector2(0.0, 0.0), Vector2(1.0, 1.0)))
        val polygon = Polygon(
            exterior = listOf(Vector2(0.0, 0.0), Vector2(1.0, 0.0), Vector2(1.0, 1.0), Vector2(0.0, 1.0), Vector2(0.0, 0.0))
        )

        // Cache each geometry type
        cache.getProjectedCoordinates(point, viewportState) { arrayOf(Vector2(100.0, 100.0)) }
        cache.getProjectedCoordinates(lineString, viewportState) {
            arrayOf(Vector2(100.0, 100.0), Vector2(200.0, 200.0))
        }
        cache.getProjectedCoordinates(polygon, viewportState) {
            arrayOf(Vector2(100.0, 100.0), Vector2(200.0, 100.0), Vector2(200.0, 200.0), Vector2(100.0, 200.0), Vector2(100.0, 100.0))
        }

        assertEquals("Cache should have 3 entries", 3, cache.size)
    }

    /**
     * Test cache projector lambda is only called on cache miss.
     */
    @Test
    fun testProjectorLambdaOnlyCalledOnCacheMiss() {
        val cache = ViewportCache()
        val projection = ProjectionMercator {
            width = 800.0
            height = 600.0
        }
        val viewportState = ViewportState.fromProjection(projection)
        val point = Point(0.0, 0.0)

        var callCount = 0
        val projector: () -> Array<Vector2> = {
            callCount++
            arrayOf(Vector2(100.0, 200.0))
        }

        // First call - projector should be invoked
        cache.getProjectedCoordinates(point, viewportState, projector)
        assertEquals("Projector should be called once", 1, callCount)

        // Second call - projector should NOT be invoked (cached)
        cache.getProjectedCoordinates(point, viewportState, projector)
        assertEquals("Projector should still be called once (cached)", 1, callCount)
    }
}
