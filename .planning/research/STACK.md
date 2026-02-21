# Technology Stack

**Project:** openrndr-geo - Kotlin/OpenRNDR Geospatial Visualization Library  
**Researched:** 2025-02-21  
**Confidence:** HIGH (verified via official docs, GitHub releases, Context7)

## Executive Summary

The standard 2025 stack for Kotlin geospatial processing with OpenRNDR visualization uses **JTS as the geometry core**, **GeoTools for I/O and projections**, and **custom adapters to OpenRNDR primitives**. This leverages mature, battle-tested Java libraries while maintaining clean Kotlin idioms and OpenRNDR's creative coding ergonomics.

---

## Recommended Stack

### Core Geometry Engine

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **JTS Topology Suite** | 1.20.0 | Geometry model, spatial operations | Industry standard, OGC-compliant, robust algorithms. Pure geometry without I/O baggage. |
| **JTS I/O Module** | 1.20.0 | WKT/WKB parsing | Lightweight format support for testing/debugging |

**Rationale:** JTS is the de facto standard for Java geometry. GeoTools, PostGIS, and virtually every Java GIS tool builds on it. Using JTS as your intermediate representation means you inherit:
- Complete spatial predicates (intersects, contains, within, etc.)
- Overlay operations (union, intersection, difference)
- Buffer, simplification, and transformation algorithms
- Robust computational geometry (no topology errors)

**Maven:**
```kotlin
implementation("org.locationtech.jts:jts-core:1.20.0")
implementation("org.locationtech.jts:jts-io-common:1.20.0")
```

---

### Data I/O Layer

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **GeoTools gt-main** | 34.2 | DataStore API, feature model | Unified API for all geo formats |
| **GeoTools gt-geopkg** | 34.2 | GeoPackage read/write | QGIS-native format, spatial indexing |
| **GeoTools gt-geojson-core** | 34.2 | GeoJSON parsing | Streaming parser, Feature collections |
| **GeoTools gt-referencing** | 34.2 | CRS transformations | Full EPSG database, OSTN15 support |
| **GeoTools gt-epsg-hsql** | 34.2 | EPSG database (embedded HSQL) | Offline EPSG code lookup |

**Rationale:** GeoTools handles the messy reality of geospatial data:
- **GeoPackage**: Spatial index support, multi-layer files, QGIS compatibility
- **GeoJSON**: Streaming reader for large files, Feature/FeatureCollection model
- **CRS**: British National Grid (EPSG:27700) ↔ WGS84 (EPSG:4326) with OSTN15 accuracy

**Maven:**
```kotlin
val geotoolsVersion = "34.2"

implementation("org.geotools:gt-main:$geotoolsVersion")
implementation("org.geotools:gt-geopkg:$geotoolsVersion")
implementation("org.geotools:gt-geojson-core:$geotoolsVersion")
implementation("org.geotools:gt-referencing:$geotoolsVersion")
implementation("org.geotools:gt-epsg-hsql:$geotoolsVersion")
```

