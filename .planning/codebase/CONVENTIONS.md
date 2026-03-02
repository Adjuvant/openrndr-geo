# Coding Conventions

**Analysis Date:** 2026-03-02

## Naming Patterns

**Files:**
- PascalCase for source files: `GeoSource.kt`, `GeoAnimator.kt`, `Feature.kt`
- PascalCase with descriptive suffixes for example files: `layer_BlendModes.kt`, `render_BasicRendering.kt`
- kebab-case for Gradle config: `build.gradle.kts`, `settings.gradle.kts`
- kebab-case for version catalog: `libs.versions.toml`

**Functions:**
- camelCase for function names: `filterFeatures()`, `totalBoundingBox()`, `toScreen()`
- Top-level functions for public API: `fun main() = application {}`
- Extension functions for feature enhancement: `fun Sequence<ProjectedFeature>.forEachWithProjection()`
- DSL-style factory functions: `geoStack()`, `geoSource()`

**Variables:**
- camelCase for all variables and properties
- Examples: `features`, `projection`, `boundingBox`, `animator`
- Top-level constants use camelCase: `MAX_MERCATOR_COORD`
- Private helper constants: `MAX_MERCATOR_LAT` (in projection packages)

**Types:**
- PascalCase for classes, interfaces, and enums: `GeoSource`, `Feature`, `Geometry`
- Sealed class hierarchies for type-safe polymorphism:
  ```kotlin
  sealed class Geometry
  data class Point(val x: Double, val y: Double) : Geometry()
  data class LineString(val points: List<Vector2>) : Geometry()
  ```
- Data classes for value types: `data class Feature(val geometry: Geometry, val properties: Map<String, Any?>)`
- Type aliases not heavily used

## Code Style

**Formatting:**
- Kotlin official code style enforced via `kotlin.code.style=official` in `gradle.properties`
- Indentation: 4 spaces (Kotlin standard)
- Maximum line length: ~120 characters (observed in practice)
- No additional linting tools configured (detekt, ktlint not present)

**Bracing:**
- Opening brace on same line: `fun foo() {`
- Trailing lambda syntax for DSL blocks:
  ```kotlin
  drawer.geo(topo) {
      this.projection = topoProjection
      styleByType = mapOf(...)
  }
  ```

**Expression Bodies:**
- Single-expression functions use `=` syntax:
  ```kotlin
  fun property(key: String): Any? = properties[key]
  fun isEmpty(): Boolean = !features.any()
  ```

## Import Organization

**Order:**
1. Kotlin standard library imports: `kotlin.math.cos`, `kotlin.math.sin`
2. External library imports (OPENRNDR, kotlinx): `org.openrndr.application`, `org.openrndr.math.Vector2`
3. Internal project imports: `geo.*`, `geo.render.*`

**Pattern observed in `src/main/kotlin/App.kt`:**
```kotlin
import geo.GeoPackage
import geo.GeoSource
import geo.animation.animator
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import kotlin.math.cos
import kotlin.math.sin
```

**Path Aliases:** Not configured

**Wildcard Imports:** Not used; explicit imports preferred

## Package Structure

**Base Package:** `geo`

**Subpackages by Concern:**
- `geo` - Core types: `GeoSource`, `Feature`, `Geometry`, `Bounds`
- `geo.crs` - Coordinate reference system definitions
- `geo.projection` - Projection algorithms and transformations
- `geo.projection.internal` - Internal projection implementations
- `geo.render` - Rendering system: `drawLineString()`, `drawPolygon()`, `Style`
- `geo.animation` - Animation framework: `GeoAnimator`, `Tweening`
- `geo.animation.composition` - Animation composition: `ChainedAnimation`, `GeoTimeline`
- `geo.animation.interpolators` - Interpolation strategies
- `geo.layer` - Layer system: `GeoLayer`, `Graticule`
- `geo.examples` - Example programs organized by category

**Example Organization:**
- `examples/core/` - Core functionality examples
- `examples/anim/` - Animation examples
- `examples/layer/` - Layer composition examples

## Error Handling

**Patterns:**
- `require()` blocks for preconditions in constructors:
  ```kotlin
  init {
      require(points.size >= 2) { "LineString must have at least 2 points" }
  }
  ```

- `error()` function for configuration failures:
  ```kotlin
  version = property("project.version") ?: error("project.version not set")
  ```

- Graceful degradation with warnings:
  ```kotlin
  if (to.isUnknown()) {
      println("Warning: Cannot transform to unknown CRS. Keeping original CRS.")
      return this
  }
  ```

**Null Safety:**
- Kotlin null safety features used throughout
- Safe cast operator: `properties[key] as? T`
- Elvis operator for defaults: `propertyTypes[key] ?: 0`
- `setOfNotNull()` for filtering nulls

## Logging

**Framework:** `kotlin-logging` with SLF4J

**Dependencies:**
```kotlin
implementation(libs.kotlin.logging)
implementation(libs.slf4j.api)
runtimeOnly(libs.bundles.logging.simple)
```

