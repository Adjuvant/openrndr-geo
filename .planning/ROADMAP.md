# Roadmap: openrndr-geo

## Milestones

- ✅ **v1.0.0 MVP** — Phases 1-5 + 4.1 (shipped 2026-02-22)
- **v1.1.0** — In Progress

---

## Current Milestone: v1.1.0

### Phase 6: Fix projection errors and API design

**Goal:** Fix projection scaling/fitBounds API and simplify data overlay workflows for confidence in multi-dataset alignment
**Depends on:** v1.0.0 foundation
**Plans:** 6 plans (5 complete, 1 gap closure pending)
**Status:** Gap closure required

Plans:
- [x] 06-01 — Fix projection scaling and fitBounds API
- [x] 06-02 — Fix MultiPolygon rendering for ocean/whole-world data
- [x] 06-03 — Reduce API boilerplate for common rendering workflows
- [x] 06-04 — Simplify CRS handling API
- [x] 06-05 — Integration and regression testing
- [ ] 06-06 — Fix zoom semantics (gap closure from UAT)

**Details:**
Addresses critical friction discovered during real-world usage:
- ✓ Scale parameter replaced with intuitive zoomLevel
- ✓ fitBounds working with contain strategy
- ✓ MultiPolygon handles ocean data via Mercator clamping
- ✓ "Load → visualize" reduced to 1-2 lines with three-tier API
- ✓ CRS auto-detection eliminates manual EPSG handling
- ⚠️ Zoom semantics still use tile-based math (256px) - needs viewport-relative fix

**Deliverables:**
- 191 tests passing (including 20+ new Phase 6 tests)
- MIGRATION-v1.0-to-v1.1.md guide
- Three-tier API: drawer.geoJSON() → geoSource() → full control
- GeoStack for multi-dataset overlays
- Verification: 06-VERIFICATION.md
- UAT: 06-UAT.md (identified zoom semantics gap)

---

_For milestone details, see .planning/milestones/_
