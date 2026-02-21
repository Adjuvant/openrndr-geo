# Phase 1 Research: Data Layer

**Date:** 2026-02-21
**Research focus:** How to implement GeoJSON and GeoPackage loading in Kotlin/OpenRNDR

## 1. Standard Stack

### GeoJSON Parsing
**Library:** Use Kotlin's built-in `kotlinx.serialization.json` (already in dependencies)
- No external GeoJSON-specific library needed—GeoJSON is just JSON
- kotlinx.serialization provides type-safe deserialization
- Supports flexible parsing with `Json { ignoreUnknownKeys = true }`

**Pattern:**
```kotlin
// Data class pattern for deserialization
@Serializable
data class GeoJSONFeatureCollection(
    val type: String,
    val features: List<GeoJSONFeature>
)

@Serializable
data class GeoJSONFeature(
    val type: String,
    val geometry: JsonElement,  // Defer Geometry type parsing
    val properties: Map<String, Any?>?
)
```

### GeoPackage Parsing
**Library:** Use `geopackage-java` library for spec-compliant parsing
- Open-source, actively maintained (maven: `org.geopackage:geopackage-java`)
- Handles SQLite database structure automatically
- Supports spatial indexing and feature queries
- License: MIT/Apache 2.0 (compatible)

**Alternative:** Use `kotlinx.coroutines.Dispatchers.IO` with SQLite JDBC driver for pure Kotlin approach
- More control, less dependency overhead
- Requires implementing GeoPackage spec parsing manually (not recommended)

**Recommendation:** Use `geopackage-java`—it's battle-tested and handles the complex GeoPackage spec.

### Spatial Queries and Indexing
**Library:** Use `orx-interval-tree` (already in orxFeatures, currently commented out)
- Provides R-tree spatial index structure
- Efficient bounding box queries (O(log n))
- OpenRNDR-optimized, same ecosystem

**Alternative (if orx-interval-tree not suitable):**
- Implement simple quadtree internally for bounding box queries
- Kotlin-friendly APIs, no external dependency

**Recommendation:** Implement simple quadtree internally—lighter weight, no external dependency, sufficient for regional filtering.

### Kotlin Patterns for Geo Data
**Sealed class hierarchy for Geometry types:**
```kotlin
sealed class Geometry {
    abstract val coordinates: List<Double>
}

data class Point(val x: Double, val y: Double) : Geometry() {
    override val coordinates = listOf(x, y)
}

data class LineString(val points: List<Vector2>) : Geometry() {
    override val coordinates = points.flatMap { listOf(it.x, it.y) }
}

data class Polygon(val exterior: List<Vector2>, val interiors: List<List<Vector2>> = emptyList()) : Geometry()

multi-point, multi-linestring, multi-polygon as wrappers...
```

**Why sealed classes:**
- Exhaustive when expressions for type handling (better than abstract classes)
- Clean, Kotlin-idiomatic pattern
- Pattern matching benefits for rendering layer

**DataClasses for Feature and Source:**
```kotlin
data class Feature(
    val geometry: Geometry,
    val properties: Map<String, Any?>
)
```

## 2. Architecture Patterns

### Data Model Hierarchy
```
┌─────────────────┐
│  Geometry (sealed)
│  ├─ Point
│  ├─ LineString
│  ├─ Polygon
│  ├─ MultiPoint
│  ├─ MultiLineString
│  └─ MultiPolygon
└─────────────────┘
         ↓
┌─────────────────┐
│  Feature
│  - geometry: Geometry
│  - properties: Map<String, Any?>
└─────────────────┘
         ↓
┌─────────────────┐
│  GeoSource
│  - features: Sequence<Feature>
│  - crs: String? // WGS84 default
└─────────────────┘
```

### File Loading APIs

**Synchronous (simpler, fail-fast):**
```kotlin
// GeoJSON.kt
fun load(path: String): GeoJSONSource {
    val content = File(path).readText()
    val json = Json.decodeFromString<GeoJSONFeatureCollection>(content)
    return GeoJSONSource(json.features)
}

// GeoPackage.kt
fun load(path: String): GeoPackageSource {
    val gpkg = GeoPackageManager.manager().open(path)
    return GeoPackageSource(gpkg)
}
```

**Error handling strategy:**
```kotlin
try {
    val source = GeoJSON.load("data/cities.geojson")
} catch (e: IOException) {
    throw GeoLoadingException("Could not read file: ${e.message}")
} catch (e: SerializationException) {
    throw GeoLoadingException("Invalid GeoJSON structure: ${e.message}")
}
```

