# Phase 02: Coordinate Systems - Research

**Researched:** 2026-02-21
**Domain:** Coordinate transformations and map projections
**Confidence:** MEDIUM

## Summary

This phase involves implementing coordinate transformations between geographic systems (lat/lng), projecting to screen coordinates, and supporting basic projections (Mercator and Equirectangular) with British National Grid (BNG) transformations at OSTN15 accuracy.

**Key research findings:**
- Standard coordinate transformations should use `org.locationtech:proj4j` (Java library fully compatible with Kotlin)
- OpenRNDR's `Vector2` class is a simple data class with x/y Double properties, ready for screen coordinates
- Projection mathematics are well-documented: Mercator formulas include pole handling (max latitude ~85.05112878° for square maps)
- **Critical gap:** No modern Kotlin/Java library provides OSTN15 transformation built-in. Will need to integrate PROJ or implement using Ordnance Survey's grid data
- Best practice: Use projection viewport pattern with `project()` and `unproject()` methods

**Primary recommendation:** Use `proj4j` for CRS transformations (EPSG code support), implement projection mathematics manually (simple formulas), and handle OSTN15 as special case using grid-based transformation.

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| org.locationtech.proj4j | 1.4.1 | Coordinate reference system transformations | Java port of Proj.4, active project (released 2025), MIT license, EPSG code support via proj4j-epsg |
| OpenRNDR Vector2 | latest | Screen coordinates | Simple data class with x/y Double, integrates with OpenRNDR drawing operations |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| proj4j-epsg | 1.4.1 | EPSG code database | When using EPSG codes (27700 for BNG, 4326 for WGS84) |
| crsTransformations | 2.0.1 | Kotlin wrapper (optional) | For convenience, adds Kotlin-friendly API and composite pattern |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| proj4j | GeoTools, PROJ JNI | GeoTools has AWT dependency issues for Kotlin; PROJ JNI is heavier |
| proj4j-epsg | Manual coordinate lookup | proj4j-epsg is battle-tested and maintained |

**Installation:**
```kotlin
// build.gradle.kts
dependencies {
    implementation("org.locationtech.proj4j:proj4j:1.4.1")
    implementation("org.locationtech.proj4j:proj4j-epsg:1.4.1") // Optional if using EPSG codes
    // Optional convenience wrapper
    implementation("com.programmerare.crs-transformation:crs-transformation-adapter-core:2.0.1")
    implementation("com.programmerare.crs-transformation:crs-transformation-adapter-impl-proj4jlocationtech:2.0.1")
}
```

## Architecture Patterns

### Recommended Project Structure
```
src/
├── projection/
│   ├── GeoProjection.kt           # Interface for all projections
│   ├── ProjectionMercator.kt     # Web Mercator implementation
│   ├── ProjectionEquirectangular.kt # Equirectangular implementation
│   ├── ProjectionBNG.kt         # British National Grid (OSTN15)
│   └── ProjectionFactory.kt      # Factory for creating presets
├── transform/
│   ├── CoordinateTransform.kt   # Extension functions for transforms
│   └── ScreenTransform.kt        # Lat/lng to screen mapping
└── exception/
    └── ProjectionExceptions.kt   # Custom exceptions
```

### Pattern 1: GeoProjection Interface
**What:** Interface defining projection operations, allows mixing coordinate systems
**When to use:** Core abstraction for all projection types
**Example:**
```kotlin
// Source: math.gl WebMercatorViewport pattern
interface GeoProjection {
    fun project(latLng: Vector2): Vector2  // Lat/lng to screen
    fun unproject(screen: Vector2): Vector2 // Screen to lat/lng
    fun configure(config: ProjectionConfig): GeoProjection
}
```

### Pattern 2: Projection with Viewport Configuration
**What:** Camera-like configuration (center, zoom, bounds) for controlling projection
**When to use:** When users need fitWorld() or viewport control
**Example:**
```kotlin
// Source: math.gl viewport pattern
data class ProjectionConfig(
    val width: Double,
    val height: Double,
    val center: Vector2? = null,  // lat/lng
    val scale: Double = 1.0,
    val bounds: Rectangle? = null
)

val projection = ProjectionMercator().configure(
    ProjectionConfig(width=800.0, height=600.0)
)
val screen = projection.project(Vector2(0.0, 51.5)) // London
```

