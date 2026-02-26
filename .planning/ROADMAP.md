# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.0.0 MVP** — Phases 1-5 + 4.1 (shipped 2026-02-22)
- **v1.1.0** — In Progress

---

## Current Milestone: v1.1.0

### Phase 6: Fix projection errors and API design ✓ COMPLETE

**Goal:** Fix projection scaling/fitBounds API and simplify data overlay workflows for confidence in multi-dataset alignment
**Depends on:** v1.0.0 foundation
**Plans:** 5/5 complete
**Completed:** 2026-02-26

Plans:
- [x] Fix projection scaling and fitBounds API
- [x] Fix MultiPolygon rendering for ocean/whole-world data
- [x] Reduce API boilerplate for common rendering workflows
- [x] Simplify CRS handling API
- [x] Integration and regression testing

**Details:**
Addresses critical friction discovered during real-world usage:
- ✓ Scale parameter replaced with intuitive zoomLevel
- ✓ fitBounds working with contain strategy
- ✓ MultiPolygon handles ocean data via Mercator clamping
- ✓ "Load → visualize" reduced to 1-2 lines with three-tier API
- ✓ CRS auto-detection eliminates manual EPSG handling

**Deliverables:**
- 191 tests passing (including 20+ new Phase 6 tests)
- MIGRATION-v1.0-to-v1.1.md guide
- Three-tier API: drawer.geoJSON() → geoSource() → full control
- GeoStack for multi-dataset overlays
- Verification: 06-VERIFICATION.md

---

_For milestone details, see .planning/milestones/_
