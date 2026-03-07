---
created: 2026-03-07T01:11:50.977Z
title: Clean up necro examples from src/main/kotlin/geo/examples
area: tooling
files:
  - src/main/kotlin/geo/examples/core_CRSTransformTest.kt
  - src/main/kotlin/geo/examples/core_DataLoadingTest.kt
  - src/main/kotlin/geo/examples/core_printSummary.kt
  - src/main/kotlin/geo/examples/layer_BlendModes.kt
  - src/main/kotlin/geo/examples/layer_Composition.kt
  - src/main/kotlin/geo/examples/layer_Graticule.kt
  - src/main/kotlin/geo/examples/layer_Output.kt
  - src/main/kotlin/geo/examples/proj_HaversineDemo.kt
  - src/main/kotlin/geo/examples/proj_ProjectionTest.kt
  - src/main/kotlin/geo/examples/render_BasicRendering.kt
  - src/main/kotlin/geo/examples/render_LiveRendering.kt
  - examples/core/03-print-summary.kt
  - examples/layer/01-graticule.kt
  - examples/layer/02-composition.kt
  - examples/core/04-geostack.kt
  - examples/core/05-batch-optimization.kt
  - examples/render/06-quick-geo.kt
  - examples/render/07-geostack-render.kt
---

## Problem

The project has necro (legacy/zombie) example files in `src/main/kotlin/geo/examples/` that need to be cleaned up. These files have the old naming convention (snake_case like `core_CRSTransformTest.kt`) instead of the new convention (kebab-case like `04-crs-transform.kt`). Some of these are duplicates of existing examples in the root `examples/` directory, while others are unique examples that should be preserved.

## Files Analysis

### MOVE to examples/ (not duplicates):
1. `core_CRSTransformTest.kt` → `examples/core/04-crs-transform-test.kt` (new)
2. `core_DataLoadingTest.kt` → `examples/core/05-data-loading-test.kt` (new)
3. `layer_BlendModes.kt` → `examples/layer/03-blend-modes.kt` (new - demonstrates all four blend modes with visual comparison)
4. `layer_Output.kt` → `examples/layer/04-output.kt` (new)
5. `proj_HaversineDemo.kt` → `examples/proj/04-haversine.kt` (new)
6. `proj_ProjectionTest.kt` → `examples/proj/05-projection-test.kt` (new)
7. `render_BasicRendering.kt` → `examples/render/08-basic-rendering.kt` (new)
8. `render_LiveRendering.kt` → `examples/render/09-live-rendering.kt` (new)

### DELETE (duplicates already exist in examples/):
1. `core_printSummary.kt` - DUPLICATE of `examples/core/03-print-summary.kt` (both load GeoJSON/GeoPackage and call printSummary())
2. `layer_Composition.kt` - DUPLICATE of `examples/layer/02-composition.kt`
3. `layer_Graticule.kt` - DUPLICATE of `examples/layer/01-graticule.kt`

### Next Available Numbers (verified):
- examples/core/: next is 04 (has 01, 02, 03, 04, 05 - check if 04/05 are the ones being moved or already exist)
- examples/layer/: next is 03 (has 01, 02)
- examples/proj/: next is 04 (has 01, 02, 03)
- examples/render/: next is 08 (has 01-07)

## Solution

**Required: User verification at each step**

Process:
1. For each file in src/main/kotlin/geo/examples/:
   - Check if a functionally equivalent example exists in root examples/
   - If YES → Delete after user confirmation
   - If NO → Move to appropriate examples/ subfolder with next available number

2. When moving, rename from snake_case to kebab-case convention:
   - Example: `core_CRSTransformTest.kt` → `04-crs-transform-test.kt`
   - Update package declaration: `package geo.examples` → `package examples.{folder}`
   - Add @file:JvmName annotation if not present

3. After all files processed → Delete empty `src/main/kotlin/geo/examples/` directory

## Context

Root examples/ uses a cleaner structure:
- Each example has a numbered prefix (01-, 02-, etc.)
- Kebab-case naming (basic-animation.kt not BasicAnimation.kt)
- Proper package structure: `package examples.{folder}`
- @file:JvmName annotation for clean gradle run commands
- Comprehensive KDoc headers explaining concepts and run commands

The necro examples in src/main/kotlin/geo/examples/:
- Use old snake_case naming (layer_BlendModes.kt)
- Are missing proper package declarations and headers
- May have outdated data paths (check "data/sample.geojson" vs "examples/data/geo/sample.geojson")
- Some have valuable content like layer_BlendModes.kt which demonstrates all blend modes (Multiply, Overlay, Screen, Add) in a 4-quadrant visual comparison

## Action Items

- [ ] Verify each file is duplicate or unique
- [ ] Get user confirmation before each delete
- [ ] Move unique files with proper naming
- [ ] Update package declarations and headers
- [ ] Fix any outdated data paths
- [ ] Delete src/main/kotlin/geo/examples/ directory when empty
- [ ] Update any references in README or documentation
