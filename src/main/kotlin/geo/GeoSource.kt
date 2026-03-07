package geo

import geo.crs.CRS
import geo.projection.CRSTransformer
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.Style
import geo.render.drawLineString
import geo.render.drawPoint
import geo.render.drawPolygon
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2

/**
 * Abstract base class for all geospatial data sources.
 * GeoSource provides a unified interface for accessing features from
 * different data formats (GeoJSON, GeoPackage, etc.).
 *
 * @property crs The Coordinate Reference System identifier (default: "EPSG:4326" for WGS84)
 */
abstract class GeoSource(
    val crs: String = "EPSG:4326" // WGS84 default
) {
    /**
     * The features in this data source as a lazy Sequence.
     * Using Sequence enables memory-efficient processing of large datasets.
     */
    abstract val features: Sequence<Feature>

    /**
     * Returns all features as a List.
     * Note: This loads all features into memory - use with caution for large datasets.
     */
    fun listFeatures(): List<Feature> = features.toList()

    /**
     * Returns the count of features in this source.
     * Default implementation iterates through all features.
     * Subclasses may provide more efficient implementations.
     */
    open fun countFeatures(): Long = features.count().toLong()

    /**
     * Filters features by a predicate.
     *
     * @param predicate The filter condition
     * @return A sequence of features matching the predicate
     */
    fun filterFeatures(predicate: (Feature) -> Boolean): Sequence<Feature> {
        return features.filter(predicate)
    }

    /**
     * Returns features that intersect the given bounding box.
     *
     * @param bounds The bounding box to test against
     * @return A sequence of features whose bounding boxes intersect the given bounds
     */
    fun featuresInBounds(bounds: Bounds): Sequence<Feature> {
        return features.filter { it.boundingBox.intersects(bounds) }
    }

    /**
     * Returns the bounding box of all features in this source.
     * Returns empty bounds if there are no features.
     */
    open fun totalBoundingBox(): Bounds {
        return features.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox)
        }
    }

    /**
     * Returns the bounding box of all features.
     * Default implementation delegates to totalBoundingBox().
     * Subclasses may provide more efficient implementations.
     */
    open fun boundingBox(): Bounds = totalBoundingBox()

    /**
     * Transforms this data source to a different CRS.
     * Creates a new GeoSource with lazy transformed Sequence.
     *
     * Identity optimization: Returns this instance if source CRS equals target CRS.
     *
     * @param targetCRS The target CRS identifier (e.g., "EPSG:4326")
     * @return A GeoSource in the target CRS (same instance if CRS matches)
     */
    open fun autoTransformTo(targetCRS: String): GeoSource {
        if (crs == targetCRS) return this  // Identity optimization

        val transformer = CRSTransformer(crs, targetCRS)

        return object : GeoSource(targetCRS) {
            override val features: Sequence<Feature> =
                this@GeoSource.features.map { feature ->
                    Feature(
                        geometry = feature.geometry.transform(transformer),
                        properties = feature.properties
                    )
                }
        }
    }

    /**
     * Transforms this GeoSource to a different CRS using the strongly-typed CRS enum.
     *
     * ## Usage
     * ```kotlin
     * val source = geoSource("data.json")
     * val webMercator = source.transform(to = CRS.WebMercator)
     * ```
     *
     * @param to The target CRS
     * @return A GeoSource in the target CRS (same instance if CRS matches)
     */
    open fun transform(to: CRS): GeoSource {
        if (to.isUnknown()) {
            println("Warning: Cannot transform to unknown CRS. Keeping original CRS.")
            return this
        }
        return autoTransformTo(to.code)
    }

    /**
     * Materializes lazy sequences into in-memory lists.
     * Use this for render loops where features are accessed multiple times.
     *
     * Tradeoff: Lazy = per-frame CRS transform cost; eager = upfront cost + memory.
     *
     * @return A new GeoSource backed by in-memory List
     */
    fun materialize(): GeoSource {
        val materializedFeatures = listFeatures()

        return object : GeoSource(crs) {
            override val features: Sequence<Feature> = materializedFeatures.asSequence()
        }
    }

    /**
     * Returns true if this source contains no features.
     */
    fun isEmpty(): Boolean = !features.any()

    /**
     * Returns the CRS identifier.
     */
    fun getCRS(): String = crs

    /**
     * Render this GeoSource to the given Drawer with the specified projection.
     * 
     * This is a convenience method that iterates through all features and renders
     * them using the provided projection. For complex use cases, access features
     * directly and use drawer.draw() for fine-grained control.
     * 
     * ## Usage
     * ```kotlin
     * val source = geoSource("data.json")
     * val projection = ProjectionMercator { width = 800; height = 600 }
     * extend {
     *     source.render(drawer, projection)
     * }
     * ```
     * 
     * @param drawer OpenRNDR Drawer for rendering
     * @param projection Projection to use for coordinate transformation
     * @param style Optional rendering style (null = use defaults)
     */
    fun render(drawer: Drawer, projection: GeoProjection, style: Style? = null) {
        features.forEach { feature ->
            feature.geometry.renderToDrawer(drawer, projection, style)
        }
    }

    /**
     * Render this GeoSource to the given Drawer, automatically fitting to the viewport.
     * 
     * This convenience method creates a projection fitted to the data bounds.
     * 
     * @param drawer OpenRNDR Drawer for rendering
     * @param style Optional rendering style
     */
    fun render(drawer: Drawer, style: Style? = null) {
        val bounds = totalBoundingBox()
        val projection = ProjectionFactory.fitBounds(
            bounds = bounds,
            width = drawer.width.toDouble(),
            height = drawer.height.toDouble(),
            // Padding as ratio of available space: 0.9 = 90% fill, 10% padding
            // Note: fitBounds() documents padding in pixels, but this usage treats
            // it as a scale factor. 0.9 provides subtle padding while maintaining
            // tight visual fit for the common case of rendering a source to viewport.
            padding = 0.9,
            projection = ProjectionType.MERCATOR
        )
        render(drawer, projection, style)
    }

    /**
     * Print a summary of this GeoSource to the console.
     * Shows feature count, bounds, CRS, geometry types, memory estimate, and property keys.
     * Uses a single pass through the features Sequence for efficiency.
     */
    fun printSummary() {
        if (isEmpty()) {
            println("GeoSource: Empty (no features)")
            return
        }

        // Single-pass statistics collection
        var featureCount = 0L
        var bounds = Bounds.empty()
        val geometryCounts = mutableMapOf<String, Int>()
        val propertyTypes = mutableMapOf<String, String>()
        var coordCount = 0

        features.forEach { feature ->
            featureCount++
            bounds = bounds.expandToInclude(feature.boundingBox)

            // Geometry type detection using exhaustive when on sealed class
            val geomType = when (feature.geometry) {
                is Point -> "Point"
                is LineString -> "LineString"
                is Polygon -> "Polygon"
                is MultiPoint -> "MultiPoint"
                is MultiLineString -> "MultiLineString"
                is MultiPolygon -> "MultiPolygon"
            }
            geometryCounts[geomType] = (geometryCounts[geomType] ?: 0) + 1

            // Coordinate counting for memory estimation
            coordCount += countCoordinates(feature.geometry)

            // Property type inference
            feature.properties.forEach { (key, value) ->
                if (key !in propertyTypes) {
                    propertyTypes[key] = inferTypeName(value)
                }
            }
        }

        // Format and print output
        val separator = "─".repeat(52)
        println("┌$separator┐")
        println("│ ${"GeoSource Summary".center(50)} │")
        println("├$separator┤")
        println("│ Features:    ${featureCount.toString().padEnd(36)} │")
        println("│ CRS:         ${crs.padEnd(36)} │")
        println("│ Bounds:      ${formatBounds(bounds).padEnd(36)} │")
        println("├$separator┤")
        println("│ ${"Geometry Types:".padEnd(50)} │")
        geometryCounts.toSortedMap().forEach { (type, count) ->
            val percentage = (count.toDouble() / featureCount * 100).toInt()
            println("│   ${"$type: $count ($percentage%)".padEnd(48)} │")
        }
        println("├$separator┤")
        println("│ Memory:      ${formatMemory(coordCount).padEnd(36)} │")
        println("├$separator┤")
        println("│ ${"Properties (${propertyTypes.size} keys):".padEnd(50)} │")
        propertyTypes.toSortedMap().entries.take(10).forEach { (key, type) ->
            println("│   ${"$key: $type".padEnd(48)} │")
        }
        if (propertyTypes.size > 10) {
            println("│   ${"... and ${propertyTypes.size - 10} more".padEnd(48)} │")
        }
        println("└$separator┘")
    }

    /**
     * Returns a Sequence of ProjectedFeature with projected screen geometry.
     * Projection is applied lazily during iteration.
     *
     * ## Usage
     * ```kotlin
     * source.withProjection(projection).forEach { (feature, projected) ->
     *     // projected.screenPoints ready for rendering
     * }
     * ```
     */
    fun withProjection(projection: GeoProjection): Sequence<ProjectedFeature> {
        return features.map { feature ->
            ProjectedFeature(feature, projectGeometry(feature.geometry, projection))
        }
    }

    private fun projectGeometry(geom: Geometry, projection: GeoProjection): ProjectedGeometry = when (geom) {
        is Point -> ProjectedPoint(projection.project(Vector2(geom.x, geom.y)))
        is LineString -> ProjectedLineString(geom.points.map { projection.project(it) })
        is Polygon -> ProjectedPolygon(
            exterior = geom.exterior.map { projection.project(it) },
            holes = geom.interiors.map { ring -> ring.map { projection.project(it) } }
        )
        is MultiPoint -> ProjectedMultiPoint(geom.points.map { projection.project(Vector2(it.x, it.y)) })
        is MultiLineString -> ProjectedMultiLineString(geom.lineStrings.map { line ->
            line.points.map { projection.project(it) }
        })
        is MultiPolygon -> ProjectedMultiPolygon(geom.polygons.map { poly ->
            ProjectedPolygon(
                exterior = poly.exterior.map { projection.project(it) },
                holes = poly.interiors.map { ring -> ring.map { projection.project(it) } }
            )
        })
    }

    /**
     * Filters features by predicate, returning a new GeoSource.
     * Use for pre-render filtering based on properties.
     *
     * ## Usage
     * ```kotlin
     * source.filter { it.doubleProperty("population") > 100000 }
     *       .withProjection(projection)
     *       .forEach { ... }
     * ```
     *
     * Note: Returns new GeoSource, original unchanged.
     */
    fun filter(predicate: (Feature) -> Boolean): GeoSource {
        val filtered = this
        return object : GeoSource(crs) {
            override val features: Sequence<Feature> = filtered.features.filter(predicate)
        }
    }

    /**
     * Transforms features, returning a new GeoSource.
     * Use for property extraction or geometry modification.
     *
     * ## Usage
     * ```kotlin
     * source.map { feature ->
     *     Feature(feature.geometry, mapOf("id" to feature.property("gid")))
     * }
     * ```
     */
    fun map(transform: (Feature) -> Feature): GeoSource {
        val source = this
        return object : GeoSource(crs) {
            override val features: Sequence<Feature> = source.features.map(transform)
        }
    }
}