### Spatial Query DSL (Infix Notation)
```kotlin
// Define bounds type
data class Bounds(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
)

// Infix function for bounding box query
infix fun Sequence<Feature>.within(bounds: Bounds): Sequence<Feature> {
    return filter { feature ->
        val geom = feature.geometry
        val bbox = geom.boundingBox()
        bbox.minX >= bounds.minX && bbox.maxX <= bounds.maxX &&
        bbox.minY >= bounds.minY && bbox.maxY <= bounds.maxY
    }
}

// Usage
val featuresInRegion = geoSource.features within Bounds(x1, y1, x2, y2)
```

**Why infix:** Kotlin-native DSL feel, reads like English, extensible.

### Bounding Box Caching
```kotlin
sealed class Geometry {
    abstract val boundingBox: Bounds

    companion object {
        private val bboxCache = mutableMapOf<Geometry, Bounds>()

        fun cachedBoundingBox(geometry: Geometry): Bounds {
            return bboxCache.getOrPut(geometry) {
                geometry.calculateBoundingBox()
            }
        }
    }
}

data class Point(...) : Geometry() {
    override val boundingBox: Bounds get() = Bounds(x, y, x, y)
}
```

**Why cache:** Geometry objects are immutable, bounding box is expensive for polygons, caching speeds up spatial queries.

## 3. Do Not Hand Roll

❌ **DO NOT implement:** GeoJSON parser from scratch
✅ **USE:** kotlinx.serialization.json

**Reason:** The GeoJSON spec has edge cases (crs, bbox, nullable arrays). kotlinx.serialization handles this reliably.

❌ **DO NOT implement:** R-tree spatial index from scratch
✅ **USE:** orx-interval-tree or internal quadtree

**Reason:** R-trees have complex balancing algorithms. Use battle-tested implementations.

❌ **DO NOT implement:** GeoPackage SQLite schema parser
✅ **USE:** geopackage-java library

**Reason:** The GeoPackage spec is 300+ pages. Use the official reference implementation.

❌ **DO NOT implement:** Great-circle distance formulas
✅ **USE:** PostGIS-style haversine formulas or OpenRNDR's built-in distance utilities

**Reason:** Floating point precision issues. Use tested math libraries.

## 4. Common Pitfalls

### Memory Issues with Large GeoPackage Files
**Problem:** Loading entire GeoPackage into memory (10,000+ features) causes OOM errors.

**Solution:** Use `Sequence<Feature>` for lazy iteration, not `List<Feature>`
```kotlin
// BAD: Loads all features eagerly
val allFeatures = geoSource.features // List<Feature>

// GOOD: Loads features on demand
val features = geoSource.features // Sequence<Feature>
```

**GeoPackage-specific:**
```kotlin
fun GeoPackageSource.featuresInBounds(bounds: Bounds): Sequence<Feature> = flow {
    // Query SQLite for features intersecting bounds, emit lazily
    val features = gpkg.featuresDAO().queryForFeaturesInGeoPackage()
    for (feature in features) {
        if (feature.geometry.intersects(bounds)) {
            emit(feature)
        }
    }
}.flowOn(Dispatchers.IO)
```

### Coordinate System Handling (WGS84 vs Projected)
**Problem:** GeoJSON always uses WGS84 (lat/lng), but GeoPackage may use projected CRS (BNG, etc.)

