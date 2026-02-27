# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.0.0 MVP** — Phases 1-5 (shipped 2026-02-22)
- ✅ **v1.1.0** — Fix Projection API (shipped 2026-02-26) — [Details](milestones/v1.1.0-ROADMAP.md)
- 🚧 **v1.2.0** — API Improvements & Examples (in progress)

---

## Current Milestone: v1.2.0

**Milestone Goal:** Polish the API with data inspection, proper polygon hole rendering, intuitive API design, and runnable examples.

### Phases

- [x] **Phase 7: Data Inspection** — GeoSource summary() for runtime debugging (completed 2026-02-26)
- [x] **Phase 8: Rendering Improvements** — Polygon holes and bounds handling (completed 2026-02-27)
- [ ] **Phase 9: API Design** — Feature traversal and two-tier API
- [ ] **Phase 10: Documentation & Examples** — Runnable examples with data files

---

## Phase Details

### Phase 7: Data Inspection
**Goal**: Users can understand their geo data before rendering
**Depends on**: Phase 6 (v1.1.0 shipped)
**Requirements**: INSP-01, INSP-02, INSP-03
**Success Criteria** (what must be TRUE):
  1. User can call `printSummary()` on any GeoSource and see feature count, bounds, and CRS
  2. User can see memory footprint estimate to understand dataset scale
  3. User can see property keys and types (not values) to understand feature attributes
  4. User sees clean, pandas-style formatted output in console
**Plans**: 1 plan

Plans:
- [x] 07-01-PLAN.md — Implement printSummary() with test scaffold (covers INSP-01, INSP-02, INSP-03)

### Phase 8: Rendering Improvements
**Goal**: Users can render complex polygons (with holes) correctly
**Depends on**: Phase 7
**Requirements**: REND-07, REND-08, REND-09
**Success Criteria** (what must be TRUE):
  1. User can render Polygon features with interior rings (holes) that appear correctly
  2. User can render MultiPolygon features with interior rings clamped to Mercator bounds
  3. User sees correct rendering of ocean/whole-world MultiPolygon data without artifacts
  4. User can verify hole rendering by comparing to QGIS output
**Plans**: 4 plans

Plans:
- [x] 08-00-PLAN.md — Create test scaffolds for hole rendering (Wave 0, REND-09)
- [x] 08-01-PLAN.md — Implement interiorsToScreen() and writePolygonWithHoles() (REND-07)
- [x] 08-02-PLAN.md — Update drawPolygon/drawMultiPolygon for hole rendering with clamping (REND-07, REND-08)
- [x] 08-03-PLAN.md — Add tests and verify ocean.geojson rendering (REND-09)

### Phase 9: API Design
**Goal**: Users have intuitive API matching OpenRNDR conventions
**Depends on**: Phase 8
**Requirements**: API-01, API-02, API-03, API-04
**Success Criteria** (what must be TRUE):
  1. User can iterate features with projected coordinates handled internally
  2. Beginner user can accomplish common tasks with simple workflow (drawer.geo style)
  3. Professional user can access detailed control when needed (escape hatches)
  4. User recognizes OpenRNDR DSL patterns in the API (not one-line magic)
**Plans**: 4 plans

Plans:
- [x] 09-00-PLAN.md — Create test scaffolds for API features (Wave 0, API-01, API-02, API-03)
- [x] 09-01-PLAN.md — Implement feature-level iteration with projected coordinates (API-01)
- [ ] 09-02-PLAN.md — Implement two-tier API with config block DSL (API-02, API-04)
- [ ] 09-03-PLAN.md — Add escape hatches (RawProjection, style resolution) (API-03, API-04)

### Phase 10: Documentation & Examples
**Goal**: Users can learn the library through runnable examples
**Depends on**: Phase 9
**Requirements**: DOC-01, DOC-02, DOC-03, DOC-04
**Success Criteria** (what must be TRUE):
  1. User can browse examples organized by functional category (core_, render_, proj_, anim_, layer_)
  2. User can run any example immediately with included data files
  3. User can understand each example because it demonstrates ONE concept
  4. Examples validate that framework features work correctly (UAT)
**Plans**: TBD

Plans:
- [ ] 10-01: Create example directory structure with categories
- [ ] 10-02: Add minimal sample data files (GeoJSON/GeoPackage)
- [ ] 10-03: Write examples for each category (one feature per example)
- [ ] 10-04: Run all examples as UAT validation

---

## Progress

**Execution Order:** 7 → 8 → 9 → 10

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 7. Data Inspection | 1/1 | Complete    | 2026-02-26 |
| 8. Rendering Improvements | 4/4 | Complete    | 2026-02-27 |
| 9. API Design | 2/4 | In Progress | 2026-02-27 |
| 10. Documentation & Examples | 0/4 | Not started | - |

---

_For completed milestone details, see .planning/milestones/_
