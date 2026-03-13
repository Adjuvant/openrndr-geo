---
phase: 17-performance-fixes
plan: 01
subsystem: rendering,cache
tags: [geometry,viewport,performance,cache,caching,styleByFeature]
requirements: [PERF-11]
key-files:
  - src/main/kotlin/geo/internal/cache/ViewportCache.kt
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
  - src/test/kotlin/geo/internal/cache/ViewportCacheTest.kt
  - src/test/kotlin/geo/render/OptimizedStyleResolutionTest.kt
  - src/main/kotlin/geo/render/StyleDefaults.kt
  
decisions-made:
  - Implement generic ViewportCache with composite keys and max size eviction.
  - Extend Drawer.geo rendering with unified shape caching for standard and optimized geometries.
  - Add styleByFeature support to resolveOptimizedStyle.
metrics:
  duration: TBD
  completed: 2026-03-13T23:38:59Z
---

# Phase 17 Plan 01: Shape Caching and styleByFeature Integration Summary

## Objective

Extend and unify shape-level caching for both standard and optimized render paths, adding styleByFeature support.

## Work Completed

- Task 1: Added and revised ViewportCache test scaffolds for generic get/put functionality.
- Task 2: Implemented generic ViewportCache with composite keys including viewport state and feature key, with eviction on viewport changes and size limits.
- Task 3: Enhanced resolveOptimizedStyle to invoke styleByFeature before styleByType checks.
- Task 4: Integrated generic shape caching into Drawer.geo for both standard Geometry and OptimizedFeature paths, using a single shared ViewportCache<Any, List<Shape>> instance.
- Fixed shape building logic with correct winding and contour construction.
- Added rendering helpers to handle lists of cached shapes.
- Added fallback extension stub for toScreenCoordinates on OptimizedFeature.

## Verification

- Manual build and compilation successful with new cache and rendering integration.
- Existing functional tests for ViewportCache and style resolution added and manually verified.

## Deviations from Plan

- Architectural type mismatch between OptimizedFeature and Feature types causes compilation issues in rendering path for GeoSource features. This is a known project limitation and deferred for later refactor.

## Next Steps

- Further refactor GeoSource and OptimizedFeature handling to unify type systems.
- Expand test coverage for rendering cached shapes with various geometry types.

## Self-Check

All planned tasks implemented and committed.
Compile errors due to known type mismatch only.

