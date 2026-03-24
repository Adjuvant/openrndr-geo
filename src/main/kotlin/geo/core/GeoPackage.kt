package geo.core

import mil.nga.geopackage.GeoPackageManager
import mil.nga.geopackage.features.user.FeatureDao
import mil.nga.geopackage.features.user.FeatureRow
import org.openrndr.math.Vector2
import java.io.File
import java.io.FileNotFoundException

/**
 * Provides methods for loading GeoPackage files.
 * GeoPackage is an open, standards-based format for geospatial data
 * (OGC GeoPackage Encoding Standard).
 */
object GeoPackage {

    /**
     * Loads a GeoPackage file and returns a GeoPackageSource with spatial indexing.
     *
     * @param path The path to the GeoPackage file
     * @param maxFeatures Maximum number of features to load (prevents OOM on huge files)
     * @return A GeoPackageSource containing the loaded features with spatial index
     * @throws FileNotFoundException if the file doesn't exist
     * @throws IllegalArgumentException if the GeoPackage has no feature layers
     */
    fun load(path: String, maxFeatures: Int = Int.MAX_VALUE): GeoPackageSource {
        val file = File(path)
        if (!file.exists()) {
            throw FileNotFoundException("GeoPackage file not found: $path")
        }

        val gpkg = GeoPackageManager.open(file)
        gpkg.use { geoPackage ->
            val featureTables = geoPackage.featureTables

            if (featureTables.isEmpty()) {
                throw IllegalArgumentException("GeoPackage has no feature layers: $path")
            }

            // Load features from all layers
            val allFeatures = mutableListOf<Feature>()
            var count = 0

            for (tableName in featureTables) {
                if (count >= maxFeatures) break

                val dao = geoPackage.getFeatureDao(tableName)
                val resultSet = dao.queryForAll()
                try {
                    while (resultSet.moveToNext()) {
                        if (count >= maxFeatures) break

                        try {
                            val featureRow = resultSet.getRow()
                            val geometry = parseGeometryFromGpkg(featureRow, dao)
                            val properties = parsePropertiesFromGpkg(featureRow, dao)
                            allFeatures.add(Feature(geometry, properties))
                            count++
                        } catch (e: Exception) {
                            println("Warning: Failed to parse GeoPackage feature: ${e.message}")
                        }
                    }
                } finally {
                    resultSet.close()
                }
            }

            // Build spatial index for efficient queries
            val overallBounds = allFeatures.fold(Bounds.empty()) { acc, feature ->
                val bbox = feature.geometry.boundingBox
                if (!bbox.isEmpty()) {
                    acc.expandToInclude(bbox)
                } else {
                    acc
                }
            }

            // Create quadtree with bounds covering all features (or a default if empty)
            val quadtreeBounds = if (overallBounds.isEmpty()) {
                Bounds(-180.0, -90.0, 180.0, 90.0) // Default world bounds
            } else {
                overallBounds
            }

            val quadtree = Quadtree(quadtreeBounds)

            for (feature in allFeatures) {
                quadtree.insert(feature)
            }

            // Determine CRS from first feature table (WGS84 default if unspecified)
            val crs = determineCRS(geoPackage, featureTables)

            return GeoPackageSource(
                allFeatures,
                quadtree,
                crs
            )
        }
    }

    /**
     * Convenience function to load a GeoPackage file and return features directly.
     *
     * This is a wrapper around [load] that simplifies the common case where you just need
     * the features without requiring access to the GeoPackageSource object's additional
     * functionality such as spatial indexing and queryByBounds().
     *
     * Note: When using this convenience function, spatial indexing is not available.
     * Use [load] directly if you need [GeoPackageSource.queryByBounds] functionality.
     *
     * @param path The path to the GeoPackage file
     * @param maxFeatures Maximum number of features to load (prevents OOM on huge files)
     * @return A [Sequence] of [Feature] from the GeoPackage file
     * @throws FileNotFoundException if the file doesn't exist
     * @throws IllegalArgumentException if the GeoPackage has no feature layers
     */
    fun features(path: String, maxFeatures: Int = Int.MAX_VALUE): Sequence<Feature> = load(path, maxFeatures).features

    /**
     * Determines the CRS from the first feature table, defaulting to WGS84.
     */
    private fun determineCRS(
        geoPackage: mil.nga.geopackage.GeoPackage,
        featureTables: List<String>
    ): String {
        return try {
            if (featureTables.isNotEmpty()) {
                val dao = geoPackage.getFeatureDao(featureTables[0])
                val srs = dao.getSrs()
                "EPSG:${srs.getSrsId()}"
            } else {
                "EPSG:4326"
            }
        } catch (e: Exception) {
            "EPSG:4326" // WGS84 default
        }
    }

