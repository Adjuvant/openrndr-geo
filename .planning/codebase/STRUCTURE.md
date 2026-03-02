# Codebase Structure

**Analysis Date:** 2026-03-02

## Directory Layout

```
openrndr-geo/
├── buildSrc/                        # Gradle build conventions
│   ├── build.gradle.kts             # BuildSrc dependencies
│   ├── settings.gradle.kts          # BuildSrc settings
│   └── src/main/kotlin/conventions/ # Convention plugins
│       ├── kotlin-jvm.gradle.kts
│       ├── openrndr-tasks.gradle.kts
│       ├── distribute-application.gradle.kts
│       └── publish-library.gradle.kts
├── examples/                        # Runnable example programs
│   ├── core/                        # Core functionality examples
│   ├── proj/                        # Projection examples
│   ├── render/                      # Rendering examples
│   ├── anim/                        # Animation examples
│   ├── layer/                       # Layer/composition examples
│   └── data/                        # Example data files
│       └── geo/                     # GeoJSON, GeoPackage sample files
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── App.kt               # Main application entry
│   │   │   ├── TemplateProgram.kt   # Template for new programs
│   │   │   ├── TemplateLiveProgram.kt  # Live-coding template
│   │   │   └── geo/                 # Library source code
│   │   │       ├── animation/       # Animation system
│   │   │       ├── crs/             # CRS definitions
│   │   │       ├── examples/        # Embedded examples
│   │   │       ├── exception/       # Custom exceptions
│   │   │       ├── layer/           # Map layers (graticule, etc.)
│   │   │       ├── projection/      # CRS transformations
│   │   │       │   └── internal/    # Internal projection math
│   │   │       └── render/          # OPENRNDR rendering integration
│   │   └── resources/               # Runtime resources
│   │       └── log4j2.yaml          # Logging configuration
│   └── test/
│       └── kotlin/geo/              # Unit and integration tests
│           ├── animation/
│           ├── projection/
│           └── render/
├── data/                            # Application data (images, fonts)
│   ├── fonts/
│   └── images/
├── gradle/
│   └── libs.versions.toml           # Version catalog
├── build.gradle.kts                 # Main build configuration
├── settings.gradle.kts              # Project settings
├── gradle.properties                # Project properties
├── gradlew                          # Gradle wrapper (Unix)
└── gradlew.bat                      # Gradle wrapper (Windows)
```

## Directory Purposes

**`src/main/kotlin/`:**
- Purpose: Application entry points
- Contains: Top-level `.kt` files (`App.kt`, `TemplateProgram.kt`, etc.)
- Convention: Flat structure at root, only `geo/` package uses nested structure

**`src/main/kotlin/geo/`:**
- Purpose: Library source code
- Contains: Core geospatial functionality organized by feature
- Key files:
  - `Geometry.kt` - Geometry types (Point, LineString, Polygon, etc.)
  - `Feature.kt` - Feature model with properties
  - `GeoSource.kt` - Abstract data source
  - `GeoStack.kt` - Multi-source composition
  - `GeoJSON.kt` - GeoJSON parsing
  - `GeoPackage.kt` - GeoPackage loading
  - `Bounds.kt` - Bounding box operations
  - `SpatialIndex.kt` - Spatial indexing

**`src/main/kotlin/geo/animation/`:**
- Purpose: Animation and tweening
- Contains: `GeoAnimator.kt`, `Tweening.kt`, `EasingExtensions.kt`, `FeatureAnimator.kt`, `ProceduralMotion.kt`
- Subpackages: `composition/`, `interpolators/`

**`src/main/kotlin/geo/projection/`:**
- Purpose: Projections and CRS transformations
- Contains: `GeoProjection.kt`, `CRSTransformer.kt`, `ProjectionFactory.kt`, `ScreenTransform.kt`
- Projections: `ProjectionMercator.kt`, `ProjectionEquirectangular.kt`, `ProjectionBNG.kt`
- Subpackage: `internal/` - Low-level projection math

