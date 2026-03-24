# Phase 19: Documentation Fixes - Context

**Gathered:** 2026-03-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Fix incorrect run commands in README files and source code "To Run" comments. All issues involve the `Kt` suffix being shown in gradle commands when `@file:JvmName` annotations mean the class names should be without suffix.
</domain>

<decisions>
## Implementation Decisions

### Scope
- Fix 23 source files (.kt) with incorrect `Kt` suffix in "To Run" comments
- Fix examples/README.md template lines with incorrect `Kt` suffix
- Verify data paths are correct

### Files to Fix

**Source files (23 files) - Remove `Kt` suffix from `./gradlew run -Popenrndr.application=...` comments:**

Core examples:
- `examples/core/01-load-geojson.kt` line 18: `LoadGeojsonKt` → `LoadGeojson`
- `examples/core/02-load-geopackage.kt` line 18: `LoadGeopackageKt` → `LoadGeopackage`
- `examples/core/03-print-summary.kt` line 21: `PrintSummaryKt` → `PrintSummary`
- `examples/core/04-geostack.kt` line 21: `GeoStackKt` → `GeoStack`
- `examples/core/05-batch-optimization.kt` line 28: `BatchOptimizationKt` → `BatchOptimization`

Projection examples:
- `examples/proj/01-mercator.kt` line 24: `MercatorKt` → `Mercator`
- `examples/proj/02-fit-bounds.kt` line 24: `FitBoundsKt` → `FitBounds`
- `examples/proj/03-crs-transform.kt` line 22: `CrsTransformKt` → `CrsTransform`

Render examples:
- `examples/render/01-points.kt` line 23: `PointsKt` → `Points`
- `examples/render/02-linestrings.kt` line 22: `LinestringsKt` → `Linestrings`
- `examples/render/03-polygons.kt` line 23: `PolygonsKt` → `Polygons`
- `examples/render/04-multipolygons.kt` line 25: `MultipolygonsKt` → `Multipolygons`
- `examples/render/05-style-dsl.kt` line 25: `StyleDslKt` → `StyleDsl`
- `examples/render/07-geostack-render.kt` line 34: `GeoStackRenderKt` → `GeoStackRender`
- `examples/render/08-feature-iteration.kt` line 26: `FeatureIterationKt` → `FeatureIteration`

Layer examples:
- `examples/layer/01-graticule.kt` line 27: `GraticuleKt` → `Graticule`
- `examples/layer/02-composition.kt` line 32: `CompositionKt` → `Composition`

Animation examples:
- `examples/anim/01-basic-animation.kt` line 26: `BasicAnimationKt` → `BasicAnimation`
- `examples/anim/02-geo-animator.kt` line 27: `GeoAnimatorKt` → `GeoAnimator`
- `examples/anim/03-timeline.kt` line 24: `TimelineKt` → `Timeline`
- `examples/anim/04-stagger-animator.kt` line 25: `StaggerAnimatorKt` → `StaggerAnimator`
- `examples/anim/05_chain-animations.kt` line 25: `ChainAnimationsKt` → `ChainAnimations`
- `examples/anim/06-linestring-color-anim.kt` line 27: `LineStringColorAnimKt` → `LineStringColorAnim`

**README files:**
- `examples/README.md` line 28: `FileNameKt` → `FileName`
- `examples/README.md` line 34: `ExampleNameKt` → `ExampleName`

### Data Path Verification
- `examples/README.md` line 39 shows `examples/data/geo/` — correct (no change needed)

### OpenCode's Discretion
- Batch replace pattern using search-and-replace across all files
- No research needed — clear mechanical fix
</decisions>

<code_context>
## Existing Code Insights

### Why `Kt` suffix is wrong
All example files use `@file:JvmName("ClassName")` which tells Kotlin to generate class name without the default `Kt` suffix. For example, `01-mercator.kt` has `@file:JvmName("Mercator")` so the correct class is `examples.proj.Mercator` NOT `examples.proj.MercatorKt`.

### Pattern
The "To Run" comment block format is consistent:
```
/**
 * ### To Run
 * ```
 * ./gradlew run -Popenrndr.application=examples.proj.MercatorKt
 * ```
 */
```

</code_context>

<specifics>
## Specific Ideas

No additional specific requirements — this is a mechanical documentation fix.
</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.
</deferred>

---

*Phase: 19-documentation-fixes*
*Context gathered: 2026-03-24*
