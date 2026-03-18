package geo.render

import geo.internal.OptimizedFeature
import geo.internal.geometry.OptimizedPoint
import geo.internal.geometry.OptimizedLineString
import geo.internal.geometry.OptimizedPolygon
import geo.internal.batch.CoordinateBatch
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for styleByFeature support with OptimizedFeature.
 * Verifies that style resolution works correctly for optimized rendering path.
 */
class OptimizedStyleResolutionTest {

    private fun createCoordBatch(vararg coords: Pair<Double, Double>): CoordinateBatch {
        val x = coords.map { it.first }
        val y = coords.map { it.second }
        return CoordinateBatch(x.toDoubleArray(), y.toDoubleArray())
    }

    private fun createOptimizedPoint(x: Double, y: Double, properties: Map<String, Any?> = emptyMap()): OptimizedFeature {
        return OptimizedFeature(OptimizedPoint(createCoordBatch(x to y)), properties)
    }

    private fun createOptimizedLineString(vararg points: Pair<Double, Double>, properties: Map<String, Any?> = emptyMap()): OptimizedFeature {
        return OptimizedFeature(OptimizedLineString(createCoordBatch(*points)), properties)
    }

    private fun createOptimizedPolygon(rings: List<List<Pair<Double, Double>>>, properties: Map<String, Any?> = emptyMap()): OptimizedFeature {
        val coordBatches = rings.map { createCoordBatch(*it.toTypedArray()) }
        return OptimizedFeature(OptimizedPolygon(coordBatches), properties)
    }

    // --- Basic style resolution tests ---

    @Test
    fun `resolveOptimizedStyle uses styleByOptimizedFeature when provided`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { feature ->
            val pop = feature.properties["population"] as? Double
            if (pop != null && pop > 1000) {
                Style { fill = org.openrndr.color.ColorRGBa.RED }
            } else null
        }

        val highPopFeature = createOptimizedPoint(0.0, 0.0, mapOf("population" to 5000.0))
        val lowPopFeature = createOptimizedPoint(1.0, 1.0, mapOf("population" to 500.0))

        val resolvedHigh = resolveOptimizedStyle(highPopFeature, config)
        val resolvedLow = resolveOptimizedStyle(lowPopFeature, config)

