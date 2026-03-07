# Phase 15: API Ergonomics — Reduce Boilerplate - Context

**Gathered:** 2026-03-07
**Status:** Ready for planning

<domain>
## Phase Boundary

Reduce API boilerplate for the most common creative coding workflow: load data → create projection → render. This phase covers import structure redesign and streamlined rendering API only. Advanced features (animation, complex layers) remain as they are — their APIs may be touched in future phases.

**Requirements covered:** API-01, API-02, API-03

</domain>

<decisions>
## Implementation Decisions

### Import Structure
- **Tiered by domain** — `geo.*` (essentials), `geo.data.*`, `geo.projection.*`, `geo.render.*`, `geo.animation.*`, `geo.layer.*`
- **Noun-oriented naming** — Matches OpenRNDR's pattern (`org.openrndr.draw`, `org.openrndr.color`)
- **`geo.*` minimal** — Only `GeoSource` and basic projection helpers exposed via wildcard
- **`drawer.geo()` moves to `geo.render.*`** — Explicit import required for rendering operations; data-only scripts don't need it

### Streamlined API Design
- **Two function approach:**
  - `geoSource()` — Raw/explicit control (manual projection, no automation)
  - `loadGeo()` — Auto-magic helper (auto-CRS, auto-projection, auto-fit, caching)
- **Data-centric workflow** — `loadGeo()` → `project()` → `drawer.geo()` (or auto versions)
- **Respects OpenRNDR program/extend pattern** — Loading in `program`, rendering in `extend`
- **Three styling options supported:**
  1. Default styling (built-in conventions)
  2. Inline style block: `drawer.geo(source) { stroke = Color.BLUE }`
  3. Separate styling step: `val styled = source.style { ... }`

### Backward Compatibility
- **Hard break** — No deprecation period, no aliases, clean migration
- **All 26 examples updated** — Convert everything to new API in this phase
- **No compatibility layer** — IDE refactoring handles import updates
- **`drawer.geo()` requires `geo.render.*`** — Part of the clean separation

### Convention vs Configuration
- **`loadGeo()` auto-everything:**
  - Auto-CRS detection (WGS84 fallback)
  - Auto-projection to fit viewport
  - Tight fit: 100% viewport (data touches edges)
  - Silent CRS transformation when needed
  - Behind-the-scenes caching
- **Default styles:**
  - White lines
  - Red fills  
  - Thin stroke (1.5)
  - Points as circles (r=5)
  - PolygonRenderer uses boolean methods (current pattern)
- **`geoSource()` explicit** — User makes all choices, no automation

### OpenCode's Discretion
- Exact class/package organization within domains
- Internal implementation of caching mechanism for `loadGeo()`
- IDE find/replace script for example migration (if needed beyond basic refactoring)
- Specific method signatures and parameter names
- Documentation wording and examples

</decisions>

<specifics>
## Specific Ideas

- Pattern from GeoStack: `geoSource()` for quick data mashing, explicit control for production work
- "Sprinkle of friction improves understanding" — explicit projection step aids debugging
- Match OpenRNDR's import philosophy: color is separate import, you pull in what you need
- Rendering is categorically different from data structures — keep them separate
- Data inspection scripts may not need rendering at all

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 15-api-ergonomics-reduce-boilerplate*
*Context gathered: 2026-03-07*
