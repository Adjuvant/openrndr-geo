# Architecture Research

**Domain:** Geospatial Visualization Library (Kotlin/OpenRNDR)
**Researched:** 2026-02-21
**Confidence:** HIGH

## Standard Architecture

Geospatial visualization systems follow a consistent layered architecture pattern, observed across Cesium, QGIS, MapLibre, and deck.gl. The key is separating data operations from rendering operations with a clear intermediate representation.

### System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │   Animation  │  │    Styling   │  │  Interaction │                  │
│  │    Layer     │  │    Engine    │  │   Handlers   │                  │
│  └──────────────┘  └──────────────┘  └──────────────┘                  │
├─────────────────────────────────────────────────────────────────────────┤
│                        RENDERING LAYER                                   │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    Rendering Adapters                              │   │
│  │  GeoPoint → OpenRNDR Circle/Point    GeoPolygon → Shape/Contour  │   │
│  │  GeoLine → OpenRNDR LineContour      GeoCollection → Batch       │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                              ↓                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                     OpenRNDR Drawer API                            │   │
│  │  drawer.circle()  drawer.contour()  drawer.shape()  drawer.line() │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                              ↓                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                        GPU Layer                                   │   │
│  │  VertexBuffers  ShadeStyles  RenderTargets  ColorBuffers          │   │
│  └──────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────┤
│                     INTERMEDIATE REPRESENTATION                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    Geo Primitive Model                             │   │
│  │  GeoPoint  GeoLineString  GeoPolygon  GeoCollection  Properties   │   │
│  │  CRS Information  BoundingBox  Style Attributes                   │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                              ↑                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    Projection Engine                               │   │
│  │  BNG ↔ WGS84  Coordinate Transforms  Bounds Calculations          │   │
│  └──────────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────────────┤
│                          DATA ACCESS LAYER                               │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐            │
│  │ GeoJSON Reader │  │ GeoPackage     │  │ Future Format  │            │
│  │ (Full Load)    │  │ Reader         │  │ Readers        │            │
│  │                │  │ (Lazy/Indexed) │  │                │            │
│  └────────────────┘  └────────────────┘  └────────────────┘            │
│                              ↓                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                 Spatial Query Engine                               │   │
│  │  Region Filtering  Bounding Box Queries  Feature Selection        │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| **GeoJSON Reader** | Load GeoJSON files into geo primitives | Full file load, JSON parsing, geometry construction |
| **GeoPackage Reader** | Lazy/region-filtered access to GeoPackage files | SQLite connection, RTree index queries, WKB geometry parsing |
| **Spatial Query Engine** | Filter features by region, attributes, geometry type | SQL generation, spatial predicate evaluation |
| **Projection Engine** | Transform coordinates between CRS (BNG ↔ WGS84) | PROJ library or custom transform functions |
| **Geo Primitive Model** | In-memory representation of geographic features | Data classes for Point/LineString/Polygon with properties |
| **Rendering Adapters** | Convert geo primitives to OpenRNDR drawing calls | Pattern matching on geometry type, batch optimization |
| **OpenRNDR Drawer** | Execute GPU rendering commands | drawer.circle(), drawer.shape(), vertex buffers |
| **Animation Layer** | Animate geo primitives over time | Property tweening, path following, temporal attributes |
| **Styling Engine** | Apply visual styles (color, stroke, fill) | Style objects, data-driven styling, cascading styles |
| **Interaction Handlers** | Process user input on geo features | Hit testing, selection, hover states |

## Recommended Project Structure

```
src/main/kotlin/org/openrndr/geo/
├── model/                      # Intermediate representation
│   ├── GeoPrimitive.kt         # Sealed class hierarchy
│   ├── GeoPoint.kt
│   ├── GeoLineString.kt
│   ├── GeoPolygon.kt
│   ├── GeoCollection.kt
│   ├── BoundingBox.kt
│   └── Properties.kt           # Feature attributes
│
├── data/                       # Data access layer
│   ├── readers/
│   │   ├── GeoReader.kt        # Interface
│   │   ├── GeoJsonReader.kt    # GeoJSON implementation
│   │   └── GeoPackageReader.kt # GeoPackage with lazy loading
│   ├── queries/
│   │   ├── SpatialQuery.kt     # Query DSL
│   │   └── RegionFilter.kt     # Bounding box filtering
│   └── index/
│       └── SpatialIndex.kt     # RTree abstraction
│
├── projection/                 # Coordinate reference systems
│   ├── CRS.kt                  # CRS definitions
│   ├── Projection.kt           # Transform interface
│   ├── BNG.kt                  # British National Grid
│   └── WGS84.kt                # World Geodetic System
│
├── render/                     # Rendering layer
│   ├── adapters/
│   │   ├── RenderAdapter.kt    # Interface
│   │   ├── PointAdapter.kt     # GeoPoint → drawer calls
│   │   ├── LineAdapter.kt      # GeoLineString → contours
│   │   ├── PolygonAdapter.kt   # GeoPolygon → shapes
│   │   └── BatchAdapter.kt     # Optimized batch rendering
│   ├── styles/
│   │   ├── GeoStyle.kt         # Style definition
│   │   └── StylingEngine.kt    # Apply styles to primitives
│   └── layers/
│       └── GeoLayer.kt         # Layer management
│
├── animation/                  # Animation layer
│   ├── GeoAnimator.kt          # Animation interface
│   ├── PathAnimation.kt        # Animate along line
│   └── PropertyAnimation.kt    # Animate attributes
│
└── facade/                     # Simplified API
    └── Geo.kt                  # Main entry point facade
```

### Structure Rationale

