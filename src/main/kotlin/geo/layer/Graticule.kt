package geo.layer

import geo.core.Bounds
import geo.core.Feature
import geo.core.GeoSource
import geo.core.Point

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
 *  *
 * @param spacing Grid spacing in degrees (minimum 1.0, typical: 1.0, 5.0, 10.0)
 * @throws IllegalArgumentException if spacing < 1.0 or bounds are unreasonably large
 * @return List of Point features at grid intersections
 *
 * @see generateGraticuleSource Generate a GeoSource from graticule features
 */
fun generateGraticule(spacing: Double, bounds: Bounds): List<Feature> {
    if (bounds.isEmpty()) return emptyList()

    // Validate minimum spacing to prevent excessive memory usage
    require(spacing >= 1.0) {
        "Graticule spacing must be at least 1.0 degrees (got $spacing). Small values cause excessive point generation."
    }

    // Validate bounds are reasonable (prevent extreme ranges)
    val boundsWidth = bounds.maxX - bounds.minX
    val boundsHeight = bounds.maxY - bounds.minY
    require(boundsWidth <= 360.0 && boundsHeight <= 180.0) {
        "Bounds too large for graticule generation (width=$boundsWidth, height=$boundsHeight)"
    }

    val features = mutableListOf<Feature>()

    // Calculate grid line positions
    val minLon = kotlin.math.floor(bounds.minX / spacing) * spacing
    val maxLon = kotlin.math.ceil(bounds.maxX / spacing) * spacing
    val minLat = kotlin.math.floor(bounds.minY / spacing) * spacing
    val maxLat = kotlin.math.ceil(bounds.maxY / spacing) * spacing

    // Calculate number of steps (integer-based with safety limit)
    val lonSteps = kotlin.math.round((maxLon - minLon) / spacing).toInt().coerceAtMost(1000)
    val latSteps = kotlin.math.round((maxLat - minLat) / spacing).toInt().coerceAtMost(1000)

    // Generate using integer indices to avoid floating-point accumulation errors
    for (i in 0..lonSteps) {
        val lon = minLon + i * spacing
        for (j in 0..latSteps) {
            val lat = minLat + j * spacing
            features.add(Feature(Point(lon, lat)))
        }
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
 * @param spacing Grid spacing in degrees (minimum 1.0, typical: 1.0, 5.0, 10.0)
 * @param bounds The geographic bounds to cover with the graticule
 * @throws IllegalArgumentException if spacing < 1.0 or bounds are unreasonably large
 * @return GeoSource containing Point features at grid intersections
 */
fun generateGraticuleSource(spacing: Double, bounds: Bounds): GeoSource {
    val features = generateGraticule(spacing, bounds)

    return object : GeoSource(crs = "EPSG:4326") {
        override val features: Sequence<Feature>
            get() = features.asSequence()
    }
}

/**
 * Calculate adaptive spacing for graticule based on viewport size.
 * 
 * Uses power-of-10 grid (1°, 10°, 30°, 90°) based on visible geographic extent.
 * Always returns at least 1.0° spacing to prevent visual clutter.
 *
 * @param bounds The geographic bounds of the viewport
 * @return The optimal spacing in degrees
 */
fun calculateAdaptiveSpacing(bounds: Bounds): Double {
    if (bounds.isEmpty()) return 1.0
    
    // Calculate visible extent
    val visibleDegrees = maxOf(bounds.width, bounds.height)
    
    // Select appropriate spacing based on visible extent
    // Always return at least 1.0° minimum floor
    return when {
        visibleDegrees < 2.0 -> 1.0
        visibleDegrees < 20.0 -> 10.0
        visibleDegrees < 60.0 -> 30.0
        else -> 90.0
    }
}