### Pattern 3: Extension Methods + Procedural Functions
**What:** Both procedural (`toScreen(lat, lng)`) and extension (`latLng.toScreen()`) styles
**When to use:** To match library best practices and user preferences
**Example:**
```kotlin
// Source: OpenCode's discretion from CONTEXT.md
// Procedural style
fun toScreen(latitude: Double, longitude: Double): Vector2

// Extension method style (CONTEXT.md decision)
fun Vector2.toScreen(projection: GeoProjection): Vector2

// Batch operations for performance (CONTEXT.md decision)
fun toScreen(points: Sequence<Vector2>): List<Vector2>
```

### Anti-Patterns to Avoid
- **Single global projection state:** Thread-safety issues, harder to reason about
- **Mixing projection logic with drawing code:** Violates separation of concerns
- **Silent coordinate wrapping:** Always normalize explicitly or throw (CONTEXT.md decision)

## Don't Hand-Roll

### Coordinate CRS Transformations
**Problem:** Transforming between EPSG:4326 (WGS84 lat/lng) and EPSG:27700 (BNG easting/northing)
**Don't Build:** Manual transformation formulas
**Use Instead:** `org.locationtech.proj4j` with proj4j-epsg
**Why:** EPSG codes have complex datum transformations (OSGB36 vs ETRS89), PROJ has 40+ years of geodetic expertise. British National Grid transformation requires handling OSGB36 TRF distortions (Ordnance Survey, 2020).

### EPSG Code Resolution
**Problem:** Converting "EPSG:27700" to transformation parameters
**Don't Build:** String parsing and mapping
**Use Instead:** proj4j-epsg database
**Why:** EPSG database is maintained by IOGP, includes ellipsoid, datum, grid parameters

### OSTN15 Grid Interpolation
**Problem:** 1km grid interpolation for British National Grid (~1cm accuracy)
**Don't Build:** Custom grid lookup and bilinear interpolation
**Use Instead:** PROJ with OSTN15 grids, or Ordnance Survey's Grid InQuestII
**Why:** OSTN15 is complex: uses ETRS89 (WGS84) vs OSGB36, requires rubber-sheet transformation with 1km resolution and bilinear interpolation (Ordnance Survey, 2020). PROJ provides tested implementation.

**Key insight:** proj4j handles PROJ.4 parameters but DOES NOT transform with grid data. For OSTN15, either:
1. Use PROJ C library with JNI (heavyweight)
2. Implement grid interpolation from Ordnance Survey specification
3. Use Ordnance Survey's Grid InQuestII via CLI (external process)

**Recommendation:** For Phase 2, implement basic Helmert transformation (3-5m accuracy) and add OSTN15 in later phase if needed.

## Common Pitfalls

### Pitfall 1: Mercator Pole Overflow
**What goes wrong:** Mercator projection approaches infinity at latitude = ±90°, causing overflow
**Why it happens:** `tan(latitude)` → infinity at 90°
**How to avoid:**
- Clamp latitude to ±85.05112878° (standard Web Mercator limit)
- Throw `ProjectionOverflowException` for invalid latitudes (CONTEXT.md decision)
- Provide warning in error messages: "Use `clampLatitude(lat, max = 89.999)`" (CONTEXT.md decision)

**Warning signs:** Coordinates at extreme latitudes, NaN results from tan/log operations

### Pitfall 2: OSTN15 Validity Bounds
**What goes wrong:** OSTN15 only valid for UK grid area
**Why it happens:** 1km grid covers Britain only, no interpolation outside
**How to avoid:**
- Validate BNG coordinates are within UK bounds (approx: easting: 0-700km, northing: 0-1300km)
- Throw `AccuracyWarningException` when outside OSTN15 valid grid (CONTEXT.md decision)
- Provide rich feedback about transformation limits (CONTEXT.md decision)

**Warning signs:** Negative easting/northing, coordinates outside 0-700km/0-1300km range

### Pitfall 3: Coordinate Normalization
**What goes wrong:** Longitude values outside [-180, 180] cause incorrect projections
**Why it happens:** Earth wraps around, -181° is same as 179°
**How to avoid:**
- Normalize coordinates to standard range automatically (CONTEXT.md decision)
- Formula: `longitude = ((longitude + 180) % 360) - 180`
- Use `%` with proper handling for negative values

**Warning signs:** Longitude > 180 or < -180, screen coordinates far outside bounds