/**
 * Extension on Sequence<ProjectedFeature> for forEach with destructuring.
 * Enables clean iteration syntax.
 *
 * ## Usage
 * ```kotlin
 * source.withProjection(projection).forEach { (feature, projected) ->
 *     drawer.drawPolygon(projected.screenPoints)
 * }
 * ```
 */
fun Sequence<ProjectedFeature>.forEachWithProjection(action: (Feature, ProjectedGeometry) -> Unit) {
    forEach { action(it.feature, it.projectedGeometry) }
}

/**
 * Render this geometry to the given Drawer.
 */
private fun Geometry.renderToDrawer(drawer: Drawer, projection: GeoProjection, style: Style?) {
    when (this) {
        is Point -> {
            val screen = projection.project(Vector2(x, y))
            drawPoint(drawer, screen, style)
        }
        is LineString -> {
            val screenPoints = points.map { projection.project(it) }
            drawLineString(drawer, screenPoints, style)
        }
        is Polygon -> {
            val screenPoints = exterior.map { projection.project(it) }
            drawPolygon(drawer, screenPoints, style)
        }
        is MultiPoint -> {
            points.forEach { pt ->
                val screen = projection.project(Vector2(pt.x, pt.y))
                drawPoint(drawer, screen, style)
            }
        }
        is MultiLineString -> {
            lineStrings.forEach { line ->
                val screenPoints = line.points.map { projection.project(it) }
                drawLineString(drawer, screenPoints, style)
            }
        }
        is MultiPolygon -> {
            polygons.forEach { poly ->
                val screenPoints = poly.exterior.map { projection.project(it) }
                drawPolygon(drawer, screenPoints, style)
            }
        }
    }
}

