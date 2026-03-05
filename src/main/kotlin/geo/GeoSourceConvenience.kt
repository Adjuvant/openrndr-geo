package geo

import java.io.File

/**
 * Convenience functions for loading geospatial data sources with auto-detection.
 * 
 * ## Tier 1: Absolute simplest
 * ```kotlin
 * extend {
 *     drawer.geoJSON("world.json")  // Auto-load, auto-fit, auto-render
 * }
 * ```
 * 
 * ## Tier 2: Load once, draw many
 * ```kotlin
 * val source = geoSource("data.json")  // With auto-detection
 * extend {
 *     source.render(drawer)
 * }
 * ```
 * 
 * ## Tier 3: Full control (existing API preserved)
 * ```kotlin
 * val features = GeoJSON.load(File("data.json"))
 * val projection = ProjectionMercator { width = 800; height = 600 }
 * extend {
 *     features.forEach { drawer.draw(it.geometry, projection) }
 * }
 * ```
 */

/**
 * Load a GeoJSON file and return a GeoJSONSource.
 *
 * This is a convenience wrapper around [GeoJSON.load] that provides a consistent
 * API for loading geospatial data.
 *
 * ## Auto-detection
 * Currently detects GeoJSON files. Future versions will auto-detect format
 * from file extension (.json, .geojson, .gpkg).
 *
 * ## Usage
 * ```kotlin
 * val source = geoSource("data.json")
 * source.features.forEach { println(it.properties) }
 * ```
 *
 * @param path Path to the geospatial data file
 * @param optimize Whether to enable batch projection optimization (default: false)
 * @return A GeoSource implementation for the loaded data
 * @throws FileNotFoundException if the file doesn't exist
 */
fun geoSource(path: String, optimize: Boolean = false): GeoJSONSource {
    return GeoJSON.load(path, optimize)
}

/**
 * Load a GeoJSON file from a File object.
 *
 * @param file File object pointing to the geospatial data
 * @param optimize Whether to enable batch projection optimization (default: false)
 * @return A GeoSource implementation for the loaded data
 * @throws FileNotFoundException if the file doesn't exist
 */
fun geoSource(file: File, optimize: Boolean = false): GeoJSONSource {
    return GeoJSON.load(file.absolutePath, optimize)
}

/**
 * Load a GeoJSON from a string content.
 *
 * @param content GeoJSON string content
 * @param optimize Whether to enable batch projection optimization (default: false)
 * @return A GeoSource implementation for the parsed data
 * @throws IllegalArgumentException if the content is not valid GeoJSON
 */
fun geoSourceFromString(content: String, optimize: Boolean = false): GeoJSONSource {
    return GeoJSON.loadString(content, optimize)
}

/**
 * Create a GeoSource from an existing sequence of features.
 * 
 * Useful when you have features in memory and want to use the GeoSource API
 * for rendering or transformation.
 * 
 * @param features Sequence of features
 * @param crs Coordinate reference system (default: WGS84)
 * @return A GeoSource wrapping the provided features
 */
fun geoSourceFromFeatures(
    features: Sequence<Feature>,
    crs: String = "EPSG:4326"
): GeoSource {
    return object : GeoSource(crs) {
        override val features: Sequence<Feature> = features
    }
}
