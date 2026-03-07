# Phase 15: API Ergonomics — Reduce Boilerplate - Research

**Phase:** 15-api-ergonomics-reduce-boilerplate  
**Research Date:** 2026-03-07  
**Status:** Ready for implementation planning

---

## User Constraints

From `15-CONTEXT.md`:

### Import Structure
- **Tiered by domain** — `geo.*` (essentials), `geo.data.*`, `geo.projection.*`, `geo.render.*`, `geo.animation.*`, `geo.layer.*`
- **Noun-oriented naming** — Matches OpenRNDR's pattern (`org.openrndr.draw`, `org.openrndr.color`)
- **`geo.*` minimal** — Only `GeoSource` and basic projection helpers exposed via wildcard
- **`drawer.geo()` moves to `geo.render.*`** — Explicit import required for rendering operations

### Streamlined API Design
- **Two function approach:**
  - `geoSource()` — Raw/explicit control (manual projection, no automation)
  - `loadGeo()` — Auto-magic helper (auto-CRS, auto-projection, auto-fit, caching)
- **Data-centric workflow** — `loadGeo()` → `project()` → `drawer.geo()` (or auto versions)
- **Three styling options:**
  1. Default styling (built-in conventions)
  2. Inline style block: `drawer.geo(source) { stroke = Color.BLUE }`
  3. Separate styling step: `val styled = source.style { ... }`

### Backward Compatibility
- **Hard break** — No deprecation period, no aliases, clean migration
- **All 26 examples updated** — Convert everything to new API in this phase
- **No compatibility layer** — IDE refactoring handles import updates

### Convention vs Configuration
- **`loadGeo()` auto-everything:**
  - Auto-CRS detection (WGS84 fallback)
  - Auto-projection to fit viewport
  - Tight fit: 100% viewport (data touches edges)
  - Silent CRS transformation when needed
  - Behind-the-scenes caching
- **Default styles:**
  - White lines
  - Red fills
  - Thin stroke (1.5)
  - Points as circles (r=5)
  - PolygonRenderer uses boolean methods (current pattern)

---

## Standard Stack

**Current Stack (from STATE.md):**
- Kotlin/JVM (1.9+)
- OPENRNDR 0.4.5
- Simple `MutableMap` caching (no external libraries)

**Package Structure (Current):**
```
geo/
├── GeoSource.kt (abstract base class)
├── GeoStack.kt (multi-source overlay)
├── Geometry.kt (sealed class hierarchy)
├── GeoJSON.kt, GeoPackage.kt (data loaders)
├── Feature.kt, Bounds.kt
├── ProjectionExtensions.kt
├── GeoSourceConvenience.kt (geoSource() functions)
├── crs/
│   └── CRS.kt
├── projection/
│   ├── GeoProjection.kt (interface)
│   ├── ProjectionMercator.kt, ProjectionEquirectangular.kt, ProjectionBNG.kt
│   ├── ProjectionConfig.kt, ProjectionFactory.kt
│   └── CRSExtensions.kt, CRSTransformer.kt
├── render/
│   ├── DrawerGeoExtensions.kt (drawer.geo(), drawer.geoJSON())
│   ├── Style.kt, StyleDefaults.kt
│   ├── PointRenderer.kt, LineRenderer.kt, PolygonRenderer.kt
│   ├── MultiRenderer.kt, GeoRenderConfig.kt
│   └── Shape.kt
├── animation/
│   ├── GeoAnimator.kt, FeatureAnimator.kt
│   ├── Tweening.kt, ProceduralMotion.kt, EasingExtensions.kt
│   └── composition/
│       ├── ChainedAnimation.kt, GeoTimeline.kt
│       └── interpolators/
│           ├── LinearInterpolator.kt, HaversineInterpolator.kt
├── layer/
│   ├── GeoLayer.kt, Graticule.kt
│   └── BlendModes.kt (used by examples)
└── internal/
    ├── OptimizedGeoSource.kt
    ├── batch/ (CoordinateBatch, BatchProjectionUtils)
    ├── cache/ (ViewportCache, CacheKey, ViewportState)
    └── geometry/ (OptimizedGeometries)
```

