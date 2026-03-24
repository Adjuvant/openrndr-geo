package geo.performance

import geo.core.*
import org.openrndr.math.Vector2
import kotlin.random.Random

/**
 * Utility class that generates synthetic geographic datasets for benchmarking.
 * 
 * Creates reproducible test data at various scales (10k, 50k, 100k, 250k features)
 * with realistic spatial distributions within world bounds.
 * 
 * Uses consistent random seed for reproducible benchmarks across runs.
 */
object SyntheticDataGenerator {
    
    /** Default random seed for reproducibility */
    const val DEFAULT_SEED = 42L
    
    /** World bounds in WGS84: minX, minY, maxX, maxY */
    val WORLD_BOUNDS = Bounds(-180.0, -90.0, 180.0, 90.0)
    
    /**
     * Creates a GeoSource containing the specified number of random Point features.
     * 
     * Points are distributed across the world bounds with some clustering
     * to simulate realistic geographic distributions.
     * 
     * @param count Number of points to generate (e.g., 10_000, 50_000, 100_000, 250_000)
     * @param seed Random seed for reproducibility
     * @return GeoSource containing Point features
     */
    fun createPointDataset(count: Int, seed: Long = DEFAULT_SEED): GeoSource {
        val random = Random(seed)
        // Materialize to list so features can be consumed multiple times
        val features = List(count) {
            Feature(
                geometry = createRandomPoint(random),
                properties = createRandomProperties(random)
            )
        }
        
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = features.asSequence()
        }
    }
    
    /**
     * Creates a GeoSource containing the specified number of LineString features.
     * 
     * Each LineString consists of multiple connected points forming a path.
     * LineStrings vary in length and direction to simulate roads, rivers, etc.
     * 
     * @param count Number of LineStrings to generate
     * @param pointsPerLine Number of points in each LineString (default: 10)
     * @param seed Random seed for reproducibility
     * @return GeoSource containing LineString features
     */
    fun createLineStringDataset(
        count: Int,
        pointsPerLine: Int = 10,
        seed: Long = DEFAULT_SEED
    ): GeoSource {
        val random = Random(seed)
        // Materialize to list so features can be consumed multiple times
        val features = List(count) {
            Feature(
                geometry = createRandomLineString(random, pointsPerLine),
                properties = createRandomProperties(random)
            )
        }
        
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = features.asSequence()
        }
    }
    
    /**
     * Creates a GeoSource containing the specified number of Polygon features.
     * 
     * Each Polygon is a simple convex shape (no holes) with the specified
     * number of vertices. Polygons vary in size to simulate land parcels,
     * lakes, building footprints, etc.
     * 
     * @param count Number of Polygons to generate
     * @param pointsPerPolygon Number of vertices in each Polygon (default: 20)
     * @param seed Random seed for reproducibility
     * @return GeoSource containing Polygon features
     */
    fun createPolygonDataset(
        count: Int,
        pointsPerPolygon: Int = 20,
        seed: Long = DEFAULT_SEED
    ): GeoSource {
        val random = Random(seed)
        // Materialize to list so features can be consumed multiple times
        val features = List(count) {
            Feature(
                geometry = createRandomPolygon(random, pointsPerPolygon),
                properties = createRandomProperties(random)
            )
        }
        
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = features.asSequence()
        }
    }
    
    /**
     * Creates a mixed dataset with various geometry types.
     * 
     * Distribution: 40% Points, 30% LineStrings, 30% Polygons
     * 
     * @param count Total number of features to generate
     * @param seed Random seed for reproducibility
     * @return GeoSource containing mixed geometry types
     */
    fun createMixedDataset(count: Int, seed: Long = DEFAULT_SEED): GeoSource {
        val random = Random(seed)
        val pointCount = (count * 0.4).toInt()
        val lineCount = (count * 0.3).toInt()
        val polygonCount = count - pointCount - lineCount
        
        // Materialize to list so features can be consumed multiple times
        val pointFeatures = List(pointCount) {
            Feature(geometry = createRandomPoint(random), properties = createRandomProperties(random))
        }
        
        val lineFeatures = List(lineCount) {
            Feature(geometry = createRandomLineString(random, 15), properties = createRandomProperties(random))
        }
        
        val polygonFeatures = List(polygonCount) {
            Feature(geometry = createRandomPolygon(random, 20), properties = createRandomProperties(random))
        }
        
        val allFeatures = pointFeatures + lineFeatures + polygonFeatures
        
        return object : GeoSource("EPSG:4326") {
            override val features: Sequence<Feature> = allFeatures.asSequence()
        }
    }
    
    // ============================================================================
    // Internal geometry generation helpers
    // ============================================================================
    
    /**
     * Creates a random Point within world bounds.
     * Uses clustered distribution for more realistic geographic spread.
     */
    private fun createRandomPoint(random: Random): Point {
        // Use clustered distribution: pick a center, then offset within radius
        val clusterCenterX = random.nextDouble(-160.0, 160.0)
        val clusterCenterY = random.nextDouble(-70.0, 70.0)
        val clusterRadius = random.nextDouble(5.0, 30.0)
        
        val angle = random.nextDouble(0.0, 2 * Math.PI)
        val distance = random.nextDouble(0.0, clusterRadius)
        
        val x = clusterCenterX + distance * kotlin.math.cos(angle)
        val y = clusterCenterY + distance * kotlin.math.sin(angle)
        
        // Clamp to world bounds, avoiding poles (±90°) for Mercator compatibility
        return Point(
            x = x.coerceIn(-180.0, 180.0),
            y = y.coerceIn(-85.0, 85.0)
        )
    }
    
    /**
     * Creates a random LineString with connected points.
     */
    private fun createRandomLineString(random: Random, pointCount: Int): LineString {
        val startX = random.nextDouble(-170.0, 170.0)
        val startY = random.nextDouble(-80.0, 80.0)
        
        val points = mutableListOf<Vector2>(Vector2(startX, startY))
        
        repeat(pointCount - 1) {
            val lastPoint = points.last()
            // Random walk with step size 0.5-5.0 degrees
            val stepX = random.nextDouble(-5.0, 5.0)
            val stepY = random.nextDouble(-5.0, 5.0)
            
            // Clamp to world bounds, avoiding poles (±90°) for Mercator compatibility
            val newX = (lastPoint.x + stepX).coerceIn(-180.0, 180.0)
            val newY = (lastPoint.y + stepY).coerceIn(-85.0, 85.0)
            
            points.add(Vector2(newX, newY))
        }
        
        return LineString(points)
    }
    
    /**
     * Creates a random convex Polygon.
     */
    private fun createRandomPolygon(random: Random, pointCount: Int): Polygon {
        val centerX = random.nextDouble(-160.0, 160.0)
        val centerY = random.nextDouble(-70.0, 70.0)
        val maxRadius = random.nextDouble(1.0, 15.0)
        
        // Generate points in a rough circle/ellipse
        val points = (0 until pointCount).map { i ->
            val angle = (i.toDouble() / pointCount) * 2 * Math.PI
            // Vary radius for irregular shapes
            val radius = maxRadius * (0.5 + random.nextDouble(0.0, 0.5))
            
            val x = centerX + radius * kotlin.math.cos(angle)
            val y = centerY + radius * kotlin.math.sin(angle)
            
            Vector2(
                x.coerceIn(-180.0, 180.0),
                y.coerceIn(-85.0, 85.0)
            )
        }
        
        return Polygon(exterior = points, interiors = emptyList())
    }
    
    /**
     * Creates random properties for features to simulate real data.
     */
    private fun createRandomProperties(random: Random): Map<String, Any?> {
        return mapOf(
            "id" to random.nextInt(1, 1_000_000),
            "name" to "Feature_${random.nextInt(1000)}",
            "value" to random.nextDouble(0.0, 1000.0),
            "category" to listOf("A", "B", "C", "D").random(random)
        )
    }
    
    /**
     * Calculates the total coordinate count for a GeoSource.
     * Useful for verifying generated datasets.
     */
    fun countCoordinates(source: GeoSource): Long {
        return source.features.sumOf { feature ->
            when (val geom = feature.geometry) {
                is Point -> 1L
                is LineString -> geom.points.size.toLong()
                is Polygon -> (geom.exterior.size + geom.interiors.sumOf { it.size }).toLong()
                is MultiPoint -> geom.points.size.toLong()
                is MultiLineString -> geom.lineStrings.sumOf { it.points.size }.toLong()
                is MultiPolygon -> geom.polygons.sumOf { poly ->
                    poly.exterior.size + poly.interiors.sumOf { it.size }
                }.toLong()
            }
        }
    }
    
    /**
     * Calculates the bounding box of all features in a GeoSource.
     */
    fun calculateBounds(source: GeoSource): Bounds {
        return source.features.fold(Bounds.empty()) { acc, feature ->
            acc.expandToInclude(feature.boundingBox)
        }
    }
}
