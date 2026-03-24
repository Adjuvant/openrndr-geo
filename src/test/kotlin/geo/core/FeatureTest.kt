package geo.core

import org.junit.Test
import org.junit.Assert.*
import org.openrndr.math.Vector2

class FeatureTest {

    @Test
    fun testFeatureCreationWithPoint() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("name" to "Origin", "population" to 1000))

        assertEquals("Origin", feature.property("name"))
        assertEquals(1000, feature.property("population"))
    }

    @Test
    fun testPropertyAsTypeSafeAccess() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf(
            "name" to "Test City",
            "population" to 100000,
            "area" to 150.5,
            "capital" to true
        ))

        assertEquals("Test City", feature.propertyAs<String>("name"))
        assertEquals(100000, feature.propertyAs<Int>("population"))
        assertEquals(150.5, feature.propertyAs<Double>("area")!!, 0.0001)
        assertEquals(true, feature.propertyAs<Boolean>("capital"))
    }

    @Test
    fun testPropertyAsReturnsNullForWrongType() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("value" to "string"))

        assertNull(feature.propertyAs<Int>("value"))
    }

    @Test
    fun testHasProperty() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("name" to "Test"))

        assertTrue(feature.hasProperty("name"))
        assertFalse(feature.hasProperty("nonexistent"))
    }

    @Test
    fun testStringProperty() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("name" to "Test City"))

        assertEquals("Test City", feature.stringProperty("name"))
        assertNull(feature.stringProperty("nonexistent"))
    }

    @Test
    fun testDoubleProperty() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf(
            "double_value" to 150.5,
            "int_value" to 100
        ))

        assertEquals(150.5, feature.doubleProperty("double_value")!!, 0.0001)
        assertEquals(100.0, feature.doubleProperty("int_value")!!, 0.0001)
    }

    @Test
    fun testIntProperty() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf(
            "int_value" to 100,
            "double_value" to 150.7
        ))

        assertEquals(100, feature.intProperty("int_value"))
        assertEquals(150, feature.intProperty("double_value"))
    }

    @Test
    fun testBooleanProperty() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("active" to true))

        assertEquals(true, feature.booleanProperty("active"))
        assertNull(feature.booleanProperty("nonexistent"))
    }

    @Test
    fun testPropertyKeys() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("a" to 1, "b" to 2))

        val keys = feature.propertyKeys()
        assertEquals(2, keys.size)
        assertTrue(keys.contains("a"))
        assertTrue(keys.contains("b"))
    }

    @Test
    fun testFeatureBoundingBox() {
        val pt = Point(5.0, 10.0)
        val feature = Feature(pt, emptyMap())

        assertEquals(5.0, feature.boundingBox.minX, 0.0001)
        assertEquals(5.0, feature.boundingBox.maxX, 0.0001)
        assertEquals(10.0, feature.boundingBox.minY, 0.0001)
        assertEquals(10.0, feature.boundingBox.maxY, 0.0001)
    }

    @Test
    fun testFromPointFactory() {
        val feature = Feature.fromPoint(3.0, 4.0, mapOf("name" to "Test"))

        assertTrue(feature.geometry is Point)
        val pt = feature.geometry as Point
        assertEquals(3.0, pt.x, 0.0001)
        assertEquals(4.0, pt.y, 0.0001)
        assertEquals("Test", feature.property("name"))
    }

    @Test
    fun testFeatureWithLineString() {
        val ls = LineString(listOf(Vector2.ZERO, Vector2.ONE))
        val feature = Feature(ls, mapOf("name" to "Route"))

        assertTrue(feature.geometry is LineString)
        assertEquals("Route", feature.property("name"))
    }

    @Test
    fun testFeatureWithPolygon() {
        val poly = Polygon(listOf(
            Vector2(0.0, 0.0),
            Vector2(10.0, 0.0),
            Vector2(5.0, 10.0)
        ))
        val feature = Feature(poly, mapOf("name" to "Area"))

        assertTrue(feature.geometry is Polygon)
        assertEquals("Area", feature.property("name"))
    }

    @Test
    fun testEmptyProperties() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt)

        assertNull(feature.property("anything"))
        assertTrue(feature.propertyKeys().isEmpty())
    }

    @Test
    fun testNullPropertyValue() {
        val pt = Point(0.0, 0.0)
        val feature = Feature(pt, mapOf("optional" to null))

        assertNull(feature.property("optional"))
        assertTrue(feature.hasProperty("optional"))
    }
}