**`src/main/kotlin/geo/render/`:**
- Purpose: OPENRNDR rendering integration
- Contains:
  - `DrawerGeoExtensions.kt` - Extension functions on Drawer
  - `Style.kt`, `StyleDefaults.kt` - Styling DSL
  - `render.kt` - Main render entry points
  - `PointRenderer.kt`, `LineRenderer.kt`, `PolygonRenderer.kt` - Geometry renderers
  - `MultiRenderer.kt` - Multi-geometry rendering
  - `GeoRenderConfig.kt` - Render configuration
  - `Shape.kt` - Shape utilities

**`src/main/kotlin/geo/layer/`:**
- Purpose: Map composition layers
- Contains: `GeoLayer.kt` (interface), `Graticule.kt` (implementation)

**`src/main/kotlin/geo/crs/`:**
- Purpose: CRS definitions
- Contains: `CRS.kt` - Strongly-typed CRS enum

**`src/main/kotlin/geo/exception/`:**
- Purpose: Custom exceptions
- Contains: `ProjectionExceptions.kt`

**`src/main/kotlin/geo/examples/`:**
- Purpose: Embedded examples callable from main app
- Contains: `core_*.kt`, `proj_*.kt`, `render_*.kt`, `layer_*.kt`

**`examples/`:**
- Purpose: Standalone runnable examples (separate from main source)
- Organized by category: `core/`, `proj/`, `render/`, `anim/`, `layer/`
- Included in main source set via `build.gradle.kts`: `kotlin.srcDir("examples")`
- Each subdirectory has a `README.md` with documentation

**`src/test/kotlin/geo/`:**
- Purpose: Unit and integration tests
- Mirrors main source structure:
  - `*Test.kt` files for core classes
  - `animation/` - Animation tests
  - `projection/` - Projection tests
  - `render/` - Rendering tests

**`buildSrc/`:**
- Purpose: Gradle convention plugins
- Structure:
  - `build.gradle.kts` - Dependencies for build logic
  - `settings.gradle.kts` - BuildSrc settings
  - `src/main/kotlin/conventions/` - Plugin definitions
- Convention plugins:
  - `kotlin-jvm.gradle.kts` - Kotlin compilation
  - `openrndr-tasks.gradle.kts` - OPENRNDR-specific tasks
  - `distribute-application.gradle.kts` - Packaging
  - `publish-library.gradle.kts` - Publishing

**`gradle/`:**
- Purpose: Gradle configuration
- Contains: `libs.versions.toml` - Version catalog with all dependency versions

**`data/`:**
- Purpose: Application runtime assets
- Contains: `fonts/`, `images/` - Loaded via relative paths
- Note: Separate from `examples/data/` which contains geospatial sample data

## Key File Locations

**Entry Points:**
- `src/main/kotlin/App.kt` - Main application (default run target)
- `src/main/kotlin/TemplateProgram.kt` - Standard program template
- `src/main/kotlin/TemplateLiveProgram.kt` - Live-coding template

**Configuration:**
- `build.gradle.kts` - Main build configuration
- `gradle/libs.versions.toml` - Dependency versions
- `gradle.properties` - Project properties (name, version, main class)
- `settings.gradle.kts` - Project settings, version catalog setup
- `buildSrc/src/main/kotlin/conventions/*.gradle.kts` - Build conventions

**Core Library:**
- `src/main/kotlin/geo/Geometry.kt` - All geometry types
- `src/main/kotlin/geo/Feature.kt` - Feature model
- `src/main/kotlin/geo/GeoSource.kt` - Data source abstraction
- `src/main/kotlin/geo/GeoStack.kt` - Multi-source composition

**Rendering:**
- `src/main/kotlin/geo/render/DrawerGeoExtensions.kt` - Main rendering API
- `src/main/kotlin/geo/render/Style.kt` - Styling DSL

**Projection:**
- `src/main/kotlin/geo/projection/ProjectionFactory.kt` - Create projections
- `src/main/kotlin/geo/projection/CRSTransformer.kt` - CRS transformations

## Naming Conventions