**Usage Pattern:**
- Console output via `println()` for user-facing summaries:
  ```kotlin
  fun printSummary() {
      println("┌${"─".repeat(52)}┐")
      println("│ ${"GeoSource Summary".center(50)} │")
      // ...
  }
  ```
- Warnings for recoverable errors (see Error Handling section)

**When to Log:**
- Application-level summaries and diagnostics
- NOT per-frame in render loops (performance critical)

## Comments

**When to Comment:**
- KDoc for all public API elements (classes, functions, properties)
- Inline comments for complex algorithms or business logic
- Section markers for large files:
  ```kotlin
  // ============================================================================
  // printSummary() Helper Functions
  // ============================================================================
  ```

**KDoc Style:**
```kotlin
/**
 * Abstract base class for all geospatial data sources.
 * GeoSource provides a unified interface for accessing features from
 * different data formats (GeoJSON, GeoPackage, etc.).
 *
 * @property crs The Coordinate Reference System identifier (default: "EPSG:4326" for WGS84)
 */
```

**KDoc with Usage Examples:**
```kotlin
/**
 * Transforms this GeoSource to a different CRS using the strongly-typed CRS enum.
 *
 * ## Usage
 * ```kotlin
 * val source = geoSource("data.json")
 * val webMercator = source.transform(to = CRS.WebMercator)
 * ```
 *
 * @param to The target CRS
 * @return A GeoSource in the target CRS (same instance if CRS matches)
 */
```

**TODO Comments:**
- Used for tracking known issues and planned improvements
- Found in: `App.kt`, `GeoSource.kt`, `GeoAnimator.kt`, example files

## Function Design

**Size:** Functions typically small and focused (10-30 lines)

**Parameters:**
- Named parameters for clarity in DSL: `configure { width = 768 }`
- Default parameters for optional configuration: `style: Style? = null`
- Lambda receivers for configuration blocks

**Return Values:**
- Expression bodies for simple returns
- Unit for side-effect operations (rendering)
- Sequence for lazy evaluation: `abstract val features: Sequence<Feature>`

**Extension Functions:**
```kotlin
// Extending OpenRNDR types
fun Drawer.geo(source: GeoSource, block: GeoRenderConfig.() -> Unit)

// Extending project types
fun Sequence<ProjectedFeature>.forEachWithProjection(action: (Feature, ProjectedGeometry) -> Unit)
```

## Class Design

**Sealed Classes:**
- Used for type-safe hierarchies with exhaustive `when`:
  ```kotlin
  sealed class Geometry
  data class Point(...) : Geometry()
  data class LineString(...) : Geometry()
  // ...
  
  fun Geometry.transform(transformer: CRSTransformer): Geometry = when (this) {
      is Point -> // ...
      is LineString -> // ...
      // Compiler ensures all cases handled
  }
  ```

**Data Classes:**
- Used for value types: `Feature`, `Bounds`, `ProjectedPoint`
- Immutable by default (val properties)
- `copy()` for creating modified instances

**Abstract Classes:**
- `GeoSource` as abstract base with lazy `Sequence<Feature>`
- Template method pattern for specialized sources

**Companion Objects:**
- Factory methods: `Bounds.empty()`, `Feature.fromPoint()`
- Singleton pattern: `GeoAnimator.instance`

## Module Design

**Build Convention Plugins:**
Located in `buildSrc/src/main/kotlin/conventions/`:
- `kotlin-jvm.gradle.kts` - Kotlin/JVM configuration
- `openrndr-tasks.gradle.kts` - OPENRNDR-specific Gradle tasks
- `distribute-application.gradle.kts` - Packaging and distribution

**Version Catalog:**
- Centralized in `gradle/libs.versions.toml`
- Type-safe accessors: `libs.versions.openrndr.get()`
- Multiple catalogs: `libs` (general), `openrndr`, `orx`

**Source Sets:**
```kotlin
kotlin.sourceSets.getByName("main") {
    kotlin.srcDir("examples")  // Examples compiled as part of main
}
```

## Testing Conventions

**Test Class Naming:** `{ClassUnderTest}Test`

**Test Method Naming:** `test{Scenario}()`

**Pattern:**
```kotlin
class GeoSourceTest {
    @Test
    fun testDefaultCRS() {
        val source = TestGeoSource()
        assertEquals("EPSG:4326", source.crs)
    }
}
```

## OPENRNDR-Specific Conventions

**Program Structure:**
```kotlin
fun main() = application {
    configure {
        width = 800
        height = 800
    }
    program {
        // Setup code
        val animator = animator()
        
        extend {
            animator.updateAnimation()
            // Render loop
            drawer.clear(ColorRGBa.BLACK)
            // ... rendering code
        }
    }
}
```

**Asset Loading:**
- Assets in `data/` directory
- Loading: `loadImage("data/images/...")`, `loadFont("data/fonts/...", size)`

**Render Extension Pattern:**
```kotlin
fun Drawer.geo(source: GeoSource, block: GeoRenderConfig.() -> Unit) {
    val config = GeoRenderConfig().apply(block)
    // Render with config
}
```

---

*Convention analysis: 2026-03-02*
