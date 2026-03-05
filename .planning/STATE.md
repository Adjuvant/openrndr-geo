# Project State: openrndr-geo

**Current Milestone:** v1.3.0 Performance  
**Phase:** Starting Phase 11  
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
| **v1.3.0 Performance** | 🔄 Ready | 0% |
| v1.4.0 Advanced Features | ⏳ Pending | 0% |

### Phase Status

| Phase | Name | Status | Plans | Blocked By |
|-------|------|--------|-------|------------|
| 11 | Batch Projection | ⏳ Ready | 0/2 | - |
| 12 | Viewport Caching | ⏳ Pending | 0/2 | Phase 11 |
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
- Created simplified ROADMAP.md for v1.3.0 with 3 phases (11-13)
- Mapped 10 PERF requirements to phases (reduced from 14)
- Validated 100% coverage
- Removed Caffeine/Aedile dependencies
- Simplified caching to clear-on-change semantics

### Next Actions
1. Run `/gsd-execute-phase 11` to start Batch Projection
2. Or `/gsd-discuss-phase 11` to clarify approach first

## Files

| File | Purpose | Last Updated |
|------|---------|--------------|
| PROJECT.md | Core value, constraints, decisions | 2026-03-05 |
| REQUIREMENTS.md | PERF-01 to PERF-10 requirements | 2026-03-05 |
| ROADMAP.md | Phase structure and success criteria | 2026-03-05 |
| MILESTONES.md | Milestone completion tracking | 2026-03-05 |
| research/SUMMARY.md | Research findings (superseded by simplification) | 2026-03-05 |

---
*State file for project continuity across sessions*