**Current Import Patterns:**
```kotlin
// Essential data types (currently in geo.*)
import geo.GeoSource
import geo.GeoJSON
import geo.Point, geo.LineString, geo.Polygon

// Rendering (currently in geo.render.*)
import geo.render.drawPoint, geo.render.drawLineString, geo.render.drawPolygon
import geo.render.Style

// Projections (currently in geo.projection.*)
import geo.projection.ProjectionMercator
import geo.projection.ProjectionFactory

// Animation (currently in geo.animation.*)
import geo.animation.GeoAnimator
```

---

## Architecture Patterns

### 1. Tiered/Wildcard Exports Pattern

**Standard Kotlin Library Pattern:**
Kotlin uses explicit exports rather than wildcard re-exports. Examples from stdlib:
```kotlin
// kotlin.collections package exports:
// - List, Map, Set interfaces
// - ArrayList, HashMap, HashSet implementations
// - listOf(), mapOf(), setOf() factory functions

// kotlin.io package exports:
// - File, Path abstractions
// - readText(), writeText() extensions
```

**OPENRNDR Pattern (from which this library derives):**
```kotlin
// org.openrndr.draw package:
import org.openrndr.draw.Drawer           // Core class
import org.openrndr.draw.ColorRGBa        // Core type
import org.openrndr.draw.renderTarget     // Factory function

// org.openrndr.color package (separate import for advanced color):
import org.openrndr.color.ColorOKLab      // Specialized
import org.openrndr.color.hsl             // Specialized factory
```

**Proposed Structure for openrndr-geo:**
```kotlin
// geo.* - Core essentials (wildcard import gets you started)
import geo.*                              // GeoSource, GeoJSON, loadGeo()
import geo.projection.*                   // Projections, transform()
import geo.render.*                       // drawer.geo(), Style

// Domain-specific for advanced use:
import geo.animation.*                    // GeoAnimator, tweens
import geo.layer.*                        // Graticule, composition layers
```

### 2. Sealed Class Hierarchies Across Packages

**Current State:**
- `Geometry` sealed class is in `geo` package
- All implementations (`Point`, `LineString`, `Polygon`, etc.) are in same file
- This is **required** by Kotlin: sealed class implementations must be in the same module

**Research Finding:**
Kotlin sealed classes have strict visibility rules:
- Subclasses must be declared in the same package
- They can be in separate files but same package
- They cannot be split across different packages

**Implication for Phase 15:**
- Keep `Geometry` and all implementations in `geo` package (not `geo.data`)
- `geo.*` wildcard will include all geometry types — this is correct
- Domain packages (`geo.render`, `geo.projection`) will import from `geo`

### 3. Domain Package Structure

**Proposed New Structure:**
```
geo/
├── *.kt (essentials: Geometry, GeoSource, Feature, Bounds, GeoJSON, GeoPackage)
├── loadGeo.kt (new: auto-magic loader with caching)
├── project.kt (new: simplified projection helpers)
├── data/
│   └── (empty or re-exports - see decision below)
├── projection/
│   ├── *.kt (existing projection classes)
│   └── project.kt (new: simplified project() function)
├── render/
│   ├── *.kt (existing renderers)
│   └── drawerExtensions.kt (consolidated Drawer.geo() extensions)
├── animation/
│   └── *.kt (existing animation classes)
├── layer/
│   └── *.kt (existing layer classes)
└── crs/
    └── CRS.kt (existing CRS enum)
```

**Decision: `geo.data.*` Package**

Option A: Create `geo.data.*` with re-exports
```kotlin
// geo/data/package.kt
package geo.data

// Re-export core data types
@file:Suppress("NOTHING_TO_INLINE")
inline fun geoSource(path: String) = geo.geoSource(path)
inline fun loadGeo(path: String) = geo.loadGeo(path)
typealias GeoSource = geo.GeoSource
// ... etc
```

Option B: Keep data types in `geo.*`, use `geo.data.*` only if needed later
- Simpler, matches current structure
- `geo.*` is already data-focused