- **model/**: Pure data classes with no dependencies - the "intermediate representation" that all layers agree on
- **data/**: Isolated data access with interfaces for swappability - GeoPackage reader can be swapped without touching rendering
- **projection/**: Separate module because projections are pure math operations on coordinates
- **render/**: All OpenRNDR-specific code isolated here - rendering adapters translate geo primitives to drawer calls
- **animation/**: Operates on geo primitives, independent of how they're rendered
- **facade/**: Simple API that orchestrates the layers - hides complexity from users

## Architectural Patterns

### Pattern 1: Lazy Region-Filtered Loading (for GeoPackage)

**What:** Load only features that intersect a bounding box, using GeoPackage's built-in RTree spatial index.

**When to use:** Large GeoPackage files (GBs) where only a region is visible at any time.

**Trade-offs:**
- Pros: Can handle 12GB files without loading them entirely; fast queries using spatial index
- Cons: Requires GeoPackage with RTree index; some delay when panning to new region

**Example:**
```kotlin
interface GeoReader {
    // Eager load - fine for small files
    fun loadAll(): GeoCollection
    
    // Lazy load - for large GeoPackages
    fun loadRegion(bbox: BoundingBox): GeoCollection
}

class GeoPackageReader(private val path: String) : GeoReader {
    private val connection: SQLiteConnection = openConnection(path)
    
    override fun loadRegion(bbox: BoundingBox): GeoCollection {
        // Query uses RTree spatial index automatically
        val sql = """
            SELECT id, geom, properties 
            FROM features
            WHERE rowid IN (
                SELECT rowid 
                FROM rtree_features_geom
                WHERE minx >= ? AND maxx <= ?
                  AND miny >= ? AND maxy <= ?
            )
        """
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setDouble(1, bbox.minX)
            stmt.setDouble(2, bbox.maxX)
            stmt.setDouble(3, bbox.minY)
            stmt.setDouble(4, bbox.maxY)
            // Execute and parse WKB geometries
            parseFeatures(stmt.executeQuery())
        }
    }
}
```

### Pattern 2: Rendering Adapter (Data-Rendering Separation)

**What:** Each geometry type has an adapter that converts geo primitives to OpenRNDR drawer calls, keeping data model independent of rendering.

**When to use:** Always - this is the core separation between data and rendering layers.

**Trade-offs:**
- Pros: Can change rendering without touching data model; can add new renderers (e.g., SVG export); testable
- Cons: Slight overhead from conversion layer

**Example:**
```kotlin
interface RenderAdapter<T : GeoPrimitive> {
    fun render(drawer: Drawer, primitive: T, style: GeoStyle)
}

class PointAdapter : RenderAdapter<GeoPoint> {
    override fun render(drawer: Drawer, point: GeoPoint, style: GeoStyle) {
        drawer.fill = style.fillColor
        drawer.stroke = style.strokeColor
        drawer.strokeWeight = style.strokeWeight
        drawer.circle(point.x, point.y, style.radius)
    }
}

class LineAdapter : RenderAdapter<GeoLineString> {
    override fun render(drawer: Drawer, line: GeoLineString, style: GeoStyle) {
        drawer.stroke = style.strokeColor
        drawer.strokeWeight = style.strokeWeight
        drawer.lineLoop(line.coordinates.map { Vector2(it.x, it.y) })
    }
}

class PolygonAdapter : RenderAdapter<GeoPolygon> {
    override fun render(drawer: Drawer, polygon: GeoPolygon, style: GeoStyle) {
        drawer.fill = style.fillColor
        drawer.stroke = style.strokeColor
        
        // Convert to OpenRNDR Shape
        val contour = ShapeContour.fromPoints(
            polygon.exteriorRing.map { Vector2(it.x, it.y) },
            closed = true
        )
        drawer.shape(Shape(listOf(contour)))
    }
}
```

### Pattern 3: Facade Pattern (Simplified API)

**What:** Single entry point that hides internal complexity - users don't need to know about readers, adapters, or layers.

**When to use:** Public API for library users.

**Trade-offs:**
- Pros: Easy to use; hides complexity; can change internals without breaking API
- Cons: May not expose all capabilities; another layer of abstraction

**Example:**
```kotlin
class Geo {
    private val readers = mutableMapOf<String, GeoReader>()
    private val layers = mutableListOf<GeoLayer>()
    private val stylingEngine = StylingEngine()
    
    // Simple API for common cases
    fun loadGeoJson(path: String): GeoLayer {
        val reader = GeoJsonReader()
        val collection = reader.loadAll(File(path))
        val layer = GeoLayer(collection)
        layers.add(layer)
        return layer
    }
    
    // Advanced API with lazy loading
    fun loadGeoPackage(path: String, lazy: Boolean = true): GeoPackageLayer {
        val reader = GeoPackageReader(path)
        return if (lazy) {
            GeoPackageLayer(reader, lazy = true)
        } else {
            GeoLayer(reader.loadAll())
        }
    }
    
    // Render all layers
    fun render(drawer: Drawer, viewport: BoundingBox) {
        layers.forEach { layer ->
            layer.featuresInRegion(viewport).forEach { feature ->
                val adapter = adapters[feature.geometry::class]
                val style = stylingEngine.styleFor(feature)
                adapter?.render(drawer, feature.geometry, style)
            }
        }
    }
}
```

### Pattern 4: Batched Rendering (Performance Optimization)

**What:** Group multiple geometries of the same type into a single draw call using vertex buffers.

**When to use:** Large numbers of points/polygons (10k+) where individual draw calls are too slow.

**Trade-offs:**
- Pros: Massive performance improvement for large datasets
- Cons: More complex; styles must be uniform or encoded in vertex attributes

**Example:**
```kotlin
class BatchPointAdapter {
    fun renderBatch(drawer: Drawer, points: List<GeoPoint>, style: GeoStyle) {
        // Create vertex buffer with all points
        val format = vertexFormat {
            position(2)  // x, y
            attribute("color", VertexElementType.VECTOR4_FLOAT32)
        }
        val buffer = vertexBuffer(format, points.size)
        
        buffer.put {
            points.forEach { point ->
                write(Vector2(point.x, point.y))
                write(style.fillColor.toVector4())
            }
        }
        
        // Single draw call for all points
        drawer.vertexBuffer(buffer, DrawPrimitive.POINTS)
    }
}
```

## Data Flow

### Request Flow (Load → Transform → Render)

```
User Action (load GeoPackage)
    ↓
GeoPackageReader.loadRegion(viewport.bbox)
    ↓
SQLite RTree Query (spatial index)
    ↓
WKB Geometry Parsing
    ↓
GeoPrimitive Creation (GeoPoint/LineString/Polygon)
    ↓
Projection Engine (if CRS != WGS84)
    ↓
GeoCollection (in-memory)
    ↓
Styling Engine (determine style per feature)
    ↓
RenderAdapter.render(drawer, primitive, style)
    ↓
OpenRNDR Drawer calls (drawer.circle, drawer.shape)
    ↓
GPU Rendering
```

### Viewport Update Flow (Pan/Zoom)

```
Viewport Changed
    ↓
Determine New BoundingBox
    ↓
Check Cache for Region
    ├─ Hit → Use cached GeoCollection
    └─ Miss → GeoPackageReader.loadRegion(newBbox)
        ↓
    Add to Cache
    ↓
Re-render All Layers
    ↓
Update Screen
```

### Key Data Flows

1. **Loading Flow:** File → Reader → GeoPrimitive → (Projection) → GeoCollection
2. **Rendering Flow:** GeoCollection → Style Resolution → Adapter Selection → OpenRNDR Calls
3. **Interaction Flow:** Mouse Event → Hit Testing → Feature Selection → Highlight/Tooltip
4. **Animation Flow:** Time Update → Animator → Modified GeoPrimitive → Re-render

## Scaling Considerations

| Data Size | Architecture Adjustments |
|-----------|--------------------------|
| < 10MB (small GeoJSON) | Eager load all data into memory; simple rendering |
| 10MB - 500MB | Eager load acceptable; consider batched rendering for many features |
| 500MB - 5GB | Lazy region-filtered loading required; spatial index essential; viewport-based caching |
| 5GB - 12GB+ | Lazy loading mandatory; tile-based approach for very dense areas; progressive loading; consider offscreen culling |

### Scaling Priorities

1. **First bottleneck (500MB+):** Memory - switch from `loadAll()` to `loadRegion(bbox)` with spatial index
2. **Second bottleneck (10k+ features visible):** Rendering - switch from individual draw calls to batched vertex buffers
3. **Third bottleneck (50k+ features visible):** CPU/GPU - add level-of-detail (LOD) system, simplify geometries at low zoom

### Specific Recommendations for 12GB GeoPackage

```kotlin
class OptimizedGeoPackageReader(path: String) {
    private val connection = SQLiteConnection(path).apply {
        // Optimize for read-only access
        execSQL("PRAGMA journal_mode=OFF")
        execSQL("PRAGMA synchronous=OFF")
        execSQL("PRAGMA mmap_size=3000000000") // 3GB memory map
        execSQL("PRAGMA temp_store=MEMORY")
        execSQL("PRAGMA cache_size=-64000") // 64MB cache
    }
    
    fun loadRegion(bbox: BoundingBox, maxFeatures: Int = 50000): GeoCollection {
        // Use spatial index + limit results to prevent overload
        val sql = """
            SELECT id, geom, properties 
            FROM features
            WHERE rowid IN (
                SELECT rowid FROM rtree_features_geom
                WHERE minx >= ? AND maxx <= ?
                  AND miny >= ? AND maxy <= ?
            )
            LIMIT ?
        """
        // ... execute and parse
    }
    
    // Pre-load bounding box metadata for fast viewport calculations
    fun getLayerBounds(): BoundingBox {
        return connection.prepareStatement(
            "SELECT min_x, min_y, max_x, max_y FROM gpkg_contents WHERE table_name = ?"
        ).use { /* query and return */ }
    }
}
```

## Anti-Patterns

### Anti-Pattern 1: Mixing Data and Rendering Code

**What people do:** Put GeoJSON parsing and drawer calls in the same class.

**Why it's wrong:** Can't test data loading independently; can't swap renderers; hard to add new formats.

**Do this instead:** 
```kotlin
// BAD
class GeoJsonRenderer {
    fun render(drawer: Drawer, file: File) {
        val json = Json.parse(file.readText())
        json.features.forEach { f ->
            when (f.geometry.type) {
                "Point" -> drawer.circle(...)
                "Polygon" -> drawer.shape(...)
            }
        }
    }
}

