# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.0.0 MVP** — Phases 1-5 (shipped 2026-02-22)
- ✅ **v1.1.0** — Fix Projection API (shipped 2026-02-26) — [Details](milestones/v1.1.0-ROADMAP.md)
- ✅ **v1.2.0** — API Improvements & Examples (shipped 2026-02-27) — [Details](milestones/v1.2.0-ROADMAP.md)
- 📋 **v1.3.0** — Performance & Advanced Features (planned)

---

## Current Milestone: v1.3.0 (Planned)

**Milestone Goal:** Performance optimizations and advanced features for production use.

### Planned Phases

- [ ] **Phase 11: Performance** — Batch projection and geometry caching
- [ ] **Phase 12: Advanced Features** — Clipping, graticule improvements, bounds strategies

---

<details>
<summary>✅ v1.2.0 API Improvements & Examples (Phases 7-10) — SHIPPED 2026-02-27</summary>

**Milestone Goal:** Polish the API with data inspection, proper polygon hole rendering, intuitive API design, and runnable examples.

### Phase 7: Data Inspection
- [x] **Phase 7: Data Inspection** — GeoSource summary() for runtime debugging (completed 2026-02-26)
  - Plans: 1 (07-01) — Implement printSummary() with test scaffold

### Phase 8: Rendering Improvements
- [x] **Phase 8: Rendering Improvements** — Polygon holes and bounds handling (completed 2026-02-27)
  - Plans: 4 (08-00, 08-01, 08-02, 08-03)

### Phase 9: API Design
- [x] **Phase 9: API Design** — Feature traversal, two-tier API, and escape hatches (completed 2026-02-27)
  - Plans: 4 (09-00, 09-01, 09-02, 09-03)

### Phase 10: Documentation & Examples
- [x] **Phase 10: Documentation & Examples** — Runnable examples with data files (completed 2026-02-27)
  - Plans: 4 (10-01, 10-02, 10-03, 10-04)

**Requirements:** INSP-01/02/03, REND-07/08/09, API-01/02/03/04, DOC-01/02/03/04

</details>

<details>
<summary>✅ v1.1.0 Fix Projection API (Phases 6) — SHIPPED 2026-02-26</summary>

See: [v1.1.0 Milestone Details](milestones/v1.1.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.0.0 MVP (Phases 1-5) — SHIPPED 2026-02-22</summary>

- [x] Phase 1: Foundation
- [x] Phase 2: GeoJSON Loading
- [x] Phase 3: GeoPackage Loading
- [x] Phase 4: Rendering Core
- [x] Phase 5: Multi-layer Composition

</details>

---

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 7. Data Inspection | v1.2.0 | 1/1 | Complete | 2026-02-26 |
| 8. Rendering Improvements | v1.2.0 | 4/4 | Complete | 2026-02-27 |
| 9. API Design | v1.2.0 | 4/4 | Complete | 2026-02-27 |
| 10. Documentation & Examples | v1.2.0 | 4/4 | Complete | 2026-02-27 |

---

_For detailed phase information, see archived milestone files in `.planning/milestones/`_
