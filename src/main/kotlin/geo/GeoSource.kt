package geo

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
    fun totalBoundingBox(): Bounds {
        return features.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox)
        }
    }

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
            padding = 0.9,
            projection = ProjectionType.MERCATOR
        )
        render(drawer, projection, style)
    }
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