**Recommendation: Option B** — Keep data types in `geo.*` root. The user wants minimal `geo.*` wildcard, not a separate data package. Moving everything to `geo.data` would require massive import changes with little benefit.

### 4. Drawer Extension Patterns in OpenRNDR

**Current OPENRNDR Pattern:**
```kotlin
// Drawer extensions in org.openrndr.draw
fun Drawer.rectangle(rect: Rectangle) { ... }
fun Drawer.circle(circle: Circle) { ... }
fun Drawer.lineSegment(line: LineSegment) { ... }
```

**Current openrndr-geo Pattern:**
```kotlin
// In geo/render/DrawerGeoExtensions.kt
fun Drawer.geoJSON(path: String, projection: GeoProjection?, style: Style?)
fun Drawer.geoSource(source: GeoJSONSource, ...)
fun Drawer.geo(geometry: Geometry, ...)
fun Drawer.geo(source: GeoSource, block: (GeoRenderConfig.() -> Unit)?)
fun Drawer.geoFeatures(features: Sequence<Feature>, ...)
```

**Phase 15 Enhancement:**
```kotlin
// Simplified with inline style configuration
fun Drawer.geo(source: GeoSource, block: Style.() -> Unit = {}) {
    val style = Style().apply(block)
    // ... render with style
}

// Usage:
drawer.geo(source) {
    stroke = ColorRGBa.BLUE
    strokeWeight = 2.0
}
```

### 5. Caching Integration Pattern

**Current Implementation:**
- `OptimizedGeoSource` uses `CoordinateBatch` for batch projection
- `ViewportCache` uses `MutableMap<CacheKey, Array<Vector2>>`
- Cache key = geometry identity + viewport state

**Phase 15 Auto-Caching:**
```kotlin
// loadGeo() returns GeoSource with internal caching
fun loadGeo(path: String): GeoSource {
    val source = geoSource(path)
    return CachedGeoSource(source)  // wraps with caching
}

// CachedGeoSource internal implementation
internal class CachedGeoSource(
    private val delegate: GeoSource
) : GeoSource(delegate.crs) {
    private val viewportCache = ViewportCache()
    
    override val features: Sequence<Feature>
        get() = delegate.features  // Passthrough
    
    // Caching happens at render time via drawer.geo()
}
```

---

## Don't Hand-Roll

**Use Existing Infrastructure:**

1. **Viewport Caching** (`internal/cache/ViewportCache.kt`)
   - Already implemented and tested
   - Use for `loadGeo()` behind-the-scenes caching
   - Don't create new cache mechanism

2. **Batch Projection** (`internal/batch/`)
   - `CoordinateBatch` for efficient DoubleArray storage
   - `batchProject()` for zero-allocation projection loops
   - Use in optimized rendering paths

3. **Style System** (`render/Style.kt`, `render/StyleDefaults.kt`)
   - Already has mutable Style class
   - Already has defaults for all geometry types
   - Extend with DSL-style configuration block

4. **Projection Factory** (`projection/ProjectionFactory.kt`)
   - `fitBounds()` already auto-fits to viewport
   - Use for `loadGeo()` auto-projection

5. **CRS Detection** (`crs/CRS.kt`)
   - Already has CRS enum with detection logic
   - Use for `loadGeo()` auto-CRS

6. **Optimized Geometries** (`internal/geometry/OptimizedGeometries.kt`)
   - Already converts Geometry to batch-friendly format
   - Use in `loadGeo()` for performance

---

## Common Pitfalls

### 1. Hard Break Migration

**Risk:** All 26 examples break simultaneously

**Mitigation:**
- Create migration script with find/replace patterns
- Test each example individually after conversion
- Keep backup of original examples during development

**Common Import Mappings:**
```
// Old → New
import geo.render.drawPoint → import geo.render.*
import geo.projection.ProjectionMercator → import geo.projection.*
import geo.animation.GeoAnimator → import geo.animation.*
```

### 2. Sealed Class Package Restrictions