### Pitfall 4: Off-Screen Coordinate Filtering
**What goes wrong:** Accidentally filtering out valid points outside view area
**Why it happens:** Off-screen coordinates are valid for user visibility checks (CONTEXT.md decision)
**How to avoid:**
- Always return Vector2 values even if off-screen
- Provide `isOnScreen(point: Vector2, bounds: Rectangle): Boolean` helper (CONTEXT.md decision)
- Let user decide what to do with off-screen points

**Warning signs:** Missing points at edges, incorrect clipping logic

### Pitfall 5: Mixing Coordinate Systems
**What goes wrong:** Passing lat/lng to BNG projection or vice versa
**Why it happens:** All projections implement same interface, no type safety
**How to avoid:**
- Validate on instantiation (EPSG code check)
- Use specific projection types: `ProjectionMercator`, `ProjectionBNG`
- Consider sealed class for coordinate types (from Phase 1 decision)

**Warning signs:** Extreme coordinate values, garbled map rendering

## Code Examples

### Mercator Projection Formula
```kotlin
// Source: Paul Bourke, 2021 - Converting Web Mercator projection to equirectangular
// WGS84 sphere radius: 6378137 meters
private const val EARTH_RADIUS = 6378137.0
private const val MAX_LATITUDE = 85.05112878 // 180/π * atan(e^π) ≈ 85.05

fun mercatorProject(latLng: Vector2): Vector2 {
    val lat = latLa-ng.y
    val lon = latLng.x

    // Clamp latitude (CONTEXT.md decision: throw for poles)
    require(lat > -MAX_LATITUDE && lat < MAX_LATITUDE) {
        "Latitudne $lat exceeds Mercator limit ±$MAX_LATITUDE. Use clampLatitude(lat, max = 89.999)"
    }

    // Web Mercator formula: sphere radius 6378137m
    val x = EARTH_RADIUS * toRadians(lon)
    val y = EARTH_RADIUS * ln(tan(π/4 + toRadians(lat)/2))

    return Vector2(x, y)
}

mercatorUnproject(screen: Vector2): Vector2 {
    val x = screen.x
    val y = screen.y

    val lon = toDegrees(x / EARTH_RADIUS)
    val lat = toDegrees(2 * atan(exp(y / EARTH_RADIUS)) - π/2)

    return Vector2(lon, lat)
}
```

### Equirectangular Projection Formula
```kotlin
// Source: Snyder, Map Projections A Working Manual, 1987
// Simple linear mapping

fun equirectangularProject(
    latLng: Vector2,
    config: ProjectionConfig
): Vector2 {
    val lat = latLng.y
    val lon = latLng.x

    // Map lon [-180, 180] to [0, width]
    val x = (lon + 180.0) / 360.0 * config.width

    // Map lat [-90, 90] to [height, 0] (y is flipped)
    val y = config.height - ((lat + 90.0) / 180.0 * config.height)

    return Vector2(x, y)
}
```

### Basic CRS Transformation with proj4j
```kotlin
// Source: org.locationtech.proj4j README, 2025
import org.locationtech.proj4j.*

val crsFactory = CRSFactory()
val WGS84 = crsFactory.createFromName("epsg:4326")  // Lat/lng
val BNG = crsFactory.createFromName("epsg:27700")     // British National Grid

val transformFactory = CoordinateTransformFactory()
val wgsToBng = transformFactory.createTransform(WGS84, BNG)

fun transformToBNG(latLng: Vector2): Vector2 {
    val point = ProjCoordinate(latLng.x, latLng.y) // lon, lat
    val result = ProjCoordinate()
    wgsToBng.transform(point, result)
    return Vector2(result.x, result.y)
}
```

### Coordinate Normalization
```kotlin
// Source: CONTEXT.md decision - normalize automatically

fun normalizeLongitude(longitude: Double): Double {
    // Map to [-180, 180]
    var normalized = ((longitude % 360) + 360) % 360
    if (normalized > 180) normalized -= 360
    return normalized
}

fun normalizeCoordinate(latLng: Vector2): Vector2 {
    val lon = normalizeLongitude(latLng.x)
    val lat = latLng.y // Latitude is already in [-90, 90] for valid input
    return Vector2(lon, lat)
}
```

