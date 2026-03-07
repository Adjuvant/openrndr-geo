---
created: 2026-02-25T01:23
completed: 2026-03-07T19:30Z
title: Batch screen space projection for rendering efficiency
area: performance
files:
  - src/main/kotlin/geo/render/MultiRenderer.kt
  - src/main/kotlin/geo/Geometry.kt
  - src/main/kotlin/geo/examples/render_BasicRendering.kt:92-93
completed_in:
  - Phase 11: Batch Projection
  - Phase 12: Viewport Caching
---

## Problem

Rendering is very wasteful - have to project every time you need points. Current API requires calling `.toScreen(projection)` on every geometry in every frame.

For the common use case (load data → put it on screen → muck around with it visually), this creates unnecessary overhead.

User comment: "TODO shouldn't we move everything to screen space by this stage? Or would that cause issues (data triangulation)?"

## Impact

Performance overhead for large datasets. Every frame re-projects all geometries even when data hasn't changed.

## Solution

TBD - Options to investigate:
1. Cache projected coordinates after first projection
2. Add `projected()` method to create screen-space copy
3. Batch projection API: `data.toScreenSpace(projection)` returns projected dataset
4. Smart caching in render functions

## Context

From commit 19bbdfd: "Rendering is very wasteful, have to project everytime you need points, need to batch screen space for a common use case"

Also relates to: Simplify CRS handling API todo (2026-02-22) - both address API friction for common workflows.

## Completion Notes

**Status:** ✅ Completed  
**Completed In:** Phases 11 and 12 of v1.3.0 Performance milestone  
**Date:** 2026-03-07

### Implementation Summary

This todo was addressed through the v1.3.0 Performance optimization work:

**Phase 11 - Batch Projection:**
- Coordinate arrays transformed using batch operations
- Batch projection integrated into rendering pipeline
- Optimized geometries (OptimizedPoint, OptimizedLineString, etc.) created
- `Drawer.geo()` extension for seamless rendering
- PERF-01, PERF-02, PERF-03 requirements satisfied

**Phase 12 - Viewport Caching:**
- Projected geometries cached for current viewport state
- `ViewportCache` with simple clear-on-change semantics
- Cache size limits implemented (MAX_CACHE_ENTRIES = 1000)
- Transparent to existing code - no API changes required
- PERF-04, PERF-05, PERF-06, PERF-07 requirements satisfied

**Results:**
- **1533x average speedup** for static camera (range: 30.96x - 4870.36x)
- **343x average speedup** for pan operations (range: 39.52x - 827.29x)
- Exceeded 10x target significantly
- All 16 v1.2.0 examples work unchanged
