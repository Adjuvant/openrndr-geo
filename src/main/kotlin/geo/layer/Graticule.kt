package geo.layer

import geo.Bounds
import geo.Feature
import geo.GeoSource
import geo.Point

/**
 * Generate graticule (latitude/longitude grid) features for reference layers.
 *
 * Creates Point features at grid line intersections for visual reference.
 * The graticule helps users orient themselves when viewing projected geo data.
 *
 * ## Usage
 * ```kotlin
 * // Generate a 5-degree graticule for the given bounds
 * val graticule = generateGraticule(5.0, bounds)
 *
 * // Use in a layer
 * val graticuleLayer = layer {
 *     source = generateGraticuleSource(5.0, bounds)
 *     style = Style {
 *         stroke = ColorRGBa.WHITE.withAlpha(0.3)
 *         strokeWeight = 0.5
 *     }
 *     blendMode = Overlay()
 * }
 * ```
 *
 * ## Spacing Values
 * Common spacing values for different zoom levels:
 * - **1.0**: Detailed view, shows individual degree lines
 * - **5.0**: Regional view, good balance of detail and clarity
 * - **10.0**: Continental view, clean reference without clutter
 *
 * @param spacing Grid spacing in degrees (e.g., 1.0, 5.0, 10.0)
 * @param bounds The geographic bounds to cover with the graticule
 * @return List of Point features at grid intersections
 *
 * @see generateGraticuleSource Generate a GeoSource from graticule features
 */
fun generateGraticule(spacing: Double, bounds: Bounds): List<Feature> {
    if (bounds.isEmpty()) return emptyList()

    val features = mutableListOf<Feature>()

    // Calculate grid line positions
    val minLon = kotlin.math.floor(bounds.minX / spacing) * spacing
    val maxLon = kotlin.math.ceil(bounds.maxX / spacing) * spacing
    val minLat = kotlin.math.floor(bounds.minY / spacing) * spacing
    val maxLat = kotlin.math.ceil(bounds.maxY / spacing) * spacing

    // Generate grid intersection points
    var lon = minLon
    while (lon <= maxLon) {
        var lat = minLat
        while (lat <= maxLat) {
            // Create a point at this grid intersection
            features.add(Feature(Point(lon, lat)))
            lat += spacing
        }
        lon += spacing
    }

    return features
}

/**
 * Generate a GeoSource containing graticule features.
 *
 * Wraps [generateGraticule] output in a GeoSource for use with GeoLayer.
 *
 * ## Usage
 * ```kotlin
 * val graticuleLayer = layer {
 *     source = generateGraticuleSource(5.0, dataBounds)
 *     style = Style {
 *         stroke = ColorRGBa.GRAY
 *         strokeWeight = 0.5
 *     }
 *     blendMode = Overlay()
 * }
 * ```
 *
 * @param spacing Grid spacing in degrees (e.g., 1.0, 5.0, 10.0)
 * @param bounds The geographic bounds to cover with the graticule
 * @return GeoSource containing Point features at grid intersections
 */
fun generateGraticuleSource(spacing: Double, bounds: Bounds): GeoSource {
    val features = generateGraticule(spacing, bounds)

    return object : GeoSource(crs = "EPSG:4326") {
        override val features: Sequence<Feature>
            get() = features.asSequence()
    }
}