**Repository required** (GeoTools isn't on Maven Central):
```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://repo.osgeo.org/repository/release") }
}
```

---

### Kotlin GeoJSON Alternative (Optional)

| Technology | Version | Purpose | When to Use |
|------------|---------|---------|-------------|
| **Spatial-K (geojson)** | 0.6.1 | Pure Kotlin GeoJSON | If you prefer kotlinx.serialization idioms, want multiplatform, or need Turf.js functions |

**Why you might choose this over GeoTools GeoJSON:**
- Kotlin-idiomatic DSL for building GeoJSON
- kotlinx.serialization integration (matches your existing stack)
- Includes Turf.js port for geospatial calculations
- Kotlin Multiplatform (future-proof for non-JVM targets)

**Why GeoTools is still recommended for I/O:**
- Handles malformed GeoJSON gracefully
- Streaming for massive files
- Direct conversion to JTS geometries
- QGIS/GeoServer compatibility (same parser)

**If using Spatial-K:**
```kotlin
implementation("org.maplibre.spatialk:geojson:0.6.1")
implementation("org.maplibre.spatialk:turf:0.6.1")
```

---

### OpenRNDR Integration

| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| **OPENRNDR** | 0.4.5 | Creative coding framework | Your existing base |
| **orx-shapes** | 0.4.5 | Shape utilities | hobbyCurve, rectified contours, shape operations |
| **orx-composition** | 0.4.5 | SVG-style composition | Layer-based rendering, masking |
| **orx-fx** | 0.4.5 | Post-processing effects | Blur, bloom, etc. for geo visualization |

**Key OpenRNDR types for geo:**
| OpenRNDR Type | Geo Equivalent | Notes |
|---------------|----------------|-------|
| `Vector2` | JTS `Coordinate` | XY position |
| `ShapeContour` | JTS `LineString` | Open/closed paths |
| `Shape` | JTS `Polygon` | Filled regions with holes |
| `Segment2D` | — | Bézier curves (JTS is linear only) |

**Maven:**
```kotlin
implementation("org.openrndr:openrndr-core:0.4.5")
implementation("org.openrndr.extra:orx-shapes:0.4.5")
implementation("org.openrndr.extra:orx-composition:0.4.5")
```

---

### Supporting Libraries

| Library | Version | Purpose | Why |
|---------|---------|---------|-----|
| **kotlinx-coroutines** | (existing) | Async data loading | Non-blocking file reads, chunked processing |
| **kotlinx-serialization-json** | (existing) | JSON for properties | Feature attributes, config files |
| **kotlinx-datetime** | (latest) | Temporal data | Time-series geo data |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│  GeoTools DataStore API                                         │
│  ├── GeoPackage (gt-geopkg)     ──┐                             │
│  ├── GeoJSON (gt-geojson-core)  ──┼──► JTS Geometry             │
│  └── Shapefile (gt-shapefile)   ──┘    (Point, LineString,      │
│                                          Polygon, Multi*)       │
├─────────────────────────────────────────────────────────────────┤
│  GeoTools CRS (gt-referencing)                                  │
│  EPSG:27700 (British National Grid) ◄────────► EPSG:4326 (WGS84)│
│  with OSTN15 grid shift for sub-meter accuracy                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    INTERMEDIATE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│  Your GeoPrimitives (data classes)                              │
│  ├── GeoPoint(center: Vector2, crs: CRS)                        │
│  ├── GeoLine(vertices: List<Vector2>, crs: CRS)                 │
│  └── GeoPolygon(shell: List<Vector2>, holes: List<...>, crs)    │
│                                                                  │
│  Cached in-memory representation, CRS-aware, animatable         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     RENDERING LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│  JTS → OpenRNDR Adapters                                        │
│  ├── LineString → ShapeContour (linear segments)                │
│  ├── Polygon → Shape (shell + holes as contours)                │
│  └── Multi* → List<ShapeContour> or Shape                       │
│                                                                  │
│  OpenRNDR Drawer                                                 │
│  ├── drawer.contour(shapeContour)                               │
│  ├── drawer.shape(shape)                                        │
│  └── drawer.points(listOf(Vector2))                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## Large Dataset Strategy

For 12GB+ files, you **cannot** load everything into memory. Use this approach:

### 1. Spatial Indexing (GeoPackage)
```kotlin
// GeoPackage has built-in R-Tree spatial index
val params = mapOf(
    "dbtype" to "geopkg",
    "database" to file,
    "read_only" to true
)
val dataStore = DataStoreFinder.getDataStore(params)
val featureSource = dataStore.getFeatureSource(layerName)

// Query by bounding box (uses spatial index)
val bbox = ReferencedEnvelope(minX, maxX, minY, maxY, CRS.decode("EPSG:27700"))
val filter = ECQL.toFilter("BBOX(the_geom, ${bbox.minX}, ${bbox.minY}, ${bbox.maxX}, ${bbox.maxY})")
val features = featureSource.getFeatures(filter)
```

### 2. Streaming with FeatureIterator
```kotlin
// Don't collect to List - stream through
val features = featureSource.getFeatures(filter)
features.features().use { iterator ->
    while (iterator.hasNext()) {
        val feature = iterator.next()
        val geometry = feature.getAttribute("the_geom") as Geometry
        // Process one feature at a time
    }
}
// Or use Kotlin sequences
val sequence = sequence {
    features.features().use { iter ->
        while (iter.hasNext()) yield(iter.next())
    }
}
```

### 3. Tile-based Loading
For interactive visualization, load only what's visible:
- Store GeoPackage file path + layer name
- On pan/zoom: query new bounding box
- Cache rendered tiles, evict old ones
- Use coroutines for background loading

### 4. SQLite Direct Access (Advanced)
For maximum control, query GeoPackage's SQLite directly:
```kotlin
// GeoPackage is just SQLite with geo extensions
// Can use SQLite JDBC with spatialite functions for custom queries
```

---

## Projection Support

### British National Grid ↔ WGS84

GeoTools handles this with full OSTN15 accuracy (~1m vs ~3m with simple 7-parameter):

```kotlin
import org.geotools.referencing.CRS
import org.opengis.referencing.operation.MathTransform

// Decode CRS by EPSG code
val bng = CRS.decode("EPSG:27700")  // British National Grid
val wgs84 = CRS.decode("EPSG:4326") // WGS84

// Create transformation (GeoTools selects best available: OSTN15)
val transform = CRS.findMathTransform(bng, wgs84, true)

// Transform geometry
val transformed = JTS.transform(geometry, transform)
```

### OSTN15 Grid Shift
For sub-meter accuracy between OSGB36 and WGS84, GeoTools uses the OSTN15 grid:
- Included in `gt-epsg-hsql` automatically
- ~1m accuracy vs ~3m with 7-parameter Helmert
- Required for UK Ordnance Survey data

---

## QGIS Export Formats

QGIS exports to all supported formats:

| Format | GeoTools Module | Notes |
|--------|-----------------|-------|
| GeoPackage | `gt-geopkg` | **Recommended** - Single file, multiple layers, spatial index |
| GeoJSON | `gt-geojson-core` | Good for web, no spatial index, large files slow |
| Shapefile | `gt-shapefile` | Legacy, 2GB limit, DBF field name limits |

**Recommendation:** Encourage users to export as GeoPackage from QGIS for best performance.

---

## Alternatives Considered

| Category | Chosen | Rejected | Why Rejected |
|----------|--------|----------|--------------|
| Geometry Core | **JTS** | GeoTools Geometry | JTS is cleaner, GeoTools just wraps JTS anyway |
| GeoJSON Parsing | **GeoTools** | geojson-kotlin | GeoTools handles malformed JSON, streams large files |
| GeoJSON Parsing | **GeoTools** | Spatial-K | Spatial-K is great but GeoTools → JTS is direct path |
| GeoPackage | **GeoTools** | mil.nga.geopackage | GeoTools integrates with JTS/CRS; mil.nga is lower-level |
| CRS | **GeoTools** | Proj4J | GeoTools has EPSG database built-in; Proj4J is manual |
| CRS | **GeoTools** | OS transforms (JS) | That's JavaScript; GeoTools has OSTN15 built-in |

---

## Dependency Summary

```kotlin
// build.gradle.kts

repositories {
    mavenCentral()
    maven { url = uri("https://repo.osgeo.org/repository/release") }
}

dependencies {
    // === Geometry Core ===
    implementation("org.locationtech.jts:jts-core:1.20.0")
    implementation("org.locationtech.jts:jts-io-common:1.20.0")
    
    // === GeoTools I/O ===
    val gt = "34.2"
    implementation("org.geotools:gt-main:$gt")
    implementation("org.geotools:gt-geopkg:$gt")
    implementation("org.geotools:gt-geojson-core:$gt")
    implementation("org.geotools:gt-referencing:$gt")
    implementation("org.geotools:gt-epsg-hsql:$gt")
    
    // === OpenRNDR ===
    implementation("org.openrndr:openrndr-core:0.4.5")
    implementation("org.openrndr.extra:orx-shapes:0.4.5")
    
    // === Existing Stack ===
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:...")
    // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:...")
}
```

---

## Confidence Assessment

| Area | Confidence | Source |
|------|------------|--------|
| JTS version | HIGH | Eclipse LocationTech releases, Sept 2024 |
| GeoTools version | HIGH | OSGeo releases, Jan 2025 |
| OpenRNDR version | HIGH | GitHub releases, matches your existing |
| Projection support | HIGH | GeoTools docs, OSTN15 included |
| GeoPackage spatial index | HIGH | GeoTools docs, GPKG spec |
| OpenRNDR ShapeContour API | HIGH | Context7, official guide |
| Large file strategy | MEDIUM | Based on GeoTools patterns, needs testing at scale |

---

## Sources

- **JTS 1.20.0 Release:** https://github.com/locationtech/jts/releases/tag/1.20.0 (Sept 2024)
- **GeoTools 34.2:** https://github.com/geotools/geotools/releases (Jan 2025)
- **OpenRNDR Guide:** https://guide.openrndr.org/drawing/curvesAndShapes.html
- **ORX 0.4.5:** https://github.com/openrndr/orx/releases
- **GeoTools GeoPackage Plugin:** https://docs.geotools.org/latest/userguide/library/data/geopackage.html
- **GeoTools CRS:** https://docs.geotools.org/latest/userguide/library/referencing/crs.html
- **Spatial-K:** https://github.com/maplibre/spatial-k (v0.6.1, Nov 2025)
