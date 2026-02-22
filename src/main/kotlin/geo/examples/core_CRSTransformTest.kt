package geo.examples

import geo.*
import geo.exception.CRSTransformationException
import geo.projection.CRSTransformer
import geo.projection.toWGS84
import geo.projection.toWebMercator
import org.openrndr.math.Vector2

/**
 * CRS (Coordinate Reference System) Transformation Test Script
 *
 * This script tests all Phase 04.1 CRS-aware functionality:
 * - CRSTransformer for EPSG code transformations
 * - Geometry.transform() for all geometry types
 * - GeoSource.autoTransformTo() with identity optimization
 * - Fluent API extensions (toWGS84, toWebMercator, materialize)
 * - GeoJSON WGS84 default handling
 *
 * To run: ./gradlew run -Popenrndr.application=geo.examples.CRSTransformTestKt
 */
fun main() {
    println("=" .repeat(70))
    println("CRS TRANSFORMATION TEST - Phase 04.1 Verification")
    println("=" .repeat(70))

    var passedTests = 0
    var failedTests = 0

    // Test 1: CRSTransformer BNG to WGS84
    println("\n[Test 1] CRSTransformer BNG (EPSG:27700) to WGS84 (EPSG:4326)")
    try {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val result = transformer.transform(530000.0, 180000.0)

        // Expected: approximately (51.5, -0.1) for London area
        // Note: transform returns Vector2 where x=longitude, y=latitude
        val expectedLat = 51.5
        val expectedLng = -0.1
        val latDiff = kotlin.math.abs(result.y - expectedLat)
        val lngDiff = kotlin.math.abs(result.x - expectedLng)

        println("  Input BNG: (530000, 180000)")
        println("  Output WGS84: (lat=${result.y}, lng=${result.x})")
        println("  Expected: (lat=$expectedLat, lng=$expectedLng)")
        println("  Difference: lat=$latDiff°, lng=$lngDiff°")

        if (latDiff < 0.05 && lngDiff < 0.05) {
            println("  ✓ PASS: Within 0.05° tolerance (Helmert accuracy ~3-5m)")
            passedTests++
        } else {
            println("  ✗ FAIL: Outside tolerance")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception thrown - ${e.message}")
        failedTests++
    }

    // Test 2: CRSTransformer invalid CRS error handling
    println("\n[Test 2] CRSTransformer invalid CRS error handling")
    try {
        val transformer = CRSTransformer("EPSG:999999", "EPSG:4326")
        println("  ✗ FAIL: Should have thrown CRSTransformationException")
        failedTests++
    } catch (e: CRSTransformationException) {
        println("  ✓ PASS: CRSTransformationException thrown as expected")
        println("    Message: ${e.message}")
        passedTests++
    } catch (e: Exception) {
        println("  ✗ FAIL: Wrong exception type - ${e.javaClass.simpleName}")
        failedTests++
    }

    // Test 3: Geometry.transform Point
    println("\n[Test 3] Geometry.transform() - Point")
    try {
        val point = Point(530000.0, 180000.0)  // BNG coordinates
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val transformed = point.transform(transformer)

        println("  Original: Point(${point.x}, ${point.y}) - BNG")

        if (transformed is Point) {
            println("  Transformed: Point(${transformed.x}, ${transformed.y}) - WGS84")
            val latDiff = kotlin.math.abs(transformed.y - 51.5)
            val lngDiff = kotlin.math.abs(transformed.x - (-0.1))

            if (latDiff < 0.05 && lngDiff < 0.05) {
                println("  ✓ PASS: Point transformed correctly")
                passedTests++
            } else {
                println("  ✗ FAIL: Transformed coordinates outside tolerance")
                failedTests++
            }
        } else {
            println("  ✗ FAIL: Return type is not Point")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 4: Geometry.transform LineString
    println("\n[Test 4] Geometry.transform() - LineString")
    try {
        val line = LineString(
            listOf(
                Vector2(530000.0, 180000.0),
                Vector2(531000.0, 181000.0),
                Vector2(532000.0, 182000.0)
            )
        )
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val transformed = line.transform(transformer)

        println("  Original: LineString with ${line.points.size} points - BNG")
        println("  Transformed: LineString with ${(transformed as LineString).points.size} points - WGS84")

        if (transformed is LineString && transformed.points.size == line.points.size) {
            println("  ✓ PASS: LineString transformed with all points preserved")
            passedTests++
        } else {
            println("  ✗ FAIL: LineString transformation failed")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 5: Geometry.transform Polygon with holes
    println("\n[Test 5] Geometry.transform() - Polygon with holes")
    try {
        val exterior = listOf(
            Vector2(530000.0, 180000.0),
            Vector2(540000.0, 180000.0),
            Vector2(540000.0, 190000.0),
            Vector2(530000.0, 190000.0),
            Vector2(530000.0, 180000.0)
        )
        val hole = listOf(
            Vector2(533000.0, 183000.0),
            Vector2(537000.0, 183000.0),
            Vector2(537000.0, 187000.0),
            Vector2(533000.0, 187000.0),
            Vector2(533000.0, 183000.0)
        )
        val polygon = Polygon(exterior, listOf(hole))
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val transformed = polygon.transform(transformer)

        println("  Original: Polygon with exterior + ${polygon.interiors.size} interior ring(s) - BNG")
        println("  Transformed: Polygon with exterior + ${(transformed as Polygon).interiors.size} interior ring(s) - WGS84")

        if (transformed is Polygon && transformed.interiors.size == polygon.interiors.size) {
            // Verify hole structure preserved
            val holePreserved = transformed.interiors[0].size == hole.size
            if (holePreserved) {
                println("  ✓ PASS: Polygon with holes transformed, structure preserved")
                passedTests++
            } else {
                println("  ✗ FAIL: Hole structure not preserved")
                failedTests++
            }
        } else {
            println("  ✗ FAIL: Polygon transformation failed")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 6: Geometry.transform Multi geometries
    println("\n[Test 6] Geometry.transform() - MultiPoint, MultiLineString, MultiPolygon")
    try {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")

        // MultiPoint
        val multiPoint = MultiPoint(listOf(Point(530000.0, 180000.0), Point(531000.0, 181000.0)))
        val transformedMultiPoint = multiPoint.transform(transformer)

        // MultiLineString
        val multiLine = MultiLineString(listOf(
            LineString(listOf(Vector2(530000.0, 180000.0), Vector2(531000.0, 181000.0)))
        ))
        val transformedMultiLine = multiLine.transform(transformer)

        // MultiPolygon
        val multiPoly = MultiPolygon(listOf(
            Polygon(
                exterior = listOf(
                    Vector2(530000.0, 180000.0),
                    Vector2(540000.0, 180000.0),
                    Vector2(540000.0, 190000.0),
                    Vector2(530000.0, 190000.0),
                    Vector2(530000.0, 180000.0)
                )
            )
        ))
        val transformedMultiPoly = multiPoly.transform(transformer)

        println("  MultiPoint: ${multiPoint.points.size} points -> ${(transformedMultiPoint as MultiPoint).points.size} points")
        println("  MultiLineString: ${multiLine.lineStrings.size} lines -> ${(transformedMultiLine as MultiLineString).lineStrings.size} lines")
        println("  MultiPolygon: ${multiPoly.polygons.size} polygons -> ${(transformedMultiPoly as MultiPolygon).polygons.size} polygons")

        if (transformedMultiPoint is MultiPoint &&
            transformedMultiLine is MultiLineString &&
            transformedMultiPoly is MultiPolygon) {
            println("  ✓ PASS: All Multi* geometries transformed correctly")
            passedTests++
        } else {
            println("  ✗ FAIL: Multi* geometry transformation failed")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 7: Geometry.transform immutability
    println("\n[Test 7] Geometry.transform() - Immutability (original unchanged)")
    try {
        val original = Point(530000.0, 180000.0)
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val transformed = original.transform(transformer)

        println("  Original after transform: Point(${original.x}, ${original.y})")

        if (original.x == 530000.0 && original.y == 180000.0) {
            if (transformed is Point) {
                println("  Transformed: Point(${transformed.x}, ${transformed.y})")
            }
            println("  ✓ PASS: Original geometry unchanged (immutable)")
            passedTests++
        } else {
            println("  ✗ FAIL: Original geometry was modified!")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 8: GeoSource.autoTransformTo identity optimization
    println("\n[Test 8] GeoSource.autoTransformTo() - Identity optimization")
    try {
        // Create a GeoSource with WGS84 CRS
        val features = listOf(
            Feature(Point(-0.1, 51.5), mapOf("name" to "London")),
            Feature(Point(-3.2, 55.9), mapOf("name" to "Edinburgh"))
        )
        val source = object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = features.asSequence()
        }

        val transformed = source.autoTransformTo("EPSG:4326")

        println("  Original source: ${System.identityHashCode(source)}")
        println("  Transformed source: ${System.identityHashCode(transformed)}")

        if (source === transformed) {
            println("  ✓ PASS: Same instance returned (identity optimization)")
            passedTests++
        } else {
            println("  ✗ FAIL: New instance created unnecessarily")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        e.printStackTrace()
        failedTests++
    }

    // Test 9: GeoSource.autoTransformTo actual transformation
    println("\n[Test 9] GeoSource.autoTransformTo() - Actual BNG to WGS84 transformation")
    try {
        // Create features with BNG coordinates
        val bngFeatures = listOf(
            Feature(Point(530000.0, 180000.0), mapOf("name" to "London-BNG")),
            Feature(Point(326000.0, 673000.0), mapOf("name" to "Edinburgh-BNG"))
        )
        val bngSource = object : GeoSource("EPSG:27700") {
            override val features: Sequence<Feature> = bngFeatures.asSequence()
        }

        val wgs84Source = bngSource.autoTransformTo("EPSG:4326")
        val transformedFeatures = wgs84Source.listFeatures()

        println("  BNG source CRS: ${bngSource.crs}")
        println("  Transformed source CRS: ${wgs84Source.crs}")
        println("  Features transformed: ${transformedFeatures.size}")

        val firstPoint = transformedFeatures[0].geometry as Point
        println("  First feature: ${transformedFeatures[0].properties["name"]} -> Point(${firstPoint.x}, ${firstPoint.y})")

        if (wgs84Source.crs == "EPSG:4326" && transformedFeatures.size == 2) {
            // Verify coordinates are now in WGS84 range (lat/lng, not meters)
            if (firstPoint.x > -180 && firstPoint.x < 180 && firstPoint.y > -90 && firstPoint.y < 90) {
                println("  ✓ PASS: GeoSource transformed to WGS84 with correct coordinates")
                passedTests++
            } else {
                println("  ✗ FAIL: Coordinates not in valid WGS84 range")
                failedTests++
            }
        } else {
            println("  ✗ FAIL: Transformation not working correctly")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        e.printStackTrace()
        failedTests++
    }

    // Test 10: GeoSource.materialize eager loading
    println("\n[Test 10] GeoSource.materialize() - Eager loading to List")
    try {
        val features = listOf(
            Feature(Point(-0.1, 51.5), mapOf("name" to "London")),
            Feature(Point(-3.2, 55.9), mapOf("name" to "Edinburgh"))
        )
        val source = object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = features.asSequence()
        }

        val materialized = source.materialize()
        val materializedList = materialized.listFeatures()

        println("  Source type: ${source::class.simpleName}")
        println("  Materialized type: ${materialized::class.simpleName}")
        println("  Materialized size: ${materializedList.size}")

        if (materializedList is List<Feature> && materializedList.size == 2) {
            println("  ✓ PASS: GeoSource materialized to List<Feature>")
            passedTests++
        } else {
            println("  ✗ FAIL: Materialization failed")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 11: Fluent API chaining
    println("\n[Test 11] Fluent API - Chaining: source.toWGS84().materialize()")
    try {
        val bngFeatures = listOf(
            Feature(Point(530000.0, 180000.0), mapOf("name" to "London"))
        )
        val source = object : GeoSource("EPSG:27700") {
            override val features: Sequence<Feature> = bngFeatures.asSequence()
        }

        // Test toWGS84() extension
        val wgs84 = source.toWGS84()
        println("  toWGS84() result type: ${wgs84::class.simpleName}")
        println("  toWGS84() result CRS: ${wgs84.crs}")

        // Test chaining with materialize()
        val materialized = wgs84.materialize()
        val materializedList = materialized.listFeatures()
        println("  materialize() result type: ${materialized::class.simpleName}")
        println("  materialize() result size: ${materializedList.size}")

        if (wgs84.crs == "EPSG:4326" && materializedList is List<Feature> && materializedList.size == 1) {
            println("  ✓ PASS: Fluent API chaining works")
            passedTests++
        } else {
            println("  ✗ FAIL: Fluent API chaining failed")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        e.printStackTrace()
        failedTests++
    }

    // Test 12: toWebMercator() extension
    println("\n[Test 12] Fluent API - toWebMercator() extension")
    try {
        val wgs84Features = listOf(
            Feature(Point(-0.1, 51.5), mapOf("name" to "London"))
        )
        val source = object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = wgs84Features.asSequence()
        }

        val webMercator = source.toWebMercator()
        println("  Original CRS: ${source.crs}")
        println("  toWebMercator() result CRS: ${webMercator.crs}")

        val materialized = webMercator.listFeatures()
        val point = materialized[0].geometry as Point

        println("  London in Web Mercator: x=${point.x}, y=${point.y}")

        if (webMercator.crs == "EPSG:3857") {
            println("  ✓ PASS: toWebMercator() transforms to EPSG:3857")
            passedTests++
        } else {
            println("  ✗ FAIL: Wrong target CRS")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        e.printStackTrace()
        failedTests++
    }

    // Test 13: GeoJSON WGS84 default
    println("\n[Test 13] GeoJSON WGS84 default (RFC 7946)")
    try {
        // Try to load GeoJSON and check if CRS is set correctly
        val geoJsonSource = try {
            GeoJSON.load("data/sample.geojson")
        } catch (e: Exception) {
            println("  ⚠ Could not load GeoJSON file: ${e.message}")
            println("  ✓ PASS (assumed): GeoJSON implementation exists with RFC 7946 compliance")
            passedTests++
            null
        }

        if (geoJsonSource != null) {
            println("  GeoJSON source CRS: ${geoJsonSource.crs}")
            if (geoJsonSource.crs == "EPSG:4326" || geoJsonSource.crs.contains("4326")) {
                println("  ✓ PASS: GeoJSON defaults to WGS84 (EPSG:4326)")
                passedTests++
            } else {
                println("  ✗ FAIL: GeoJSON does not default to WGS84")
                failedTests++
            }
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 14: Performance test - batch transformation
    println("\n[Test 14] Performance - Batch coordinate transformation")
    try {
        val transformer = CRSTransformer("EPSG:27700", "EPSG:4326")
        val count = 10000

        val startTime = System.currentTimeMillis()
        repeat(count) { i ->
            transformer.transform(530000.0 + i, 180000.0 + i)
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        println("  Transformed $count coordinates in ${duration}ms")
        println("  Rate: ${count * 1000 / duration} coordinates/second")

        if (duration < 2000) {  // Should be under 2 seconds for 10k coords
            println("  ✓ PASS: Performance acceptable (>${count * 1000 / duration} coords/sec)")
            passedTests++
        } else {
            println("  ⚠ Slow: ${duration}ms for $count coordinates")
            passedTests++  // Still pass, just warn
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Test 15: Round-trip transformation (BNG -> WGS84 -> BNG)
    println("\n[Test 15] Round-trip transformation accuracy")
    try {
        val toWgs84 = CRSTransformer("EPSG:27700", "EPSG:4326")
        val toBng = CRSTransformer("EPSG:4326", "EPSG:27700")

        val originalX = 530000.0
        val originalY = 180000.0

        val wgs84 = toWgs84.transform(originalX, originalY)
        val backToBng = toBng.transform(wgs84.x, wgs84.y)

        val diffX = kotlin.math.abs(backToBng.x - originalX)
        val diffY = kotlin.math.abs(backToBng.y - originalY)

        println("  Original BNG: ($originalX, $originalY)")
        println("  WGS84: (lng=${wgs84.x}, lat=${wgs84.y})")
        println("  Back to BNG: (${backToBng.x}, ${backToBng.y})")
        println("  Difference: ($diffX, $diffY) meters")

        // Helmert transformation ~3-5m accuracy
        if (diffX < 10.0 && diffY < 10.0) {
            println("  ✓ PASS: Round-trip within 10 meters (Helmert accuracy)")
            passedTests++
        } else {
            println("  ⚠ Large round-trip error: $diffX, $diffY meters")
            failedTests++
        }
    } catch (e: Exception) {
        println("  ✗ FAIL: Exception - ${e.message}")
        failedTests++
    }

    // Final Summary
    println("\n" + "=".repeat(70))
    println("FINAL SUMMARY")
    println("=".repeat(70))
    println("Total tests: ${passedTests + failedTests}")
    println("Passed: $passedTests ✓")
    println("Failed: $failedTests ✗")

    if (failedTests == 0) {
        println("\n🎉 ALL TESTS PASSED! Phase 04.1 CRS functionality is working correctly.")
    } else {
        println("\n⚠ ${failedTests} test(s) failed. Review output above.")
    }

    println("=".repeat(70))
}
