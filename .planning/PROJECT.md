# openrndr-geo

## What This Is

A Kotlin/OpenRNDR prototyping library for creative geospatial visualization. It enables layering disparate datasets (geology, weather, planning, lithography, etc.) to reveal relationships through novel visual forms, from a critical cartography perspective. The library provides an expressive API for exploring intersections of geo datasets with computational generative ideas and animation.

## Core Value

An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## Requirements

### Validated

**v1.0.0 Foundation:**
- ✓ OPENRNDR application structure — existing template with `application { configure { } program { } }` pattern — v1.0
- ✓ Live-coding support via oliveProgram — existing for hot-reloading — v1.0
- ✓ ORX extensions available — shapes, noise, color, composition, envelopes, etc. — v1.0
- ✓ Geo data ingestion — GeoJSON, GeoPackage loading with spatial indexing (DATA-01, DATA-02, DATA-03) — v1.0
- ✓ Efficient handling of large datasets — lazy Sequence-based access, Quadtree spatial index (DATA-02) — v1.0
- ✓ Intermediate representation — normalized geo primitives (Point, LineString, Polygon, Multi*) — v1.0
- ✓ Projection support — Mercator, Equirectangular, BNG ↔ WGS84 transforms (COORD-01, COORD-02, COORD-03) — v1.0
- ✓ Drawing adapters — map geo primitives to OpenRNDR `Contour`, `Vector2`, shapes (REND-01, REND-02, REND-03, REND-04) — v1.0
- ✓ Clean architecture — separation of data layer and rendering layer — v1.0
- ✓ Swappable components — GeoSource abstraction, GeoJSON/GeoPackage implementations — v1.0
- ✓ Multi-layer composition — Layer system with blend modes (REND-05, REND-06) — v1.0
- ✓ Screenshot capture — Native OpenRNDR output (OUTP-01) — v1.0
- ✓ Graticule generation — Lat/lng grid reference (REF-01) — v1.0

**v1.1.0 API Improvements:**
- ✓ Animation layer — GeoAnimator with easing, tweening, procedural motion (ANIM-01, ANIM-02, ANIM-03) — v1.1.0
- ✓ CRS auto-detection — Automatic CRS handling with autoTransformTo() — v1.1.0
- ✓ Three-tier API — Simplified workflows: drawer.geoJSON() → geoSource() → full control — v1.1.0
- ✓ Viewport-relative zoom — zoom=0 fits world, not tile-based (256px) — v1.1.0
- ✓ fitBounds API — Reliable bounding box fitting with contain strategy — v1.1.0
- ✓ Multi-dataset overlays — GeoStack with CRS unification — v1.1.0

### Active

- [ ] Documentation — guide for exploring available functions, organized by visual intent (deferred from v1.0)
- [ ] Real-time pan/zoom — defer to v2
- [ ] Custom aesthetic primitives (hatching, stippling) — defer to v2
- [ ] Temporal animation (time-series playback) — defer to v2

### Out of Scope

- 3D rendering — defer to v2 (architecture should not preclude)
- Interactive pan/zoom — defer to v2 (batch-style rendering for v1)
- Production deployment tooling — focus is prototyping/exploration, not end-user apps
- Mobile/web targets — Kotlin/JVM desktop only for v1
- GIS-style analysis (spatial joins/buffers) — visualization library, not GIS

## Context

**Current State (v1.1.0 shipped):**
- ~12,713 lines of Kotlin production code
- 304 files created/modified
- 27 plans completed across 7 phases
- Build: OPENRNDR 0.4.5, Kotlin 1.9.22, JVM 17
- Test coverage: 193 tests passing

- Built on OPENRNDR 0.4.5 with Kotlin 2.2.10, JVM 17
- Existing template project with ORX extensions (shapes, noise, color, composition, envelopes, etc.)
- User comes from 3D transpiler experience — values intermediate data formats for design environments
- Critical cartography perspective: challenging default map aesthetics, revealing hidden relationships
- Data sources: UK Ordnance Survey (Zoomstack GeoPackage ~12GB), lithography, weather, public planning, traditional maps
- QGIS used for preprocessing; exports expected in standard geo formats
- Java libraries acceptable for geo heavy lifting (parsing, spatial ops)

**v1.1.0 Improvements:**
- Fixed critical projection API friction discovered in v1.0 real-world usage
- Added animation layer (GeoAnimator, easing, tweening, procedural motion)
- Simplified multi-dataset workflows with CRS auto-detection
- Reduced "load → visualize" boilerplate from 10+ lines to 1-2 lines
- Viewport-relative zoom semantics (zoom=0 fits world)
- Three-tier API: drawer.geoJSON() → geoSource() → full control

## Constraints

- **Tech Stack:** Kotlin/JVM with OPENRNDR 0.4.5 — creative coding framework choice
- **Platform:** Desktop only (macOS, Windows, Linux x64) — JVM 17 required
- **Data Size:** Must handle multi-GB GeoPackages without loading entirely into memory
- **Architecture:** Data ops and rendering ops must be cleanly separated

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Kotlin/JVM with OPENRNDR | Existing template, creative coding focus, access to Java geo libs | ✓ Shipped in v1.0 — works well |
| Batch rendering for v1 | Simpler architecture, real-time interaction adds complexity | ✓ Shipped in v1.0 — enables rapid prototyping |
| Intermediate representation for geo primitives | Enables swappable sources, decouples ingestion from drawing | ✓ Shipped in v1.0 — Geometry sealed class works great |
| Use OpenRNDR Vector2 for points | Integrates directly with drawing operations | ✓ Shipped in v1.0 — seamless OpenRNDR integration |
| Use proj4j for CRS transformations | Proven Java library with EPSG code support | ✓ Shipped in v1.0 — BNG→WGS84 works with ~3-5m accuracy |
| Use OpenRNDR built-in Easing (not orx-easing) | Fewer dependencies, simpler API | ✓ Shipped in v1.0 — works well |
| Sealed class hierarchy for Geometry | Enables exhaustive when expressions | ✓ Shipped in v1.0 — type-safe rendering |
| Sequence-based lazy iteration | Memory-efficient large dataset processing | ✓ Shipped in v1.0 — handles multi-GB GeoPackages |
| Viewport-relative zoom (not tile-based) | Tile pyramid math wrong for creative coding | ✓ Shipped in v1.1.0 — zoom=0 fits world |
| Three-tier API design | Common case too verbose | ✓ Shipped in v1.1.0 — 1-2 line workflows |
| CRS auto-detection | Manual EPSG handling painful | ✓ Shipped in v1.1.0 — transparent transforms |

---
*Last updated: 2026-02-26 after v1.1.0 milestone*
