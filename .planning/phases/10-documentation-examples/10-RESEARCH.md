# Phase 10: Documentation & Examples - Research

**Researched:** 2026-02-27
**Domain:** Kotlin/OPENRNDR geospatial library documentation, runnable examples, sample data organization
**Confidence:** HIGH

## Summary

This phase creates a structured examples directory that teaches users the library API through runnable code. The examples must be reorganized from the current location (`src/main/kotlin/geo/examples/`) to a new root `examples/` folder with category subfolders. Each example demonstrates ONE concept, uses KDoc for IDE hover support, and includes README files per category.

**Primary recommendation:** Create numbered, single-concept examples in `examples/{category}/NN-name.kt` structure with KDoc comments, per-category READMEs, and data files in `examples/data/geo/`. Replace outdated existing examples entirely.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Category folders with numbered files: `examples/render/01-polygon.kt`
- Numbered descriptive names: `01-xxx.kt`, `02-xxx.kt`
- Two tiers: minimal examples for single concepts, full-workflow examples for complete flows
- Categories mirror source packages: `core_`, `render_`, `proj_`, `anim_`, `layer_`
- Current examples folder may contain outdated code — replace as needed, no need to preserve
- Sample data: Mix of synthetic/hand-crafted and real-world data
- Data stored in `examples/data/geo/` (geo subdirectory for future extensibility)
- Formats: GeoJSON + GeoPackage
- KDoc comments in each example (IDE hover support)
- README.md per category folder (overview, list of examples, concepts covered)
- Show just the essential code — minimal boilerplate, focus on key API calls
- Standalone examples — no external links to API docs
- Individual `main()` functions per example
- Run via `./gradlew run --args="render/01-polygon"` style
- Visual output in OPENRNDR window (user sees rendered result)
- Zero config required — just Gradle
- No batch "run all" task needed (run individually only)
- Examples must link to current API level (not outdated patterns)
- WIP/incomplete features should be noted in examples
- Examples serve as tool for owner to plan new features and identify shortcomings

### OpenCode's Discretion
- Exact naming of categories if source packages differ
- How to handle examples that span multiple categories
- Console output format for non-visual examples
- Whether to include a root examples/README.md

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope

</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| DOC-01 | User can browse examples organized by category (core_, render_, proj_, anim_, layer_) | Directory structure `examples/{category}/` mirrors source packages; numbered files for ordering |
| DOC-02 | User can run examples with included data files (small GeoJSON/GeoPackage) | Data files in `examples/data/geo/`; synthetic + real-world samples available |
| DOC-03 | Each example demonstrates ONE feature/concept | Minimal examples pattern with focused KDoc; single-concept files |
| DOC-04 | Examples serve as UAT validation for framework features | Examples exercise actual API; visual output confirms correct behavior |

</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OPENRNDR | 0.4.x | Creative coding framework | Project foundation |
| Kotlin | 2.0 | Primary language | JVM target, DSL patterns |
| GeoPackage (mil.nga) | 6.6.5 | Spatial data format support | Standard Java library for .gpkg |
| Proj4J | 1.4.1 | CRS transformations | EPSG database support |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| orx-composition | (orx version) | Layer composition | Multi-layer examples |
| orx-shade-styles | (orx version) | Custom shading | Advanced rendering |
| kotlinx-serialization | 1.x | JSON parsing | GeoJSON support |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Root `examples/` folder | Keep in `src/main/kotlin/geo/examples/` | New structure provides cleaner separation; user expects examples at project root |
| Numbered files | Alphabetical naming | Numbers provide explicit ordering for learning path |
| KDoc comments | External markdown docs | KDoc provides IDE hover support; keeps docs with code |

**No new installation required** — examples use existing project dependencies.

## Architecture Patterns

### Recommended Project Structure
```
examples/
├── data/
│   └── geo/                    # Sample data files
│       ├── sample.geojson      # Small synthetic dataset (Netherlands cities)
│       ├── coastline.geojson   # World coastlines
│       ├── ocean.geojson       # Ocean polygons
│       └── *.gpkg              # GeoPackage files
├── core/                       # Core functionality (data loading, inspection)
│   ├── README.md
│   ├── 01-load-geojson.kt
│   ├── 02-load-geopackage.kt
│   └── 03-print-summary.kt
├── render/                     # Rendering examples
│   ├── README.md
│   ├── 01-points.kt
│   ├── 02-linestrings.kt
│   ├── 03-polygons.kt
│   ├── 04-multipolygons.kt
│   └── 05-style-dsl.kt
├── proj/                       # Projection examples
│   ├── README.md
│   ├── 01-mercator.kt
│   ├── 02-fit-bounds.kt
│   └── 03-crs-transform.kt
├── anim/                       # Animation examples
│   ├── README.md
│   ├── 01-basic-tween.kt
│   ├── 02-geo-animator.kt
│   └── 03-timeline.kt
├── layer/                      # Layer/composition examples
│   ├── README.md
│   ├── 01-graticule.kt
│   └── 02-composition.kt
└── README.md                   # Root examples guide (optional)
```

