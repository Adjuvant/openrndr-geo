# Phase 9: API Design - Research

**Researched:** 2026-02-27
**Domain:** Kotlin DSL design, OpenRNDR API conventions, two-tier API patterns
**Confidence:** HIGH

## Summary

Phase 9 focuses on refining the public API to match OpenRNDR conventions while providing a clear two-tier experience (beginner vs professional). The existing codebase already has strong foundations: extension functions on `Drawer`, DSL-style builders (`Style { }`, `ProjectionMercator { }`), and a naming convention distinguishing "write" (internal) from "draw" (user API).

Key gaps to address: feature-level iteration with projected coordinates internalized, chainable operations (filter/map/forEach), per-feature styling via functions or maps, and raw projection escape hatch.

**Primary recommendation:** Extend existing `drawer.geo()` pattern with configuration block support, add `GeoSource.forEach/filter/map` chainable operations, and implement style resolution via function or map.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Feature Iteration:**
- Lambda-based iteration: `features.forEach { feature -> ... }`
- Hybrid projection: internalized as base case (screen coordinates), both geo and screen coordinates available
- Each iteration provides: feature + projected geometry (ready to render)
- Chainable operations: `filter`, `map`, `forEach` — leverage feature properties (e.g., height from dataset-specific naming conventions)

**Two-tier API:**
- Primary entry: `drawer.geo(source)` — generic, works for any GeoSource (GeoJSON, GeoPackage), render feature parity
- Same function with additional parameters for detailed control: `drawer.geo(source, projection = ..., style = ...)`
- Detailed API includes: projection control + styling + per-feature overrides
- Sensible defaults: minimal, black background, thin white lines

**Escape Hatches:**
- Direct geometry access via `feature.geometry`
- Naming convention: "write" prefix for internals, "draw" for user API
- Raw projection type for bypassing standard projections (not `project = false` flag)
- Both style options: style function per feature `style = { feature -> Style(...) }` AND style map by type `{ "Polygon" to polygonStyle }`

**OpenRNDR Conventions:**
- Builder with configuration blocks: `drawer.geo(source) { projection = ...; style = ... }`
- Extension functions on Drawer as primary entry point (matches `drawer.circle()`, `drawer.rectangle()`)
- Factory functions / DSL for configuration objects: `Mercator()`, `Style { stroke = ColorRGBa.RED }`
- Explicit, composable design — each piece does one thing, power from combining

### OpenCode's Discretion
- Exact syntax for chainable operations (filter/map/forEach)
- How raw projection type is configured
- Implementation details of style function vs style map precedence

### Deferred Ideas (OUT OF SCOPE)
Per-feature callback with layer-based styling (v1.3):
- Layer-based styling/ordering integration
- Conditional rendering per feature (skip or customize based on properties)
- Inline lambda in config block: `drawer.geo(source) { feature -> if (feature.properties["pop"] > 1000) drawCustom(feature) }`
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| API-01 | Feature-level iteration with projected coordinates internalized | GeoSource already has `features: Sequence<Feature>`; extend with `forEach/filter/map` returning projected geometry |
| API-02 | Simple workflow (beginner) and detailed control (professional) | `drawer.geo(source)` exists; add config block overload `drawer.geo(source) { }` |
| API-03 | Escape hatches for advanced/custom rendering patterns | `feature.geometry` access exists; add raw projection type, expose internal "write" functions |
| API-04 | API style matches OpenRNDR DSL conventions | Existing `Style { }` and `ProjectionMercator { }` patterns are canonical; follow exactly |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.0 | Language + DSL support | OpenRNDR requires it; builder patterns via lambda-with-receiver |
| OpenRNDR | (managed by template) | Graphics framework | Target platform |
| kotlinx.serialization | (in build.gradle.kts) | JSON parsing | Already used for GeoJSON |

### Existing API Patterns (follow these)
| Pattern | Location | Purpose |
|---------|----------|---------|
| `Style { }` DSL | `Style.kt:68` | Builder with `operator fun invoke(block: Style.() -> Unit)` |
| `ProjectionMercator { }` | `ProjectionMercator.kt:39` | Same builder pattern |
| Extension on Drawer | `DrawerGeoExtensions.kt` | Entry point pattern |
| "write" vs "draw" | `render.kt`, `PolygonRenderer.kt` | Internal vs public naming |