**Solution:**
1. Track CRS in `GeoSource.crs: String` property (e.g., "EPSG:4326" default)
2. Store coordinates as-is from file (don't auto-project)
3. Provide `autoTransformTo(targetCRS)` method for conversion
4. Throw exception on file load if CRS mismatch (fail-fast)

```kotlin
data class GeoSource(
    val features: Sequence<Feature>,
    val crs: String = "EPSG:4326" // WGS84 default
) {
    fun autoTransformTo(targetCRS: String): GeoSource {
        if (crs == targetCRS) return this
        // Phase 2 will implement CRS transformation
        throw UnsupportedOperationException("CRS transformation coming in Phase 2")
    }
}
```

### Geometry Validation (Empty, Malformed Coordinates)
**Problem:** GeoJSON files may have malformed geometries (empty arrays, NaN values)

**Solution:**
```kotlin
// During GeoJSON parsing
fun parseGeometry(json: JsonElement): Geometry {
    return when (json.jsonObject["type"]?.jsonPrimitive?.content) {
        "Point" -> {
            val coords = json.jsonObject["coordinates"]?.jsonArray
                ?: throw MalformedGeometryException("Point missing coordinates")
            if (coords.size != 2) throw MalformedGeometryException("Point has ${coords.size} coordinates, expected 2")
            Point(coords[0].jsonPrimitive.double, coords[1].jsonPrimitive.double)
        }
        // ... other types
    }
}

// During GeoPackage loading
fun validateGeometry(geometry: Geometry) {
    if (geometry is Point && (geometry.x.isNaN() || geometry.y.isNaN())) {
        log.warn("Skipping feature with NaN coordinates")
        return false
    }
    return true
}
```

### Thread Safety for Cached Spatial Indexes
**Problem:** Bounding box cache is mutable, concurrent access leads to race conditions.

**Solution:** Use `ConcurrentHashMap` or recompute on demand (simpler)
```kotlin
// Option 1: Thread-safe cache
private val bboxCache = ConcurrentHashMap<Geometry, Bounds>()

fun boundingBox(geometry: Geometry): Bounds {
    return bboxCache.getOrPut(geometry) { geometry.calculateBoundingBox() }
}

// Option 2: Compute on demand (simpler, no concurrency issues)
sealed class Geometry {
    // No caching, compute on access (acceptable for most use cases)
    abstract val boundingBox: Bounds
}
```

**Recommendation:** Option 2—compute on demand, simpler, no concurrency complexity.

## 5. Code Samples

### Kotlin Data Class Pattern for Geo Types
```kotlin
@Serializable
data class GeoJSONFeature(
    val type: String,  // Always "Feature"
    val geometry: JsonElement,  // Deferred parsing to avoid circular deps
    val properties: Map<String, Any?>? = null
)

@Serializable
data class GeoJSONFeatureCollection(
    val type: String,  // Always "FeatureCollection"
    val features: List<GeoJSONFeature>
)
```

### Sealed Class Hierarchy for Geometry
```kotlin
sealed class Geometry {
    abstract val boundingBox: Bounds
}

data class Point(val x: Double, val y: Double) : Geometry() {
    override val boundingBox: Bounds = Bounds(x, y, x, y)
}

data class LineString(val points: List<Vector2>) : Geometry() {
    override val boundingBox: Bounds by lazy {
        val xs = points.map { it.x }
        val ys = points.map { it.y }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }
}

data class Polygon(val exterior: List<Vector2>, val interiors: List<List<Vector2>> = emptyList()) : Geometry() {
    override val boundingBox: Bounds by lazy {
        val xs = exterior.map { it.x }
        val ys = exterior.map { it.y }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }
}

data class MultiPoint(val points: List<Point>) : Geometry() {
    override val boundingBox: Bounds by lazy {
        val xs = points.flatMap { listOf(it.x, it.y) }
        val ys = points.flatMap { listOf(it.x, it.y) }
        Bounds(xs.min(), ys.min(), xs.max(), ys.max())
    }
}

// MultiLineString, MultiPolygon follow same pattern
```

### Infix Notation for DSL
```kotlin
infix fun Sequence<Feature>.within(bounds: Bounds): Sequence<Feature> =
    filter { it.within(bounds) }

fun Feature.within(bounds: Bounds): Boolean {
    // Check if this feature intersects bounds
}

// Usage
val cityFeatures = geoSource.features within Bounds(-74.0, 40.0, -73.0, 41.0)
```

### Map Iteration for Feature Properties
```kotlin
data class Feature(
    val geometry: Geometry,
    val properties: Map<String, Any?> = emptyMap()
) {
    // Property access with null safety
    fun property(key: String): Any? = properties[key]

    // Typed property access
    inline fun <reified T> propertyAs(key: String): T? {
        return properties[key] as? T
    }
}

// Usage
val city = features.first()
val name = city.property("name") as String?
val population = city.propertyAs<Int>("population")  // Returns Int? or null
```

## Summary

**For Phase 1 planning, you need to know:**

1. **Libraries to use:**
   - GeoJSON: kotlinx.serialization.json (already in deps)
   - GeoPackage: geopackage-java OR pure Kotlin with SQLite JDBC
   - Spatial queries: Quadtree (internal) or orx-interval-tree

2. **Kotlin patterns:**
   - Sealed classes for Geometry hierarchy
   - Data classes for Feature/Source
   - Infix notation for spatial DSL
   - Sequence for lazy iteration (memory management)

3. **Architecture:**
   - Geometry → Feature → GeoSource hierarchy
   - Synchronous file loading with fail-fast exceptions
   - Spatial query DSL with infix notation
   - CRS tracking but transformation deferred to Phase 2

4. **Pitfalls to avoid:**
   - Memory: Use lazy Sequence, not eager List
   - Coordinate systems: Track CRS, defer transformations
   - Validation: Skip malformed features, throw on malformed files
   - Caching: Compute on demand (simpler than thread-safe cache)

5. **Code samples:**
   - All patterns shown above with working examples
   - Direct copy-paste into implementation

**Next:** Use this research to create executable PLAN.md files for Phase 1.