### Pattern 1: Minimal Example Structure
**What:** Single-concept example with KDoc header and focused code
**When to use:** All minimal examples (tier 1)
**Example:**
```kotlin
package examples.render

import geo.GeoJSON
import geo.Point
import geo.render.drawPoint
import geo.render.Style
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

/**
 * ## 01 - Drawing Points
 *
 * Demonstrates the simplest rendering: loading data and drawing points.
 *
 * ### Concepts
 * - Loading GeoJSON with `GeoJSON.load()`
 * - Creating a projection with `ProjectionFactory.fitBounds()`
 * - Drawing points with `drawPoint()`
 * - Using `Style { }` DSL for styling
 *
 * ### To Run
 * ```
 * ./gradlew run --args="render/01-points"
 * ```
 */
fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        // Load sample data
        val source = GeoJSON.load("examples/data/geo/sample.geojson")
        
        // Create projection fitted to data bounds
        val projection = ProjectionFactory.fitBounds(
            bounds = source.totalBoundingBox(),
            width = width.toDouble(),
            height = height.toDouble()
        )
        
        // Define point style using DSL
        val style = Style {
            fill = ColorRGBa.RED
            stroke = ColorRGBa.BLACK
            size = 8.0
        }

        extend {
            drawer.clear(ColorRGBa.WHITE)
            
            source.features.forEach { feature ->
                if (feature.geometry is Point) {
                    val screen = projection.project(
                        Vector2(feature.geometry.x, feature.geometry.y)
                    )
                    drawPoint(drawer, screen, style)
                }
            }
        }
    }
}
```

### Pattern 2: Category README Structure
**What:** Per-category README with overview and example list
**When to use:** Every category folder
**Example:**
```markdown
# Render Examples

Examples demonstrating the rendering API for geographic features.

## Overview

The render module provides functions for drawing geographic primitives:
- `drawPoint()` - Draw single points
- `drawLineString()` - Draw line features
- `drawPolygon()` - Draw polygon features (with hole support)
- `drawMultiPolygon()` - Draw multi-polygon features

## Examples

| File | Concept | Data Used |
|------|---------|-----------|
| 01-points.kt | Point rendering, Style DSL | sample.geojson |
| 02-linestrings.kt | Line rendering, stroke styling | coastline.geojson |
| 03-polygons.kt | Polygon fill and stroke | sample.geojson |
| 04-multipolygons.kt | MultiPolygon with holes | sample.geojson |
| 05-style-dsl.kt | Advanced Style configuration | sample.geojson |

## Key Concepts

1. **Style DSL**: Use `Style { }` block to configure appearance
2. **Projection**: All rendering requires screen coordinates via projection
3. **Geometry Types**: Use `when` to handle different geometry types
```

### Anti-Patterns to Avoid
- **Kitchen sink examples:** Files showing 5+ concepts become overwhelming; split into multiple files
- **Missing KDoc:** Users can't discover documentation via IDE hover
- **Hardcoded paths:** Use `examples/data/geo/` consistently, not `data/` or absolute paths
- **Outdated API usage:** Examples must use current API (e.g., `Style { }` DSL, not deprecated patterns)
- **No visual output:** Console-only examples need clear output format guidance

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Sample data | Create new synthetic datasets | Existing `data/geo/` files | Already tested, known to work |
| Test framework | Custom validation | JUnit + existing tests | Tests already exist for UAT |
| Documentation generator | Custom doc tool | KDoc + README.md | Standard tooling, IDE support |

**Key insight:** This phase is primarily about organization and documentation, not new code. Reuse existing patterns from current examples.

## Common Pitfalls

### Pitfall 1: Running Examples from Wrong Directory
**What goes wrong:** `./gradlew run --args="render/01-points"` fails with "file not found"
**Why it happens:** Working directory is not project root, or path is incorrect
**How to avoid:** Always run from project root; use `examples/data/geo/` paths in code
**Warning signs:** FileNotFoundException for data files

### Pitfall 2: Outdated API Patterns in Examples
**What goes wrong:** Examples use deprecated or removed API methods
**Why it happens:** Existing examples in `src/main/kotlin/geo/examples/` may be stale
**How to avoid:** Check current API in `src/main/kotlin/geo/` before writing examples; verify against tests
**Warning signs:** Compiler warnings, deprecated annotations

### Pitfall 3: Multi-Concept Examples
**What goes wrong:** Single example tries to show projection, styling, animation, and layers
**Why it happens:** "This would be a cool demo" mindset instead of teaching mindset
**How to avoid:** Each file = ONE concept; create separate files for combinations
**Warning signs:** Example file > 100 lines, multiple `when` blocks

### Pitfall 4: Missing Data File Documentation
**What goes wrong:** User can't find or understand the data files used in examples
**Why it happens:** Assumption that data files are self-explanatory
**How to avoid:** Document data source, CRS, and feature types in README or KDoc
**Warning signs:** User asks "where did this data come from?"

## Code Examples

