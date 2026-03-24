package geo.tools

import geo.core.*
import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

/**
 * Configuration for synthetic data generation.
 *
 * Controls the spatial extent, clustering behaviour, and property
 * generation for all geometry types. Sensible defaults are provided
 * for quick exploration; override individual fields to taste.
 *
 * @param bounds Spatial extent for generated geometries (default: world WGS84)
 * @param clusterSpread Controls how tightly features cluster. Lower values
 *        produce denser clusters; higher values scatter more uniformly.
 *        Expressed as a fraction of the bounds extent (default: 0.15)
 * @param irregularity Shape irregularity for polygons, 0.0 = perfect circles,
 *        1.0 = maximum wobble (default: 0.5)
 * @param crs Coordinate reference system string attached to the GeoSource
 * @param properties Property generator, or null to omit properties entirely
 */
data class SyntheticDataConfig(
    val bounds: Bounds = Bounds(-180.0, -85.0, 180.0, 85.0),
    val clusterSpread: Double = 0.15,
    val irregularity: Double = 0.5,
    val crs: String = "EPSG:4326",
    val properties: ((Random, Int) -> Map<String, Any?>)? = ::defaultProperties
)

/**
 * Generates synthetic geographic datasets for visual exploration and prototyping.
 *
 * Unlike a benchmark generator, this is tuned for smaller, visually interesting
 * datasets with configurable randomness. Geometry is not geographically
 * "correct" — the goal is plausible shapes and distributions that look good
 * on screen and exercise rendering pipelines.
 *
 * Usage:
 * ```kotlin
 * // Quick and random every run
 * val points = SyntheticData.points(200)
 *
 * // Reproducible
 * val lines = SyntheticData.lines(50, seed = 99L)
 *
 * // Custom bounds and config
 * val config = SyntheticDataConfig(
 *     bounds = Bounds(0.0, 0.0, 100.0, 100.0),
 *     crs = "EPSG:27700"
 * )
 * val polys = SyntheticData.polygons(30, config = config)
 *
 * // Presets
 * val demo = SyntheticData.Presets.scatterPlot()
 * val tiled = SyntheticData.Presets.grid(cols = 10, rows = 10)
 * ```
 */
object SyntheticData {

    // ========================================================================
    // Public dataset generators
    // ========================================================================

    /**
     * Creates a GeoSource of random Point features.
     *
     * Points use a clustered distribution: each point picks a random
     * cluster centre within the bounds, then offsets by [SyntheticDataConfig.clusterSpread].
     *
     * @param count Number of points (default: 100)
     * @param seed Random seed, or null for non-deterministic
     * @param config Generation parameters
     */
    fun points(
        count: Int = 100,
        seed: Long? = null,
        config: SyntheticDataConfig = SyntheticDataConfig()
    ): GeoSource {
        val random = seed?.let { Random(it) } ?: Random.Default
        val features = List(count) { index ->
            Feature(
                geometry = randomPoint(random, config),
                properties = config.properties?.invoke(random, index) ?: emptyMap()
            )
        }
        return geoSource(features, config.crs)
    }

    /**
     * Creates a GeoSource of random LineString features.
     *
     * Each line is a random walk from a start point, with step size
     * proportional to the bounds extent.
     *
     * @param count Number of lines (default: 50)
     * @param verticesPerLine Vertices in each line (default: 8)
     * @param seed Random seed, or null for non-deterministic
     * @param config Generation parameters
     */
    fun lines(
        count: Int = 50,
        verticesPerLine: Int = 8,
        seed: Long? = null,
        config: SyntheticDataConfig = SyntheticDataConfig()
    ): GeoSource {
        val random = seed?.let { Random(it) } ?: Random.Default
        val features = List(count) { index ->
            Feature(
                geometry = randomLineString(random, verticesPerLine, config),
                properties = config.properties?.invoke(random, index) ?: emptyMap()
            )
        }
        return geoSource(features, config.crs)
    }

    /**
     * Creates a GeoSource of random Polygon features.
     *
     * Polygons are generated as irregular star-like shapes around a centre,
     * controlled by [SyntheticDataConfig.irregularity].
     *
     * @param count Number of polygons (default: 30)
     * @param verticesPerPolygon Vertices per polygon (default: 12)
     * @param seed Random seed, or null for non-deterministic
     * @param config Generation parameters
     */
    fun polygons(
        count: Int = 30,
        verticesPerPolygon: Int = 12,
        seed: Long? = null,
        config: SyntheticDataConfig = SyntheticDataConfig()
    ): GeoSource {
        val random = seed?.let { Random(it) } ?: Random.Default
        val features = List(count) { index ->
            Feature(
                geometry = randomPolygon(random, verticesPerPolygon, config),
                properties = config.properties?.invoke(random, index) ?: emptyMap()
            )
        }
        return geoSource(features, config.crs)
    }