## Architecture Patterns

### Recommended API Surface

```
geo/
├── render/
│   ├── DrawerGeoExtensions.kt   # drawer.geo() entry points (MODIFY)
│   ├── GeoRenderConfig.kt       # NEW: configuration block holder
│   ├── Style.kt                 # EXISTING: style DSL
│   └── render.kt                # EXISTING: draw* public API
├── GeoSource.kt                 # MODIFY: add forEach/filter/map
└── Feature.kt                   # MODIFY: add projectedGeometry accessor
```

### Pattern 1: Configuration Block with Defaults

**What:** Single function with optional configuration block, sensible defaults when omitted.

**When to use:** Primary `drawer.geo()` entry point.

**Example:**
```kotlin
// Tier 1: Beginner - one-liner
drawer.geo(source)

// Tier 2: Professional - config block
drawer.geo(source) {
    projection = ProjectionMercator { width = 800.0; height = 600.0 }
    style = Style { fill = ColorRGBa.RED }
    styleByType = mapOf("Polygon" to polygonStyle, "LineString" to lineStyle)
    styleByFeature = { feature -> 
        if (feature.property("pop") as? Double > 1000000) Style { stroke = ColorRGBa.RED }
        else null  // fallback to styleByType or default
    }
}
```

**Implementation approach:**
```kotlin
fun Drawer.geo(source: GeoSource, block: (GeoRenderConfig.() -> Unit)? = null) {
    val config = GeoRenderConfig().apply { block?.invoke(this) }
    // ... render with config
}
```

### Pattern 2: Chainable Feature Operations

**What:** Extension functions on GeoSource returning new GeoSource (lazy sequences).

**When to use:** Pre-render filtering/transformation.

**Example:**
```kotlin
// Filter by property
source.filter { it.doubleProperty("population") > 100000 }
      .forEach { feature -> 
          // feature has projectedGeometry ready to render
      }

// With projection context
source.withProjection(projection).forEach { feature, projected ->
    drawer.drawPolygon(projected.points, styleFor(feature))
}
```

### Pattern 3: Style Resolution Chain

**What:** Cascading style resolution: per-feature > by-type > default.

**When to use:** When user provides multiple style options.

**Precedence:**
1. `styleByFeature(feature)` returns non-null → use it
2. `styleByType[geometryType]` exists → use it
3. `style` global override → use it
4. Geometry-type default → use `StyleDefaults`

**Example:**
```kotlin
data class GeoRenderConfig(
    var projection: GeoProjection? = null,
    var style: Style? = null,
    var styleByType: Map<String, Style> = emptyMap(),
    var styleByFeature: ((Feature) -> Style?)? = null
)

fun resolveStyle(feature: Feature, config: GeoRenderConfig): Style {
    return config.styleByFeature?.invoke(feature)
        ?: config.styleByType[feature.geometry.typeName]
        ?: config.style
        ?: StyleDefaults.forGeometry(feature.geometry)
}
```

### Pattern 4: Escape Hatch - Raw Projection

**What:** Marker type or interface for bypassing coordinate projection.

**When to use:** Data already in screen coordinates (e.g., pre-projected, raster overlays).

**Example:**
```kotlin
// Option A: Marker interface
object RawProjection : GeoProjection {
    override fun project(latLng: Vector2) = latLng  // Identity
    override fun unproject(screen: Vector2) = screen
}

// Usage
drawer.geo(source) {
    projection = RawProjection  // Bypass all projection math
}

// Option B: Dedicated function
drawer.geoRaw(source, style)  // No projection applied
```

### Anti-Patterns to Avoid