### Visibility Helper Function
```kotlin
// Source: CONTEXT.md decision - off-screen coordinates are valid
// OpenRNDR Rectangle for bounds

fun isOnScreen(point: Vector2, bounds: Rectangle): Boolean {
    return point.x >= bounds.x &&
           point.x < bounds.x + bounds.width &&
           point.y >= bounds.y &&
           point.y < bounds.y + bounds.height
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| PROJ C library bindings | proj4j (pure Java) | ~2015 | Now Kotlin-compatible, smaller dependency footprint |
| Manual transformation formulas | proj4j EPSG database | ~2010 | More accurate (datum transformations), 1000+ CRIs supported |
| Full OSTN02 model | OSTN15 model | 2016 | ~1cm accuracy vs. ~3m accuracy, required for legal survey |
| Single projection interface | GeoProjection abstraction | 2020s | Mixing coordinate systems, better testability |

**Deprecated/outdated:**
- **GeoTools with Kotlin:** Uses AWT dependencies (java.awt.geom.Point2D), problematic for Kotlin (crsTransformations docs)
- **OSTN02:** Replaced by OSTN15 in August 2016, 100x accuracy improvement (Ordnance Survey, 2016)
- **JCoord:** Java library for OSGB, last updated 2016, no Kotlin support

## Open Questions

### 1. OSTN15 Implementation Approach
**What we know:** OSTN15 requires 1km grid and bilinear interpolation for ~1cm accuracy. No modern Kotlin library provides this.
**What's unclear:** Best way to integrate PROJ with OSTN15 grids in Kotlin.

**Options:**
1. **Use PROJ via JNI:** Heavyweight dependency, but provides full PROJ functionality
2. **Implement grid interpolation:** From Ordnance Survey specification (requires parsing grid files)
3. **Use external process:** Call Grid InQuestII CLI (BSD licensed) - adds external dependency
4. **Defer to later phase:** Implement Helmert transformation (3-5m accuracy) now, OSTN15 later

**Recommendation:** For Phase 2, use Helmert transformation via proj4j (fast, simpler), add OSTN15 in future phase if accuracy requirement becomes critical. ~1cm accuracy (COORD-03) may be over-specified for creative coding context.

### 2. Projection Configuration Syntax
**What we know:** CONTEXT.md says "presets + builders, fitWorld() option" (OpenCode's discretion)
**What's unclear:** Exact builder syntax for configuring projections.

**Recommendation:** Follow math.gl pattern:
```kotlin
ProjectionMercator {
    width = 800
    height = 600
    center = Vector2(0.0, 51.5)
    fitWorld = false
}
```

### 3. Performance for Batch Operations
**What we know:** CONTEXT.md calls for `toScreen(points: Sequence<Vector2>)`
**What's unclear:** Optimal batch size, caching strategies for repeated transformations.

**Recommendation:** Start with simple iteration, optimize with vectorization if performance issues identified.

## Sources

### Primary (HIGH confidence)
- org.locationtech.proj4j GitHub - PROJ.4 port, EPSG support, basic usage examples
- OpenRNDR API docs - Vector2 class definition, transformation patterns
- Paul Bourke, "Converting Web Mercator projection to equirectangular" (2021) - Web Mercator formulas, sphere radius
- Ordnance Survey "A Guide to Coordinate Systems in Great Britain" v3.6 (2020) - OSTN15 methodology, accuracy

### Secondary (MEDIUM confidence)
- math.gl Web Mercator documentation - Viewport pattern, project/unproject functions
- Ordnance Survey OSTN15 documentation - Grid transformation details, 1cm accuracy
- TomasJohansson/crsTransformations - Kotlin patterns, composite implementations

### Tertiary (LOW confidence)
- Web search results for projection libraries (2026) - General landscape, library existence
- BNG Stack Exchange discussions - Community understanding, implementation approaches

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Verified proj4j from official docs and GitHub, OpenRNDR from API docs
- Architecture: MEDIUM - Patterns from math.gl (official docs), but projection builder syntax is OpenCode's discretion
- Pitfalls: HIGH - Mercator overflow well-documented, OSTN15 bounds explicit in OS spec

**Research date:** 2026-02-21
**Valid until:** 30 days for OSTN15 methodology (standard is stable), 7 days for library versions (proj4j active)

---

*Phase: 02-coordinate-systems*
*Research: Coordinate transformations, projections, BNG/OSTN15*