    /**
     * Creates a mixed GeoSource with configurable geometry distribution.
     *
     * @param count Total feature count (default: 100)
     * @param pointRatio Fraction of points (default: 0.4)
     * @param lineRatio Fraction of lines (default: 0.3)
     * @param seed Random seed, or null for non-deterministic
     * @param config Generation parameters
     */
    fun mixed(
        count: Int = 100,
        pointRatio: Double = 0.4,
        lineRatio: Double = 0.3,
        seed: Long? = null,
        config: SyntheticDataConfig = SyntheticDataConfig()
    ): GeoSource {
        val random = seed?.let { Random(it) } ?: Random.Default
        val nPoints = (count * pointRatio).toInt()
        val nLines = (count * lineRatio).toInt()
        val nPolygons = count - nPoints - nLines

        val features = buildList {
            repeat(nPoints) { i ->
                add(Feature(randomPoint(random, config), config.properties?.invoke(random, i) ?: emptyMap()))
            }
            repeat(nLines) { i ->
                add(Feature(randomLineString(random, 8, config), config.properties?.invoke(random, nPoints + i) ?: emptyMap()))
            }
            repeat(nPolygons) { i ->
                add(Feature(randomPolygon(random, 12, config), config.properties?.invoke(random, nPoints + nLines + i) ?: emptyMap()))
            }
        }

        return geoSource(features.shuffled(random), config.crs)
    }

    // ========================================================================
    // Presets — one-call datasets for common visual scenarios
    // ========================================================================

    object Presets {

        /**
         * A scatter of points — good for testing colour maps,
         * symbol sizing, and density rendering.
         */
        fun scatterPlot(
            count: Int = 200,
            seed: Long? = null,
            config: SyntheticDataConfig = SyntheticDataConfig()
        ): GeoSource = points(count, seed, config)

        /**
         * A regular grid of point features. Useful for testing
         * heatmaps, tiling, and uniform distributions.
         *
         * @param cols Grid columns
         * @param rows Grid rows
         * @param config Bounds and CRS are taken from config
         */
        fun grid(
            cols: Int = 10,
            rows: Int = 10,
            seed: Long? = null,
            config: SyntheticDataConfig = SyntheticDataConfig()
        ): GeoSource {
            val random = seed?.let { Random(it) } ?: Random.Default
            val b = config.bounds
            val dx = (b.maxX - b.minX) / (cols + 1)
            val dy = (b.maxY - b.minY) / (rows + 1)

            var index = 0
            val features = buildList {
                for (col in 1..cols) {
                    for (row in 1..rows) {
                        add(
                            Feature(
                                geometry = Point(b.minX + col * dx, b.minY + row * dy),
                                properties = config.properties?.invoke(random, index++) ?: emptyMap()
                            )
                        )
                    }
                }
            }
            return geoSource(features, config.crs)
        }

        /**
         * Concentric ring polygons around a centre — useful for testing
         * layered fills, stroke styles, and transparency.
         *
         * @param rings Number of concentric rings
         * @param verticesPerRing Vertices in each ring polygon
         * @param center Centre point (defaults to bounds centre)
         */
        fun concentricRings(
            rings: Int = 5,
            verticesPerRing: Int = 36,
            center: Vector2? = null,
            seed: Long? = null,
            config: SyntheticDataConfig = SyntheticDataConfig()
        ): GeoSource {
            val random = seed?.let { Random(it) } ?: Random.Default
            val b = config.bounds
            val cx = center?.x ?: ((b.minX + b.maxX) / 2.0)
            val cy = center?.y ?: ((b.minY + b.maxY) / 2.0)
            val maxR = minOf(b.maxX - b.minX, b.maxY - b.minY) / 4.0

            val features = List(rings) { ring ->
                val radius = maxR * (ring + 1).toDouble() / rings
                val pts = (0 until verticesPerRing).map { i ->
                    val a = (i.toDouble() / verticesPerRing) * 2 * PI
                    Vector2(
                        (cx + radius * cos(a)).coerceIn(b.minX, b.maxX),
                        (cy + radius * sin(a)).coerceIn(b.minY, b.maxY)
                    )
                }
                Feature(
                    geometry = Polygon(exterior = pts, interiors = emptyList()),
                    properties = config.properties?.invoke(random, ring) ?: emptyMap()
                )
            }
            return geoSource(features, config.crs)
        }

