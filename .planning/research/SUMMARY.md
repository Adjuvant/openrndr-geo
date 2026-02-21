# Project Research Summary

**Project:** openrndr-geo — Kotlin/OpenRNDR Geospatial Visualization Library
**Domain:** Creative Geospatial Visualization / Critical Cartography
**Researched:** 2026-02-21
**Confidence:** HIGH

## Executive Summary

This is a **batch-first geospatial visualization library** for creative coders using Kotlin and OpenRNDR. The niche is clear: critical cartography that challenges conventional map aesthetics, with first-class animation support and programmatic batch rendering — no existing tool combines all three. Unlike interactive-first tools (deck.gl, MapLibre), this library targets designers and artists who want to generate print-resolution stills and video frames programmatically, not build web maps.

The recommended architecture follows the standard geospatial pattern: **JTS for geometry**, **GeoTools for I/O and CRS transformations**, and **custom adapters to OpenRNDR primitives**. This leverages battle-tested Java libraries while maintaining clean Kotlin idioms. The critical architectural decision is **layered separation** — data, model, projection, rendering, and animation layers are isolated with clear interfaces. This prevents the common pitfall where adding new data formats requires renderer changes.

Key risks center on **CRS handling** and **memory management**. UK data often arrives in British National Grid (BNG), and using simple transformations introduces 3-5 meter errors — OSTN15 grid transformation is required for accuracy. For the target 12GB GeoPackage files, loading everything into memory is impossible; streaming with spatial indexing from day one is mandatory.

## Key Findings

### Recommended Stack

The stack prioritizes mature, well-documented Java GIS libraries with Kotlin-friendly APIs. JTS provides robust computational geometry; GeoTools handles the messy reality of format variations and coordinate systems; OpenRNDR brings creative coding ergonomics.

**Core technologies:**
- **JTS Topology Suite 1.20.0** — Geometry model, spatial operations, WKT/WKB parsing — industry standard, OGC-compliant
- **GeoTools 34.2** — DataStore API, GeoPackage/GeoJSON readers, CRS transformations with OSTN15 — unified I/O for all geo formats
- **OpenRNDR 0.4.5 + orx-shapes** — Creative coding framework, ShapeContour/Shape primitives for rendering
- **kotlinx-coroutines/serialization** — Async data loading, JSON parsing — matches existing stack

**Critical version notes:**
- GeoTools requires OSGeo Maven repository (not on Maven Central)
- GeoTools 34.2 includes OSTN15 grid transformation for accurate BNG ↔ WGS84

### Expected Features

**Must have (table stakes) — v1:**
- GeoJSON/GeoPackage loading — without data input, nothing works
- Coordinate transformations (lat/lng → screen) — fundamental to all geo rendering
- Basic projections (Mercator + 2-3 alternatives) — users need projection choice
- Point/Line/Polygon rendering with styling — core visual primitives
- Layer system — the "layering disparate datasets" use case is explicit
- Batch rendering (output to image) — v1 is explicitly batch-first

**Should have (differentiators) — v1.x:**
- Unconventional projections — orthographic, azimuthal, interrupted for critical cartography
- Animation primitives — motion reveals patterns static maps miss
- Layer blend modes — creative compositing beyond transparency
- Projection animation/transitions — popular visual effect in D3 demos

**Defer (v2+):**
- Interactive mode — major scope expansion; wait for validation
- Time/temporal dimension — complex data model
- Spatial queries/indexing — performance optimization

### Architecture Approach

Standard layered architecture separates data operations from rendering with a clean intermediate representation (GeoPrimitives). Each layer has clear interfaces and no upward dependencies — Data Layer never knows about OpenRNDR, Render Layer never knows about GeoPackage.

**Major components:**
1. **Data Layer** — GeoJSON/GeoPackage readers, spatial query engine, streaming with region filtering
2. **Model Layer** — GeoPrimitive sealed class hierarchy (Point/LineString/Polygon/Collection), BoundingBox, Properties
3. **Projection Layer** — CRS definitions, BNG ↔ WGS84 with OSTN15, coordinate transforms
4. **Rendering Layer** — RenderAdapter pattern (geometry type → drawer calls), StylingEngine, GeoLayer management
5. **Animation Layer** — GeoAnimator interface, path animations, property tweening
6. **Facade** — Simple API that orchestrates layers, hides complexity from users

### Critical Pitfalls

1. **CRS Confusion** — Never perform spatial operations on geographic coordinates (degrees); always transform to projected CRS first. UK data requires British National Grid for accurate meter-based measurements.
2. **Memory Exhaustion** — 12GB GeoPackage files cannot be loaded entirely. Design for streaming from day one: load by bounding box using GeoPackage's RTree spatial index.
3. **Silent Geometry Errors** — Invalid geometries (self-intersecting polygons, wrong winding order) don't throw errors but produce wrong results. Validate all input geometries on load with explicit error reporting.
4. **Inaccurate BNG Transformations** — Simple 7-parameter Helmert transformation introduces 3-5m errors. Must use OSTN15 grid transformation (included in GeoTools) for ~1cm accuracy.
5. **Renderer-Data Coupling** — Monolithic architecture where data loading and rendering are tightly coupled makes scaling impossible. Enforce clean layer separation from the start.

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Core Model & Data Layer
**Rationale:** All other layers depend on geometry model and data loading. CRS handling and validation must be correct from day one to avoid pervasive bugs.
**Delivers:** GeoPrimitive model, GeoJSON/GeoPackage readers, CRS transformations with OSTN15, geometry validation, streaming architecture
**Addresses:** Table stakes features: GeoJSON loading, coordinate transformations, layer system foundation
**Avoids:** CRS Confusion, Memory Exhaustion, Silent Geometry Errors, Inaccurate BNG Transformations