### Data Loading Pattern (Core)
```kotlin
// From existing core_printSummary.kt
val jsonDataSource = GeoJSON.load("data/sample.geojson")
jsonDataSource.printSummary()

val gpkgDataSource = GeoPackage.load("data/geo/ness-vectors.gpkg")
gpkgDataSource.printSummary()
```

### Rendering Pattern (Render)
```kotlin
// Style DSL - current API pattern
val pointStyle = Style {
    fill = ColorRGBa.RED
    stroke = ColorRGBa.BLACK
    size = 5.0
    shape = Shape.Circle
}

// Drawing
drawPoint(drawer, screenCoords, pointStyle)
```

### Projection Pattern (Proj)
```kotlin
// Factory methods for projections
val projection = ProjectionFactory.fitBounds(
    bounds = source.totalBoundingBox(),
    width = width.toDouble(),
    height = height.toDouble(),
    padding = 50.0,
    projection = ProjectionType.MERCATOR
)

// Or world-scale
val worldProjection = ProjectionFactory.fitWorldMercator(width, height)
```

### Animation Pattern (Anim)
```kotlin
// GeoAnimator pattern
val animator = animator()
animator.apply {
    ::x.animate(400.0, 2000, Easing.CubicInOut)
    ::y.animate(300.0, 2000, Easing.CubicOut)
}

extend {
    animator.updateAnimation()  // CRITICAL: call each frame
    drawer.circle(animator.x, animator.y, 50.0)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Inline style objects | `Style { }` DSL builder | Phase 9 | Cleaner, mutable for animation |
| Manual projection config | `ProjectionFactory.fitBounds()` | Phase 3 | Auto-fit to data |
| `src/main/kotlin/geo/examples/` | `examples/` root folder | Phase 10 (this) | Cleaner separation, user-facing |

**Deprecated/outdated:**
- Old example location: `src/main/kotlin/geo/examples/` — move to `examples/`
- Non-numbered files: `render_BasicRendering.kt` — rename to `01-basic-rendering.kt`

## Open Questions

1. **How to handle Gradle task for running examples?**
   - What we know: Build uses `./gradlew run -Popenrndr.application=package.ClassNameKt`
   - What's unclear: How to map `--args="render/01-points"` to actual main class
   - Recommendation: May need custom Gradle task or script to resolve example path to class name

2. **Root README.md in examples/?**
   - What we know: User discretion allows this
   - What's unclear: Whether it adds value vs. just per-category READMEs
   - Recommendation: Create a simple index README listing all categories with brief descriptions

3. **Console output format for non-visual examples (core category)?**
   - What we know: Some examples like `printSummary()` don't open windows
   - What's unclear: Standard output format for consistency
   - Recommendation: Use box-drawing characters (as in existing `printSummary()`), clear section headers

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 |
| Config file | None (standard Gradle test) |
| Quick run command | `./gradlew test --tests "geo.*"` |
| Full suite command | `./gradlew test` |
| Estimated runtime | ~10-30 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DOC-01 | Examples organized by category | Manual (directory inspection) | `ls -la examples/` | ❌ Wave 0 gap |
| DOC-02 | Examples run with data files | Integration | `./gradlew run -Popenrndr.application=...` | ❌ Wave 0 gap |
| DOC-03 | Each example demonstrates ONE concept | Manual (code review) | N/A | N/A |
| DOC-04 | Examples validate framework features | UAT (visual) | Manual run + inspection | ❌ Wave 0 gap |

### Nyquist Sampling Rate
- **Minimum sample interval:** After each example file created → verify it compiles: `./gradlew compileKotlin`
- **Full suite trigger:** Before phase completion → run all examples manually
- **Phase-complete gate:** All examples compile, run without exceptions, show expected output
- **Estimated feedback latency per task:** ~15-30 seconds (compile check)

### Wave 0 Gaps (must be created before implementation)
- [ ] `examples/` directory structure — verify with `mkdir -p examples/{core,render,proj,anim,layer,data/geo}`
- [ ] Manual test procedure document — how to verify each example runs correctly
- [ ] Data file copy/setup — ensure `examples/data/geo/` has required files

**Note:** Documentation phases have limited automated testing. Primary validation is:
1. Compile check: All examples compile without errors
2. Run check: Each example runs without exceptions
3. Visual check: Rendered output matches expected (manual)

## Sources

### Primary (HIGH confidence)
- Project source code: `src/main/kotlin/geo/` - examined API patterns
- Existing examples: `src/main/kotlin/geo/examples/` - examined current structure
- Build configuration: `build.gradle.kts` - verified Gradle task patterns
- CONTEXT.md: User decisions locked for this phase

### Secondary (MEDIUM confidence)
- OPENRNDR documentation patterns (training knowledge) - example structure conventions
- Kotlin KDoc conventions (training knowledge) - documentation format

### Tertiary (LOW confidence)
- None — all findings verified against project source

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All libraries are existing project dependencies, no new installs
- Architecture: HIGH - Clear pattern from existing examples and user decisions
- Pitfalls: HIGH - Based on observed issues in current examples and common documentation patterns

**Research date:** 2026-02-27
**Valid until:** 30 days (stable API, documentation patterns don't change frequently)
