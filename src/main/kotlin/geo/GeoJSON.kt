package geo

import geo.internal.OptimizedFeature
import geo.internal.OptimizedGeoSource
import geo.internal.checkOptimizationRecommendation
import geo.internal.geometry.OptimizedLineString
import geo.internal.geometry.OptimizedMultiLineString
import geo.internal.geometry.OptimizedMultiPoint
import geo.internal.geometry.OptimizedMultiPolygon
import geo.internal.geometry.OptimizedPoint
import geo.internal.geometry.OptimizedPolygon
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.openrndr.math.Vector2
import java.io.File
import java.io.FileNotFoundException

/**
 * Object providing GeoJSON file loading and parsing capabilities.
 * Converts GeoJSON format files into the unified Feature/Geometry data model.
 * Based on RFC 7946 (2016)
 */
object GeoJSON {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Internal representation of a GeoJSON Feature.
     */
    @Serializable
    private data class GeoJSONFeature(
        val type: String,
        val geometry: JsonElement,
        @SerialName("properties")
        val properties: JsonElement? = null
    )

    /**
     * Internal representation of a GeoJSON FeatureCollection.
     */
    @Serializable
    private data class GeoJSONFeatureCollection(
        val type: String,
        val features: List<GeoJSONFeature> = emptyList(),
        val bbox: List<Double>? = null
    )

    /**
     * Loads a GeoJSON file from the given path and returns a GeoSource.
     *
     * @param path The file path to the GeoJSON file
     * @param optimize Whether to enable batch projection optimization (default: false)
     * @return A GeoSource containing the parsed features (GeoJSONSource or OptimizedGeoSource)
     * @throws FileNotFoundException if the file doesn't exist
     * @throws IllegalArgumentException if the JSON is not a valid FeatureCollection
     */
    fun load(path: String, optimize: Boolean = false): GeoSource {
        val file = File(path)
        if (!file.exists()) {
            throw FileNotFoundException("GeoJSON file not found: $path")
        }

        val content = file.readText()
        return loadString(content, optimize)
    }

    /**
     * Parses a GeoJSON string and returns a GeoSource.
     *
     * @param content The GeoJSON string content
     * @param optimize Whether to enable batch projection optimization (default: false)
     * @return A GeoSource containing the parsed features (GeoJSONSource or OptimizedGeoSource)
     * @throws IllegalArgumentException if the JSON is not a valid FeatureCollection
     */
    fun loadString(content: String, optimize: Boolean = false): GeoSource {
        val root = json.parseToJsonElement(content)
        val rootObject = root.jsonObject
        val type = rootObject["type"]?.jsonPrimitive?.content

        val (features, bbox) = when (type) {
            "FeatureCollection" -> {
                val collection = json.decodeFromString(GeoJSONFeatureCollection.serializer(), content)
                Pair(
                    collection.features.mapNotNull { parseFeature(it) },
                    collection.bbox?.let { b ->
                        if (b.size >= 4) Bounds(b[0], b[1], b[2], b[3]) else null
                    }
                )
            }
            "Feature" -> {
                val feature = json.decodeFromString(GeoJSONFeature.serializer(), content)
                Pair(listOfNotNull(parseFeature(feature)), null)
            }
            else -> throw IllegalArgumentException("Expected FeatureCollection or Feature, got: ${type ?: "unknown"}")
        }

        // Count coordinates and check optimization recommendation
        val featureList = features.toList()
        val coordinateCount = featureList.sumOf { countCoordinates(it.geometry) }
        checkOptimizationRecommendation(
            featureCount = featureList.size,
            coordinateCount = coordinateCount,
            optimizeFlag = optimize
        )

        // Apply optimization if requested
        if (optimize) {
            val optimizedFeatures = featureList.map { feature ->
                OptimizedFeature(
                    optimizedGeometry = feature.geometry.toOptimized(),
                    properties = feature.properties
                )
            }
            return OptimizedGeoSource(optimizedFeatures.asSequence(), crs = "EPSG:4326")
        }

        return GeoJSONSource(featureList.asSequence(), bbox = bbox)
    }

