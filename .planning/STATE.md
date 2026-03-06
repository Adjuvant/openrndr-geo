# Project State: openrndr-geo

**Current Milestone:** v1.3.0 Performance  
**Phase:** 12-viewport-caching  
**Current Plan:** 02
**Last Updated:** 2026-03-06

## Project Reference

**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

**Current Focus:** Optimize rendering performance through batch coordinate projection and simple viewport caching. Target: 10x+ improvement for static cameras.

**Tech Stack:** Kotlin/JVM, OPENRNDR 0.4.5, simple `MutableMap` caching (no external libraries)

## Current Position

| Milestone | Status | Progress |
|-----------|--------|----------|
| v1.0.0 Foundation | ✓ Complete | 100% |
| v1.1.0 API Improvements | ✓ Complete | 100% |
| v1.2.0 API & Examples | ✓ Complete | 100% |
| **v1.3.0 Performance** | 🔄 In Progress | 25% |
| v1.4.0 Advanced Features | ⏳ Pending | 0% |

### Phase Status

| Phase | Name | Status | Plans | Blocked By |
|-------|------|--------|-------|------------|
| 11 | Batch Projection | ✓ Complete | 2/2 | - |
| 12 | Viewport Caching | ✓ Complete | 3/3 | - |
| 13 | Integration & Validation | ⏳ Pending | 0/2 | Phase 12 |

## Performance Metrics

### Current Baseline (v1.2.0)
- No performance optimization implemented
- Per-point coordinate transformation
- No geometry caching
- 16 runnable examples working

### Target Metrics (v1.3.0)
- **Static camera:** 10x+ frame time improvement
- **Pan operations:** Improvement from batch projection
- **Memory:** Simple size limit (clear-on-change, no LRU)
- **Compatibility:** All 16 v1.2.0 examples work unchanged

## Accumulated Context

### Key Decisions

| Decision | Rationale | Status |
|----------|-----------|--------|
| Start phases at 11 | v1.2.0 ended at phase 10 | ✓ Confirmed |
| 3 phases for v1.3.0 | Simplified from 4-phase structure | ✓ Approved |
| Simple MutableMap caching | Creative coding use case — clear-on-change sufficient | ✓ Simplified |
| No Caffeine/Aedile | Overkill for viewport-based geometry | ✓ Agreed |
| No LRU/LFU | Web-style optimization not needed | ✓ Agreed |
| Batch before caching | Prerequisite dependency | ✓ Logical |
| Optimized geometries don't extend Geometry | Sealed class package restriction - only extendable in same package | ✓ Implemented |
| Geometry.toOptimized() returns Any | Heterogeneous return types (OptimizedPoint, OptimizedLineString, etc.) | ✓ Implemented |
| Use inline for batch transformation | Eliminate lambda allocation overhead in hot paths | ✓ Implemented |
| Phase 11-batch-projection P01 | 7min | 3 tasks | 5 files |
| Phase 11-batch-projection P02 | 16min | 3 tasks | 8 files |
| Phase 12-viewport-caching P01 | 2min | 3 tasks | 4 files |
| Identity equality for cache keys | Avoid O(n) content hashing on large geometries | ✓ Implemented |
| MAX_CACHE_ENTRIES = 1000 | Upper end of suggested range for typical scenes | ✓ Implemented |
| Phase 12-viewport-caching P02 | 1min | 3 tasks | 1 files |
| Phase 12-viewport-caching P03 | 4min | 3 tasks | 2 files |

### Active Requirements (v1.3.0)

**Phase 11 — Batch Projection:**
- PERF-01: Batch-transform coordinate arrays
- PERF-02: Rendering pipeline uses batch projection
- PERF-03: Preserve existing API contracts

**Phase 12 — Viewport Caching:**
- PERF-04: Cache projected geometries for viewport
- PERF-05: Clear cache on viewport change
- PERF-06: Simple size limit (not LRU)
- PERF-07: Transparent to existing code

**Phase 13 — Integration & Validation:**
- PERF-08: 10x+ improvement for static camera
- PERF-09: Performance validated with datasets
- PERF-10: All 16 v1.2.0 examples work unchanged

### Known Blockers

None currently.

## Session Continuity

### Last Actions
- ✅ Completed Plan 12-03: ViewportCache integration into GeoStack rendering pipeline
- Integrated private viewportCache instance with renderWithCache() and projectGeometryToArray() helpers
- Created 8 comprehensive unit tests covering cache storage, invalidation, limits, dirty flag, and transparency
- Full test suite passes with no regressions
- Public API unchanged (PERF-07 satisfied)

### Next Actions
1. Start Phase 13: Integration & Validation (`/gsd-plan-phase 13`)
2. Or review Phase 12 progress (`/gsd-verify-work 12`)

## Files

| File | Purpose | Last Updated |
|------|---------|--------------|
| PROJECT.md | Core value, constraints, decisions | 2026-03-05 |
| REQUIREMENTS.md | PERF-01 to PERF-10 requirements | 2026-03-05 |
| ROADMAP.md | Phase structure and success criteria | 2026-03-05 |
| MILESTONES.md | Milestone completion tracking | 2026-03-05 |
| research/SUMMARY.md | Research findings (superseded by simplification) | 2026-03-05 |
| 11-01-SUMMARY.md | Core batch projection infrastructure | 2026-03-05 |
| 11-02-SUMMARY.md | Batch integration and benchmarks | 2026-03-05 |
| 12-01-SUMMARY.md | Viewport caching infrastructure | 2026-03-06 |
| 12-03-SUMMARY.md | ViewportCache integration and tests | 2026-03-06 |

### Session Continuity

**Last Session:** 2026-03-06T23:20:04.010Z
**Stopped At:** Completed 12-03-PLAN.md
**Duration:** 4 minutes

---
*State file for project continuity across sessions*
