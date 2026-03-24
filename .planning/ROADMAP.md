# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.3.0 Performance** — Phases 11-14 (shipped 2026-03-07) — [Details](milestones/v1.3.0-ROADMAP.md)
- 🚧 **v1.4.0 Developer Experience** — Phases 15-20 (in progress)

## Current Focus

**Milestone:** v1.4.0 Developer Experience  
**Goal:** Improve API ergonomics, fix rendering edge cases, and enhance developer workflow  
**Phase:** 16 (Rendering Improvements) — Complete, ready for Phase 17

---

## Active Phases

### Phase 15: API Ergonomics — Reduce Boilerplate

**Status:** Planned  
**Plans:** 2/2 plans complete
**Depends on:** Phase 14 (v1.3.0 completion)

**Goal:** Reduce API boilerplate for the most common creative coding workflow: load data → create projection → render.

**Requirements:** API-01, API-02, API-03

**Success Criteria:**
1. Single-import API works (`import geo.*`)
2. Streamlined rendering workflow (3 lines or less)
3. Conventions over configuration
4. Hard break — no backward compatibility (all 26 examples updated)

**Planned:**
- [x] [`15-01-PLAN.md`](phases/15-api-ergonomics-reduce-boilerplate/15-01-PLAN.md) — Import structure reorganization and package migration
- [x] [`15-02-PLAN.md`](phases/15-api-ergonomics-reduce-boilerplate/15-02-PLAN.md) — Streamlined API implementation (geoSource, loadGeo, updated examples)

---

### Phase 16: Rendering Improvements

**Status:** Complete  
**Plans:** 5/5 plans complete

**Goal:** Fix MultiPolygon rendering for ocean/whole-world data and improve polygon interior/exterior ring handling.

**Requirements:** RENDER-01, RENDER-02

**Success Criteria:**
1. ✅ MultiPolygons spanning antimeridian render without world-spanning artifacts
2. ✅ Polygon winding order is normalized (exterior clockwise, interior counter-clockwise)
3. ✅ Interior ring validation logs warnings for degenerate/out-of-bounds holes
4. ✅ MultiPolygons render as single Shape with combined contours (no overdraw/seams)
5. ✅ Both standard and optimized render paths use combined Shape approach
6. ✅ Geometry normalization integrated into GeoJSON loading pipeline
7. ✅ Polygon holes render correctly in standard rendering paths

**Completed:**
- [x] [`16-01-PLAN.md`](phases/16-rendering-improvements/16-01-PLAN.md) — Geometry normalization utilities (antimeridian splitting, winding normalization, ring validation)
- [x] [`16-02-PLAN.md`](phases/16-rendering-improvements/16-02-PLAN.md) — MultiPolygon rendering improvements (combined Shape rendering, optimized path updates)
- [x] [`16-03-PLAN.md`](phases/16-rendering-improvements/16-03-PLAN.md) — Gap closure (test helpers, multi-ring handling)
- [x] [`16-04-PLAN.md`](phases/16-rendering-improvements/16-04-PLAN.md) — Gap closure (normalization integration, hole rendering via compound difference)

---

### Phase 17: Performance Fixes

**Status:** In Progress  
**Plans:** 5/5 plans complete

**Planned:**
- [x] [`17-01-PLAN.md`](phases/17-performance-fixes/17-01-PLAN.md) — Extend ViewportCache to optimized path
- [x] [`17-02-PLAN.md`](phases/17-performance-fixes/17-02-PLAN.md) — Fix optimized feature stub and add tests
- [x] [`17-03-PLAN.md`](phases/17-performance-fixes/17-03-PLAN.md) — Use open contours for lines and points in cache path
- [x] [`17-04-PLAN.md`](phases/17-performance-fixes/17-04-PLAN.md) — Gap analysis and fix planning (test failures fix)
- [x] [`17-05-PLAN.md`](phases/17-performance-fixes/17-05-PLAN.md) — styleByOptimizedFeature and tests

**Goal:** Extend ViewportCache to OptimizedGeoSource rendering path.

**Requirements:** PERF-11

**Critical:** Fixes performance bypass for optimized geometries

---

### Phase 17.1: Antimeridian splitting fixes and validation (INSERTED)

**Goal:** Fix antimeridian splitting logic and validation in optimized rendering path.
**Requirements:** PERF-11
**Depends on:** Phase 16
**Plans:** 1/1 plans defined

