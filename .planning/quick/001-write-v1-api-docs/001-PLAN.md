---
phase: quick
plan: 001
type: execute
wave: 1
depends_on: []
files_modified:
  - docs/API.md
  - README.md
autonomous: true

must_haves:
  truths:
    - "Developer can discover all API modules from docs"
    - "Developer can find quick-start code examples"
    - "All public classes and functions are documented"
  artifacts:
    - path: "docs/API.md"
      provides: "Comprehensive API reference"
      min_lines: 200
  key_links:
    - from: "README.md"
      to: "docs/API.md"
      via: "markdown link"
      pattern: "\\[API Reference\\]"
---

<objective>
Write comprehensive v1.0.0 API documentation covering all five library modules: Data Layer, Coordinate Systems, Rendering, Layer System, and Animation.

Purpose: Developers need clear documentation to discover and use the openrndr-geo API effectively without reading source code.
Output: Complete API reference document with code examples, organized by module.
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
@~/.config/opencode/get-shit-done/templates/summary.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/STATE.md

# Existing docs
@docs/rendering.md

# Core API files to document
@src/main/kotlin/geo/Geometry.kt
@src/main/kotlin/geo/Feature.kt
@src/main/kotlin/geo/Bounds.kt
@src/main/kotlin/geo/GeoSource.kt
@src/main/kotlin/geo/GeoJSON.kt
@src/main/kotlin/geo/GeoPackage.kt
@src/main/kotlin/geo/projection/GeoProjection.kt
@src/main/kotlin/geo/projection/ProjectionFactory.kt
@src/main/kotlin/geo/projection/CRSTransformer.kt
@src/main/kotlin/geo/projection/CRSExtensions.kt
@src/main/kotlin/geo/render/Style.kt
@src/main/kotlin/geo/render/render.kt
@src/main/kotlin/geo/layer/GeoLayer.kt
@src/main/kotlin/geo/layer/Graticule.kt
@src/main/kotlin/geo/animation/GeoAnimator.kt
@src/main/kotlin/geo/animation/Tweening.kt
@src/main/kotlin/geo/animation/ProceduralMotion.kt
@src/main/kotlin/geo/animation/composition/GeoTimeline.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Create comprehensive API reference</name>
  <files>docs/API.md</files>
  <action>
    Create `docs/API.md` with the following structure:

    ```markdown
    # openrndr-geo API Reference v1.0.0
    
    ## Quick Start
    [Minimal example loading GeoJSON and rendering]
    
    ## Modules
    
    ### 1. Data Layer
    - Geometry sealed class (Point, LineString, Polygon, Multi*)
    - Feature with properties access
    - Bounds for bounding boxes
    - GeoSource abstraction
    - GeoJSON loader
    - GeoPackage loader
    - CRS transformation extensions
    
    ### 2. Coordinate Systems
    - GeoProjection interface
    - ProjectionFactory (mercator, equirectangular)
    - ProjectionBNG for British National Grid
    - CRSTransformer for EPSG code transformations
    - CRSExtensions fluent API (toWGS84, toWebMercator, materialize)
    
    ### 3. Rendering
    [Link to existing docs/rendering.md - summarize key functions]
    - Style class with DSL syntax
    - drawPoint, drawLineString, drawPolygon
    - drawMultiPoint, drawMultiLineString, drawMultiPolygon
    - Shape enum for point markers
    
    ### 4. Layer System
    - GeoLayer for compositing
    - Graticule for coordinate grid
    
    ### 5. Animation
    - GeoAnimator singleton
    - EasingExtensions (15 convenience functions)
    - Tweening with Position class
    - Interpolators (Linear, Haversine for geo)
    - ProceduralMotion (staggerByIndex, staggerByDistance)
    - Composition (GeoTimeline, ChainedAnimation)
    ```

    For each class/function include:
    - Brief description
    - Code example showing typical usage
    - Key parameters/options

    Extract API details from KDoc in source files. Keep examples concise (5-15 lines).
  </action>
  <verify>
    wc -l docs/API.md returns 200+ lines
    grep -c "##" docs/API.md returns 10+ (has module sections)
    grep -c "```kotlin" docs/API.md returns 15+ (has code examples)
  </verify>
  <done>
    docs/API.md exists with 5 module sections, each containing class descriptions and runnable code examples.
  </done>
</task>

<task type="auto">
  <name>Task 2: Update README with documentation links</name>
  <files>README.md</files>
  <action>
    Update README.md to add a Documentation section near the top (after the existing content):

    Add after line 8 (after the guide.openrndr.org reference):

    ```markdown
    ## Documentation

    - **[API Reference](docs/API.md)** - Complete v1.0.0 API documentation with code examples
    - **[Rendering Guide](docs/rendering.md)** - Detailed rendering API with styling options

    ## openrndr-geo

    This project includes openrndr-geo, a Kotlin library for creative geospatial visualization:

    - Load GeoJSON and GeoPackage data
    - Project coordinates (Mercator, BNG, custom EPSG codes)
    - Render geometries with configurable styles
    - Animate features with easing and stagger effects
    ```

    The README should clearly indicate this is not just a template but a geo visualization library.
  </action>
  <verify>
    grep -q "API Reference" README.md
    grep -q "openrndr-geo" README.md
    grep -q "docs/API.md" README.md
  </verify>
  <done>
    README.md links to docs/API.md and introduces the openrndr-geo library.
  </done>
</task>

</tasks>

<verification>
- docs/API.md covers all 5 modules (Data, CRS, Rendering, Layer, Animation)
- Each module has at least 2 code examples
- README.md links to the new documentation
</verification>

<success_criteria>
- Developer can read docs/API.md and understand the full API surface
- Code examples are copy-pasteable and demonstrate common patterns
- README clearly positions this as a geo visualization library
</success_criteria>

<output>
After completion, create `.planning/quick/001-write-v1-api-docs/001-SUMMARY.md`
</output>
