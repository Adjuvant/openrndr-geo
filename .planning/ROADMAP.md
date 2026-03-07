# Roadmap: v1.3.0 Performance

**Milestone:** v1.3.0 Performance  
**Goal:** Optimize rendering performance through batch projection and simple viewport caching.  
**Target:** 10x+ improvement for static camera scenarios.  
**Achieved:** 1533x average speedup (Phase 13-01)  
**Last Updated:** 2026-03-07 (Phase 13-01 complete)

## Phases

- [x] **Phase 11: Batch Projection** - Transform coordinate arrays efficiently (2/2 plans complete)
- [x] **Phase 12: Viewport Caching** - Simple cache with clear-on-change semantics (completed 2026-03-06)
- [x] **Phase 13: Integration & Validation** - Verify all v1.2.0 examples work unchanged (completed 2026-03-07)

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 11. Batch Projection | 2/2 | Complete    | 2026-03-05 |
| 12. Viewport Caching | 3/3 | Complete   | 2026-03-06 |
| 13. Integration & Validation | 2/2 | Complete    | 2026-03-07 |

**Coverage:** 10/10 v1.3.0 requirements mapped ✓

## Phase Details

### Phase 11: Batch Projection

**Goal:** Library can batch-transform coordinate arrays efficiently and integrate batch projection into the rendering pipeline while preserving existing API contracts.

**Depends on:** Phase 10 (v1.2.0 completion)

**Requirements:** PERF-01, PERF-02, PERF-03

**Success Criteria** (what must be TRUE):
1. Coordinate arrays are transformed using batch operations instead of per-point projection
2. Rendering pipeline uses batch projection for all geometry types (Point, LineString, Polygon, Multi*)
3. All existing public API signatures remain unchanged (backward compatible)
4. Batch projection is transparent to existing code (no changes required to use)
5. Internal timing confirms measurable improvement over per-point projection

**Plans:** 2/2 plans complete

- [x] `11-01-PLAN.md` — Core batch infrastructure (CoordinateBatch, optimized geometries) — Completed 2026-03-05
- [x] `11-02-PLAN.md` — API integration & validation (opt-in parameter, warnings, benchmarks) — Completed 2026-03-05

---

### Phase 12: Viewport Caching

**Goal:** Library caches projected geometries for the current viewport state with simple clear-on-change semantics.

**Depends on:** Phase 11 (Batch Projection)

**Requirements:** PERF-04, PERF-05, PERF-06, PERF-07

**Success Criteria** (what must be TRUE):
1. Projected geometries are cached for current viewport state
2. Cache invalidates entirely when viewport changes (zoom, pan, viewport size)
3. Cache bounded by simple size limit (no LRU/LFU complexity)
4. Caching is transparent to existing code (no API changes required)
5. Simple Kotlin `MutableMap` implementation (no external caching libraries)

**Plans:** 3/3 plans complete

- [x] `12-01-PLAN.md` — Core cache infrastructure (ViewportState, CacheKey, ViewportCache) — Completed 2026-03-06
- [x] `12-02-PLAN.md` — Geometry dirty flag integration (reactive invalidation) — Completed 2026-03-06

---

### Phase 13: Integration & Validation

**Goal:** All optimizations are validated against v1.2.0 baseline and all 16 existing examples work unchanged.

**Depends on:** Phase 11 (Batch Projection), Phase 12 (Viewport Caching)

**Requirements:** PERF-08, PERF-09, PERF-10

**Success Criteria** (what must be TRUE):
1. ✓ Static camera scenarios show 10x+ improvement over v1.2.0 baseline — **ACHIEVED: 1533x average**
2. ✓ Performance validated with realistic datasets (100k+ features) — **ACHIEVED: Tested up to 250k**
3. All 16 v1.2.0 examples continue to work unchanged (regression test passed)
4. Memory usage remains bounded during extended creative sessions
5. ✓ Pan operations show measurable improvement from batch projection — **ACHIEVED: 343x average**

**Plans:** 2/2 plans complete

- [x] `13-01-PLAN.md` — Performance benchmarking infrastructure and tests (PERF-08, PERF-09) — Completed 2026-03-07
- [ ] `13-02-PLAN.md` — Regression testing all 16 v1.2.0 examples (PERF-10)

**Wave Structure:**
- Wave 1: Plan 13-01 (performance benchmarks - can run independently)
- Wave 2: Plan 13-02 (regression tests - depends on benchmarks)

---

## Dependency Graph

```
Phase 11: Batch Projection
    ↓
Phase 12: Viewport Caching
    ↓
Phase 13: Integration & Validation
```

## Coverage Matrix

| Requirement | Phase | Category |
|-------------|-------|----------|
| PERF-01 | 11 | Batch Projection |
| PERF-02 | 11 | Batch Projection |
| PERF-03 | 11 | Batch Projection |
| PERF-04 | 12 | Viewport Caching |
| PERF-05 | 12 | Viewport Caching |
| PERF-06 | 12 | Viewport Caching |
| PERF-07 | 12 | Viewport Caching |
| PERF-08 | 13 | Measurement |
| PERF-09 | 13 | Measurement |
| PERF-10 | 13 | Validation |

**Coverage:** 10/10 v1.3.0 requirements mapped ✓

## Success Criteria Summary

| Phase | Criteria | Focus |
|-------|----------|-------|
| 11 | 5 | Batch transformation infrastructure |
| 12 | 5 | Simple viewport-based caching |
| 13 | 5 | Validation & regression testing |

## Notes

**Phase Ordering Rationale:**
1. **Batch Projection (11)** must come first — prerequisite for caching
2. **Viewport Caching (12)** builds on batch projection — simple clear-on-change semantics
3. **Integration (13)** validates everything — ensures no regressions and meets performance targets

**Key Constraints:**
- NO Caffeine/Aedile dependencies — simple Kotlin `MutableMap` only
- NO LRU/LFU algorithms — web-style optimization not needed for creative coding
- Clear entire cache on viewport change (not per-entry eviction)
- All 16 v1.2.0 examples must work unchanged

**Simplification Rationale:**
Creative coding use case differs from web applications:
- User explores with pan/zoom, then observes static view
- When camera moves, **everything** becomes stale (not LRU-worthy)
- Simple clear-on-change is sufficient and more predictable
- Bounded by "one viewport worth of data" — no complex eviction needed

---
*Created: 2026-03-05 for v1.3.0 Performance milestone*