// GOOD
class GeoJsonReader : GeoReader {
    fun loadAll(file: File): GeoCollection { /* parse only */ }
}

class GeoRenderer {
    fun render(drawer: Drawer, collection: GeoCollection) { /* render only */ }
}
```

### Anti-Pattern 2: Loading Entire GeoPackage into Memory

**What people do:** `val allFeatures = reader.loadAll()` on a 12GB file.

**Why it's wrong:** OutOfMemoryError; long load times; wasted memory on data user may never see.

**Do this instead:**
```kotlin
// BAD
val collection = geoPackageReader.loadAll() // 12GB in memory!

// GOOD
val collection = geoPackageReader.loadRegion(viewport.bbox)
// Only load what's visible (maybe 10-50MB in memory)
```

### Anti-Pattern 3: Individual Draw Calls for Thousands of Features

**What people do:** Loop through 50,000 points calling `drawer.circle()` each time.

**Why it's wrong:** 50,000 draw calls = very slow; GPU underutilized.

**Do this instead:**
```kotlin
// BAD
points.forEach { drawer.circle(it.x, it.y, 5.0) } // 50k draw calls

// GOOD
val buffer = createVertexBuffer(points) // Single buffer
drawer.vertexBuffer(buffer, DrawPrimitive.POINTS) // 1 draw call
```

### Anti-Pattern 4: Tight Coupling to Specific CRS

**What people do:** Assume all coordinates are WGS84 (lat/lon) and use them directly.

**Why it's wrong:** UK data is often BNG (British National Grid); user may have data in other projections; mixing projections breaks rendering.

**Do this instead:**
```kotlin
// BAD
val x = feature.geometry.coordinates[0] // Is this lon? meters? degrees?

// GOOD
data class GeoPoint(val x: Double, val y: Double, val crs: CRS)

