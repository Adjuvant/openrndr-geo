# Architecture

**Analysis Date:** 2026-03-02

## Pattern Overview

**Overall:** Geospatial Data Processing & Rendering Library for OPENRNDR

**Key Characteristics:**
- Domain-driven package structure with clear separation of concerns
- Immutable data models (Geometry, Feature, Bounds) with transformation operations
- Lazy sequence-based processing for memory-efficient large dataset handling
- Fluent API with extension functions for Kotlin idiomatic usage
- DSL-style configuration for rendering and animation
- Plugin-based build system with convention scripts

## Layers

**Application Layer:**
- Purpose: Program entry points and demonstration applications
- Location: `src/main/kotlin/*.kt` (root level), `examples/**/*.kt`
- Contains: Top-level Kotlin files with `fun main() = application { }`, runnable examples
- Key files: `App.kt`, `TemplateProgram.kt`, `TemplateLiveProgram.kt`
- Depends on: OPENRNDR core, `geo.*` library packages
- Used by: JVM runtime, end users

**Library Core Layer:**
- Purpose: Core geospatial data models and operations
- Location: `src/main/kotlin/geo/`
- Contains: Geometry types, Feature model, GeoSource abstraction, Bounds
- Key files: `Geometry.kt`, `Feature.kt`, `GeoSource.kt`, `Bounds.kt`, `GeoStack.kt`
- Depends on: GeoPackage library, Proj4J for CRS transformations
- Used by: All other layers

**Data Source Layer:**
- Purpose: Data loading from various geospatial formats
- Location: `src/main/kotlin/geo/`
- Contains: GeoJSON parser, GeoPackage loader, source convenience functions
- Key files: `GeoJSON.kt`, `GeoPackage.kt`, `GeoSourceConvenience.kt`
- Depends on: Core layer, external libraries (geopackage, proj4j)
- Used by: Application layer, rendering layer

**Projection Layer:**
- Purpose: Coordinate reference system transformations and projections
- Location: `src/main/kotlin/geo/projection/`
- Contains: Projection interfaces, CRS transformations, screen transforms, factory
- Key files: `GeoProjection.kt`, `CRSTransformer.kt`, `ProjectionFactory.kt`, `ScreenTransform.kt`
- Depends on: Core layer, Proj4J library
- Used by: Rendering layer, animation layer

**Rendering Layer:**
- Purpose: OPENRNDR integration for drawing geospatial data
- Location: `src/main/kotlin/geo/render/`
- Contains: Drawer extensions, style DSL, geometry renderers (Point, Line, Polygon)
- Key files: `DrawerGeoExtensions.kt`, `Style.kt`, `render.kt`, `PointRenderer.kt`, `LineRenderer.kt`, `PolygonRenderer.kt`
- Depends on: OPENRNDR Drawer API, core layer, projection layer
- Used by: Application layer

**Animation Layer:**
- Purpose: Time-based animations and transitions for geospatial data
- Location: `src/main/kotlin/geo/animation/`
- Contains: Animator classes, tweening, easing, interpolators, composition
- Key files: `GeoAnimator.kt`, `Tweening.kt`, `EasingExtensions.kt`, `FeatureAnimator.kt`
- Depends on: OPENRNDR animation, core layer, projection layer
- Used by: Application layer

**Layer/Composition Layer:**
- Purpose: Map composition elements (graticules, overlays)
- Location: `src/main/kotlin/geo/layer/`
- Contains: GeoLayer interface, Graticule implementation
- Key files: `GeoLayer.kt`, `Graticule.kt`
- Depends on: Rendering layer, core layer
- Used by: Application layer

**CRS & Exception Layer:**
- Purpose: Coordinate system definitions and error handling
- Location: `src/main/kotlin/geo/crs/`, `src/main/kotlin/geo/exception/`
- Contains: CRS enum, custom exceptions
- Key files: `CRS.kt`, `ProjectionExceptions.kt`
- Depends on: Minimal
- Used by: All layers

**Examples Layer:**
- Purpose: Runnable demonstrations and tutorials
- Location: `examples/` (compiled as part of main source set)
- Contains: Category-organized example programs
- Key categories: `core/`, `proj/`, `render/`, `anim/`, `layer/`
- Depends on: All library layers
- Used by: Developers learning the library

**Build Configuration Layer:**
- Purpose: Gradle build logic and conventions
- Location: `buildSrc/src/main/kotlin/conventions/`
- Contains: Kotlin JVM conventions, OPENRNDR tasks, distribution
- Key files: `kotlin-jvm.gradle.kts`, `openrndr-tasks.gradle.kts`, `distribute-application.gradle.kts`
- Depends on: Gradle Kotlin DSL
- Used by: Build system

## Data Flow

**Data Loading Flow:**

1. `geoSource("path/to/file.geojson")` or `GeoPackage.load("path.gpkg")` - Entry point
2. Format-specific parser reads file → Sequence<Feature>
3. CRS detected from file or defaulted to EPSG:4326
4. Features stored as lazy Sequence (not loaded into memory yet)
5. Optional: Transform to different CRS via `autoTransformTo(targetCRS)`

**Rendering Flow:**

1. Application creates `program { }` block
2. Load data sources via `geoSource()` or `GeoPackage.load()`
3. Create projection via `ProjectionFactory.fitBounds()` or use `RawProjection`
4. Call rendering method:
   - `drawer.geo(source) { /* style DSL */ }` - Extension function
   - `source.render(drawer, projection)` - Direct render
   - `map.render(drawer)` - GeoStack with auto-fit
5. Renderer projects coordinates: `GeoProjection.project(Vector2)`
6. Geometry drawn via OPENRNDR Drawer API

