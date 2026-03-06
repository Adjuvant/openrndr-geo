# Project State: openrndr-geo

**Current Milestone:** v1.3.0 Performance  
**Phase:** 11-batch-projection  
**Current Plan:** Not started
**Last Updated:** 2026-03-05

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
| **v1.3.0 Performance** | 🔄 In Progress | 17% |
| v1.4.0 Advanced Features | ⏳ Pending | 0% |

### Phase Status

| Phase | Name | Status | Plans | Blocked By |
|-------|------|--------|-------|------------|
| 11 | Batch Projection | ✓ Complete | 2/2 | - |
| 12 | Viewport Caching | 🔄 Ready | 0/2 | Phase 11 |
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
- ✅ Completed Plan 11-02: Batch integration and benchmarks
- Added opt-in optimization with `loadGeoJSON(path, optimize = true)`
- Created OptimizationWarnings for large geometry guidance
- Implemented OptimizedGeoSource wrapper for optimized data
- Integrated batch projection into GeoStack rendering pipeline
- Created micro-benchmarks showing 1.1-1.5x speedup
- All existing tests pass, backward compatible

### Next Actions
1. Move to Phase 12: Viewport Caching (`/gsd-plan-phase 12`)
2. Or review Phase 11 completion (`/gsd-verify-work 11`)

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

### Session Continuity

**Last Session:** 2026-03-06T22:46:01.323Z
**Stopped At:** Phase 12 context gathered
**Duration:** 16 minutes

---
*State file for project continuity across sessions*