    /**
     * Counts the total number of coordinates in a geometry.
     * Used for optimization warning threshold checking.
     */
    private fun countCoordinates(geometry: Geometry): Int = when (geometry) {
        is Point -> 1
        is LineString -> geometry.points.size
        is Polygon -> geometry.exterior.size + geometry.interiors.sumOf { it.size }
        is MultiPoint -> geometry.points.size
        is MultiLineString -> geometry.lineStrings.sumOf { it.points.size }
        is MultiPolygon -> geometry.polygons.sumOf { poly ->
            poly.exterior.size + poly.interiors.sumOf { it.size }
        }
    }

    /**
     * Convenience function to load a GeoJSON file and return features directly.
     *
     * This is a wrapper around [load] that simplifies the common case where you just need
     * the features without requiring access to the GeoJSONSource object's additional functionality
     * such as CRS tracking or spatial queries.
     *
     * @param path The file path to the GeoJSON file
     * @return A [Sequence] of [Feature] from the GeoJSON file
     * @throws FileNotFoundException if the file doesn't exist
     * @throws IllegalArgumentException if the JSON is not a valid FeatureCollection or Feature
     */
    fun features(path: String): Sequence<Feature> = load(path).features

    /**
     * Convenience function to parse a GeoJSON string and return features directly.
     *
     * This is a wrapper around [loadString] that simplifies the common case where you just need
     * the features without requiring access to the GeoJSONSource object's additional functionality
     * such as CRS tracking or spatial queries.
     *
     * @param content The GeoJSON string content
     * @return A [Sequence] of [Feature] from the GeoJSON content
     * @throws IllegalArgumentException if the JSON is not a valid FeatureCollection or Feature
     */
    fun featuresString(content: String): Sequence<Feature> = loadString(content).features

    /**
     * Parses a GeoJSON feature into a Feature object.
     */
    private fun parseFeature(geoJsonFeature: GeoJSONFeature): Feature? {
        return try {
            val geometry = parseGeometry(geoJsonFeature.geometry)
            val properties = geoJsonFeature.properties?.let { parseProperties(it) } ?: emptyMap()
            Feature(geometry = geometry, properties = properties)
        } catch (e: Exception) {
            println("Warning: Failed to parse feature: ${e.message}")
            null
        }
    }

    /**
     * Parses properties from JSON element.
     */
    private fun parseProperties(properties: JsonElement): Map<String, Any?> {
        if (properties !is kotlinx.serialization.json.JsonObject) return emptyMap()

        return properties.mapValues { (_, value) ->
            when {
                value is kotlinx.serialization.json.JsonPrimitive -> {
                    when {
                        value.isString -> value.content
                        value.content.toDoubleOrNull() != null -> value.content.toDouble()
                        value.content == "true" || value.content == "false" -> value.content.toBoolean()
                        else -> value.content
                    }
                }
                else -> null
            }
        }
    }

