# Feature Research

**Domain:** Creative Geospatial Visualization / Critical Cartography Library
**Researched:** 2026-02-21
**Confidence:** MEDIUM (web research + domain inference; no direct user interviews)

---

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| GeoJSON loading | De facto standard for web geo data | LOW | Must handle Feature, FeatureCollection, geometry types (Point, LineString, Polygon, Multi*) |
| Coordinate transformations | lat/lng → screen coordinates is fundamental | MEDIUM | Projection from geographic to pixel space is non-negotiable |
| Basic projections | Web Mercator is the default assumption | MEDIUM | At minimum: Mercator, Equirectangular. Most tools support these out of box |
| Layer system | Compositing multiple data sources is core use case | MEDIUM | Users expect to stack geology + weather + boundaries, etc. |
| Point/Line/Polygon rendering | Fundamental geometric primitives | LOW | Must draw all basic geometry types with styling |
| Styling API (color, stroke, fill) | Visual customization is assumed | LOW | ColorRGB, stroke weight, fill opacity at minimum |
| Viewport control (pan/zoom) | Navigation is standard for geo tools | MEDIUM | Even batch-rendered tools need viewport setup for framing |
| Attribute access | Properties on features are standard | LOW | GeoJSON properties must be queryable for data-driven styling |

### Differentiators (Competitive Advantage)

Features that set the product apart. Not required, but valuable.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Unconventional projections** | Critical cartography challenges Mercator hegemony; offers new perspectives | MEDIUM | Orthographic, azimuthal, interrupted, custom projections. D3-geo has 40+; creative coders want unusual ones |
| **Animation on geo structures** | Motion reveals patterns static maps miss; enables storytelling | MEDIUM | Animated flow lines, morphing boundaries, time-series playback, procedural motion |
| **Layer blend modes** | Creative compositing beyond transparency (multiply, overlay, screen, etc.) | LOW-MEDIUM | Borrowed from graphics tools; enables artistic layering of disparate datasets |
| **Batch rendering pipeline** | Generate print-resolution stills and video frames without GUI | MEDIUM | Headless rendering is crucial for "design tools that make tools" — programmatic output generation |
| **Time/temporal dimension** | Animate through time slices, reveal temporal patterns | MEDIUM-HIGH | Time-enabled GeoJSON, timestamp handling, time-series interpolation |
| **Custom aesthetic primitives** | Beyond standard cartographic symbols — generative, procedural, unconventional | MEDIUM | Dot density, hatching, stippling, lithographic patterns, texture-based fills |
| **Projection animation** | Smoothly transition between projections for visual impact | MEDIUM | Orthographic → Mercator morph, etc. Popular in D3/observable demos |
| **Data-driven styling API** | Expressive, functional mapping from attributes to visuals | LOW | `stroke { dataValue.times(10) }` style syntax; creative coders want minimal boilerplate |
| **Graticule/grid lines** | Reference structure for projection exploration | LOW | Drawing lat/lng grid helps users understand projection behavior |
| **Bounding box queries** | Filter geometries by spatial extent | MEDIUM | Essential for performance with large datasets; spatial indexing |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Interactive web maps (v1)** | Users assume "maps = interactive" | Scope explosion; requires tile servers, event handling, state management; distracts from core expressive API | Focus on batch rendering first; add interaction layer later (v2) |
| **Real-time data streams** | "I want live data on my map" | Architectural complexity; WebSocket infrastructure; out of scope for prototyping library | Load static snapshots; user handles their own data refresh |
| **Basemap tiles** | Familiar Google/Mapbox-style background maps | Locks users into tile providers; contradicts critical cartography goal of challenging default aesthetics | Provide projection + graticule; users overlay their own reference layers |
| **GIS-style analysis** | "Can this do spatial joins/buffers?" | This is a visualization library, not GIS software; scope creep into PostGIS/QGIS territory | Recommend loading pre-processed data; keep library focused on rendering |
| **Built-in data sourcing** | "Add weather API / geology feeds" | Maintenance burden; API keys; rate limits; data licensing | Provide format loaders (GeoJSON, CSV, Shapefile); users source their own data |
| **Marker clustering** | Handle overlapping points gracefully | Complex algorithm; many clustering strategies exist; opinionated choice may not fit creative use case | Provide point styling options; let users implement their own clustering logic |
| **Standard map UI widgets** | Zoom buttons, scale bar, attribution | Encourages conventional map aesthetics which critical cartography challenges | Keep raw API; users build their own UI if needed |

---

## Feature Dependencies

```
GeoJSON Loading
    └──requires──> Coordinate Transformations
                        └──requires──> Projection System

Animation on Geo Structures
    └──requires──> Time/Temporal Dimension (for time-based)
    └──requires──> Projection System (for spatial animation)

Projection Animation
    └──requires──> Multiple Projections
    └──requires──> Interpolation System

Batch Rendering Pipeline
    └──requires──> Headless Rendering Context
    └──requires──> Frame/Animation System

Layer Blend Modes
    └──requires──> Layer System
    └──requires──> Render Target / Compositing

Data-Driven Styling API
    └──requires──> Attribute Access
    └──requires──> Expression Evaluator
```

### Dependency Notes