// Transform to common CRS before rendering
val transformed = projectionEngine.transform(point, from = CRS.BNG, to = CRS.WGS84)
```

## Integration Points

### External Dependencies

| Dependency | Integration Pattern | Notes |
|------------|--------------------|-------|
| **OpenRNDR** | Drawer API, VertexBuffer, RenderTarget | Already using; rendering layer only |
| **SQLite JDBC** | Direct connection for GeoPackage | Use xerial/sqlite-jdbc for Kotlin |
| **PROJ (optional)** | Coordinate transforms | Consider proj4j or implement BNG ↔ WGS84 directly |
| **WKB Parser** | Parse binary geometry from GeoPackage | Implement custom or use existing library |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Data Layer ↔ Model | GeoCollection return types | Data layer returns model objects |
| Model ↔ Projection | Transformed copies | Projection returns new objects, doesn't mutate |
| Model ↔ Rendering | RenderAdapter pattern | Adapters take model, call drawer |
| Facade ↔ All Layers | Orchestrates via interfaces | Facade knows all layers, they don't know each other |

## Build Order Implications

Based on dependencies, build in this order:

1. **Phase 1: Model Layer** (no dependencies)
   - `model/GeoPrimitive.kt` and subtypes
   - `model/BoundingBox.kt`
   - `model/Properties.kt`
   
2. **Phase 2: Projection Layer** (depends on Model)
   - `projection/CRS.kt`
   - `projection/Projection.kt`
   - `projection/BNG.kt` and `WGS84.kt`
   
3. **Phase 3: Data Layer** (depends on Model)
   - `data/readers/GeoReader.kt` interface
   - `data/readers/GeoJsonReader.kt` (easier to implement first)
   - `data/readers/GeoPackageReader.kt` (requires SQLite knowledge)
   - `data/queries/SpatialQuery.kt`
   
4. **Phase 4: Rendering Layer** (depends on Model)
   - `render/styles/GeoStyle.kt`
   - `render/adapters/RenderAdapter.kt` interface
   - Individual adapters (PointAdapter, LineAdapter, PolygonAdapter)
   - `render/adapters/BatchAdapter.kt` (optimization)
   - `render/layers/GeoLayer.kt`
   
5. **Phase 5: Animation Layer** (depends on Model + Rendering)
   - `animation/GeoAnimator.kt`
   - `animation/PathAnimation.kt`
   - `animation/PropertyAnimation.kt`
   
6. **Phase 6: Facade** (depends on all layers)
   - `facade/Geo.kt`
   - Integration testing

## Sources

- Cesium Architecture: https://github.com/CesiumGS/cesium/wiki/Architecture
- deck.gl Layer Architecture: https://deck.gl/docs/developer-guide/using-layers
- GeoPackage RTree Spatial Index: http://www.geopackage.org/spec/#_r_tree_spatial_indexes
- OpenRNDR Drawing Guide: https://guide.openrndr.org/drawing/
- OpenRNDR Custom Rendering: https://guide.openrndr.org/drawing/customRendering.html
- SQLite Optimization for Large Databases: https://erouault.blogspot.com/2017/03/dealing-with-huge-vector-geopackage.html
- Optimizing SQLite for Read Performance: https://jacobfilipp.com/sqliteoptimize/
- Galileo GIS Architecture: https://github.com/Maximkaaa/galileo
- Mosaic Architecture for Data Views: https://idl.cs.washington.edu/files/2024-Mosaic-TVCG.pdf
- lazysf Lazy Loading Pattern: https://github.com/hypertidy/lazysf

---

# v1.2.0 Architecture Integration

**Domain:** Geospatial visualization library (OpenRNDR-based)
**Researched:** 2026-02-26
**Confidence:** HIGH (based on direct codebase analysis)

## Executive Summary

v1.2.0 adds API improvements and examples to the existing openrndr-geo library. The architecture follows a clean layered pattern:

```
┌─────────────────────────────────────────────────────────────┐
│  geo/examples/           Demo programs (end-user facing)    │
├─────────────────────────────────────────────────────────────┤
│  geo/render/             Rendering adapters (drawer → geom) │
├─────────────────────────────────────────────────────────────┤
│  geo/                    Core data layer (GeoSource, etc.)  │
├─────────────────────────────────────────────────────────────┤
│  geo/projection/         Projection infrastructure          │
└─────────────────────────────────────────────────────────────┘
```

All v1.2.0 features integrate cleanly into existing modules—no new packages required.

---

## Integration Points by Feature

### 1. GeoSource.summary() — Data Inspection

**Module:** `geo/GeoSource.kt`  
**Integration:** Add as new method to abstract class  
**Pattern:** Similar to existing `totalBoundingBox()` and `countFeatures()`

```kotlin
// Add to GeoSource abstract class (line ~72)
fun summary(): GeoSourceSummary {
    return GeoSourceSummary(
        crs = crs,
        featureCount = countFeatures(),
        geometryTypes = features.map { it.geometry::class.simpleName }.toSet(),
        propertyKeys = features.flatMap { it.properties.keys }.toSet(),
        bounds = totalBoundingBox()
    )
}
```

**New component needed:** `GeoSourceSummary` data class in `geo/` package

**Rationale:** Follows existing patterns (`totalBoundingBox()` computes from features). Summary is a natural extension that aggregates multiple statistics in one call.

---

### 2. Polygon Ring Handling — Interior/Exterior

**Module:** `geo/render/PolygonRenderer.kt`  
**Integration:** Modify existing `writePolygon()` + add new overload  
**Pattern:** Extend to accept interiors parameter

**Current signature:**
```kotlin
fun writePolygon(drawer: Drawer, points: List<Vector2>, style: Style)
```

**New signature:**
```kotlin
fun writePolygon(
    drawer: Drawer,
    exterior: List<Vector2>,
    interiors: List<List<Vector2>> = emptyList(),
    style: Style
)
```

**Implementation approach:**
- Use OpenRNDR's `ShapeBuilder` or `CompoundShape` for multi-ring polygons
- Interior rings create holes in the exterior shape
- Backward compatible via default parameter

**Rationale:** Modifying existing renderer is cleaner than creating a parallel implementation. The existing `writePolygon` already handles single-ring polygons; extending it maintains API consistency.

---

### 3. Boilerplate Reduction — Convenience Functions

**Module:** `geo/GeoSourceConvenience.kt`  
**Integration:** Add new functions to existing file  
**Pattern:** Follow `geoSource()` pattern for factory functions

**Existing convenience layer:**
- `geoSource(path)` → GeoJSONSource
- `geoSourceFromString(content)` → GeoJSONSource  
- `geoSourceFromFeatures(features)` → GeoSource

**New additions:**
```kotlin
// Single import convenience
fun geoAll(): List<KClass<*>> = listOf(/* all public API classes */)