### Phase 2: Projection & Rendering Layer
**Rationale:** Once data is loading and validated, rendering adapters and projections are needed to visualize anything. Rendering adapters translate geo primitives to OpenRNDR calls.
**Delivers:** RenderAdapter pattern, basic projection implementations (Mercator + 2 alternatives), styling API, point/line/polygon rendering
**Uses:** JTS geometries, OpenRNDR Drawer API, GeoTools CRS
**Implements:** Rendering Layer from architecture

### Phase 3: Layer System & Batch Rendering
**Rationale:** Layer management composites multiple data sources. Batch rendering is the primary output mode for v1 — generating images/frames without GUI.
**Delivers:** GeoLayer management, batch rendering pipeline, image/frame output, viewport control
**Addresses:** Table stakes: layer system, batch rendering
**Uses:** Rendering adapters, OpenRNDR RenderTarget

### Phase 4: Animation Primitives
**Rationale:** Animation is a key differentiator. Once static rendering works, animation operates on the same primitives with time-based property modification.
**Delivers:** GeoAnimator interface, path animations, property tweening, projection transitions
**Addresses:** Differentiator: animation on geo structures

### Phase 5: Extended Projections & Polish
**Rationale:** Unconventional projections for critical cartography are a differentiator. This phase expands projection library and adds graticules, blend modes.
**Delivers:** Orthographic, azimuthal, interrupted projections, graticule drawing, layer blend modes
**Addresses:** Differentiators: unconventional projections, layer blend modes

### Phase Ordering Rationale

- Phase 1 comes first because all other layers depend on the model and data access — you can't render what you can't load
- Phase 2-3 order: rendering adapters must exist before layer system can composite them; batch rendering is core v1 functionality
- Phase 4-5 order: animation requires stable rendering; extended projections are nice-to-have differentiators
- CRS, validation, and streaming architecture in Phase 1 prevents 5 of 6 critical pitfalls from the start

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1:** GeoPackage RTree spatial index implementation — SQLite integration details, performance tuning for 12GB files
- **Phase 4:** Animation interpolation algorithms — path following, property tweening best practices for geo data

Phases with standard patterns (skip research-phase):
- **Phase 2:** RenderAdapter pattern is well-documented in architecture research
- **Phase 3:** Layer composition and batch rendering have clear OpenRNDR patterns

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Verified via official docs, GitHub releases, Context7 library |
| Features | MEDIUM | Web research + domain inference; no direct user interviews |
| Architecture | HIGH | Multiple sources (Cesium, deck.gl, MapLibre, Galileo) confirm layered pattern |
| Pitfalls | HIGH | Multiple GIS/geo sources verified, Ordnance Survey docs for BNG |

**Overall confidence:** HIGH

### Gaps to Address

- **User validation for features:** Features are inferred from competitor analysis and critical cartography discourse. During v1 development, validate that identified differentiators (unconventional projections, animation) match actual creative coder needs.
- **Performance testing at scale:** Memory/streaming strategies are based on GeoTools patterns but need validation with actual 12GB GeoPackage files during Phase 1.
- **Animation API ergonomics:** Animation layer is sketched but creative coders may need more or different primitives — iterate based on early user feedback.

## Sources

### Primary (HIGH confidence)
- **JTS 1.20.0 Release** — https://github.com/locationtech/jts/releases/tag/1.20.0
- **GeoTools 34.2** — https://github.com/geotools/geotools/releases
- **OpenRNDR Guide** — https://guide.openrndr.org/drawing/curvesAndShapes.html
- **GeoTools GeoPackage Plugin** — https://docs.geotools.org/latest/userguide/library/data/geopackage.html
- **GeoTools CRS Documentation** — https://docs.geotools.org/latest/userguide/library/referencing/crs.html
- **Ordnance Survey OSTN15** — Official UK transformation documentation

### Secondary (MEDIUM confidence)
- **deck.gl Architecture** — Layer architecture patterns
- **MapLibre Architecture** — Spatial indexing, rendering patterns
- **Cesium Architecture** — Layered system design
- **Galileo GIS** — Kotlin geo library reference
- **D3-geo Documentation** — Projection variety and animation patterns

### Tertiary (LOW confidence)
- **Critical cartography discourse** — Feature prioritization for critical cartography use cases
- **Creative coding community** — Feature needs inference from OPENRNDR studio projects, Noodles.gl examples

---
*Research completed: 2026-02-21*
*Ready for roadmap: yes*