- **`project = false` flag:** Boolean flags are not explicit. Use dedicated type or function.
- **Mutable config during render:** Config should be resolved once before iteration.
- **Hiding Feature behind wrapper:** Users need direct access to `feature.geometry` and `feature.properties`.
- **Overloading by parameter count:** Use named parameters or config block, not `geo(source)`, `geo(source, proj)`, `geo(source, proj, style)`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Sequence operations | Custom iterator | Kotlin `Sequence` extensions | Already lazy, well-tested |
| DSL builder | Custom builder pattern | Lambda-with-receiver `T.() -> Unit` | Idiomatic Kotlin, used throughout OpenRNDR |
| Style merging | Merge logic in renderer | `StyleDefaults.merge(userStyle)` pattern | Already exists in `render.kt` |
| Projection fitting | Custom bounds calc | `ProjectionFactory.fitBounds()` | Already handles edge cases |

## Common Pitfalls

### Pitfall 1: Config Block Mutation
**What goes wrong:** User modifies config object inside render loop, causing inconsistent rendering.
**Why it happens:** Mutable config held by reference.
**How to avoid:** Snapshot config at render start: `val resolved = GeoRenderConfig().apply(block).snapshot()`
**Warning signs:** Rendering changes between frames without explicit changes.

### Pitfall 2: Style Resolution Ambiguity
**What goes wrong:** Multiple style sources conflict, unclear which wins.
**Why it happens:** No documented precedence chain.
**How to avoid:** Document and implement strict precedence: per-feature > by-type > global > default.
**Warning signs:** User asks "why isn't my style being applied?"

### Pitfall 3: Sequence Exhaustion
**What goes wrong:** Calling `forEach` then `filter` on same source yields empty results.
**Why it happens:** `Sequence` is single-use; each terminal operation exhausts it.
**How to avoid:** Document that GeoSource operations return NEW sources; for animation, use `materialize()` first.
**Warning signs:** "My data disappeared after I called forEach"

### Pitfall 4: Projection Not Fitted
**What goes wrong:** Beginner calls `drawer.geo(source)` and sees nothing or tiny dots.
**Why it happens:** Default projection center/scale doesn't match data bounds.
**How to avoid:** Auto-fit projection when null: `projection ?: ProjectionFactory.fitBounds(source.totalBoundingBox(), ...)`
**Warning signs:** "I loaded data but see blank screen"

## Code Examples

### Existing Pattern to Follow: Style DSL
```kotlin
// Source: Style.kt:68
companion object {
    operator fun invoke(block: Style.() -> Unit): Style {
        val style = Style()
        style.block()
        return style
    }
}
```

### Existing Pattern to Follow: ProjectionMercator Builder
```kotlin
// Source: ProjectionMercator.kt:39
companion object {
    operator fun invoke(block: ProjectionConfigBuilder.() -> Unit): ProjectionMercator {
        val builder = ProjectionConfigBuilder()
        builder.block()
        return ProjectionMercator(builder.build())
    }
}
```

### Proposed: GeoRenderConfig Builder
```kotlin
data class GeoRenderConfig(
    var projection: GeoProjection? = null,
    var style: Style? = null,
    var styleByType: Map<String, Style> = emptyMap(),
    var styleByFeature: ((Feature) -> Style?)? = null
) {
    companion object {
        operator fun invoke(block: GeoRenderConfig.() -> Unit): GeoRenderConfig {
            return GeoRenderConfig().apply(block)
        }
    }
}
```

### Proposed: Enhanced drawer.geo()
```kotlin
fun Drawer.geo(source: GeoSource, block: (GeoRenderConfig.() -> Unit)? = null) {
    val config = block?.let { GeoRenderConfig().apply(it) } ?: GeoRenderConfig()
    val proj = config.projection ?: ProjectionFactory.fitBounds(
        source.totalBoundingBox(),
        width.toDouble(),
        height.toDouble()
    )
    
    source.features.forEach { feature ->
        val resolvedStyle = resolveStyle(feature, config)
        drawGeometry(drawer, feature.geometry, proj, resolvedStyle)
    }
}
```

