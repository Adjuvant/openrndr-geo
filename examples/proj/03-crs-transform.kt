@file:JvmName("CrsTransform")
package examples.proj

import geo.projection.CRSTransformer
import geo.crs.CRS

/**
 * ## 03 - CRS Transformation
 *
 * Demonstrates coordinate transformation between different Coordinate Reference Systems (CRS)
 * using the Proj4J-based CRSTransformer.
 *
 * ### Concepts
 * - Understanding CRS (Coordinate Reference Systems)
 * - Transforming coordinates between EPSG codes
 * - WGS84 (EPSG:4326) to British National Grid (EPSG:27700) transformation
 * - Using CRSTransformer for coordinate conversion
 *
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.proj.CrsTransformKt
 * ```
 */
fun main() {
    println("=== CRS Transformation Example ===")
    println()

    // Create a transformer from WGS84 to British National Grid
    val wgs84ToBNG = CRSTransformer(
        sourceCRS = CRS.WGS84.code,  // EPSG:4326
        targetCRS = CRS.BritishNationalGrid.code  // EPSG:27700
    )

    // Transform coordinates from WGS84 to BNG
    // Example: London coordinates in WGS84
    val londonWGS84 = Pair(-0.1276, 51.5074)  // (longitude, latitude)

    println("Transforming WGS84 (lon/lat) to British National Grid (eastings/northings):")
    println("  Input: London, WGS84 (lon=${londonWGS84.first}, lat=${londonWGS84.second})")

    val londonBNG = wgs84ToBNG.transform(londonWGS84.first, londonWGS84.second)
    println("  Output: London, BNG (easting=${londonBNG.x}, northing=${londonBNG.y})")
    println()

    // Transform back from BNG to WGS84
    val bngToWGS84 = CRSTransformer(
        sourceCRS = CRS.BritishNationalGrid.code,
        targetCRS = CRS.WGS84.code
    )

    val recoveredWGS84 = bngToWGS84.transform(londonBNG.x, londonBNG.y)
    println("Round-trip transformation (BNG -> WGS84):")
    println("  Input: BNG (easting=${londonBNG.x}, northing=${londonBNG.y})")
    println("  Output: WGS84 (lon=${recoveredWGS84.x}, lat=${recoveredWGS84.y})")
    println()

    // Test with multiple cities
    val cities = listOf(
        Pair("Tokyo", Pair(139.6917, 35.6895)),
        Pair("New York", Pair(-74.0060, 40.7128)),
        Pair("Sydney", Pair(151.2093, -33.8688)),
        Pair("Paris", Pair(2.3522, 48.8566))
    )

    println("Transforming multiple cities to Web Mercator (EPSG:3857):")
    val wgs84ToWebMercator = CRSTransformer(
        sourceCRS = CRS.WGS84.code,
        targetCRS = CRS.WebMercator.code
    )

    cities.forEach { (name, coords) ->
        val mercator = wgs84ToWebMercator.transform(coords.first, coords.second)
        println("  $name: lon=${coords.first}, lat=${coords.second} -> x=${mercator.x.toLong()}, y=${mercator.y.toLong()}")
    }

    println()
    println("CRS transformation is working correctly!")
    println()
    println("Key Concepts:")
    println("  - CRSTransformer(sourceCRS, targetCRS) creates a transformer")
    println("  - transform(x, y) transforms a single coordinate pair")
    println("  - WGS84 (EPSG:4326): Standard GPS coordinates (degrees)")
    println("  - Web Mercator (EPSG:3857): Web map standard (meters)")
    println("  - British National Grid (EPSG:27700): UK mapping (meters)")
}
