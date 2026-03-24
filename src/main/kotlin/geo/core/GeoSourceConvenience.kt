package geo.core

import geo.crs.CRS
import java.io.File

/**
 * Convenience functions for loading geospatial data sources.
 *
 * ## Tier 1: Absolute simplest (loadGeo - auto-magic)
 * ```kotlin
 * val data = loadGeo("world.json")  // Auto-detect CRS, auto-caching
 * drawer.geo(data)
 * ```
 *
 * ## Tier 2: Load once, draw many (geoSource - explicit control)
 * ```kotlin
 * val source = geoSource("data.json")  // No auto-magic
 * val projection = source.projectToFit(width, height)
 * drawer.geo(source, projection)
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
 * Load a GeoJSON file and return a GeoSource with explicit control.
 *
 * This function provides explicit, manual control over loading:
 * - No automatic CRS detection
 * - No automatic projection fitting
 * - No caching (unless you wrap it yourself)
 *
 * For auto-magic loading, use [loadGeo] instead.
 *
 * ## Usage
 * ```kotlin
 * val source = geoSource("data.json")
 * val projection = source.projectToFit(width, height)
 * drawer.geo(source, projection)
 * ```
 *
 * @param path Path to the geospatial data file
 * @return A GeoSource implementation for the loaded data
 * @throws FileNotFoundException if the file doesn't exist
 */
fun geoSource(path: String): GeoSource {
    return when {
        path.endsWith(".json", ignoreCase = true) ||
        path.endsWith(".geojson", ignoreCase = true) -> {
            GeoJSON.load(path)
        }
        path.endsWith(".gpkg", ignoreCase = true) -> {
            GeoPackage.load(path)
        }
        else -> throw IllegalArgumentException(
            "Unsupported file format: $path. " +
            "Supported formats: .json, .geojson, .gpkg"
        )
    }
}

/**
 * Load a GeoJSON file with explicit CRS control.
 *
 * @param path Path to the geospatial data file
 * @param crs Explicit CRS to use
 * @return A GeoSource implementation for the loaded data
 * @throws FileNotFoundException if the file doesn't exist
 */
fun geoSource(path: String, crs: CRS): GeoSource {
    val source = geoSource(path)
    return if (crs != CRS.WGS84) {
        source.transform(crs)
    } else {
        source
    }
}

/**
 * Load a GeoJSON file from a File object.
 *
 * @param file File object pointing to the geospatial data
 * @return A GeoSource implementation for the loaded data
 * @throws FileNotFoundException if the file doesn't exist
 */
fun geoSource(file: File): GeoSource {
    return geoSource(file.absolutePath)
}

/**
 * Load a GeoJSON from a string content.
 *
 * @param content GeoJSON string content
 * @return A GeoSource implementation for the parsed data
 * @throws IllegalArgumentException if the content is not valid GeoJSON
 */
fun geoSourceFromString(content: String): GeoSource {
    return GeoJSON.loadString(content)
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