**Risk:** Cannot move `Geometry` subclasses to `geo.data.*`

**Decision:** Keep `Geometry` and all implementations in `geo` package root. Do NOT create `geo.data` package for geometry types.

### 3. Drawer Extension Conflicts

**Risk:** `drawer.geo()` overloads may conflict

**Current Overloads:**
- `Drawer.geo(geometry: Geometry, ...)`
- `Drawer.geo(source: GeoSource, ...)`
- `Drawer.geo(source: GeoSource, block: ...)` ← Phase 15 addition

**Resolution:** Use distinct signatures with default parameters

### 4. Caching Scope

**Risk:** `loadGeo()` caching may cause memory leaks

**Current Safeguards:**
- `ViewportCache` has `MAX_CACHE_ENTRIES = 1000`
- Uses identity equality (fast, no hashing)
- Clears on viewport state change

**Phase 15 Addition:**
- `loadGeo()` returns `CachedGeoSource` with bounded cache
- Consider adding `clearCache()` method for long-running apps

### 5. Style Block Ambiguity

**Risk:** `drawer.geo(source) { }` could be confused with `GeoRenderConfig`

**Current:** `drawer.geo(source) { projection = ...; style = ... }`
**Phase 15:** `drawer.geo(source) { stroke = ...; fill = ... }`

**Resolution:** 
- Keep `GeoRenderConfig` for advanced use (projection + styleByType)
- Add `Style.() -> Unit` overload for simple styling
- Use different function names if needed:
  - `drawer.geo(source, config = { ... })` for GeoRenderConfig
  - `drawer.geo(source) { ... }` for Style DSL

### 6. Auto-CRS Edge Cases

**Risk:** `loadGeo()` auto-CRS detection may fail

**Current CRS Sources:**
- GeoJSON: `crs` member or default to WGS84
- GeoPackage: internal CRS metadata

**Fallback Strategy:**
- Detect from file metadata where possible
- Default to WGS84 (EPSG:4326) when unknown
- Print warning: "Unknown CRS, assuming WGS84. Use geoSource() for explicit control."

---

## Code Examples

### Example 1: New Three-Line Workflow

```kotlin
import geo.*
import geo.projection.*
import geo.render.*
import org.openrndr.application

fun main() = application {
    configure { width = 800; height = 600 }
    
    program {
        // Line 1: Load with auto-everything
        val data = loadGeo("data/world.json")
        
        // Line 2: Auto-project to fit viewport
        val projection = data.projectToFit(width, height)
        
        extend {
            // Line 3: Render with optional inline style
            drawer.geo(data, projection) {
                stroke = ColorRGBa.WHITE
                fill = ColorRGBa.RED
            }
        }
    }
}
```

### Example 2: Two-Function Approach (Explicit vs Auto)

```kotlin
// Option A: Explicit control (geoSource)
val source = geoSource("data.json")  // No automation
val projection = ProjectionMercator { 
    width = 800.0; height = 600.0 
}
source.transform(CRS.WebMercator)     // Manual CRS
    .render(drawer, projection)       // Manual projection

// Option B: Auto-magic (loadGeo)
val data = loadGeo("data.json")        // Auto-CRS, auto-cache
// Auto-projection in drawer.geo()
drawer.geo(data) {                     // Auto-fits to viewport
    stroke = ColorRGBa.BLUE
}
```

### Example 3: Style Configuration Patterns

```kotlin
// Pattern 1: Default styling
drawer.geo(data)

// Pattern 2: Inline style block
drawer.geo(data) {
    stroke = ColorRGBa.WHITE
    strokeWeight = 1.5
    fill = ColorRGBa.RED
    size = 5.0  // For points
}

// Pattern 3: Separate styling step
val styled = data.style {
    stroke = ColorRGBa.YELLOW
    fill = ColorRGBa.TRANSPARENT
}
drawer.geo(styled)

// Pattern 4: Type-based styling (advanced)
drawer.geo(data) {
    styleByType = mapOf(
        "Polygon" to Style { fill = ColorRGBa.BLUE },
        "LineString" to Style { stroke = ColorRGBa.GREEN }
    )
}
```