- **GeoJSON Loading requires Coordinate Transformations:** Raw coordinates must be projected before rendering
- **Animation requires Time Dimension (for temporal):** Time-series animation needs temporal data model
- **Batch Rendering requires Headless Context:** Programmatic output needs rendering without window/GUI
- **Layer Blend Modes require Render Target:** Composite operations need intermediate buffers
- **Data-Driven Styling requires Expression Evaluator:** `dataValue.times(10)` needs parsing/evaluation

---

## MVP Definition

### Launch With (v1)

Minimum viable product — what's needed to validate the concept.

- [x] **GeoJSON/Shapefile loading** — Without data input, nothing works. Core requirement.
- [x] **Coordinate transformations (lat/lng → screen)** — Fundamental to all geo rendering.
- [x] **Basic projection (Mercator + 2-3 alternatives)** — Users need at least projection choice.
- [x] **Point/Line/Polygon rendering with styling** — Core visual primitives.
- [x] **Layer system (stack datasets)** — The "layering disparate datasets" use case is explicit in project goals.
- [x] **Batch rendering (output to image)** — V1 is explicitly batch-first, interactive later.

### Add After Validation (v1.x)

Features to add once core is working.

- [ ] **Animation primitives** — Once static rendering is validated, add motion
- [ ] **Layer blend modes** — Low complexity, high creative value
- [ ] **More projections (orthographic, azimuthal, etc.)** — Expand projection library
- [ ] **Projection animation/transition** — Popular visual effect
- [ ] **Graticule drawing** — Reference structure for projection work

### Future Consideration (v2+)

Features to defer until product-market fit is established.

- [ ] **Interactive mode** — Major scope expansion; wait for validation
- [ ] **Time/temporal dimension** — Complex data model; defer
- [ ] **Custom aesthetic primitives (hatching, stippling)** — Polish/features
- [ ] **Spatial queries/indexing** — Performance optimization for large datasets

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| GeoJSON loading | HIGH | LOW | P1 |
| Coordinate transformations | HIGH | MEDIUM | P1 |
| Basic projections (3-4) | HIGH | MEDIUM | P1 |
| Point/Line/Polygon rendering | HIGH | LOW | P1 |
| Layer system | HIGH | MEDIUM | P1 |
| Styling API | HIGH | LOW | P1 |
| Batch rendering (image output) | HIGH | MEDIUM | P1 |
| Layer blend modes | MEDIUM | LOW | P2 |
| Animation primitives | MEDIUM | MEDIUM | P2 |
| Unconventional projections | MEDIUM | MEDIUM | P2 |
| Projection animation | MEDIUM | MEDIUM | P2 |
| Graticule/grid | LOW | LOW | P2 |
| Time/temporal dimension | MEDIUM | HIGH | P3 |
| Interactive mode | MEDIUM | HIGH | P3 |
| Custom aesthetic primitives | MEDIUM | MEDIUM | P3 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

---

## Competitor Feature Analysis

| Feature | deck.gl | Unfolding Maps | D3-geo | cartokit | Our Approach |
|---------|---------|----------------|--------|----------|--------------|
| **Data format** | GeoJSON/binary | GeoJSON | GeoJSON/TopoJSON | GeoJSON | GeoJSON + Shapefile |
| **Projections** | Web Mercator (limited) | Mercator | 40+ projections | MapLibre projections | Curated set + unusual ones |
| **Animation** | Limited | Basic | Full control | None | First-class citizen |
| **Rendering** | WebGL (interactive) | Java2D/OpenGL | SVG/Canvas | MapLibre (interactive) | OPENRNDR (batch-first) |
| **Aesthetic freedom** | Cartographic defaults | Standard markers | Unlimited | Direct manipulation | Critical cartography lens |
| **Target user** | Data viz developers | Processing artists | Web developers | Cartographers | Creative coders |

### Key Differentiators from Competitors

1. **vs. deck.gl:** deck.gl is web-first, interactive, WebGL. We're batch-first, JVM/Kotlin, OPENRNDR-native. Different ecosystem, different use case.

2. **vs. Unfolding Maps:** Unfolding is Processing/Java but interactive-first. We're Kotlin/OPENRNDR with batch rendering focus. Both target creative coders, but different runtimes and philosophies.

3. **vs. D3-geo:** D3 is the gold standard for projection variety and animation. We can't match its breadth, but can offer: simpler API, Kotlin type safety, OPENRNDR ecosystem integration.

4. **vs. cartokit:** cartokit is direct-manipulation styling for web maps. We're code-first, programmatic generation. Opposite ends of the abstraction spectrum.

5. **Our niche:** Critical cartography + creative coding + batch rendering. No existing tool combines all three.

---

## Sources

- **Mainstream tools:** CARTO, Atlas, deck.gl, Felt, Mapbox, Google Earth Engine — standard GIS/visualization features
- **Creative coding tools:** Unfolding Maps (Processing), p5.js geo examples, OPENRNDR studio projects, Noodles.gl
- **Critical cartography:** "This Is Not an Atlas" (counter-cartographies collection), Counter Cartographies Collective, decolonial mapping discourse, ACME journal articles
- **Technical references:** D3-geo projection documentation, GeoJSON specification (RFC 7946), Observable projection examples
- **Pitfall research:** Spatial analysis pitfalls (MAUP, ecological fallacy), spatiotemporal data analysis anti-patterns

---

*Feature research for: Creative Geospatial Visualization / Critical Cartography Library*
*Researched: 2026-02-21*
