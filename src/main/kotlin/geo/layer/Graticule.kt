package geo.layer

import geo.core.Bounds
import geo.core.Feature
import geo.core.GeoSource
import geo.core.LineString
import geo.core.Point
import geo.render.geometry.splitAtAntimeridian
import org.openrndr.math.Vector2

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

/**
 * Data class holding latitude and longitude graticule lines as separate GeoSources.
 * 
 * @property latLines GeoSource containing horizontal latitude lines as LineString features
 * @property lngLines GeoSource containing vertical longitude lines as LineString features
 */
data class GraticuleLines(
    val latLines: GeoSource,
    val lngLines: GeoSource
)

/**
 * Generate graticule lines (LineStrings) for the given bounds and spacing.
 * 
 * Creates horizontal latitude lines and vertical longitude lines as separate GeoSources,
 * each containing LineString features.
 *
 * @param bounds The geographic bounds to generate lines within
 * @param spacing The spacing between lines in degrees
 * @return GraticuleLines containing latLines and lngLines GeoSources
 */
fun generateGraticuleLines(bounds: Bounds, spacing: Double): GraticuleLines {
    if (bounds.isEmpty()) {
        val emptySource = object : GeoSource(crs = "EPSG:4326") {
            override val features: Sequence<Feature> = emptySequence()
        }
        return GraticuleLines(emptySource, emptySource)
    }
    
    // Validate minimum spacing
    require(spacing >= 1.0) {
        "Graticule spacing must be at least 1.0 degrees (got $spacing)"
    }
    
    val latLineFeatures = mutableListOf<Feature>()
    val lngLineFeatures = mutableListOf<Feature>()
    
    // Generate latitude lines (horizontal lines at constant y)
    val minLat = kotlin.math.floor(bounds.minY / spacing) * spacing
    val maxLat = kotlin.math.ceil(bounds.maxY / spacing) * spacing
    var lat = minLat
    while (lat <= maxLat + 0.001) {  // Small epsilon for floating point
        val linePoints = listOf(
            Vector2(bounds.minX, lat),
            Vector2(bounds.maxX, lat)
        )
        latLineFeatures.add(Feature(LineString(linePoints)))
        lat += spacing
    }
    
    // Generate longitude lines (vertical lines at constant x)
    // Handle antimeridian crossing: if minX > maxX, bounds spans the antimeridian
    val crossesAntimeridian = bounds.minX > bounds.maxX
    
    // For antimeridian crossing, we need to generate lines in two ranges
    // from minLon to 180, and from -180 to maxLon
    val minLon = kotlin.math.floor(bounds.minX / spacing) * spacing
    val maxLon = kotlin.math.ceil(bounds.maxX / spacing) * spacing
    
    // Normalize: if lon > 180, subtract 360 to get into -180 to 180 range
    fun normalizeLon(l: Double): Double {
        var normalized = l
        while (normalized > 180.0) normalized -= 360.0
        while (normalized < -180.0) normalized += 360.0
        return normalized
    }
    
    fun generateLongitudeLine(lon: Double): List<Vector2> {
        return listOf(
            Vector2(lon, bounds.minY),
            Vector2(lon, bounds.maxY)
        )
    }
    
    if (crossesAntimeridian) {
        // Generate from minLon to 180
        var lon = minLon
        while (lon <= 180.0 + 0.001) {
            val linePoints = generateLongitudeLine(lon)
            // Create closed ring for splitting
            val lineRing = linePoints + linePoints.first()
            val splitRings = splitAtAntimeridian(lineRing)
            for (ring in splitRings) {
                // Remove the closing point (last == first)
                val points = if (ring.first() == ring.last() && ring.size > 2) {
                    ring.dropLast(1)
                } else {
                    ring
                }
                if (points.size >= 2) {
                    lngLineFeatures.add(Feature(LineString(points)))
                }
            }
            lon += spacing
        }
        
        // Generate from -180 to maxLon
        lon = -180.0
        while (lon <= maxLon + 0.001) {
            val linePoints = generateLongitudeLine(lon)
            // Create closed ring for splitting
            val lineRing = linePoints + linePoints.first()
            val splitRings = splitAtAntimeridian(lineRing)
            for (ring in splitRings) {
                val points = if (ring.first() == ring.last() && ring.size > 2) {
                    ring.dropLast(1)
                } else {
                    ring
                }
                if (points.size >= 2) {
                    lngLineFeatures.add(Feature(LineString(points)))
                }
            }
            lon += spacing
        }
    } else {
        // No antimeridian crossing - simple case
        var lon = minLon
        while (lon <= maxLon + 0.001) {
            val linePoints = generateLongitudeLine(lon)
            lngLineFeatures.add(Feature(LineString(linePoints)))
            lon += spacing
        }
    }
    
    val latSource = object : GeoSource(crs = "EPSG:4326") {
        override val features: Sequence<Feature> = latLineFeatures.asSequence()
    }
    
    val lngSource = object : GeoSource(crs = "EPSG:4326") {
        override val features: Sequence<Feature> = lngLineFeatures.asSequence()
    }
    
    return GraticuleLines(latSource, lngSource)
}

/**
 * Generate a GeoLayer containing graticule lines with separate lat/lng sources.
 * 
 * Creates a GeoLayer with latLines and lngLines as separate GeoSource properties,
 * allowing independent styling of latitude and longitude lines.
 *
 * ## Usage
 * ```kotlin
 * val graticule = generateGraticuleLayer(bounds)
 * 
 * // Render latitude lines
 * drawer.geo(graticule.latLines)
 * 
 * // Render longitude lines
 * drawer.geo(graticule.lngLines)
 * ```
 *
 * @param bounds The geographic bounds to generate graticule lines within
 * @param spacing The spacing between lines in degrees (default: auto-calculated)
 * @return GeoLayer with latLines and lngLines GeoSource properties
 */
fun generateGraticuleLayer(bounds: Bounds, spacing: Double? = null): GeoLayer {
    val effectiveSpacing = spacing ?: calculateAdaptiveSpacing(bounds)
    val graticuleLines = generateGraticuleLines(bounds, effectiveSpacing)
    
    return GeoLayer(
        latLines = graticuleLines.latLines,
        lngLines = graticuleLines.lngLines
    )
}

/**
 * Represents a single graticule label with its text and position.
 *
 * @property text The formatted label text (e.g., "45°N", "120°W")
 * @property longitude The geographic longitude coordinate
 * @property latitude The geographic latitude coordinate
 * @property projectedX The projected screen X coordinate
 * @property projectedY The projected screen Y coordinate
 */
data class LabelPosition(
    val text: String,
    val longitude: Double,
    val latitude: Double,
    val projectedX: Double,
    val projectedY: Double
)

/**
 * Container for graticule latitude and longitude labels.
 *
 * @property latitudeLabels List of labels for latitude lines (positioned at left edge)
 * @property longitudeLabels List of labels for longitude lines (positioned at bottom edge)
 */
data class GraticuleLabels(
    val latitudeLabels: List<LabelPosition>,
    val longitudeLabels: List<LabelPosition>
)
