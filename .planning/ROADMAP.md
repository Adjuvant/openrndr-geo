# Roadmap: openrndr-geo

## Overview

Build a Kotlin/OpenRNDR geospatial visualization library that enables creative coders to layer disparate datasets and explore intersections through novel visual forms. The journey starts with data ingestion and coordinate foundations, progresses through rendering and layer composition, and culminates with animation primitives for motion-based visualization.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Data Layer** - Load and access geo data from GeoJSON and GeoPackage formats
- [ ] **Phase 2: Coordinate Systems** - Transform coordinates between geographic systems and projections
- [ ] **Phase 3: Core Rendering** - Visualize geo primitives with configurable styling
- [ ] **Phase 4: Layer System** - Composite multiple data sources with blend modes and output
- [ ] **Phase 5: Animation** - Create animated visualizations from geo data

## Phase Details

### Phase 1: Data Layer
**Goal**: Users can load and access geo data from multiple formats with efficient querying
**Depends on**: Nothing (first phase)
**Requirements**: DATA-01, DATA-02, DATA-03
**Success Criteria** (what must be TRUE):
  1. User can load a GeoJSON file and access its features with Point/LineString/Polygon/Multi* geometries
  2. User can load a GeoPackage file and query features by bounding box (region-filtered access)
  3. User can access feature properties (key-value pairs) for data-driven operations
**Plans**: 3 plans

Plans:
- [ ] 01-01: Data model and GeoPrimitive hierarchy
- [ ] 01-02: GeoJSON reader implementation
- [ ] 01-03: GeoPackage reader with spatial indexing

### Phase 2: Coordinate Systems
**Goal**: Users can transform coordinates between geographic systems and render in basic projections
**Depends on**: Phase 1
**Requirements**: COORD-01, COORD-02, COORD-03
**Success Criteria** (what must be TRUE):
  1. User can transform geographic coordinates (lat/lng) to screen coordinates
  2. User can render data in Mercator and Equirectangular projections
  3. User can transform British National Grid (BNG) coordinates with OSTN15 accuracy (~1cm)
**Plans**: TBD

Plans:
- [ ] 02-01: Coordinate transformation infrastructure
- [ ] 02-02: Basic projection implementations (Mercator, Equirectangular)
- [ ] 02-03: British National Grid with OSTN15 grid transformation

### Phase 3: Core Rendering
**Goal**: Users can visualize geo primitives with configurable styling
**Depends on**: Phase 2
**Requirements**: REND-01, REND-02, REND-03, REND-04
**Success Criteria** (what must be TRUE):
  1. User can render Point geometries with configurable color, size, and shape
  2. User can render LineString geometries with configurable color and stroke weight
  3. User can render Polygon geometries with configurable fill color and stroke
  4. User can apply a consistent styling API across all geometry types
**Plans**: TBD

Plans:
- [ ] 03-01: RenderAdapter pattern and Point rendering
- [ ] 03-02: LineString and Polygon rendering adapters
- [ ] 03-03: Styling API and geometry styling

### Phase 4: Layer System
**Goal**: Users can composite multiple data sources as layers and capture rendered output
**Depends on**: Phase 3
**Requirements**: REND-05, REND-06, OUTP-01, REF-01
**Success Criteria** (what must be TRUE):
  1. User can stack multiple data sources as visual layers in a single composition
  2. User can apply blend modes (multiply, overlay, screen, etc.) to layers
  3. User can draw graticule/grid lines for lat/lng reference
  4. User can capture rendered output as image files using OpenRNDR screenshot
**Plans**: TBD

Plans:
- [ ] 04-01: GeoLayer management and composition
- [ ] 04-02: Layer blend modes implementation
- [ ] 04-03: Graticule drawing and output integration

### Phase 5: Animation
**Goal**: Users can create animated visualizations by animating geo structures over time
**Depends on**: Phase 4
**Requirements**: ANIM-01, ANIM-02, ANIM-03
**Success Criteria** (what must be TRUE):
  1. User can animate geo structures along defined paths
  2. User can smoothly tween geometry properties (position, color, size) over time
  3. User can apply procedural motion effects to geo primitives
**Plans**: TBD

Plans:
- [ ] 05-01: GeoAnimator interface and path animations
- [ ] 05-02: Property tweening system
- [ ] 05-03: Procedural motion primitives

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Data Layer | 0/3 | Not started | - |
| 2. Coordinate Systems | 0/3 | Not started | - |
| 3. Core Rendering | 0/3 | Not started | - |
| 4. Layer System | 0/3 | Not started | - |
| 5. Animation | 0/3 | Not started | - |

---
*Roadmap created: 2026-02-21*
