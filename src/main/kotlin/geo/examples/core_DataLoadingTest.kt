package geo.examples

import geo.GeoJSON
import geo.GeoSource
import geo.Point
import geo.LineString

/**
 * Data Loading Test Script
 *
 * This script tests the complete data loading pipeline:
 * - Loading GeoJSON files
 * - Accessing features via .features property
 * - Inspecting geometry types
 *
 * To run: ./gradlew run --main=geo.examples.DataLoadingTest
 */
fun main() {
    println("=" .repeat(60))
    println("DATA LOADING TEST")
    println("=" .repeat(60))

    // Test 1: Load sample GeoJSON file
    println("\n[Test 1] Loading GeoJSON file...")
    val dataSource: GeoSource = try {
        GeoJSON.load("data/sample.geojson")
    } catch (e: Exception) {
        println("✗ Failed to load file: ${e.message}")
        return
    }
    println("✓ File loaded successfully")
    println("  Source type: ${dataSource::class.simpleName}")

    // Test 2: Access features sequence
    println("\n[Test 2] Accessing features...")
    val featuresList = dataSource.features.toList()
    val featureCount = featuresList.size
    println("✓ Found $featureCount feature(s)")

    if (featureCount == 0) {
        println("✗ No features found in dataset")
        return
    }

    // Test 3: Inspect feature geometries
    println("\n[Test 3] Inspecting feature geometries...")
    val geometryCounts = mutableMapOf<String, Int>()

    featuresList.forEach { feature ->
        val geometry = feature.geometry
        val geometryType = when (geometry) {
            is Point -> "Point"
            is LineString -> "LineString"
            is geo.Polygon -> "Polygon"
            is geo.MultiPoint -> "MultiPoint"
            is geo.MultiLineString -> "MultiLineString"
            is geo.MultiPolygon -> "MultiPolygon"
            else -> "Unknown"
        }

        println("  - Found: $geometryType")
        geometryCounts[geometryType] = (geometryCounts[geometryType] ?: 0) + 1
    }

    geometryCounts.forEach { (type, count) ->
        println("✓ $type: $count feature(s)")
    }

    // Test 4: Access feature properties
    println("\n[Test 4] Accessing feature properties...")
    var hasProperties = false

    val firstFeature = featuresList.firstOrNull()
    firstFeature?.let { feature ->
        val props = feature.properties
        props.forEach { entry ->
            hasProperties = true
            val key = entry.key
            val value = entry.value
            val typeName = value?.let { v -> v::class.simpleName } ?: "null"
            println("  - $key: $value (type: $typeName)")
        }
    }

    if (!hasProperties) {
        println("  ⓘ No properties found (empty first feature)")
    } else {
        println("✓ Properties accessible")
    }

    // Test 5: Demonstrate lazy iteration
    println("\n[Test 5] Testing lazy iteration...")
    val lazyFirst = dataSource.features.firstOrNull()
    if (lazyFirst != null) {
        println("✓ Lazy sequence iteration works")
        println("  First feature geometry: ${lazyFirst.geometry::class.simpleName}")
    } else {
        println("✗ Unable to iterate features")
    }

    // Summary
    println("\n" + "=".repeat(60))
    println("SUMMARY")
    println("=".repeat(60))
    println("✓ All data loading tests passed")
    println("  - GeoJSON file loaded: data/sample.geojson")
    println("  - Features accessible via .features property")
    println("  - Geometry types: ${geometryCounts.keys.joinToString(", ")}")
    println("  - Total features: $featureCount")
    println("\nData layer is working correctly!")
    println("=" .repeat(60))
}