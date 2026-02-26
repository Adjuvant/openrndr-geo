# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.0.0 MVP** — Phases 1-5 (shipped 2026-02-22)
- ✅ **v1.1.0** — Fix Projection API (shipped 2026-02-26) — [Details](milestones/v1.1.0-ROADMAP.md)
- 🚧 **v1.2.0** — API Improvements & Examples (in progress)

---

## Current Milestone: v1.2.0

**Milestone Goal:** Polish the API with data inspection, proper polygon hole rendering, intuitive API design, and runnable examples.

### Phases

- [ ] **Phase 7: Data Inspection** — GeoSource summary() for runtime debugging
- [ ] **Phase 8: Rendering Improvements** — Polygon holes and bounds handling
- [ ] **Phase 9: API Design** — Feature traversal and two-tier API
- [ ] **Phase 10: Documentation & Examples** — Runnable examples with data files

---

## Phase Details

### Phase 7: Data Inspection
**Goal**: Users can understand their geo data before rendering
**Depends on**: Phase 6 (v1.1.0 shipped)
**Requirements**: INSP-01, INSP-02, INSP-03
**Success Criteria** (what must be TRUE):
  1. User can call `summary()` on any GeoSource and see feature count, bounds, and CRS
  2. User can see memory footprint estimate to understand dataset scale
  3. User can inspect property keys and sample values to understand feature attributes
  4. User can decide whether to use lazy or eager evaluation based on documented performance notes
**Plans**: TBD

Plans:
- [ ] 07-01: Implement GeoSourceSummary data class with feature count, bounds, CRS
- [ ] 07-02: Add memory footprint estimation and property inspection
- [ ] 07-03: Document lazy vs eager performance tradeoffs

### Phase 8: Rendering Improvements
**Goal**: Users can render complex polygons (with holes) correctly
**Depends on**: Phase 7
**Requirements**: REND-07, REND-08, REND-09
**Success Criteria** (what must be TRUE):
  1. User can render Polygon features with interior rings (holes) that appear correctly
  2. User can render MultiPolygon features with interior rings clamped to Mercator bounds
  3. User sees correct rendering of ocean/whole-world MultiPolygon data without artifacts
  4. User can verify hole rendering by comparing to QGIS output
**Plans**: TBD

Plans:
- [ ] 08-01: Implement Polygon interior ring rendering using OpenRNDR Shape API
- [ ] 08-02: Add interior ring clamping to clampToMercator()
- [ ] 08-03: Fix ocean/whole-world MultiPolygon rendering

### Phase 9: API Design
**Goal**: Users have intuitive API matching OpenRNDR conventions
**Depends on**: Phase 8
**Requirements**: API-01, API-02, API-03, API-04
**Success Criteria** (what must be TRUE):
  1. User can iterate features with projected coordinates handled internally
  2. Beginner user can accomplish common tasks with simple workflow (drawer.geoJSON style)
  3. Professional user can access detailed control when needed (escape hatches)
  4. User recognizes OpenRNDR DSL patterns in the API (not one-line magic)
**Plans**: TBD

Plans:
- [ ] 09-01: Implement feature-level iteration with internalized projection
- [ ] 09-02: Design two-tier API (simple + detailed) with escape hatches
- [ ] 09-03: Review API style against OpenRNDR DSL conventions

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
| 7. Data Inspection | 0/3 | Not started | - |
| 8. Rendering Improvements | 0/3 | Not started | - |
| 9. API Design | 0/3 | Not started | - |
| 10. Documentation & Examples | 0/4 | Not started | - |

---

_For completed milestone details, see .planning/milestones/_