**Files:**
- Kotlin source: `PascalCase.kt`
- Top-level programs: `App.kt`, `TemplateProgram.kt`
- Library classes: `GeoSource.kt`, `Feature.kt`, `Geometry.kt`
- Example programs: `01-load-geojson.kt` (kebab-case with numbers)
- Gradle files: `lowercase.gradle.kts`
- Test files: `*Test.kt` suffix

**Packages:**
- Root library: `geo.*`
- Subpackages: lowercase, descriptive (`animation`, `projection`, `render`)

**Classes:**
- Data classes: `Geometry`, `Feature`, `Bounds`
- Service classes: `GeoSource`, `CRSTransformer`, `GeoStack`
- Renderers: `PointRenderer`, `LineRenderer`, `PolygonRenderer`

**Functions:**
- Extension functions: Lowercase, descriptive
  - `geoSource(path)` - Create GeoSource
  - `geoStack(vararg sources)` - Create GeoStack
  - `drawer.geo(source)` - Render to Drawer

## Where to Add New Code

**New Geometry Type:**
- Add to: `src/main/kotlin/geo/Geometry.kt`
- Add renderer: `src/main/kotlin/geo/render/{Type}Renderer.kt`
- Add tests: `src/test/kotlin/geo/GeometryTest.kt`

**New Projection:**
- Add class: `src/main/kotlin/geo/projection/Projection{Name}.kt`
- Implement: `GeoProjection` interface
- Register: `ProjectionFactory` or `ProjectionType` enum
- Add tests: `src/test/kotlin/geo/projection/ProjectionTest.kt`

**New Data Source:**
- Create class: Extends `GeoSource`
- Location: `src/main/kotlin/geo/`
- Add loader function: `geoSource()` or `GeoPackage.load()` pattern
- Add tests: `src/test/kotlin/geo/{Name}Test.kt`

**New Animation Feature:**
- Location: `src/main/kotlin/geo/animation/`
- Add interpolators to: `interpolators/` subpackage
- Add composition types to: `composition/` subpackage
- Add tests: `src/test/kotlin/geo/animation/`

**New Example:**
- Location: `examples/{category}/`
- Naming: `##-descriptive-name.kt` (numbered for ordering)
- Update: `examples/{category}/README.md`

**New Layer Type:**
- Implement: `GeoLayer` interface
- Location: `src/main/kotlin/geo/layer/`
- Pattern after: `Graticule.kt`

**New Tests:**
- Mirror structure: `src/test/kotlin/geo/` mirrors `src/main/kotlin/geo/`
- Naming: `{ClassUnderTest}Test.kt`
- Framework: JUnit 4

## Build System Details

**Module Catalogs:**
- OPENRNDR catalog: `openrndr.*` - Core OPENRNDR modules
- ORX catalog: `orx.*` - OPENRNDR extensions
- Versions parsed from `gradle/libs.versions.toml` via regex in `settings.gradle.kts`

**Convention Plugins:**
- Applied in `build.gradle.kts`:
  - `conventions.kotlin-jvm` - Kotlin JVM settings
  - `conventions.openrndr-tasks` - OPENRNDR tasks
  - `conventions.distribute-application` - Packaging

**Source Sets:**
- Main: `src/main/kotlin/` + `examples/` (examples compiled as part of main)
- Test: `src/test/kotlin/`

**Running Programs:**
```bash
./gradlew run                                    # Run App.kt (default)
./gradlew run -Popenrndr.application=AppKt       # Explicit main class
./gradlew run -Popenrndr.application=01_load_geojsonKt  # Run example
```

## Special Directories

**`buildSrc/`:**
- Purpose: Build logic as code
- Generated: Build outputs in `buildSrc/build/`
- Committed: Source yes, build no
- Note: Changes trigger full Gradle reconfiguration

**`examples/`:**
- Purpose: Runnable examples and tutorials
- Included: In main source set via `kotlin.srcDir("examples")`
- Categories: Each subdirectory is a feature area
- Data: `examples/data/geo/` contains sample geospatial files

**`examples/data/` vs `data/`:**
- `examples/data/` - Geospatial sample files for examples
- `data/` - Application assets (fonts, images) for main app

---

*Structure analysis: 2026-03-02*
