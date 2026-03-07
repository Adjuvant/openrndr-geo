---
created: 2026-03-07T01:21:32.962Z
title: Move geo root files to core subdirectory
area: tooling
status: scheduled
roadmap_phase: 18
files:
  - src/main/kotlin/geo/Bounds.kt
  - src/main/kotlin/geo/Feature.kt
  - src/main/kotlin/geo/GeoJSON.kt
  - src/main/kotlin/geo/Geometry.kt
  - src/main/kotlin/geo/GeoPackage.kt
  - src/main/kotlin/geo/GeoSource.kt
  - src/main/kotlin/geo/GeoSourceConvenience.kt
  - src/main/kotlin/geo/GeoStack.kt
  - src/main/kotlin/geo/ProjectionExtensions.kt
  - src/main/kotlin/geo/SpatialIndex.kt
---

## Problem

The `src/main/kotlin/geo/` root directory contains 18 entries including multiple Kotlin files that represent core domain logic. The current flat structure mixes:
- Core domain classes (Bounds.kt, Geometry.kt, Feature.kt, etc.)
- Data source implementations (GeoJSON.kt, GeoPackage.kt, GeoSource.kt)
- Subpackages for specialized areas (animation/, crs/, layer/, projection/, render/, etc.)

This creates visual clutter and makes it harder to understand the package structure at a glance. Moving core files into a `core/` subdirectory would improve organization and maintainability.

## Files to Consider Moving

Core domain files that could be grouped:
- `Bounds.kt` - Bounding box calculations
- `Feature.kt` - Geo feature representation
- `GeoJSON.kt` - GeoJSON data source
- `Geometry.kt` - Geometry primitives
- `GeoPackage.kt` - GeoPackage data source
- `GeoSource.kt` - Base data source interface
- `GeoSourceConvenience.kt` - Data source helpers
- `GeoStack.kt` - Layer stack management
- `ProjectionExtensions.kt` - Projection utilities
- `SpatialIndex.kt` - Spatial indexing

## Solution

**Safe refactoring approach:**

1. Create `src/main/kotlin/geo/core/` directory
2. Move core files one at a time, updating:
   - Package declarations (geo → geo.core)
   - Import statements in all dependent files
   - Any references in tests and examples
3. After each move:
   - Run `./gradlew build` to verify compilation
   - Run tests to ensure no regressions
4. Consider creating re-export files at the old locations with `@Deprecated` annotations for backward compatibility during transition

## Considerations

- **Impact scope:** Check for imports in:
  - Test files (src/test/kotlin/)
  - Example files (examples/ and src/main/kotlin/geo/examples/)
  - Any other source files
  
- **Package visibility:** Ensure internal visibility modifiers still work correctly after move

- **Build verification:** Run full test suite after all moves complete

## Action Items

- [ ] Create core/ subdirectory
- [ ] Move core domain files with package updates
- [ ] Update all import statements across codebase
- [ ] Verify build compiles successfully
- [ ] Run full test suite
- [ ] Update any documentation references

## Roadmap

**Status:** Scheduled for Phase 18 (Code Organization) — v1.4.0 Developer Experience milestone

**ROADMAP.md:** See Phase 18: Code Organization — Directory Structure
**Requirements:** ORG-02