// Render with auto-fit (extends existing pattern)
fun GeoSource.renderFit(drawer: Drawer, style: Style? = null) {
    val bounds = totalBoundingBox()
    val projection = ProjectionFactory.fitBounds(bounds, drawer.width, drawer.height)
    render(drawer, projection, style)
}
```

**Alternative:** Add extension methods to `Drawer` in `geo/render/DrawerGeoExtensions.kt`:
```kotlin
fun Drawer.geoAuto(source: GeoSource, style: Style? = null)
```

**Rationale:** `GeoSourceConvenience.kt` already owns the "easy mode" API. Keep related functions together.

---

### 4. MultiPolygon Bounds — Geometry Preprocessing

**Module:** `geo/Geometry.kt` (NOT render/)  
**Integration:** Add bounds computation for MultiPolygon  
**Pattern:** Use existing `boundingBox` property pattern

**Current issue:** MultiPolygon with ocean data fails on coordinates beyond Mercator limits.

**Solution location:**
- **Preprocessing:** `geo/Geometry.kt` — add `clampToMercator()` already exists
- **Rendering:** `geo/render/MultiRenderer.kt` — already has `clampToMercatorBounds` flag

**Current state (MultiRenderer.kt:155-164):**
```kotlin
val polygonsToRender = if (clampToMercatorBounds && projection is ProjectionMercator) {
    multiPolygon.polygons.map { polygon ->
        polygon.exterior.map { coord ->
            Vector2(coord.x, coord.y.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
        }
    }
} else { ... }
```

**Enhancement needed:**
1. Extend clamp to also normalize longitude (dateline handling)
2. Apply clamp to interiors, not just exterior
3. Consider using existing `Geometry.clampAndNormalize()` from Geometry.kt

**Rationale:** This is geometry preprocessing logic, not rendering logic. The renderer should receive valid geometry; validation/clamping happens before projection.

---

### 5. Batch Projection — Performance Utility

**Module:** `geo/projection/` (new utility) or extend `geo/ProjectionExtensions.kt`  
**Integration:** Add batch projection functions  
**Pattern:** Extend existing `projectToScreen()` pattern

**Current projection flow (per-frame):**
```kotlin
// In render loop - projects EVERY frame
features.forEach { feature ->
    val screenPoints = feature.geometry.projectToScreen(projection)
    // render...
}
```

**New batch projection:**
```kotlin
// In ProjectionExtensions.kt
fun <T : Geometry> T.projectOnce(projection: GeoProjection): ProjectedGeometry<T>

// Or in new file: geo/projection/BatchProjection.kt
class ProjectedGeometry<T : Geometry>(val original: T, val screenSpace: List<Vector2>)
fun GeoSource.projectAll(projection: GeoProjection): ProjectedGeoSource
```

**Implementation strategy:**
1. Create `ProjectedGeometry` wrapper that caches screen coordinates
2. Add `GeoSource.projectAll()` that returns projected copy
3. Render functions accept `ProjectedGeometry` directly

**Rationale:** Projection infrastructure already exists. Batch projection is a performance optimization layer that sits between data loading and rendering.

---

### 6. Examples — Structure and Placement

**Module:** `geo/examples/`  
**Integration:** Add new example files following existing naming convention  
**Pattern:** `category_Description.kt` naming

**Existing examples:**
```
geo/examples/
├── render_BasicRendering.kt
├── render_LiveRendering.kt
├── proj_HaversineDemo.kt
├── proj_ProjectionTest.kt
├── anim_BasicAnimation.kt
├── anim_ChainDemo.kt
├── layer_Graticule.kt
├── layer_BlendModes.kt
├── core_CRSTransformTest.kt
└── core_DataLoadingTest.kt
```

**New examples for v1.2.0:**
```
geo/examples/
├── api_SummaryDemo.kt          # GeoSource.summary() usage
├── render_PolygonHoles.kt      # Interior ring rendering
├── api_BoilerplateFree.kt      # One-line rendering demo
├── render_OceanData.kt         # MultiPolygon with clamping
└── perf_BatchProjection.kt     # Cached projection demo
```

**Naming convention:**
- `api_*` — API convenience features
- `render_*` — Rendering techniques
- `perf_*` — Performance patterns
- `proj_*` — Projection demos
- `anim_*` — Animation features
- `layer_*` — Layer system
- `core_*` — Core functionality

**Rationale:** Existing convention uses category prefix + descriptive name. New examples should follow this pattern for discoverability.

---

## New vs Modified Components Summary

| Feature | New Component | Modified Component | Rationale |
|---------|---------------|-------------------|-----------|
| GeoSource.summary() | `GeoSourceSummary` data class | `GeoSource.kt` | Follows existing pattern |
| Polygon rings | None | `PolygonRenderer.kt` | Extend existing renderer |
| Boilerplate | None | `GeoSourceConvenience.kt`, `DrawerGeoExtensions.kt` | Add convenience functions |
| MultiPolygon bounds | None | `Geometry.kt`, `MultiRenderer.kt` | Enhance preprocessing |
| Batch projection | `ProjectedGeometry` wrapper | `ProjectionExtensions.kt` | New caching layer |
| Examples | 5 new demo files | None | Self-contained examples |

---

## Suggested Build Order

Based on dependencies, implement in this order:

```
Phase 1: Data Layer (no dependencies)
├── 1. GeoSource.summary() + GeoSourceSummary
│   └── Enables: api_SummaryDemo example
│
├── 2. MultiPolygon bounds enhancement
│   └── Fix Geometry.clampAndNormalize() for interiors
│   └── Enables: render_OceanData example
│
Phase 2: Rendering Layer (depends on Phase 1)
├── 3. Polygon ring handling
│   └── Modify PolygonRenderer for interiors
│   └── Enables: render_PolygonHoles example
│
Phase 3: Convenience Layer (depends on Phase 1-2)
├── 4. Boilerplate reduction
│   └── Add to GeoSourceConvenience.kt
│   └── Add to DrawerGeoExtensions.kt
│   └── Enables: api_BoilerplateFree example
│
Phase 4: Performance Layer (optional, depends on all above)
├── 5. Batch projection
│   └── Create ProjectedGeometry wrapper
│   └── Extend ProjectionExtensions
│   └── Enables: perf_BatchProjection example
│
Phase 5: Documentation
└── 6. All examples
```

**Critical path:** 1 → 3 → 6 (summary → convenience → examples)

**Parallelizable:** Phase 1 items (1, 2) can be done simultaneously. Phase 3 and 4 can overlap.

---

## Component Boundaries

```
┌─────────────────────────────────────────────────────────────────────┐
│  geo/examples/                                                       │
│  - Self-contained demo programs                                      │
│  - Import from geo.* and geo.render.*                                │
│  - NO business logic, only demonstration                             │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│  geo/render/                                                         │
│  - DrawerGeoExtensions.kt  ←── Add convenience here                 │
│  - PolygonRenderer.kt      ←── Modify for rings                     │
│  - MultiRenderer.kt        ←── Uses clamp from Geometry             │
│  - Style.kt, Shape.kt                                                │
│  - NO projection logic (uses geo.projection.*)                       │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│  geo/                                                                │
│  - GeoSource.kt            ←── Add summary() here                   │
│  - GeoSourceConvenience.kt ←── Add convenience functions            │
│  - Geometry.kt             ←── Enhance clamp/preprocessing          │
│  - Feature.kt, Bounds.kt, Point.kt, etc.                            │
│  - NO rendering logic                                                │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│  geo/projection/                                                     │
│  - ProjectionExtensions.kt ←── Add batch projection                 │
│  - GeoProjection.kt, ProjectionMercator.kt                          │
│  - CRSTransformer.kt                                                 │
│  - NO rendering, NO data loading                                    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Anti-Patterns to Avoid (v1.2.0 Specific)

### 1. Don't Put Rendering Logic in Geometry
❌ **Bad:** Adding `render()` method to `Polygon` class
✅ **Good:** Keep rendering in `geo/render/` package, Geometry only holds data

### 2. Don't Duplicate Projection Logic
❌ **Bad:** Re-implementing Mercator clamping in renderer
✅ **Good:** Use existing `Geometry.clampToMercator()` before projection

### 3. Don't Create Parallel Convenience APIs
❌ **Bad:** New `EasyGeoSource` class with different API
✅ **Good:** Extend existing `GeoSource` with new methods

### 4. Don't Put Examples in Test Directory
❌ **Bad:** `src/test/kotlin/geo/examples/`
✅ **Good:** `src/main/kotlin/geo/examples/` (they're runnable programs)

---

## Sources

- Direct codebase analysis of existing architecture
- Existing patterns from `GeoSource.kt`, `Geometry.kt`, `ProjectionExtensions.kt`
- Example naming convention from `geo/examples/` directory

---

# v1.3.0 Architecture: Batch Projection & Geometry Caching

**Domain:** Geospatial visualization library (OpenRNDR-based)
**Researched:** 2026-03-05
**Confidence:** HIGH (based on existing codebase analysis + Kotlin patterns)

## Executive Summary

This research addresses integrating **batch projection** and **geometry caching** into the existing openrndr-geo architecture while maintaining the clean data/rendering separation and lazy Sequence patterns established in v1.2.0.

**Key Decision:** Implement a **CachingGeoSource** wrapper that sits between the data layer (GeoSource) and rendering layer, with cache invalidation keyed by projection parameters. This preserves the existing API while adding performance optimizations behind the scenes.

---

## Current Architecture Baseline

### Existing Layer Separation

```
┌─────────────────────────────────────────────────────────────┐
│  RENDERING LAYER                                            │
│  • Drawer extensions (drawer.geo(), drawer.geoJSON())       │
│  • Individual renderers (drawPoint, drawLineString, etc.)   │
│  • Style resolution chain (per-feature → by-type → global)  │
└──────────────────────┬──────────────────────────────────────┘
                       │ projects coordinates
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  PROJECTION LAYER                                           │
│  • GeoProjection interface                                  │
│  • Real-time projection: geometry.toScreen(projection)      │
│  • CRS transformations via CRSTransformer                   │
└──────────────────────┬──────────────────────────────────────┘
                       │ provides projected geometry
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  DATA LAYER                                                 │
│  • GeoSource abstract class                                 │
│  • Lazy Sequence<Feature> for memory efficiency             │
│  • withProjection() for per-feature projection              │
└─────────────────────────────────────────────────────────────┘
```

### Current Data Flow (Per Frame)

```kotlin
// Current pattern - projection happens every frame
source.features.forEach { feature ->           // 1. Iterate features
    val screen = feature.geometry               // 2. Get geometry
        .toScreen(projection)                   // 3. Project (every frame!)
    drawPoint(drawer, screen, style)           // 4. Render
}
```

**Performance Issue:** Every coordinate is projected every frame, even when:
- Projection hasn't changed
- Geometry hasn't changed
- Only style/animation properties changed

---

## Recommended Architecture

### New Component: `CachingGeoSource`

A wrapper around GeoSource that maintains a projection cache with invalidation semantics.

```
┌─────────────────────────────────────────────────────────────┐
│  RENDERING LAYER                                            │
│  • Same API as before                                       │
│  • Optionally receives pre-projected geometries             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  CACHING LAYER (NEW)                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ CachingGeoSource                                    │   │
│  │ • Cache: Map<FeatureId, ProjectedGeometry>          │   │
│  │ • CacheKey: projection + viewport params            │   │
│  │ • Invalidation: on projection change                │   │
│  └─────────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────────┘
                       │ delegates to
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  DATA LAYER (unchanged)                                     │
│  • GeoSource with lazy Sequence<Feature>                    │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Integration Point |
|-----------|---------------|-------------------|
| `CachingGeoSource` | Cache projected geometries, invalidation logic | Wraps any GeoSource |
| `ProjectionCacheKey` | Immutable key for cache entries | Based on projection config |
| `BatchProjector` | Efficient batch coordinate transformation | Used internally by cache |
| `CacheStats` | Hit/miss metrics for performance tuning | Optional monitoring |

---

## Data Flow Changes

### Before (v1.2.0)

```
Every Frame:
  GeoSource.features ──► Feature.geometry ──► project() ──► render()
  (Sequence)              (WGS84 coords)      (real-time)   (OpenRNDR)
```

### After (v1.3.0 with caching)

```
First Frame:
  CachingGeoSource.features ──► Check cache ──► miss ──► BatchProjector.project() ──► cache ──► render()
  
Subsequent Frames (same projection):
  CachingGeoSource.features ──► Check cache ──► hit ──► cached ProjectedGeometry ──► render()
  
Projection Change:
  CachingGeoSource.features ──► cache invalidated ──► BatchProjector.project() ──► cache ──► render()
```

### Cache Invalidation Rules

| Event | Action | Rationale |
|-------|--------|-----------|
| Projection config changes | Invalidate all | Screen coordinates differ |
| Viewport size changes | Invalidate all | Scale/translation changes |
| New features added | Add to cache | Incremental update |
| Feature properties change | Keep cached | Geometry unchanged |
| Manual invalidate() called | Invalidate all | Force refresh |

---

## New Components Required

### 1. CachingGeoSource

```kotlin
/**
 * GeoSource wrapper that caches projected geometries.
 * Transparent to callers - implements same patterns as GeoSource.
 */
class CachingGeoSource(
    private val source: GeoSource,
    private val projection: GeoProjection
) : GeoSource(source.crs) {
    
    private val cache = mutableMapOf<String, ProjectedGeometry>()
    private var cacheKey: ProjectionCacheKey = ProjectionCacheKey(projection)
    
    override val features: Sequence<Feature>
        get() = source.features  // Pass-through for feature iteration
    
    /**
     * Get projected geometry from cache or compute.
     */
    fun getProjected(feature: Feature): ProjectedGeometry {
        val currentKey = ProjectionCacheKey(projection)
        if (currentKey != cacheKey) {
            cache.clear()
            cacheKey = currentKey
        }
        
        return cache.getOrPut(feature.id) {
            projectGeometry(feature.geometry, projection)
        }
    }
    
    /**
     * Force cache invalidation.
     */
    fun invalidate() {
        cache.clear()
        cacheKey = ProjectionCacheKey(projection)
    }
    
    val cacheStats: CacheStats
        get() = CacheStats(cache.size, /* hits, misses */)
}
```

### 2. BatchProjector

```kotlin
/**
 * Batch projection for efficient coordinate transformation.
 * Uses optimized bulk operations instead of per-point projection.
 */
object BatchProjector {
    /**
     * Project multiple points in a single batch.
     * More efficient than iterative projection for large datasets.
     */
    fun projectPoints(
        points: List<Vector2>,
        projection: GeoProjection
    ): List<Vector2> {
        // Optimization: projection may have internal batching
        return points.map { projection.project(it) }
    }
    
    /**
     * Project entire geometry hierarchy in batch.
     */
    fun projectGeometry(
        geometry: Geometry,
        projection: GeoProjection
    ): ProjectedGeometry = when (geometry) {
        is Point -> ProjectedPoint(projection.project(Vector2(geometry.x, geometry.y)))
        is LineString -> ProjectedLineString(
            projectPoints(geometry.points, projection)
        )
        is Polygon -> ProjectedPolygon(
            exterior = projectPoints(geometry.exterior, projection),
            holes = geometry.interiors.map { projectPoints(it, projection) }
        )
        // ... other geometry types
    }
}
```

### 3. ProjectionCacheKey

```kotlin
/**
 * Immutable key for cache entries.
 * Captures all projection parameters that affect screen coordinates.
 */
data class ProjectionCacheKey(
    val projectionType: String,
    val width: Double,
    val height: Double,
    val centerX: Double,
    val centerY: Double,
    val scale: Double
) {
    companion object {
        fun from(projection: GeoProjection): ProjectionCacheKey {
            // Extract parameters from projection
            // Implementation depends on projection internals
        }
    }
}
```

### 4. CacheStats (for performance monitoring)

```kotlin
/**
 * Performance metrics for cache tuning.
 */
data class CacheStats(
    val cachedEntries: Int,
    val hitCount: Long,
    val missCount: Long,
    val invalidationCount: Long
) {
    val hitRate: Double
        get() = if (hitCount + missCount > 0) {
            hitCount.toDouble() / (hitCount + missCount)
        } else 0.0
}
```

---

## Integration Points

### With Existing Code

| Existing Component | Integration | Change Type |
|-------------------|-------------|-------------|
| `GeoSource` | `CachingGeoSource` wraps any GeoSource | New wrapper |
| `withProjection()` | Returns cached projection if available | Modified |
| `Drawer.geo()` | Optionally uses CachingGeoSource internally | Modified |
| `Geometry.toScreen()` | No change - point projection still works | Unchanged |
| `ProjectedGeometry` | Used as cache value type | Unchanged |

### API Compatibility

```kotlin
// Existing code continues to work (no caching)
source.features.forEach { feature ->
    val screen = feature.geometry.toScreen(projection)
    drawPoint(drawer, screen, style)
}

// New opt-in caching API
val cached = source.cached(projection)
cached.features.forEach { feature ->
    val projected = cached.getProjected(feature)  // From cache
    drawProjected(drawer, projected, style)
}

// Or use higher-level API
drawer.geo(source) {
    this.projection = projection
    cache = true  // New option
}
```

---

## Build Order (Dependencies)

### Phase 1: Foundation (Week 1)

**Goal:** Batch projection infrastructure

1. **BatchProjector** 
   - Batch coordinate transformation
   - Geometry hierarchy projection
   - Tests for correctness

2. **ProjectionCacheKey**
   - Immutable key generation
   - Captures all projection params
   - Hash/equals for Map keys

**Dependencies:** None (uses existing projection API)

**Outputs:**
- `geo.cache.BatchProjector`
- `geo.cache.ProjectionCacheKey`
- Unit tests

### Phase 2: Caching Layer (Week 2)

**Goal:** CachingGeoSource implementation

1. **CachingGeoSource**
   - Wrap GeoSource
   - Cache with invalidation
   - CacheStats monitoring

2. **Integration with withProjection()**
   - Optional caching path
   - Backwards compatible

**Dependencies:** Phase 1 components

**Outputs:**
- `geo.cache.CachingGeoSource`
- `geo.cache.CacheStats`
- Integration tests

### Phase 3: Rendering Integration (Week 3)

**Goal:** Higher-level API integration

1. **Drawer extension updates**
   - Optional `cache = true` parameter
   - Automatic CachingGeoSource wrapping

2. **Performance benchmarks**
   - Before/after measurements
   - Cache hit rate reporting

**Dependencies:** Phase 2

**Outputs:**
- Updated `Drawer.geo()` with caching option
- Performance benchmark suite
- Example: `perf_CachingDemo.kt`

### Phase 4: Optimization (Week 4)

**Goal:** Advanced cache strategies

1. **LRU eviction** (optional)
2. **Memory-bounded cache**
3. **Multi-level cache** (feature-level + geometry-level)

**Dependencies:** Phase 3

**Outputs:**
- Advanced cache strategies
- Memory profiling
- Documentation

---

## Data Flow Diagrams

### Detailed Cache Flow

```
┌────────────────────────────────────────────────────────────────────┐
│ Render Loop (extend block)                                         │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│ drawer.geo(source) { cache = true }                                │
│ • Checks if source is CachingGeoSource                             │
│ • Wraps if needed                                                  │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│ CachingGeoSource.withProjection(projection)                        │
│ • Create cache key from projection                                 │
│ • Check: key matches current?                                      │
│   ├── NO: Clear cache, update key                                  │
│   └── YES: Continue                                                │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│ For each feature in source.features:                               │
│ • Check cache[feature.id]                                          │
│   ├── HIT: Return cached ProjectedGeometry                         │
│   └── MISS: BatchProjector.project() → Store → Return              │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│ Render with cached ProjectedGeometry                               │
│ • Skip projection step                                             │
│ • Direct to OpenRNDR drawing                                       │
└────────────────────────────────────────────────────────────────────┘
```

### Cache Invalidation Flow

```
┌────────────────────────────────────────────────────────────────────┐
│ Projection Change Detected                                         │
│ (width, height, center, scale changed)                             │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────┐
│ CachingGeoSource.getProjected()                                    │
│ • New ProjectionCacheKey(projection)                               │
│ • Compare with stored key                                          │
│   ├── EQUAL: Use cache                                             │
│   └── NOT EQUAL: Invalidate and reproject                          │
└────────────────────────────────────────────────────────────────────┘
                                │
                                ▼ (if not equal)
┌────────────────────────────────────────────────────────────────────┐
│ cache.clear()                                                      │
│ storedKey = newKey                                                 │
│ Reproject all visible features                                     │
└────────────────────────────────────────────────────────────────────┘
```

---

## Memory Considerations

### Cache Size Estimation

```
Per geometry memory:
- Point: ~24 bytes (Vector2)
- LineString (100 pts): ~2,400 bytes  
- Polygon (ext 100 pts + 1 hole 50 pts): ~3,600 bytes

For 10K features:
- Average 500 bytes/feature = 5MB cache
- Acceptable for desktop JVM (available: GBs)
```

### Memory Management Strategy

1. **Default:** Unbounded cache (for v1.3.0)
   - Simple, predictable
   - Suitable for prototyping workloads

2. **Future:** Bounded cache with LRU eviction
   - Configurable max size
   - Eviction callbacks

### Lazy Sequence Compatibility

The cache preserves lazy evaluation:

```kotlin
// Features are still lazy at the source level
source.features  // Sequence<Feature> - not loaded yet
    .filter { it.boundingBox.intersects(viewport) }  // Spatial filter
    .map { cached.getProjected(it) }  // Cache lookup (or project)
    .forEach { render(it) }
```

Only features that pass through the pipeline are cached, maintaining memory efficiency for large datasets.

---

## Performance Expectations

### Scenarios

| Scenario | Without Cache | With Cache | Improvement |
|----------|--------------|------------|-------------|
| Static map, 60fps | Project every frame | Cache hit every frame | ~10-50x |
| Animation (zoom) | Project every frame | Invalidate + project | ~1x (expected) |
| Pan without zoom | Project every frame | Reuse cache | ~10-50x |
| Style-only animation | Project every frame | Cache hit | ~10-50x |

### Cache Hit Rate Targets

- **Animation with fixed projection:** >95%
- **Pan without zoom change:** >95%
- **Zoom animation:** 0% (intentional invalidation)

---

## Anti-Patterns to Avoid

### ❌ Don't: Cache at wrong granularity

```kotlin
// BAD: Caching raw coordinates loses type information
val cache = mutableMapOf<String, List<Vector2>>()  // Don't do this
```

### ✅ Do: Cache ProjectedGeometry

```kotlin
// GOOD: Preserves geometry type for type-safe rendering
val cache = mutableMapOf<String, ProjectedGeometry>()
```

### ❌ Don't: Cache unbounded without consideration

```kotlin
// BAD: For 1M features, this is 500MB+
val cache = mutableMapOf<String, ProjectedGeometry>()  // Unbounded
```

### ✅ Do: Consider spatial indexing for large datasets

```kotlin
// GOOD: Only cache visible features
source.featuresInBounds(viewport)
    .map { cache.getOrPut(it.id) { project(it) } }
```

### ❌ Don't: Mix caching with mutation

```kotlin
// BAD: Mutating cached geometry corrupts cache
val projected = cache[feature.id]
projected.screenPoints[0] = Vector2.ZERO  // Mutates cache!
```

### ✅ Do: Immutable cache values

```kotlin
// GOOD: ProjectedGeometry uses immutable Lists
data class ProjectedLineString(
    override val screenPoints: List<Vector2>  // Immutable
) : ProjectedGeometry()
```

---

## Testing Strategy

### Unit Tests

```kotlin
@Test
fun `cache returns same projection for same key`() {
    val cache = CachingGeoSource(source, projection)
    val p1 = cache.getProjected(feature)
    val p2 = cache.getProjected(feature)
    assertSame(p1, p2)  // Same instance
}

@Test
fun `cache invalidates on projection change`() {
    val cache = CachingGeoSource(source, projection)
    val p1 = cache.getProjected(feature)
    
    cache.updateProjection(newProjection)
    val p2 = cache.getProjected(feature)
    
    assertNotSame(p1, p2)  // Different instance
}
```

### Integration Tests

```kotlin
@Test
fun `caching improves performance`() {
    val uncachedTime = measureTime {
        repeat(100) { renderWithoutCache() }
    }
    val cachedTime = measureTime {
        repeat(100) { renderWithCache() }
    }
    assertTrue(cachedTime < uncachedTime / 10)
}
```

---

## Summary

| Aspect | Decision |
|--------|----------|
| Cache Location | `CachingGeoSource` wrapper between data/rendering |
| Cache Key | `ProjectionCacheKey` capturing all projection params |
| Invalidation | Automatic on projection change, manual option |
| API Impact | Opt-in via `cache = true`, backwards compatible |
| Memory Strategy | Unbounded for v1.3.0, bounded for future |
| Build Order | BatchProjector → CachingGeoSource → Rendering integration |

**Confidence Level:** HIGH
- Pattern is established (wrapper/proxy pattern)
- Kotlin collections provide efficient Map-based caching
- Existing ProjectedGeometry types are immutable
- Lazy Sequence pattern is preserved

**Risk Areas:**
- Feature ID stability (need consistent ID generation)
- Memory pressure with very large datasets (mitigation: spatial filtering)
- Thread safety (OpenRNDR is single-threaded for rendering)

---

## Sources

- Existing codebase analysis (Geometry.kt, GeoSource.kt, Feature.kt)
- Kotlin best practices (immutable data classes, sealed classes)
- Cache design patterns (wrapper invalidation strategy)
- OpenRNDR rendering lifecycle (single-threaded extend block)

---
*Architecture research for: openrndr-geo geospatial visualization library*
*Researched: 2026-02-21 (general), 2026-02-26 (v1.2.0 integration), 2026-03-05 (v1.3.0 performance)*
