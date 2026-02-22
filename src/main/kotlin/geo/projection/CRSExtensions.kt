package geo.projection

import geo.GeoSource

/**
 * Convenience extension to transform source to WGS84 (EPSG:4326).
 * Recommended first step after loading data, since all GeoProjection
 * implementations expect WGS84 coordinates.
 *
 * Example:
 * ```kotlin
 * val data = GeoPackage.load("data/geo/ness-vectors.gpkg").toWGS84()
 * ```
 */
fun GeoSource.toWGS84(): GeoSource = autoTransformTo("EPSG:4326")

/**
 * Convenience extension to transform source to Web Mercator (EPSG:3857).
 * Useful for tiled map rendering.
 *
 * Example:
 * ```kotlin
 * val data = GeoJSON.load("file.geojson").toWebMercator()
 * ```
 */
fun GeoSource.toWebMercator(): GeoSource = autoTransformTo("EPSG:3857")

/**
 * Materializes lazy sequences into in-memory lists.
 * Use this for render loops where features are accessed multiple times.
 * Tradeoff: Lazy = per-frame CRS transform cost; eager = upfront cost + memory.
 *
 * Example:
 * ```kotlin
 * val data = GeoPackage.load("data/geo/ness-vectors.gpkg")
 *     .toWGS84()
 *     .materialize()  // Cache transformed features for render loop
 * ```
 */
fun GeoSource.materialize(): GeoSource {
    val materializedFeatures = listFeatures()

    return object : GeoSource(crs) {
        override val features: Sequence<geo.Feature> = materializedFeatures.asSequence()
    }
}
