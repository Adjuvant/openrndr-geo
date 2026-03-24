---
phase: 18-code-organization
plan: "01"
subsystem: examples
tags: [cleanup, file-move, file-delete, necro-examples]
dependency_graph:
  requires: []
  provides: [ORG-01]
  affects: []
tech_stack:
  added: []
  patterns: [git-move, git-rm]
key_files:
  created:
    - examples/layer/layer_BlendModes.kt
    - examples/layer/layer_Output.kt
    - examples/proj/proj_HaversineDemo.kt
    - examples/render/render_LiveRendering.kt
  modified: []
decisions:
  - "Moved layer_BlendModes.kt and layer_Output.kt to examples/layer/ (blend modes and output examples)"
  - "Moved proj_HaversineDemo.kt to examples/proj/ (haversine interpolation example)"
  - "Moved render_LiveRendering.kt to examples/render/ (olive live-rendering example)"
  - "Deleted 6 necro/duplicate files: core_CRSTransformTest, core_DataLoadingTest, core_printSummary, layer_Composition, layer_Graticule, proj_ProjectionTest"
requirements_completed: [ORG-01]
metrics:
  duration: "~2 minutes"
  completed: "2026-03-24T22:02:02Z"
---

# Phase 18 Plan 01: Clean Up Necro Examples

## One-liner
Cleaned up 10 necro examples from src/main/kotlin/geo/examples/ — merged 4 into examples/ subdirectories, deleted 6 as duplicates.

## Performance

- **Duration:** ~2 min
- **Started:** 2026-03-24T22:00:41Z
- **Completed:** 2026-03-24T22:02:02Z
- **Tasks:** 2
- **Files modified:** 10 (4 moved, 6 deleted)

## Accomplishments
- 4 necro examples moved to examples/ subdirectories (layer/, proj/, render/)
- 6 necro/duplicate examples deleted from src/main/kotlin/geo/examples/
- src/main/kotlin/geo/examples/ directory removed (all files cleaned up)
- examples/ directory expanded with useful example files

## task Commits

Each task was committed atomically:

1. **task 1: Move 4 necro files to examples/ subdirectories** - `e86171e` (feat)
2. **task 2: Delete 6 necro example files** - `2497f62` (part of 18-02 refactor commit)

## Files Created/Modified

**Moved (4 files):**
- `examples/layer/layer_BlendModes.kt` - Blend modes visual comparison example
- `examples/layer/layer_Output.kt` - Screenshot capture workflow example
- `examples/proj/proj_HaversineDemo.kt` - Haversine interpolation visualizer
- `examples/render/render_LiveRendering.kt` - Live-coding with olive hot reload

**Deleted (6 files):**
- `src/main/kotlin/geo/examples/core_CRSTransformTest.kt` (covered by examples/proj/03-crs-transform.kt)
- `src/main/kotlin/geo/examples/core_DataLoadingTest.kt` (covered by examples/core/01-load-geojson.kt, 02-load-geopackage.kt)
- `src/main/kotlin/geo/examples/core_printSummary.kt` (covered by examples/core/03-print-summary.kt)
- `src/main/kotlin/geo/examples/layer_Composition.kt` (covered by examples/layer/02-composition.kt)
- `src/main/kotlin/geo/examples/layer_Graticule.kt` (covered by examples/layer/01-graticule.kt)
- `src/main/kotlin/geo/examples/proj_ProjectionTest.kt` (covered by examples/proj/)

## Decisions Made

- Moved layer_BlendModes.kt and layer_Output.kt to examples/layer/ (blend modes and output examples)
- Moved proj_HaversineDemo.kt to examples/proj/ (haversine interpolation example)
- Moved render_LiveRendering.kt to examples/render/ (olive live-rendering example)
- Deleted 6 necro/duplicate files that are covered by existing examples

## Deviations from Plan

None - plan executed as specified. All 4 files moved, all 6 files deleted.

**Note:** The 6 necro files were deleted as part of commit `2497f62` which also contained 18-02 package refactoring. The deletion was done atomically with the package updates.

## Verification Results

```
$ ls examples/layer/layer_BlendModes.kt examples/layer/layer_Output.kt \
       examples/proj/proj_HaversineDemo.kt examples/render/render_LiveRendering.kt
4 files exist

$ ls src/main/kotlin/geo/examples/*.kt 2>/dev/null | wc -l
0 (src/main/kotlin/geo/examples/ directory removed)
```

## Commits

- `e86171e`: feat(18-01): move 4 necro examples to examples/ subdirectories
- `2497f62`: refactor(18-02): update package declarations to geo.core (includes deletion of 6 necro files)

## Self-Check: PASSED