// ============================================================================
// printSummary() Helper Functions
// ============================================================================

/**
 * Count the number of coordinates in a geometry for memory estimation.
 */
private fun countCoordinates(geom: Geometry): Int = when (geom) {
    is Point -> 1
    is LineString -> geom.points.size
    is Polygon -> geom.exterior.size + geom.interiors.sumOf { it.size }
    is MultiPoint -> geom.points.size
    is MultiLineString -> geom.lineStrings.sumOf { it.points.size }
    is MultiPolygon -> geom.polygons.sumOf { p ->
        p.exterior.size + p.interiors.sumOf { it.size }
    }
}

/**
 * Infer a readable type name from a property value.
 */
private fun inferTypeName(value: Any?): String = when (value) {
    null -> "null"
    is String -> "String"
    is Int -> "Int"
    is Long -> "Long"
    is Double -> "Double"
    is Float -> "Float"
    is Boolean -> "Boolean"
    is Number -> "Number"
    is List<*> -> "List"
    is Map<*, *> -> "Map"
    else -> value::class.simpleName ?: "Any"
}

/**
 * Format bounds for display.
 */
private fun formatBounds(bounds: Bounds): String {
    if (bounds.isEmpty()) return "N/A"
    return "[${bounds.minX.format(2)}, ${bounds.minY.format(2)}] → [${bounds.maxX.format(2)}, ${bounds.maxY.format(2)}]"
}

/**
 * Format memory estimate for display.
 * Uses ~24 bytes per coordinate as a rough estimate.
 */
private fun formatMemory(coordCount: Int): String {
    val bytes = coordCount * 24L + 100L // ~24 bytes per coord + overhead
    return when {
        bytes < 1024 -> "~$bytes B"
        bytes < 1024 * 1024 -> "~${bytes / 1024} KB"
        else -> "~${bytes / (1024 * 1024)} MB"
    }
}

/**
 * Format a Double with the specified number of decimal places.
 */
private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

/**
 * Center a string within a given width.
 */
private fun String.center(width: Int): String = padStart((width + length) / 2).padEnd(width)