### Example 4: Tiered Import Examples

```kotlin
// === BEGINNER: Everything wildcard ===
import geo.*
import geo.projection.*
import geo.render.*

val data = loadGeo("world.json")
drawer.geo(data) { stroke = ColorRGBa.WHITE }

// === INTERMEDIATE: Pull in specific domains ===
import geo.*                              // Core data
import geo.render.*                        // Rendering
import geo.animation.*                      // Animation

val data = loadGeo("world.json")
val animator = GeoAnimator()

// === ADVANCED: Explicit imports only ===
import geo.GeoSource
import geo.geoSource
import geo.render.Style
import geo.render.drawPolygon
import geo.projection.ProjectionMercator
import geo.animation.GeoAnimator
import geo.animation.easeInOutCubic

// Full control over every component
```

### Example 5: Hard Break Migration

```kotlin
// BEFORE (Old API)
import geo.GeoJSON
import geo.render.drawPolygon
import geo.projection.ProjectionMercator

val source = GeoJSON.load("data.json")
val projection = ProjectionMercator { width = 800.0; height = 600.0 }
extend {
    source.features.forEach { feature ->
        if (feature.geometry is Polygon) {
            drawPolygon(drawer, feature.geometry.exteriorToScreen(projection))
        }
    }
}

// AFTER (New API)
import geo.*
import geo.projection.*
import geo.render.*

val data = loadGeo("data.json")
extend {
    drawer.geo(data) {  // Auto-projection, auto-render
        stroke = ColorRGBa.WHITE
    }
}
```

---

## Confidence Levels

| Research Area | Confidence | Notes |
|--------------|------------|-------|
| Tiered package structure | **High** | Matches Kotlin conventions and OpenRNDR patterns |
| Sealed class package rules | **High** | Kotlin compiler enforces this — cannot change |
| Drawer extension patterns | **High** | Existing implementation is solid base |
| Two-function API (geoSource/loadGeo) | **High** | Clear distinction serves different use cases |
| Inline style configuration | **Medium-High** | Existing Style class supports this, needs testing |
| Hard break migration | **Medium** | Risky but manageable with good tooling |
| Caching integration | **High** | ViewportCache already proven in v1.3.0 |
| Auto-CRS detection | **Medium** | Depends on file format support |
| Default style values | **High** | StyleDefaults already defined |

### Open Questions for Implementation

1. **Should `loadGeo()` return a new type or wrapped GeoSource?**
   - Option: `CachedGeoSource` wrapper (transparent)
   - Option: Add caching to base `GeoSource` interface
   - **Tendency:** Wrapper keeps concerns separate

2. **How to handle `data.style { }` separate styling step?**
   - Option: Return `StyledGeoSource` wrapper
   - Option: Add style property to GeoSource
   - **Tendency:** Wrapper pattern keeps GeoSource focused on data

3. **Migration tool approach?**
   - Option: Kotlin compiler plugin (overkill)
   - Option: IDE find/replace templates
   - Option: Simple sed/bash script
   - **Tendency:** IDE templates + documentation

---

## RESEARCH COMPLETE

**Summary:**
- Phase 15 will introduce tiered imports (`geo.*`, `geo.render.*`, etc.)
- Two-function API: `geoSource()` (explicit) and `loadGeo()` (auto)
- Style configuration via inline DSL: `drawer.geo(source) { stroke = BLUE }`
- Hard break requires updating all 26 examples but simplifies maintenance
- Sealed class Geometry stays in `geo` package (cannot move to subpackage)
- Leverage existing infrastructure: ViewportCache, Style, ProjectionFactory

**Next Steps:**
1. Create implementation plan (P01, P02, etc.)
2. Implement new API structure
3. Migrate examples
4. Update documentation

**Files to Modify:**
- Create `loadGeo.kt` with auto-magic loader
- Create `project.kt` with simplified projection helpers
- Modify `DrawerGeoExtensions.kt` with new style DSL
- Update all 26 examples in `geo/examples/`
- Update README.md with new API patterns