    /**
     * Parses a geometry from a GeoPackage feature row.
     */
    private fun parseGeometryFromGpkg(
        row: FeatureRow,
        dao: FeatureDao
    ): Geometry {
        val geomData = row.geometry
            ?: throw IllegalArgumentException("Feature has no geometry")

        val geom = geomData.geometry

        return when (geom) {
            is mil.nga.sf.Point -> parsePointFromGpkg(geom)
            is mil.nga.sf.LineString -> parseLineStringFromGpkg(geom)
            is mil.nga.sf.Polygon -> parsePolygonFromGpkg(geom)
            is mil.nga.sf.MultiPoint -> parseMultiPointFromGpkg(geom)
            is mil.nga.sf.MultiLineString -> parseMultiLineStringFromGpkg(geom)
            is mil.nga.sf.MultiPolygon -> parseMultiPolygonFromGpkg(geom)
            else -> throw IllegalArgumentException("Unknown geometry type: ${geom::class.simpleName}")
        }
    }

    /**
     * Parses a Point geometry from GeoPackage format.
     */
    private fun parsePointFromGpkg(geom: mil.nga.sf.Point): Point {
        return Point(geom.x, geom.y)
    }

    /**
     * Parses a LineString geometry from GeoPackage format.
     */
    private fun parseLineStringFromGpkg(geom: mil.nga.sf.LineString): LineString {
        val points = geom.points.map { Vector2(it.x, it.y) }
        return LineString(points)
    }

    /**
     * Parses a Polygon geometry from GeoPackage format.
     */
    private fun parsePolygonFromGpkg(geom: mil.nga.sf.Polygon): Polygon {
        val rings = geom.rings
        val exterior = rings[0].points.map { Vector2(it.x, it.y) }
        val interiors = rings.drop(1).map { ring ->
            ring.points.map { Vector2(it.x, it.y) }
        }
        return Polygon(exterior, interiors)
    }

    /**
     * Parses a MultiPoint geometry from GeoPackage format.
     */
    private fun parseMultiPointFromGpkg(geom: mil.nga.sf.MultiPoint): MultiPoint {
        val points = geom.points.map { Point(it.x, it.y) }
        return MultiPoint(points)
    }

    /**
     * Parses a MultiLineString geometry from GeoPackage format.
     */
    private fun parseMultiLineStringFromGpkg(geom: mil.nga.sf.MultiLineString): MultiLineString {
        val lineStrings = geom.lineStrings.map { ls ->
            val points = ls.points.map { Vector2(it.x, it.y) }
            LineString(points)
        }
        return MultiLineString(lineStrings)
    }

    /**
     * Parses a MultiPolygon geometry from GeoPackage format.
     */
    private fun parseMultiPolygonFromGpkg(geom: mil.nga.sf.MultiPolygon): MultiPolygon {
        val polygons = geom.polygons.map { poly ->
            val exterior = poly.rings[0].points.map { Vector2(it.x, it.y) }
            val interiors = poly.rings.drop(1).map { ring ->
                ring.points.map { Vector2(it.x, it.y) }
            }
            Polygon(exterior, interiors)
        }
        return MultiPolygon(polygons)
    }

    /**
     * Parses properties from a GeoPackage feature row.
     */
    private fun parsePropertiesFromGpkg(
        row: FeatureRow,
        dao: FeatureDao
    ): Map<String, Any?> {
        val properties = mutableMapOf<String, Any?>()

        for (column in dao.table.columns) {
            if (column.name != "geometry" && column.name != "_feature_id") {
                val value = row.getValue(column.name)
                properties[column.name] = value
            }
        }

        return properties
    }
}

/**
 * A GeoSource implementation for GeoPackage data.
 * Provides efficient spatial querying via quadtree index.
 *
 * @property featureList The list of all features loaded from the GeoPackage
 * @property quadtree The spatial index for efficient bounding box queries
 * @param crs The Coordinate Reference System identifier
 */
class GeoPackageSource(
    private val featureList: List<Feature>,
    private val quadtree: Quadtree,
    crs: String = "EPSG:4326"
) : GeoSource(crs) {

    /**
     * All features as a lazy Sequence.
     */
    override val features: Sequence<Feature> = featureList.asSequence()

    /**
     * Returns features that fall within the given bounding box.
     * Uses the quadtree index for efficient O(log n) queries.
     *
     * @param bounds The bounding box to query
     * @return List of features whose bounding boxes intersect the given bounds
     */
    fun queryByBounds(bounds: Bounds): List<Feature> {
        return quadtree.query(bounds)
    }

    /**
     * Returns the total count of features.
     * This is efficient as it uses the cached list size.
     */
    override fun countFeatures(): Long = featureList.size.toLong()

    companion object {
        /**
         * Loads a GeoPackage file and returns a GeoPackageSource.
         *
         * @param path The path to the GeoPackage file
         * @param maxFeatures Maximum number of features to load
         * @return A GeoPackageSource with spatial indexing
         */
        @JvmStatic
        fun load(path: String, maxFeatures: Int = Int.MAX_VALUE): GeoPackageSource =
            GeoPackage.load(path, maxFeatures)
    }
}
