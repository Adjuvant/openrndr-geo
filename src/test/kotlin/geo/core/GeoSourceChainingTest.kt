package geo.core

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2
import geo.projection.GeoProjection
import geo.projection.ProjectionConfig
import geo.projection.ProjectionFactory

/**
 * Tests for API-01: Feature iteration with projected coordinates.
 * Tests chainable operations (forEach/filter/map) on GeoSource with projection context.
 */
class GeoSourceChainingTest {

    /**
     * Test that withProjection provides access to projected geometry.
     * Each feature iteration should have projected coordinates available.
     */
    @Test
    fun testForEachWithProjection() {
        val features = sequenceOf(
            Feature(Point(0.0, 0.0), mapOf("name" to "origin")),
            Feature(Point(10.0, 10.0), mapOf("name" to "offset"))
        )
        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features
        }
        val projection = ProjectionFactory.mercator(800.0, 600.0)

        val projected = source.withProjection(projection).toList()
        assertEquals(2, projected.size)
        assertTrue(projected[0].projectedGeometry is ProjectedPoint)
        assertTrue(projected[1].projectedGeometry is ProjectedPoint)

        // Verify the original feature is preserved
        assertEquals("origin", projected[0].feature.stringProperty("name"))
    }

    /**
     * Test that filter returns a new GeoSource that maintains the chainable API.
     */
    @Test
    fun testFilterReturnsProjectedSequence() {
        val features = sequenceOf(
            Feature(Point(0.0, 0.0), mapOf("pop" to 50000)),
            Feature(Point(10.0, 10.0), mapOf("pop" to 150000))
        )
        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features
        }

        val result = source.filter { it.doubleProperty("pop")!! > 100000 }
        val filteredFeatures = result.features.toList()

        assertEquals(1, filteredFeatures.size)
        assertEquals(150000.0, filteredFeatures[0].doubleProperty("pop"))
    }

    /**
     * Test that map transforms features while preserving the chainable API.
     */
    @Test
    fun testMapTransformsFeatures() {
        val features = sequenceOf(
            Feature(Point(0.0, 0.0), mapOf("gid" to 1)),
            Feature(Point(10.0, 10.0), mapOf("gid" to 2))
        )
        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features
        }

        val result = source.map { feature ->
            Feature(feature.geometry, mapOf("id" to feature.property("gid")))
        }
        val transformedFeatures = result.features.toList()

        assertEquals(2, transformedFeatures.size)
        assertEquals(1, transformedFeatures[0].property("id"))
        assertEquals(2, transformedFeatures[1].property("id"))
    }

    /**
     * Test that filter().map().withProjection().forEach() chain works correctly.
     */
    @Test
    fun testChainedOperations() {
        val features = sequenceOf(
            Feature(Point(0.0, 0.0), mapOf("pop" to 50000)),
            Feature(Point(10.0, 10.0), mapOf("pop" to 150000)),
            Feature(Point(20.0, 20.0), mapOf("pop" to 200000))
        )
        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features
        }
        val projection = ProjectionFactory.mercator(800.0, 600.0)

        val result = source
            .filter { it.doubleProperty("pop")!! > 100000 }
            .withProjection(projection)
            .toList()

        assertEquals(2, result.size)

        // Verify both have projected geometry
        assertTrue(result[0].projectedGeometry is ProjectedPoint)
        assertTrue(result[1].projectedGeometry is ProjectedPoint)
    }

    /**
     * Test that withProjection() stays lazy (Sequence-based).
     */
    @Test
    fun testWithProjectionLazy() {
        var evaluationCount = 0
        val features = sequenceOf(
            Feature(Point(0.0, 0.0), mapOf("name" to "a")),
            Feature(Point(10.0, 10.0), mapOf("name" to "b"))
        )
        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features.map {
                evaluationCount++
                it
            }
        }
        val projection = ProjectionFactory.mercator(800.0, 600.0)

        // Before iteration, count should be 0 (lazy)
        assertEquals(0, evaluationCount)

        // Create the projected sequence (still lazy)
        val projected = source.withProjection(projection)

        // Still 0 - no iteration yet
        assertEquals(0, evaluationCount)

        // Take one - triggers evaluation
        projected.first()

        // Now we should have evaluated at least once
        assertTrue("Should have evaluated features lazily", evaluationCount > 0)
    }

    // ============================================================================
    // Test Helpers
    // ============================================================================

    private fun createTestGeoSource(): GeoSource {
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = sequenceOf(
                Feature(
                    geometry = Point(0.0, 0.0),
                    properties = mapOf("name" to "point1", "active" to true)
                ),
                Feature(
                    geometry = Point(1.0, 1.0),
                    properties = mapOf("name" to "point2", "active" to false)
                ),
                Feature(
                    geometry = Point(2.0, 2.0),
                    properties = mapOf("name" to "point3", "active" to true)
                )
            )
        }
    }

    private fun createTestProjection(): GeoProjection {
        return object : GeoProjection {
            override fun project(latLng: Vector2): Vector2 {
                // Simple identity projection for testing
                return latLng
            }

            override fun unproject(screen: Vector2): Vector2 {
                return screen
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