**Animation Flow:**

1. Create animator: `val animator = animator()`
2. Define animations: `animator::x.animate(target, duration, easing)`
3. In `extend { }` block: `animator.updateAnimation()`
4. Use animated values in rendering
5. Animation composition via `GeoTimeline` and chained animations

**GeoStack Multi-Source Flow:**

1. Create stack: `geoStack(source1, source2, ...)`
2. Stack unifies CRS to first source's CRS
3. Subsequent sources auto-transform if CRS differs
4. View operations: `zoom()`, `pan()`, `centerOn()`, `zoomTo()`
5. Render with current view bounds or auto-fit

## Key Abstractions

**Geometry:**
- Purpose: Immutable geospatial geometry types
- Types: `Point`, `LineString`, `Polygon`, `MultiPoint`, `MultiLineString`, `MultiPolygon`
- Location: `src/main/kotlin/geo/Geometry.kt`
- Pattern: Sealed class hierarchy with data classes
- Operations: `boundingBox`, `transform(CRSTransformer)`, `toWGS84()`

**Feature:**
- Purpose: Geometry with properties (attribute data)
- Location: `src/main/kotlin/geo/Feature.kt`
- Pattern: Data class with typed property accessors
- Key methods: `property(key)`, `doubleProperty(key)`, `propertyKeys()`

**GeoSource:**
- Purpose: Abstract data source for geospatial features
- Location: `src/main/kotlin/geo/GeoSource.kt`
- Pattern: Abstract class with lazy Sequence<Feature>
- Implementations: GeoJSON loader, GeoPackage loader, transformed sources
- Operations: `filter()`, `map()`, `autoTransformTo()`, `withProjection()`

**GeoProjection:**
- Purpose: Project geographic coordinates to screen space
- Location: `src/main/kotlin/geo/projection/GeoProjection.kt`
- Pattern: Functional interface with `project(Vector2): Vector2`
- Implementations: Mercator, Equirectangular, BNG (British National Grid)

**CRSTransformer:**
- Purpose: Transform coordinates between CRS
- Location: `src/main/kotlin/geo/projection/CRSTransformer.kt`
- Pattern: Wrapper around Proj4J with caching

**GeoStack:**
- Purpose: Multi-source composition with unified CRS
- Location: `src/main/kotlin/geo/GeoStack.kt`
- Pattern: Aggregates multiple GeoSources, auto-transforms to common CRS
- Operations: `zoom()`, `pan()`, `centerOn()`, `render()`

**Style DSL:**
- Purpose: Declarative styling for rendered features
- Location: `src/main/kotlin/geo/render/Style.kt`
- Pattern: Data class with lambda-based configuration
- Usage: `styleByFeature = { feature -> Style(...) }`, `styleByType = mapOf(...)`

**Animator:**
- Purpose: Time-based property animation
- Location: `src/main/kotlin/geo/animation/GeoAnimator.kt`
- Pattern: Property delegate with easing functions
- Usage: `animator::x.animate(target, duration, Easing.QuartInOut)`

## Entry Points

**Main Application:**
- Location: `src/main/kotlin/App.kt`
- Triggers: `./gradlew run` (uses `applicationMainClass=AppKt` from gradle.properties)
- Responsibilities: Demonstrates library features, interactive testing

**Template Programs:**
- Standard: `src/main/kotlin/TemplateProgram.kt`
- Live-coding: `src/main/kotlin/TemplateLiveProgram.kt`
- Triggers: `./gradlew run -Popenrndr.application=TemplateProgramKt`
- Responsibilities: Starting points for new programs

**Example Programs:**
- Location: `examples/{category}/*.kt`
- Categories: `core/`, `proj/`, `render/`, `anim/`, `layer/`
- Triggers: `./gradlew run -Popenrndr.application=01_load_geojsonKt`
- Responsibilities: Feature demonstrations, tutorials

**Test Entry Points:**
- Location: `src/test/kotlin/geo/**/*.kt`
- Triggers: `./gradlew test`
- Framework: JUnit 4

## Error Handling

**Strategy:** Exceptions for unrecoverable errors, null/optional for recoverable

**Patterns:**
- Custom exceptions in `geo.exception.ProjectionExceptions`
- CRS detection failures throw with context
- Missing properties return null (e.g., `feature.property("key")`)
- Invalid geometries handled gracefully where possible

**Logging:**
- Framework: kotlin-logging with SLF4J
- Pattern: `logger.debug { "message" }`, `logger.error { "message" }`
- Configuration: Simple SLF4J logging (gradle.properties sets logging level)

## Cross-Cutting Concerns

**CRS Management:**
- Default: EPSG:4326 (WGS84)
- Detection: From GeoJSON "crs" field, GeoPackage metadata
- Transformation: Lazy via `autoTransformTo()`, cached CRSTransformer instances

**Memory Efficiency:**
- Pattern: Kotlin Sequences for feature streams
- Large datasets processed without loading all into memory
- `take()`, `filter()`, `map()` operations are lazy

**Thread Safety:**
- Geometry/Feature/Bounds are immutable data classes
- GeoSource creates new instances for transformations
- Rendering happens on OPENRNDR's single thread

**Testing Strategy:**
- Unit tests for geometry operations, projections, rendering
- Integration tests for CRS transformations, GeoPackage loading
- Test data in `src/test/resources/` (if needed)

**Build Conventions:**
- `kotlin-jvm.gradle.kts`: Kotlin compilation settings, JVM 17 target
- `openrndr-tasks.gradle.kts`: OPENRNDR-specific Gradle tasks
- `distribute-application.gradle.kts`: Native packaging via jpackage

---

*Architecture analysis: 2026-03-02*