        /**
         * A set of radiating lines from a centre point — useful for
         * testing stroke rendering, dash patterns, and line colour maps.
         */
        fun radialBurst(
            rays: Int = 24,
            verticesPerRay: Int = 6,
            center: Vector2? = null,
            seed: Long? = null,
            config: SyntheticDataConfig = SyntheticDataConfig()
        ): GeoSource {
            val random = seed?.let { Random(it) } ?: Random.Default
            val b = config.bounds
            val cx = center?.x ?: ((b.minX + b.maxX) / 2.0)
            val cy = center?.y ?: ((b.minY + b.maxY) / 2.0)
            val reach = minOf(b.maxX - b.minX, b.maxY - b.minY) / 3.0

            val features = List(rays) { i ->
                val baseAngle = (i.toDouble() / rays) * 2 * PI
                val pts = (0 until verticesPerRay).map { v ->
                    val t = v.toDouble() / (verticesPerRay - 1)
                    val wobble = random.nextDouble(-0.05, 0.05) * reach
                    Vector2(
                        (cx + (reach * t + wobble) * cos(baseAngle)).coerceIn(b.minX, b.maxX),
                        (cy + (reach * t + wobble) * sin(baseAngle)).coerceIn(b.minY, b.maxY)
                    )
                }
                Feature(
                    geometry = LineString(pts),
                    properties = config.properties?.invoke(random, i) ?: emptyMap()
                )
            }
            return geoSource(features, config.crs)
        }
    }

    // ========================================================================
    // Geometry helpers (internal)
    // ========================================================================

    private fun randomPoint(random: Random, config: SyntheticDataConfig): Point {
        val b = config.bounds
        val extentX = b.maxX - b.minX
        val extentY = b.maxY - b.minY
        val spreadX = extentX * config.clusterSpread
        val spreadY = extentY * config.clusterSpread

        val cx = random.nextDouble(b.minX + spreadX, b.maxX - spreadX)
        val cy = random.nextDouble(b.minY + spreadY, b.maxY - spreadY)

        val angle = random.nextDouble(0.0, 2 * PI)
        val dist = random.nextDouble(0.0, 1.0) * spreadX // circle in X-proportion

        return Point(
            x = (cx + dist * cos(angle)).coerceIn(b.minX, b.maxX),
            y = (cy + dist * sin(angle) * (spreadY / spreadX)).coerceIn(b.minY, b.maxY)
        )
    }

    private fun randomLineString(random: Random, pointCount: Int, config: SyntheticDataConfig): LineString {
        val b = config.bounds
        val stepScale = (b.maxX - b.minX) * 0.02 // ~2% of extent per step

        val startX = random.nextDouble(b.minX * 0.9, b.maxX * 0.9)
        val startY = random.nextDouble(b.minY * 0.9, b.maxY * 0.9)
        val pts = mutableListOf(Vector2(startX, startY))

        repeat(pointCount - 1) {
            val last = pts.last()
            val nx = (last.x + random.nextDouble(-stepScale, stepScale)).coerceIn(b.minX, b.maxX)
            val ny = (last.y + random.nextDouble(-stepScale, stepScale)).coerceIn(b.minY, b.maxY)
            pts.add(Vector2(nx, ny))
        }
        return LineString(pts)
    }

    private fun randomPolygon(random: Random, pointCount: Int, config: SyntheticDataConfig): Polygon {
        val b = config.bounds
        val extentMin = minOf(b.maxX - b.minX, b.maxY - b.minY)
        val maxRadius = extentMin * random.nextDouble(0.01, 0.06)

        val cx = random.nextDouble(b.minX + maxRadius, b.maxX - maxRadius)
        val cy = random.nextDouble(b.minY + maxRadius, b.maxY - maxRadius)

        val pts = (0 until pointCount).map { i ->
            val a = (i.toDouble() / pointCount) * 2 * PI
            val radiusVar = 1.0 - config.irregularity * 0.5 + random.nextDouble(0.0, config.irregularity * 0.5)
            val r = maxRadius * radiusVar
            Vector2(
                (cx + r * cos(a)).coerceIn(b.minX, b.maxX),
                (cy + r * sin(a)).coerceIn(b.minY, b.maxY)
            )
        }
        return Polygon(exterior = pts, interiors = emptyList())
    }

    // ========================================================================
    // Property generation
    // ========================================================================

    private fun geoSource(features: List<Feature>, crs: String): GeoSource {
        return object : GeoSource(crs) {
            override val features: Sequence<Feature> = features.asSequence()
        }
    }
}

/**
 * Default property generator. Produces visually useful attributes:
 * - `id`: sequential index
 * - `label`: human-readable label
 * - `value`: continuous 0..1 (useful for colour mapping)
 * - `magnitude`: continuous 0..100 (useful for sizing)
 * - `category`: one of several named groups (useful for categorical palettes)
 * - `flag`: boolean (useful for binary filtering/styling)
 */
fun defaultProperties(random: Random, index: Int): Map<String, Any?> {
    val categories = listOf("alpha", "beta", "gamma", "delta", "epsilon")
    return mapOf(
        "id" to index,
        "label" to "feature-$index",
        "value" to random.nextDouble(),            // 0..1 normalised
        "magnitude" to random.nextDouble(0.0, 100.0),
        "category" to categories.random(random),
        "flag" to random.nextBoolean()
    )
}