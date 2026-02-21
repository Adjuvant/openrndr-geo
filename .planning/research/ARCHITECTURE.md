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
*Architecture research for: openrndr-geo geospatial visualization library*
*Researched: 2026-02-21*
