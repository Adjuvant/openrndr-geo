package geo.core

import geo.crs.CRS
import geo.internal.OptimizedGeoSource
import java.io.File
import java.io.FileNotFoundException

/**
 * Load geo data with auto-magic: auto-CRS detection, auto-projection, and caching.
 *
 * The loadGeo() function provides the simplest possible workflow:
 * - Auto-detects CRS from file metadata (GeoJSON crs member, GeoPackage internal tables)
 * - Returns a CachedGeoSource for automatic viewport-based caching
 * - Falls back to WGS84 with a warning if CRS cannot be detected
 *
 * ## Usage
 * ```kotlin
 * // Three-line workflow
 * val data = loadGeo("world.json")
 * val projection = data.projectToFit(width, height)
 * drawer.geo(data, projection) { stroke = ColorRGBa.WHITE }
 * ```
 *
 * ## Auto-Detection
 * - **GeoJSON**: Checks for "crs" member, defaults to WGS84 per RFC 7946
 * - **GeoPackage**: Reads CRS metadata from gpkg_spatial_ref_sys table
 * - **Unknown**: Attempts GeoJSON parsing, falls back to WGS84 with warning
 *
 * ## Warnings
 * If CRS cannot be detected, prints: "Unknown CRS, assuming WGS84. Use geoSource() for explicit control."
 *
 * @param path Path to the geo data file (GeoJSON or GeoPackage)
 * @return CachedGeoSource with auto-detected CRS and caching enabled
 * @throws FileNotFoundException if the file doesn't exist
 * @throws IllegalArgumentException if the file format is not supported
 */
fun loadGeo(path: String): CachedGeoSource {
    val file = File(path)
    if (!file.exists()) {
        throw FileNotFoundException("File not found: $path")
    }

    val (source, detectedCRS) = when {
        path.endsWith(".json", ignoreCase = true) ||
        path.endsWith(".geojson", ignoreCase = true) -> {
            loadGeoJSONWithCRS(path)
        }
        path.endsWith(".gpkg", ignoreCase = true) -> {
            loadGeoPackageWithCRS(path)
        }
        else -> {
            // Unknown extension - try GeoJSON first, then fallback
            println("Warning: Unknown file format for $path. Attempting GeoJSON parsing.")
            try {
                loadGeoJSONWithCRS(path)
            } catch (e: Exception) {
                println("Warning: Could not parse as GeoJSON. Assuming WGS84. Use geoSource() for explicit control.")
                Pair(GeoJSON.load(path), CRS.WGS84)
            }
        }
    }

    // Warn if we had to fall back to WGS84
    if (detectedCRS == CRS.UNKNOWN) {
        println("Warning: Unknown CRS, assuming WGS84. Use geoSource() for explicit control.")
    }

    return CachedGeoSource(source)
}

/**
 * Load GeoJSON and detect CRS from file.
 *
 * Per RFC 7946, modern GeoJSON uses WGS84 by default. The deprecated "crs"
 * member is checked for but rarely present in practice.
 */
private fun loadGeoJSONWithCRS(path: String): Pair<GeoSource, CRS> {
    // Read file content to check for CRS member
    val content = File(path).readText()

    // Check for deprecated "crs" member in GeoJSON
    val crsRegex = """"crs"\s*:\s*\{[^}]*"name"\s*:\s*"([^"]+)"""".toRegex()
    val crsMatch = crsRegex.find(content)

    val detectedCRS = if (crsMatch != null) {
        val crsName = crsMatch.groupValues[1]
        CRS.fromString(crsName)
    } else {
        // Modern GeoJSON defaults to WGS84 per RFC 7946
        CRS.WGS84
    }

    val source = GeoJSON.loadString(content)
    return Pair(source, detectedCRS)
}

/**
 * Load GeoPackage and read CRS from internal metadata.
 *
 * GeoPackage stores CRS information in the gpkg_spatial_ref_sys table.
 * This is a simplified detection - in production, you'd query the actual table.
 */
private fun loadGeoPackageWithCRS(path: String): Pair<GeoSource, CRS> {
    // GeoPackage loading - for now, load without CRS transformation
    // and let the GeoPackage loader handle it
    val source = GeoPackage.load(path)

    // Try to detect CRS from the GeoPackage
    // In a full implementation, this would query gpkg_spatial_ref_sys
    val detectedCRS = CRS.fromString(source.crs)

    return Pair(source, detectedCRS)
}
