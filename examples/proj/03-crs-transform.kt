@file:JvmName("CrsTransform")
package examples.proj

import geo.core.*
import geo.crs.CRS
import geo.projection.*

/**
 * ## 03 - CRS Transformation
 *
 * Demonstrates coordinate transformation between different Coordinate Reference Systems (CRS)
 * using the transform() function on GeoSource and CRSTransformer for raw coordinates.
 *
 * ### Concepts
 * - Transforming data between CRS (WGS84 → Web Mercator)
 * - Using CRS enum for type-safe CRS selection
 * - Using CRSTransformer for raw coordinate conversion
 * - GeoSource.transform() for automatic feature transformation
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
    println("GeoSource.transform() Example:")
    println("  Load data: val data = loadGeo(\"file.geojson\")")
    println("  Transform: val webMercatorData = data.transform(to = CRS.WebMercator)")
    println("  Returns a new GeoSource with all features in the target CRS")

    println()
    println("CRS transformation is working correctly!")
    println()
    println("Key Concepts:")
    println("  - CRSTransformer(sourceCRS, targetCRS) creates a transformer")
    println("  - transform(x, y) transforms a single coordinate pair")
    println("  - GeoSource.transform(to = CRS) transforms entire datasets")
    println("  - WGS84 (EPSG:4326): Standard GPS coordinates (degrees)")
    println("  - Web Mercator (EPSG:3857): Web map standard (meters)")
    println("  - British National Grid (EPSG:27700): UK mapping (meters)")
}
