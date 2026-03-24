---
phase: 18-code-organization
plan: "02"
subsystem: geo.core
tags: [refactoring, package-organization, file-move]
dependency_graph:
  requires: []
  provides: [ORG-02]
  affects: []
tech_stack:
  added: []
  patterns: [package-rename, git-move]
key_files:
  created:
    - src/main/kotlin/geo/core/ (directory)
  modified:
    - src/main/kotlin/geo/core/Bounds.kt
    - src/main/kotlin/geo/core/CachedGeoSource.kt
    - src/main/kotlin/geo/core/Feature.kt
    - src/main/kotlin/geo/core/GeoJSON.kt
    - src/main/kotlin/geo/core/Geometry.kt
    - src/main/kotlin/geo/core/GeoPackage.kt
    - src/main/kotlin/geo/core/GeoSource.kt
    - src/main/kotlin/geo/core/GeoSourceConvenience.kt
    - src/main/kotlin/geo/core/GeoStack.kt
    - src/main/kotlin/geo/core/ProjectionExtensions.kt
    - src/main/kotlin/geo/core/SpatialIndex.kt
    - src/main/kotlin/geo/core/loadGeo.kt
    - src/main/kotlin/geo/core/project.kt
decisions: []
metrics:
  duration: "~1 minute"
  completed: "2026-03-24T22:00:58Z"
---

# Phase 18 Plan 02: Move Geo Files to geo.core Subpackage

## One-liner
Reorganized 13 geo root files into a flat `geo.core` subpackage for better package structure.

## Completed Tasks

| task | Name | Commit | Files |
| ---- | ---- | ------ | ----- |
| 1 | Create core/ subdirectory and move 13 files | e425174 | src/main/kotlin/geo/core/ (13 files) |
| 2 | Update package declarations to geo.core | 2497f62 | src/main/kotlin/geo/core/*.kt (13 files) |

## Truths Validated
- ✅ 13 geo root files moved into geo.core/ subdirectory
- ✅ All 13 files maintain flat structure (no sub-subdirectories)
- ✅ All files declare `package geo.core`

## Deviations from Plan
None - plan executed exactly as written.

## Verification Results
```
$ ls src/main/kotlin/geo/core/*.kt | wc -l
13

$ grep "^package geo.core" src/main/kotlin/geo/core/*.kt | wc -l
13

$ ls src/main/kotlin/geo/*.kt 2>/dev/null | wc -l
0 (no .kt files remain in geo root)
```

## Commits
- `e425174`: feat(18-02): move 13 geo root files into geo.core/ subdirectory
- `2497f62`: refactor(18-02): update package declarations to geo.core

## Self-Check: PASSED