Plans:
- [ ] [`17.1-01-PLAN.md`](phases/17.1-antimeridian-splitting-fixes-and-validation/17.1-01-PLAN.md) — Fix antimeridian splitting and validation in optimized rendering path

### Phase 18: Code Organization

**Status:** In Progress  
**Plans:** 3/3 plans complete

**Goal:** Improve project structure through file organization and cleanup.

**Requirements:** ORG-01, ORG-02, ORG-03

**Plans:**
- [x] [`18-01-PLAN.md`](phases/18-code-organization/18-01-PLAN.md) — Examples cleanup (ORG-01) ✅ COMPLETE
- [x] [`18-02-PLAN.md`](phases/18-code-organization/18-02-PLAN.md) — Core subdirectory (ORG-02) ✅ COMPLETE
- [ ] [`18-03-PLAN.md`](phases/18-code-organization/18-03-PLAN.md) — Import rewrites (ORG-03)

---

### Phase 19: Documentation Fixes

**Status:** In Progress  
**Plans:** 1/1

**Goal:** Fix README run commands and data paths.

**Requirements:** DOCS-01

**Plans:**
- [x] [`19-01-PLAN.md`](phases/19-documentation-fixes/19-01-PLAN.md) — Fix Kt suffix in run commands ✅ COMPLETE

---

### Phase 20: Layer Features

**Status:** Not Started  
**Plans:** 0/1

**Goal:** Fix and enhance graticule layer for zoomed-in maps.

**Requirements:** LAYER-01

---

## Progress

| Phase | Milestone | Plans | Status | Target |
|-------|-----------|-------|--------|--------|
| 11-14 | v1.3.0 | 13/13 | ✅ Complete | 2026-03-07 |
| 15 | v1.4.0 | 2/2 | ✅ Complete | 2026-03-07 |
| 16 | 5/5 | Complete    | 2026-03-08 | TBD |
| 17 | 6/6 | Complete    | 2026-03-18 | TBD |
| 18 | 3/3 | Complete   | 2026-03-24 | TBD |
| 19 | v1.4.0 | 1/1 | ✅ Complete | 2026-03-24 |
| 20 | v1.4.0 | 0/1 | ⏳ Not Started | TBD |

---

## Archived Milestones

<details>
<summary>✅ v1.3.0 Performance (Phases 11-14) — SHIPPED 2026-03-07</summary>

**Achieved:** 1533x average speedup (target: 10x+)

**Phases:**
- [x] Phase 11: Batch Projection (2/2 plans)
- [x] Phase 12: Viewport Caching (3/3 plans)
- [x] Phase 13: Integration & Validation (2/2 plans)
- [x] Phase 14: Refactoring & Cleanup (6/6 plans)

**Archive:** [v1.3.0-ROADMAP.md](milestones/v1.3.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.2.0 API & Examples (Phases 07-10) — SHIPPED 2026-02-27</summary>

**Phases:**
- [x] Phase 07: Data Inspection
- [x] Phase 08: Rendering Improvements
- [x] Phase 09: API Design
- [x] Phase 10: Documentation & Examples

**Archive:** [v1.2.0-ROADMAP.md](milestones/v1.2.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.1.0 API Improvements — SHIPPED 2026-02-27</summary>

**Archive:** [v1.1.0-ROADMAP.md](milestones/v1.1.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.0.0 Foundation — SHIPPED 2026-02-22</summary>

**Archive:** [v1.0.0-ROADMAP.md](milestones/v1.0.0-ROADMAP.md)

</details>

---

## Dependency Graph

```
v1.3.0 Performance (Complete):
Phase 11: Batch Projection
    ↓
Phase 12: Viewport Caching
    ↓
Phase 13: Integration & Validation
    ↓
Phase 14: Refactoring & Cleanup

v1.4.0 Developer Experience (In Progress):
                    ↓
        ┌───────────────────────────────┐
        │                               │
Phase 15: API Ergonomics      Phase 16: Rendering
        │                               │
Phase 17: Performance Fixes     Phase 20: Layer Features
        │
Phase 18: Code Organization
        │
Phase 19: Documentation
```

---

*Last Updated: 2026-03-07 — v1.3.0 milestone complete, v1.4.0 planning begins*
