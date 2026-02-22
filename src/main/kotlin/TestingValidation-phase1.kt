import geo.Bounds
import geo.GeoPackage
import geo.GeoPackageSource
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/default.otf", 64.0)
// Phase 1 test 2
//        // Load GeoJSON and access features
//        val source = GeoJSON.load("data/sample.geojson")
//        val features = source.features
//
//        // Iterate over features and access properties
//        features.forEach { feature ->
//            // Method 1: Type-safe property access with reified generics
//            val name: String? = feature.propertyAs<String>("name")
//            val category: String? = feature.propertyAs<String>("category")
//            val length: Double? = feature.propertyAs<Double>("length")
//            println("Name: $name, Category: $category, Length: $length")
//            // Method 2: Convenience methods (returns null-safe types)
//            feature.stringProperty("name")?.let { println("String: $it") }
//            feature.doubleProperty("length")?.let { println("Double: $it") }
//            feature.intProperty("value")?.let { println("Int: $it") }
//            feature.booleanProperty("active")?.let { println("Boolean: $it") }
//            // Method 3: Direct property access as Any
//            val rawValue = feature.property("name")
//            // Check if property exists
//            if (feature.hasProperty("name")) {
//                println("Has name property")
//            }
//            // Get all property keys
//            feature.propertyKeys().forEach { key ->
//                println("Property: $key = ${feature.property(key)}")
//            }
//            // Safe access with default values
//            val nameOrDefault = feature.stringProperty("name") ?: "Unknown"
//            val populationOrDefault = feature.intProperty("population") ?: 0
//        }
//         Phase 1 test 3
//        // Load GeoPackage file
//        val source = GeoPackage.load("data/geo/ness-vectors.gpkg")
//        val features = source.features
//        println("Loading GeoPackage: ${source.crs}")
//        println()
//// Iterate over all features
//        var pointCount = 0
//        var lineCount = 0
//        var polygonCount = 0
//        var multiCount = 0
//        features.forEach { feature ->
//            when (feature.geometry) {
//                is Point -> {
//                    pointCount++
//                    val pt = feature.geometry as Point
//                    println("Point #${pointCount}")
//                    println("  Location: (${pt.x}, ${pt.y})")
//                }
//                is LineString -> {
//                    lineCount++
//                    val ls = feature.geometry as LineString
//                    println("LineString #${lineCount}")
//                    println("  Points: ${ls.points.size}")
//                }
//                is Polygon -> {
//                    polygonCount++
//                    val poly = feature.geometry as Polygon
//                    println("Polygon #${polygonCount}")
//                    println("  Exterior: ${poly.exterior.size} points")
//                    println("  Interiors: ${poly.interiors.size} holes")
//                }
//                is MultiPoint, is MultiLineString, is MultiPolygon -> multiCount++
//            }
//            // Check properties
//            if (feature.propertyKeys().isNotEmpty()) {
//                println("  Properties:")
//                feature.propertyKeys().take(3).forEach { key ->
//                    println("    $key = ${feature.property(key)}")
//                }
//            }
//            println()
//        }
//        println()
//        println("Summary:")
//        println("  Points: $pointCount")
//        println("  LineStrings: $lineCount")
//        println("  Polygons: $polygonCount")
//        println("  Multi-features: $multiCount")

//      Phase 1 test 4

        // Load GeoPackage
        val source: GeoPackageSource = GeoPackage.load("data/geo/ness-vectors.gpkg")
// Get all features first
        val allFeatures = source.features.toList()
        println("Total features: $allFeatures")
        println()
// Test 1: Query by bounds using quadtree
        val bounds = Bounds(minX = 262000.0, minY = 841000.0, maxX = 269000.0, maxY = 841100.0)
        val inBounds = source.queryByBounds(bounds)
        println("Test: queryByBounds()")
        println("  Features in bounds: ${inBounds.count()}")
        println()
// Test 2: Empty bounds
        val emptyBounds = Bounds.empty()
        val inEmpty = source.queryByBounds(emptyBounds)
        println("Test: Empty bounds")
        println("  Count: ${inEmpty.count()}")
        println("  Expected: 0")
        println("  Result: ${if (inEmpty.isEmpty()) "PASS" else "FAIL"}")


        extend {
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))
            drawer.image(image)

            drawer.fill = ColorRGBa.PINK
            drawer.circle(
                cos(seconds) * width / 2.0 + width / 2.0,
                sin(0.5 * seconds) * height / 2.0 + height / 2.0,
                140.0
            )

            drawer.fontMap = font
            drawer.fill = ColorRGBa.WHITE
            drawer.text("OPENRNDR", width / 2.0, height / 2.0)
        }
    }
}
