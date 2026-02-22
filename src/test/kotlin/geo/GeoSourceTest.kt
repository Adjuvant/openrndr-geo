package geo

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class GeoSourceTest {

    // Test implementation of GeoSource
    private class TestGeoSource(
        crs: String = "EPSG:4326",
        private val featureList: List<Feature> = emptyList()
    ) : GeoSource(crs) {
        override val features: Sequence<Feature> = featureList.asSequence()
    }

    @Test
    fun testDefaultCRS() {
        val source = TestGeoSource()
        assertEquals("EPSG:4326", source.crs)
        assertEquals("EPSG:4326", source.getCRS())
    }

    @Test
    fun testCustomCRS() {
        val source = TestGeoSource("EPSG:3857")
        assertEquals("EPSG:3857", source.crs)
    }

    @Test
    fun testListFeatures() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0, mapOf("name" to "A")),
            Feature.fromPoint(1.0, 1.0, mapOf("name" to "B"))
        )
        val source = TestGeoSource(featureList = features)
        val list = source.listFeatures()

        assertEquals(2, list.size)
        assertEquals("A", list[0].property("name"))
        assertEquals("B", list[1].property("name"))
    }

    @Test
    fun testCountFeatures() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0),
            Feature.fromPoint(1.0, 1.0),
            Feature.fromPoint(2.0, 2.0)
        )
        val source = TestGeoSource(featureList = features)

        assertEquals(3L, source.countFeatures())
    }

    @Test
    fun testIsEmpty() {
        val emptySource = TestGeoSource()
        assertTrue(emptySource.isEmpty())

        val nonEmptySource = TestGeoSource(featureList = listOf(Feature.fromPoint(0.0, 0.0)))
        assertFalse(nonEmptySource.isEmpty())
    }

    @Test
    fun testFilterFeatures() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0, mapOf("type" to "city")),
            Feature.fromPoint(1.0, 1.0, mapOf("type" to "town")),
            Feature.fromPoint(2.0, 2.0, mapOf("type" to "city"))
        )
        val source = TestGeoSource(featureList = features)

        val cities = source.filterFeatures { it.property("type") == "city" }.toList()
        assertEquals(2, cities.size)
    }

    @Test
    fun testFeaturesInBounds() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0, mapOf("name" to "A")),
            Feature.fromPoint(5.0, 5.0, mapOf("name" to "B")),
            Feature.fromPoint(10.0, 10.0, mapOf("name" to "C"))
        )
        val source = TestGeoSource(featureList = features)

        val searchBounds = Bounds(2.0, 2.0, 8.0, 8.0)
        val found = source.featuresInBounds(searchBounds).toList()

        assertEquals(1, found.size)
        assertEquals("B", found[0].property("name"))
    }

    @Test
    fun testTotalBoundingBox() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0),
            Feature.fromPoint(10.0, 5.0),
            Feature(Point(5.0, 10.0), emptyMap())
        )
        val source = TestGeoSource(featureList = features)

        val bbox = source.totalBoundingBox()
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxX, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }

    @Test
    fun testTotalBoundingBoxEmpty() {
        val source = TestGeoSource()
        val bbox = source.totalBoundingBox()
        assertTrue(bbox.isEmpty())
    }

    @Test
    fun testFeaturesIsLazySequence() {
        var accessCount = 0
        val features = listOf(
            Feature.fromPoint(0.0, 0.0),
            Feature.fromPoint(1.0, 1.0)
        )

        val source = object : GeoSource() {
            override val features: Sequence<Feature> = features.asSequence()
                .map { accessCount++; it }
        }

        // Accessing the sequence shouldn't trigger computation
        val seq = source.features
        assertEquals(0, accessCount)

        // Consuming the sequence triggers computation
        seq.toList()
        assertEquals(2, accessCount)
    }

    @Test
    fun testAutoTransformToCreatesNewSourceForDifferentCRS() {
        val source = TestGeoSource("EPSG:4326")
        val transformed = source.autoTransformTo("EPSG:3857")

        // Should create a new GeoSource, not throw
        assertNotNull(transformed)
        assertNotSame("Should be a different instance", source, transformed)
        assertEquals("Should have target CRS", "EPSG:3857", transformed.crs)
    }

    @Test
    fun testAutoTransformToReturnsSameIfSameCRS() {
        val source = TestGeoSource("EPSG:4326")
        val transformed = source.autoTransformTo("EPSG:4326")
        assertSame(source, transformed)
    }

    @Test
    fun testGeoSourceIsAbstract() {
        // Verify GeoSource is abstract and requires subclassing
        val clazz = GeoSource::class.java
        assertTrue(java.lang.reflect.Modifier.isAbstract(clazz.modifiers))
    }

    @Test
    fun testGeoSourceWithPolygonFeatures() {
        val poly = Polygon(listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(5.0, 10.0)
        ))
        val feature = Feature(poly, mapOf("name" to "Area"))
        val source = TestGeoSource(featureList = listOf(feature))

        val bbox = source.totalBoundingBox()
        assertEquals(0.0, bbox.minX, 0.0001)
        assertEquals(0.0, bbox.minY, 0.0001)
        assertEquals(10.0, bbox.maxX, 0.0001)
        assertEquals(10.0, bbox.maxY, 0.0001)
    }
}
