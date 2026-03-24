package geo.core

import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GeoSourceSummaryTest {

    // Test implementation of GeoSource (matching pattern from GeoSourceTest.kt)
    private class TestGeoSource(
        crs: String = "EPSG:4326",
        private val featureList: List<Feature> = emptyList()
    ) : GeoSource(crs) {
        override val features: Sequence<Feature> = featureList.asSequence()
    }

    @Test
    fun testPrintSummaryEmptySource() {
        val source = TestGeoSource()

        // Capture stdout
        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain 'Empty'", output.contains("Empty"))
    }

    @Test
    fun testPrintSummaryFeatureCount() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0, mapOf("name" to "A")),
            Feature.fromPoint(1.0, 1.0, mapOf("name" to "B")),
            Feature.fromPoint(2.0, 2.0, mapOf("name" to "C"))
        )
        val source = TestGeoSource(featureList = features)

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain feature count", output.contains("3") || output.contains("Features:"))
    }

    @Test
    fun testPrintSummaryBoundsAndCRS() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0),
            Feature.fromPoint(10.0, 5.0),
            Feature.fromPoint(5.0, 10.0)
        )
        val source = TestGeoSource("EPSG:4326", featureList = features)

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain CRS", output.contains("EPSG:4326"))
        assertTrue("Output should contain Bounds", output.contains("Bounds") || output.contains("bounds"))
    }

    @Test
    fun testPrintSummaryGeometryTypeDistribution() {
        val point = Feature.fromPoint(0.0, 0.0)
        val poly = Feature(
            Polygon(listOf(
                org.openrndr.math.Vector2(10.0, 10.0),
                org.openrndr.math.Vector2(20.0, 10.0),
                org.openrndr.math.Vector2(15.0, 20.0)
            )),
            emptyMap()
        )
        val line = Feature(
            LineString(listOf(
                org.openrndr.math.Vector2(30.0, 0.0),
                org.openrndr.math.Vector2(40.0, 10.0)
            )),
            emptyMap()
        )

        val source = TestGeoSource(featureList = listOf(point, poly, line))

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain geometry types", 
            output.contains("Point") || output.contains("Polygon") || output.contains("LineString"))
    }

    @Test
    fun testPrintSummaryMemoryEstimate() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0),
            Feature.fromPoint(1.0, 1.0)
        )
        val source = TestGeoSource(featureList = features)

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain memory estimate", 
            output.contains("Memory") || output.contains("KB") || output.contains("B"))
    }

    @Test
    fun testPrintSummaryPropertyKeysAndTypes() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0, mapOf(
                "name" to "Test",
                "population" to 1000,
                "area" to 50.5
            ))
        )
        val source = TestGeoSource(featureList = features)

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        assertTrue("Output should contain property keys", 
            output.contains("name") || output.contains("population") || output.contains("area"))
    }

    @Test
    fun testPrintSummaryMixedGeometries() {
        val features = listOf(
            Feature.fromPoint(0.0, 0.0), // Point
            Feature( // LineString
                LineString(listOf(
                    org.openrndr.math.Vector2(10.0, 10.0),
                    org.openrndr.math.Vector2(20.0, 20.0)
                )),
                emptyMap()
            ),
            Feature( // Polygon
                Polygon(listOf(
                    org.openrndr.math.Vector2(30.0, 0.0),
                    org.openrndr.math.Vector2(40.0, 0.0),
                    org.openrndr.math.Vector2(35.0, 10.0)
                )),
                emptyMap()
            )
        )
        val source = TestGeoSource(featureList = features)

        val baos = ByteArrayOutputStream()
        val oldOut = System.out
        System.setOut(PrintStream(baos))

        try {
            source.printSummary()
        } finally {
            System.setOut(oldOut)
        }

        val output = baos.toString()
        // Should show all three geometry types
        assertTrue("Output should contain Point", output.contains("Point"))
        assertTrue("Output should contain LineString", output.contains("LineString"))
        assertTrue("Output should contain Polygon", output.contains("Polygon"))
    }
}