### Proposed: Feature with Projected Geometry
```kotlin
// Extension for iteration with projection context
data class ProjectedFeature(
    val feature: Feature,
    val projectedGeometry: ProjectedGeometry
)

sealed class ProjectedGeometry {
    abstract val screenPoints: List<Vector2>
}

fun GeoSource.withProjection(projection: GeoProjection): Sequence<ProjectedFeature> {
    return features.map { feature ->
        ProjectedFeature(feature, projectGeometry(feature.geometry, projection))
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `drawer.geoJSON(path)` | `drawer.geo(source)` | v1.2.0 | Generic entry point, format-agnostic |
| Implicit style | `Style { }` DSL | v1.0.0 | Explicit, mutable for animation |
| `project = false` flag | `RawProjection` type | v1.2.0 (this phase) | Type-safe escape hatch |

**Deprecated/outdated:**
- `drawer.geoJSON()` with `projection: GeoProjection?` parameter: Keep for backward compat, but prefer `drawer.geo(source) { }`

## Open Questions

1. **Should `styleByFeature` return `Style?` or `Style`?**
   - What we know: Returning `null` enables fallback chain.
   - What's unclear: Should user be forced to handle all cases?
   - Recommendation: Return `Style?` — null means "use default chain". Document clearly.

2. **Should chainable operations (`filter`, `map`) preserve CRS?**
   - What we know: GeoSource has `crs` property.
   - What's unclear: Does filtered source maintain same CRS reference?
   - Recommendation: Yes — filtering doesn't transform coordinates, CRS is preserved.

3. **Should `withProjection()` eagerly project or stay lazy?**
   - What we know: `features` is already a lazy Sequence.
   - What's unclear: Performance tradeoff for animation loops.
   - Recommendation: Stay lazy; document that for animation, use `materialize().withProjection()`.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 |
| Config file | None (standard JUnit) |
| Quick run command | `./gradlew test --tests "geo.render.*Test" -x compileTestKotlin` |
| Full suite command | `./gradlew test` |
| Estimated runtime | ~10 seconds |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|--------------|
| API-01 | Feature iteration with projected coords | unit | `./gradlew test --tests "geo.GeoSourceTest"` | ✅ yes (extend) |
| API-02 | Two-tier API (simple + config block) | unit | `./gradlew test --tests "geo.render.DrawerGeoExtensionsTest"` | ❌ Wave 0 gap |
| API-03 | Escape hatches (geometry access, RawProjection) | unit | `./gradlew test --tests "geo.render.EscapeHatchTest"` | ❌ Wave 0 gap |
| API-04 | DSL conventions match OpenRNDR | visual/manual | Run examples, compare to OpenRNDR style | ❌ Manual |

### Nyquist Sampling Rate
- **Minimum sample interval:** After every committed task → run: `./gradlew test`
- **Full suite trigger:** Before merging final task of any plan wave
- **Phase-complete gate:** Full suite green before `/gsd-verify-work` runs
- **Estimated feedback latency per task:** ~15 seconds

### Wave 0 Gaps (must be created before implementation)
- [ ] `src/test/kotlin/geo/render/DrawerGeoExtensionsTest.kt` — covers API-02 config block tests
- [ ] `src/test/kotlin/geo/render/EscapeHatchTest.kt` — covers API-03 RawProjection and geometry access
- [ ] `src/test/kotlin/geo/GeoSourceChainingTest.kt` — covers API-01 filter/map/forEach chainable operations

## Sources

### Primary (HIGH confidence)
- OpenRNDR source: `Style.kt`, `ProjectionMercator.kt` — builder patterns verified in codebase
- Project source: `DrawerGeoExtensions.kt` — existing extension function pattern
- Project source: `render.kt` — existing "write" vs "draw" naming convention

### Secondary (MEDIUM confidence)
- Kotlin docs: Lambda-with-receiver DSL patterns — standard Kotlin idiom
- OpenRNDR examples: `render_BasicRendering.kt` — existing usage patterns

### Tertiary (LOW confidence)
- None — all recommendations based on verified existing code patterns

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All patterns already exist in codebase
- Architecture: HIGH - Follows existing Style/ProjectionMercator patterns exactly
- Pitfalls: MEDIUM - Based on general Kotlin/sequence experience, not project-specific issues

**Research date:** 2026-02-27
**Valid until:** 2026-03-27 (stable Kotlin/OpenRNDR APIs)
