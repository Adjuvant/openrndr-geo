# Phase 6: Fix projection errors and API design - Context

**Gathered:** 2026-02-25
**Status:** Ready for planning

<domain>
## Phase Boundary

Fix projection/fitBounds bugs and reduce API friction for data visualization workflows. Address critical issues discovered during real-world v1.0.0 usage: broken projection scaling, MultiPolygon failures on edge-case data (ocean/whole-world), and excessive boilerplate for simple "load → visualize" workflows.

This phase makes the library more accessible to creative coders while preserving power-user control.

</domain>

<decisions>
## Implementation Decisions

### fitBounds API Design

**Three variants for different use cases:**
- `projection.fit(bbox)` — mutates projection in place (80% case: "just use this bit of map")
- `projection.fitted(bbox)` — returns new Projection instance (when steps need clear accounting)
- `projection.fitBoundsParameters(bbox)` — returns transform parameters (for animation)

**Padding:**
- Pixel-based padding parameter: `fit(bbox, padding=20)`
- Rationale: Width/height are ints, pixels align with OpenRNDR rendering model

**Aspect ratio handling:**
- Contain strategy (fit entirely, letterbox)
- Never crop user data

**BBox handling:**
- Never modify user's bbox — use exactly what they provide
- Distinguish mental model: site bbox (user's area of interest) vs data bbox (features in file)
- Clear parameter naming conveys intent: `fitTo(siteBounds)` vs working with `dataBounds`

**CRS handling in fitBounds:**
- Support both BBox with embedded CRS property AND explicit CRS parameter
- Rationale: Supports experimentation while helping knowledgeable users with error checking

**Zoom constraints:**
- Optional min/max zoom parameters: `fit(bbox, minZoom=1.0, maxZoom=100000.0)`
- Important for creative visualization where zoom experimentation matters
- Abstract zoom values acceptable given creative use case

### API Boilerplate Reduction

**Tiered API philosophy:**
- Simple users: get it up and play quickly
- Advanced users: detailed inter-dataset mapping and visualization
- API provides pathways for different levels of detail

**Core pattern:**
- Extension on Drawer for draw operations: `drawer.geoJSON(data)`
- GeoSource pattern for load-once, draw-many: `val source = geoSource("file.json")`
- Separation: load/store in program loop, draw in render loop

**geoSource() defaults:**
- Auto-detect CRS: yes
- Provide default projection that fits to view: yes
- Ready to draw: simple boolean parameter
- Store both projection coordinates AND screen coordinates in GeoSource
- Rationale: Each serves different purposes (mapping relations, drawing, expression)

**Multi-dataset overlays (geoStack):**
- `val map = geoStack(coastline, cities, rivers)` — implicit collection
- Auto-unify CRS to first dataset's CRS (with warning log)
- Bounding boxes align across stack
- Single zoom/fit parameter controls whole stack
- Rationale: Common QGIS workflow — 1 layer = 1 file, but really one composite map

### CRS Handling Simplification

**CRS representation:**
- Strongly typed CRS enum: `CRS.WGS84`, `CRS.WebMercator`, `CRS.BritishNationalGrid`
- Rationale: Compile-time safety for creative coding tool

**Auto-detection strategy:**
- Primary: at load time via `geoSource()`
- Secondary: at transform time for special cases (e.g., side-by-side comparison)
- Avoid lazy detection — risks users not understanding what's happening

**Fallback when detection fails:**
- Assume WGS84 with warning log
- Rationale: WGS84 is most common, but user should know assumption was made

**Transform API:**
- Generic method: `geoSource.transform(to = CRS.WebMercator)`
- Rationale: Single discoverable method vs many extension methods

**Caching strategies:**
- Two approaches based on dataset size:
  - < 1000 points: cache screen coordinates, recompute on projection change
  - > 1000 points: spatial indexing + view culling
- Auto-select by default, allow explicit override for advanced users
- `materialize()` for eager evaluation when needed

**Animation + large datasets:**
- Support zoom-level constrained rendering (like QGIS symbology)
- Different detail levels at different scales

### OpenCode's Discretion

- Exact implementation of spatial indexing (quadtree vs other structures)
- Specific warning message format for CRS assumptions
- Default zoom constraint values
- Exact threshold for auto-selecting caching strategy (1000 points is guidance)
- MultiPolygon ocean data edge case handling (not explicitly discussed)
- Loading state/feedback UI during geoSource operations
- Exact naming of boolean parameter for "ready to draw" mode

</decisions>

<specifics>
## Specific Ideas

**OpenRNDR idioms to follow:**
- Extension methods on Drawer feel native to OpenRNDR ecosystem
- Pattern: `drawer.circle()`, `drawer.geoJSON()` — consistent with existing API
- Program-level setup, Drawer-level rendering

**QGIS workflow inspiration:**
- Export: 1 layer = 1 file
- Import: stack layers like "one map"
- Symbology changes with zoom level

**Use case driving design:**
- Site analysis of specific river in UK
- Load UK coastline, cities, ocean files (all UK coverage)
- But fit to river catchment bounds (specific site), not UK bounds (arbitrary data collection)
- Mental model: data files are containers, site bounds are intent

**Creative coding priorities:**
- Experimentation over strict correctness
- "We all need a little magic, but a trace of a warning may help"
- Keep pathways open for unexpected uses

</specifics>

<deferred>
## Deferred Ideas

**MultiPolygon ocean data handling:**
- Specific algorithm for whole-world/ocean datasets that span projection bounds
- Phase 6 will address the failure, exact approach left to research/planning

**Level-of-detail rendering:**
- Full QGIS-style symbology system with zoom-constrained features
- Phase 6: basic zoom constraints; full system may be future phase

**Advanced spatial operations:**
- Spatial joins, overlays, unions
- Out of scope for Phase 6

**Alternative projection systems:**
- Non-Earth projections (celestial, abstract)
- Future phase if needed

</deferred>

---

*Phase: 06-fix-projection-api*
*Context gathered: 2026-02-25*