        assertEquals("High population should get RED fill", org.openrndr.color.ColorRGBa.RED, resolvedHigh.fill)
        assertNull("Low population should not get style from callback (null return)", resolvedLow.fill)
    }

    @Test
    fun `resolveOptimizedStyle falls through to styleByType when callback returns null`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { _ -> null }  // Always return null
        config.styleByType = mapOf("Point" to Style { fill = org.openrndr.color.ColorRGBa.BLUE })

        val feature = createOptimizedPoint(0.0, 0.0)

        val resolved = resolveOptimizedStyle(feature, config)

        assertEquals("Should fall through to styleByType", org.openrndr.color.ColorRGBa.BLUE, resolved.fill)
    }

    @Test
    fun `resolveOptimizedStyle falls through to global style when callback and type return null`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { _ -> null }
        config.fill = org.openrndr.color.ColorRGBa.GREEN

        val feature = createOptimizedPoint(0.0, 0.0)

        val resolved = resolveOptimizedStyle(feature, config)

        assertEquals("Should fall through to global style", org.openrndr.color.ColorRGBa.GREEN, resolved.fill)
    }

    @Test
    fun `resolveOptimizedStyle uses defaults when nothing is set`() {
        val config = GeoRenderConfig()
        val feature = createOptimizedPoint(0.0, 0.0)

        val resolved = resolveOptimizedStyle(feature, config)

        // Should use StyleDefaults.defaultStyle
        assertNotNull("Should return a valid style", resolved)
    }

    // --- styleByOptimizedFeature callback context tests ---

    @Test
    fun `styleByOptimizedFeature receives correct properties`() {
        val receivedProperties = mutableMapOf<String, Any?>()
        
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { feature ->
            receivedProperties.putAll(feature.properties)
            Style { fill = org.openrndr.color.ColorRGBa.WHITE }
        }

        val expectedProps = mapOf("name" to "test", "value" to 42, "active" to true)
        val feature = createOptimizedPoint(0.0, 0.0, expectedProps)

        resolveOptimizedStyle(feature, config)

        assertEquals("Should receive correct properties", expectedProps, receivedProperties)
    }

    @Test
    fun `styleByOptimizedFeature works with LineString geometry`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { feature ->
            val type = feature.properties["road_type"] as? String
            if (type == "highway") {
                Style { stroke = org.openrndr.color.ColorRGBa.YELLOW; strokeWeight = 3.0 }
            } else null
        }

        val highway = createOptimizedLineString(0.0 to 0.0, 1.0 to 1.0, properties = mapOf("road_type" to "highway"))
        val street = createOptimizedLineString(2.0 to 2.0, 3.0 to 3.0, properties = mapOf("road_type" to "street"))

        val resolvedHighway = resolveOptimizedStyle(highway, config)
        val resolvedStreet = resolveOptimizedStyle(street, config)

        assertEquals("Highway should get YELLOW stroke", org.openrndr.color.ColorRGBa.YELLOW, resolvedHighway.stroke)
        assertEquals("Highway should get 3.0 strokeWeight", 3.0, resolvedHighway.strokeWeight, 0.001)
        // Note: Street falls through to styleByType (not set) -> resolvedStyle (not set) -> StyleDefaults.defaultLineStyle
        // StyleDefaults.defaultLineStyle has stroke = WHITE (default), so stroke is not null
        assertEquals("Street should get default WHITE stroke from StyleDefaults", 
            org.openrndr.color.ColorRGBa.WHITE, resolvedStreet.stroke)
    }

    @Test
    fun `styleByOptimizedFeature works with Polygon geometry`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { feature ->
            val area = feature.properties["area"] as? Double
            if (area != null && area > 1000) {
                Style { fill = org.openrndr.color.ColorRGBa.GREEN }
            } else null
        }

        val largePolygon = createOptimizedPolygon(
            listOf(
                listOf(0.0 to 0.0, 100.0 to 0.0, 100.0 to 100.0, 0.0 to 100.0, 0.0 to 0.0)
            ),
            mapOf("area" to 10000.0)
        )
        val smallPolygon = createOptimizedPolygon(
            listOf(
                listOf(0.0 to 0.0, 10.0 to 0.0, 10.0 to 10.0, 0.0 to 10.0, 0.0 to 0.0)
            ),
            mapOf("area" to 100.0)
        )

        val resolvedLarge = resolveOptimizedStyle(largePolygon, config)
        val resolvedSmall = resolveOptimizedStyle(smallPolygon, config)

        assertEquals("Large polygon should get GREEN fill", org.openrndr.color.ColorRGBa.GREEN, resolvedLarge.fill)
        assertNull("Small polygon should not get style from callback", resolvedSmall.fill)
    }

    // --- Priority chain tests ---

    @Test
    fun `styleByOptimizedFeature has higher priority than styleByType`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { _ ->
            Style { fill = org.openrndr.color.ColorRGBa.RED }
        }
        config.styleByType = mapOf("Point" to Style { fill = org.openrndr.color.ColorRGBa.BLUE })

        val feature = createOptimizedPoint(0.0, 0.0)
        val resolved = resolveOptimizedStyle(feature, config)

        assertEquals("styleByOptimizedFeature should take priority", org.openrndr.color.ColorRGBa.RED, resolved.fill)
    }

    @Test
    fun `styleByOptimizedFeature has higher priority than global style`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { _ ->
            Style { fill = org.openrndr.color.ColorRGBa.RED }
        }
        config.fill = org.openrndr.color.ColorRGBa.GREEN

        val feature = createOptimizedPoint(0.0, 0.0)
        val resolved = resolveOptimizedStyle(feature, config)

        assertEquals("styleByOptimizedFeature should take priority over global style", org.openrndr.color.ColorRGBa.RED, resolved.fill)
    }

    // --- Edge cases ---

    @Test
    fun `resolveOptimizedStyle handles empty properties`() {
        val config = GeoRenderConfig()
        config.styleByOptimizedFeature = { feature ->
            assertTrue("Properties should be empty or contain expected keys", feature.properties.isEmpty() || feature.properties.containsKey("test"))
            null
        }

        val feature = createOptimizedPoint(0.0, 0.0, emptyMap())
        resolveOptimizedStyle(feature, config)
    }

    @Test
    fun `resolveOptimizedStyle handles missing callback`() {
        val config = GeoRenderConfig()
        config.styleByType = mapOf("Point" to Style { fill = org.openrndr.color.ColorRGBa.BLUE })

        val feature = createOptimizedPoint(0.0, 0.0)
        val resolved = resolveOptimizedStyle(feature, config)

        assertEquals("Should use styleByType when no callback", org.openrndr.color.ColorRGBa.BLUE, resolved.fill)
    }

    @Test
    fun `resolveOptimizedStyle uses type name correctly for different geometry types`() {
        val config = GeoRenderConfig()
        
        // Create a map to track which type was resolved
        var resolvedType: String? = null
        config.styleByType = mapOf(
            "Point" to Style { fill = org.openrndr.color.ColorRGBa.RED },
            "LineString" to Style { fill = org.openrndr.color.ColorRGBa.GREEN },
            "Polygon" to Style { fill = org.openrndr.color.ColorRGBa.BLUE }
        )
        // Don't set styleByOptimizedFeature to fall through to styleByType

        val pointFeature = createOptimizedPoint(0.0, 0.0)
        val lineFeature = createOptimizedLineString(0.0 to 0.0, 1.0 to 1.0)
        val polygonFeature = createOptimizedPolygon(
            listOf(listOf(0.0 to 0.0, 10.0 to 0.0, 10.0 to 10.0, 0.0 to 10.0, 0.0 to 0.0))
        )

        val resolvedPoint = resolveOptimizedStyle(pointFeature, config)
        val resolvedLine = resolveOptimizedStyle(lineFeature, config)
        val resolvedPolygon = resolveOptimizedStyle(polygonFeature, config)

        assertEquals("Point should use Point style", org.openrndr.color.ColorRGBa.RED, resolvedPoint.fill)
        assertEquals("LineString should use LineString style", org.openrndr.color.ColorRGBa.GREEN, resolvedLine.fill)
        assertEquals("Polygon should use Polygon style", org.openrndr.color.ColorRGBa.BLUE, resolvedPolygon.fill)
    }
}
