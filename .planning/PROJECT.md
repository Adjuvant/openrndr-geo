# openrndr-geo

## What This Is

A Kotlin/OpenRNDR prototyping library for creative geospatial visualization. It enables layering disparate datasets (geology, weather, planning, lithography, etc.) to reveal relationships through novel visual forms, from a critical cartography perspective. The library provides an expressive API for exploring intersections of geo datasets with computational generative ideas and animation.

## Core Value

An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative — decoupling data operations from rendering so new visual ideas can be prototyped quickly.

## Requirements

### Validated

- ✓ OPENRNDR application structure — existing template with `application { configure { } program { } }` pattern
- ✓ Live-coding support via oliveProgram — existing for hot-reloading
- ✓ ORX extensions available — shapes, noise, color, composition, envelopes, etc.

### Active

- [ ] Geo data ingestion — GeoJSON, GeoPackage, QGIS exports
- [ ] Efficient handling of large datasets — lazy/region-filtered access (e.g., extract subset from 12GB GeoPackage)
- [ ] Intermediate representation — normalized geo primitives (point, poly, line) in memory
- [ ] Projection support — British National Grid ↔ WGS84 coordinate transforms
- [ ] Drawing adapters — map geo primitives to OpenRNDR `Contour`, `Vector2`, shapes
- [ ] Animation layer — expose motion/generative capabilities on geo structures (leverage OpenRNDR Animatable, easing)
- [ ] Clean architecture — separation of data layer (ingestion, filtering, projection, IR) and rendering layer (drawing adapters, styling, animation)
- [ ] Swappable components — facade patterns so data sources are interchangeable at drawing level
- [ ] Documentation — guide for exploring available functions, organized by visual intent

### Out of Scope

- 3D rendering — defer to v2 (architecture should not preclude)
- Interactive pan/zoom — defer to v2 (batch-style rendering for v1)
- Production deployment tooling — focus is prototyping/exploration, not end-user apps
- Mobile/web targets — Kotlin/JVM desktop only for v1

## Context

- Built on OPENRNDR 0.4.5 with Kotlin 2.2.10, JVM 17
- Existing template project with ORX extensions (shapes, noise, color, composition, envelopes, etc.)
- User comes from 3D transpiler experience — values intermediate data formats for design environments
- Critical cartography perspective: challenging default map aesthetics, revealing hidden relationships
- Data sources: UK Ordnance Survey (Zoomstack GeoPackage ~12GB), lithography, weather, public planning, traditional maps
- QGIS used for preprocessing; exports expected in standard geo formats
- Java libraries acceptable for geo heavy lifting (parsing, spatial ops)

## Constraints

- **Tech Stack:** Kotlin/JVM with OPENRNDR 0.4.5 — creative coding framework choice
- **Platform:** Desktop only (macOS, Windows, Linux x64) — JVM 17 required
- **Data Size:** Must handle multi-GB GeoPackages without loading entirely into memory
- **Architecture:** Data ops and rendering ops must be cleanly separated

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Kotlin/JVM with OPENRNDR | Existing template, creative coding focus, access to Java geo libs | — Pending |
| Batch rendering for v1 | Simpler architecture, real-time interaction adds complexity | — Pending |
| Intermediate representation for geo primitives | Enables swappable sources, decouples ingestion from drawing | — Pending |

---
*Last updated: 2026-02-21 after initialization*
