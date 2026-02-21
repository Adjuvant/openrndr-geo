# Requirements: openrndr-geo

**Defined:** 2026-02-21
**Core Value:** An expressive, well-architected API that makes exploring intersections of geo datasets feel fluid and creative

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Data Ingestion

- [x] **DATA-01**: User can load GeoJSON files (Feature, FeatureCollection, Point/LineString/Polygon/Multi* geometries)
- [x] **DATA-02**: User can load GeoPackage files with spatial indexing for efficient region-filtered access
- [x] **DATA-03**: User can access feature properties for data-driven styling

### Coordinate Systems

- [ ] **COORD-01**: User can transform geographic coordinates (lat/lng) to screen coordinates
- [ ] **COORD-02**: User can render data in basic projections (Mercator, Equirectangular minimum)
- [ ] **COORD-03**: User can transform British National Grid (BNG) coordinates with OSTN15 accuracy (~1cm)

### Rendering

- [ ] **REND-01**: User can render Point geometries with configurable styling (color, size, shape)
- [ ] **REND-02**: User can render LineString geometries with configurable styling (color, stroke weight)
- [ ] **REND-03**: User can render Polygon geometries with configurable fill and stroke
- [ ] **REND-04**: User can apply styling API (color, stroke, fill, opacity) to geometries
- [ ] **REND-05**: User can stack multiple data sources as layers
- [ ] **REND-06**: User can apply blend modes to layers (multiply, overlay, screen, etc.)

### Output

- [ ] **OUTP-01**: User can capture screenshots via OpenRNDR's existing screenshot function — preserve natural creative coding workflow, don't build separate batch infrastructure

### Animation

- [ ] **ANIM-01**: User can animate geo structures along paths
- [ ] **ANIM-02**: User can tween geometry properties over time (position, color, size)
- [ ] **ANIM-03**: User can apply procedural motion to geo primitives

### Reference

- [ ] **REF-01**: User can draw graticule/grid lines for lat/lng reference

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Extended Features

- **DATA-04**: User can load Shapefile format (multi-file .shp/.shx/.dbf)
- **COORD-04**: User can render in unconventional projections (orthographic, azimuthal, interrupted)
- **REND-07**: User can apply custom aesthetic primitives (hatching, stippling, lithographic patterns)
- **ANIM-04**: User can animate smooth transitions between projections
- **ANIM-05**: User can animate through temporal data (time-series playback)
- **OUTP-03**: User can control viewport programmatically (pan/zoom)

### Interactive Mode

- **INTR-01**: User can interact with map in real-time (pan, zoom, click)
- **INTR-02**: User can respond to mouse/keyboard events on geo features

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Interactive web maps | V1 is batch-first; web/WASM adds significant complexity |
| Real-time data streams | Out of scope for prototyping library; user handles data refresh |
| Basemap tiles | Contradicts critical cartography goal; users overlay own reference |
| GIS-style analysis | Visualization library, not GIS; spatial joins/buffers are PostGIS/QGIS territory |
| Built-in data sourcing | Maintenance burden; users source their own data |
| Marker clustering | Opinionated choice; creative coders implement own strategies |
| Standard map UI widgets | Encourages conventional aesthetics; users build own UI if needed |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| DATA-01 | 1 | Complete |
| DATA-02 | 1 | Complete |
| DATA-03 | 1 | Complete |
| COORD-01 | 2 | Pending |
| COORD-02 | 2 | Pending |
| COORD-03 | 2 | Pending |
| REND-01 | 3 | Pending |
| REND-02 | 3 | Pending |
| REND-03 | 3 | Pending |
| REND-04 | 3 | Pending |
| REND-05 | 4 | Pending |
| REND-06 | 4 | Pending |
| OUTP-01 | 4 | Pending |
| ANIM-01 | 5 | Pending |
| ANIM-02 | 5 | Pending |
| ANIM-03 | 5 | Pending |
| REF-01 | 4 | Pending |

**Coverage:**
- v1 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0 ✓

---
*Requirements defined: 2026-02-21*
*Last updated: 2026-02-21 after initial definition*
