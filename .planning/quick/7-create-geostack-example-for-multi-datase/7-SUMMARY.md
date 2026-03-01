---
phase: quick
plan: 7
subsystem: examples
tags: [geostack, multi-dataset, core-example, crs-unification]
dependency_graph:
  requires: []
  provides: [GeoStack example documentation]
  affects: [examples/core/README.md]
tech-stack:
  added: []
  patterns: [Box-drawing console output, KDoc documentation]
key-files:
  created:
    - examples/core/04-geostack.kt
  modified:
    - examples/core/README.md
decisions: []
metrics:
  duration: 5m
  completed-date: 2026-03-01
---

# Quick Task 7: Create GeoStack Example for Multi-Dataset Composition

**One-liner:** Created a core example demonstrating GeoStack multi-dataset composition with automatic CRS unification across 3 GeoJSON sources.

## What Was Built

A new core example `04-geostack.kt` that demonstrates:

1. **Multi-dataset loading**: Loads 3 different GeoJSON files (sample.geojson, populated_places.geojson, rivers_lakes.geojson)
2. **GeoStack composition**: Uses `geoStack()` function to combine sources
3. **Stack inspection**: Shows source count, unified CRS, total feature count, and combined bounding box
4. **Console output**: Box-formatted summary matching pandas-style from printSummary()

## Changes Made

### New File: `examples/core/04-geostack.kt`
- 55 lines
- `@file:JvmName("GeoStack")` annotation for proper Kotlin class naming
- Follows existing core example patterns (KDoc header, package declaration, main function)
- Demonstrates all GeoStack capabilities: `sourceCount()`, `crs`, `features`, `totalBoundingBox()`
- Run command: `./gradlew run -Popenrndr.application=examples.core.GeoStackKt`

### Modified: `examples/core/README.md`
- Added 04-geostack.kt to examples table with data files listed
- Added 3 new Key Concepts entries for GeoStack functionality

## Commits

| Hash | Message | Files |
|------|---------|-------|
| 91c072d | feat(quick-7): add GeoStack multi-dataset composition example | examples/core/04-geostack.kt |
| 0b2c6dd | docs(quick-7): update README with GeoStack example | examples/core/README.md |

## Deviations from Plan

None - plan executed exactly as written.

## Self-Check: PASSED

- [x] File examples/core/04-geostack.kt exists and has correct structure
- [x] File uses @file:JvmName annotation
- [x] File loads multiple GeoJSON sources and creates GeoStack
- [x] Console output shows GeoStack capabilities
- [x] README.md updated with new example entry
- [x] Both commits recorded

## Artifacts

| Path | Type | Description |
|------|------|-------------|
| examples/core/04-geostack.kt | Created | Runnable GeoStack demonstration |
| examples/core/README.md | Modified | Updated example documentation |

## Verification

Run the example:
```bash
./gradlew run -Popenrndr.application=examples.core.GeoStackKt
```

Expected output:
```
=== GeoStack Multi-Dataset Composition Example ===

Loading datasets...
Creating GeoStack...

┌─────────────────────────────────────────────────────┐
│                  GeoStack Summary                   │
├─────────────────────────────────────────────────────┤
│ Sources:          3                                 │
│ Unified CRS:      EPSG:4326                         │
│ Total Features:   [count]                           │
│ Bounding Box:                                             │
│   minX: [value]   minY: [value]                     │
│   maxX: [value]   maxY: [value]                     │
└─────────────────────────────────────────────────────┘
```