    /**
     * Parses a geometry JSON element into a Geometry object.
     */
    private fun parseGeometry(geometry: JsonElement): Geometry {
        val geomObject = geometry.jsonObject
        val type = geomObject["type"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Missing 'type' in geometry")

        val coords = geomObject["coordinates"]
            ?: throw IllegalArgumentException("Missing 'coordinates' in geometry")

        return when (type) {
            "Point" -> parsePoint(coords)
            "LineString" -> parseLineString(coords)
            "Polygon" -> parsePolygon(coords)
            "MultiPoint" -> parseMultiPoint(coords)
            "MultiLineString" -> parseMultiLineString(coords)
            "MultiPolygon" -> parseMultiPolygon(coords)
            else -> throw IllegalArgumentException("Unknown geometry type: $type")
        }
    }

    /**
     * Parses a Point geometry.
     */
    private fun parsePoint(coords: JsonElement): Point {
        val coordArray = coords.jsonArray
        if (coordArray.size < 2) {
            throw IllegalArgumentException("Point must have at least 2 coordinates, got ${coordArray.size}")
        }
        val x = coordArray[0].jsonPrimitive.content.toDouble()
        val y = coordArray[1].jsonPrimitive.content.toDouble()
        return Point(x, y)
    }

    /**
     * Parses a LineString geometry.
     */
    private fun parseLineString(coords: JsonElement): LineString {
        val coordArray = coords.jsonArray
        if (coordArray.size < 2) {
            throw IllegalArgumentException("LineString must have at least 2 points")
        }
        val points = coordArray.mapNotNull { coord ->
            val arr = coord.jsonArray
            if (arr.size >= 2) {
                Vector2(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
            } else null
        }
        return LineString(points)
    }

    /**
     * Parses a Polygon geometry.
     */
    private fun parsePolygon(coords: JsonElement): Polygon {
        val rings = coords.jsonArray
        if (rings.isEmpty()) {
            throw IllegalArgumentException("Polygon must have at least 1 ring")
        }
        val exterior = rings[0].jsonArray.mapNotNull { coord ->
            val arr = coord.jsonArray
            if (arr.size >= 2) {
                Vector2(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
            } else null
        }
        val interiors = rings.drop(1).map { ring ->
            ring.jsonArray.mapNotNull { coord ->
                val arr = coord.jsonArray
                if (arr.size >= 2) {
                    Vector2(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
                } else null
            }
        }
        return Polygon(exterior, interiors)
    }

    /**
     * Parses a MultiPoint geometry.
     */
    private fun parseMultiPoint(coords: JsonElement): MultiPoint {
        val coordArray = coords.jsonArray
        val points = coordArray.mapNotNull { coord ->
            val arr = coord.jsonArray
            if (arr.size >= 2) {
                Point(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
            } else null
        }
        return MultiPoint(points)
    }

    /**
     * Parses a MultiLineString geometry.
     */
    private fun parseMultiLineString(coords: JsonElement): MultiLineString {
        val lines = coords.jsonArray
        val lineStrings = lines.mapNotNull { line ->
            val arr = line.jsonArray
            if (arr.size >= 2) {
                val points = arr.mapNotNull { coord ->
                    val c = coord.jsonArray
                    if (c.size >= 2) {
                        Vector2(c[0].jsonPrimitive.content.toDouble(), c[1].jsonPrimitive.content.toDouble())
                    } else null
                }
                LineString(points)
            } else null
        }
        return MultiLineString(lineStrings)
    }

    /**
     * Parses a MultiPolygon geometry.
     */
    private fun parseMultiPolygon(coords: JsonElement): MultiPolygon {
        val polygons = coords.jsonArray
        val polygonList = polygons.mapNotNull { poly ->
            val rings = poly.jsonArray
            if (rings.isNotEmpty()) {
                val exterior = rings[0].jsonArray.mapNotNull { coord ->
                    val arr = coord.jsonArray
                    if (arr.size >= 2) {
                        Vector2(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
                    } else null
                }
                val interiors = rings.drop(1).map { ring ->
                    ring.jsonArray.mapNotNull { coord ->
                        val arr = coord.jsonArray
                        if (arr.size >= 2) {
                            Vector2(arr[0].jsonPrimitive.content.toDouble(), arr[1].jsonPrimitive.content.toDouble())
                        } else null
                    }
                }
                Polygon(exterior, interiors)
            } else null
        }
        return MultiPolygon(polygonList)
    }
}

/**
 * A GeoSource implementation for GeoJSON data.
 *
 * GeoJSON uses WGS84 per RFC 7946 (no CRS property in modern GeoJSON).
 *
 * @property features The sequence of features from the GeoJSON file
 * @param crs The Coordinate Reference System identifier (default: "EPSG:4326" for WGS84)
 */
class GeoJSONSource(
    override val features: Sequence<Feature>,
    crs: String = "EPSG:4326",
    private val bbox: Bounds? = null
) : GeoSource(crs) {

    override fun boundingBox(): Bounds = bbox ?: totalBoundingBox()
    companion object {
        /**
         * Loads a GeoJSON file from the given path.
         *
         * @param path The file path to the GeoJSON file
         * @return A GeoSource containing the parsed features
         */
        @JvmStatic
        fun load(path: String): GeoSource = GeoJSON.load(path)
    }
}
