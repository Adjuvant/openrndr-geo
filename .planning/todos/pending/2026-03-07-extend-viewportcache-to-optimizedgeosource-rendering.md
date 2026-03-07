---
created: 2026-03-07T01:06:49.242Z
title: Extend ViewportCache to OptimizedGeoSource rendering
area: performance
status: scheduled
roadmap_phase: 17
files:
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt:272-279
  - src/main/kotlin/geo/internal/geometry/OptimizedGeometries.kt
  - src/main/kotlin/geo/internal/OptimizedGeoSource.kt
---

## Problem

Despite implementing ViewportCache for standard geometries in Drawer.geo(), the optimized rendering path (OptimizedGeoSource) completely bypasses the cache.

**Root Cause:**
- `Drawer.geo()` has a separate code path for `OptimizedGeoSource` (lines 272-279)
- This path calls `optFeature.renderOptimizedToDrawer()` directly
- `renderOptimizedToDrawer()` calls `geom.toScreenCoordinates(projection)` which projects coordinates EVERY FRAME
- The ViewportCache is never checked or used for optimized geometries

**Impact:**
- Examples using `optimize=true` (like 05-batch-optimization.kt with 16MB GeoJSON) run at ~5 FPS
- All the Phase 11-12 optimization work (batch projection, viewport caching) is bypassed
- The benchmarks show 1533x improvement but real-world usage with optimized sources sees no benefit

**Key Files:**
- `DrawerGeoExtensions.kt:272-279` - The OptimizedGeoSource bypass
- `OptimizedGeometries.kt` - All `toScreenCoordinates()` methods project every time
- `OptimizedGeoSource.kt` - How optimized features are stored and accessed

## Solution

Integrate ViewportCache into the optimized rendering pipeline:

1. **Option A: Modify renderOptimizedToDrawer()**
   - Add ViewportCache parameter to the method
   - Check cache before calling toScreenCoordinates()
   - Cache projected Array<Vector2> results

2. **Option B: Create cached wrapper in Drawer.geo()**
   - Similar to the fix for standard geometries
   - Cache optimized geometry screen coordinates the same way
   - Use geometry object reference as cache key (identity equality)

3. **Key Challenge:**
   - Optimized geometries are internal classes (OptimizedLineString, etc.)
   - Need to access them for cache key creation
   - Must maintain dirty flag integration for cache invalidation

**Test:**
- Run examples/core/05-batch-optimization.kt
- Should see FPS improve from ~5 to 60+ (matching benchmark results)

## Related

- Quick task 10 fixed standard geometries in Drawer.geo()
- This todo covers the remaining optimized path
- PERF-04 and PERF-07 requirements partially unfulfilled without this fix

## Roadmap

**Status:** Scheduled for Phase 17 (Performance Fixes) — v1.4.0 Developer Experience milestone

**ROADMAP.md:** See Phase 17: Performance Fixes — ViewportCache for OptimizedGeoSource
**Requirements:** PERF-11
**Critical:** This fixes a major performance gap where optimized rendering bypasses all caching
