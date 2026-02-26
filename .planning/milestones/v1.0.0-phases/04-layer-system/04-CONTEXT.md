# Phase 4: Layer System - Context

**Gathered:** 2026-02-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Compose multiple geo data sources as visual layers with blend modes and screenshot capture capabilities. Integration with orx-compositor, not building a layer system from scratch.

</domain>

<decisions>
## Implementation Decisions

### Use orx-compositor
- Don't reinvent the wheel — use existing OpenRNDR orx-compositor library
- Add orx-compositor to Gradle dependencies
- Geo drawing works directly in compositor layers (standard drawer calls)

### Layer system design
- Use orx-compositor's `compose { layer { draw { ... } } }` DSL
- Layer visibility managed at code level (no API layer for toggling)
- No custom layer management infrastructure needed

### Blend modes
- Provided by orx-compositor: Add(), Multiply(), Overlay(), Screen(), etc.
- No geo-specific blend mode handling required
- Documentation should blend modes showing which work well with geo features

### Graticule implementation
- Graticule is just a layer (same as any other geo data)
- Provide helper function `generateGraticule(spacing: Double, bounds: Bounds): List<Feature>`
- Users can generate graticule manually if needed

### Example program structure
- 4 focused examples (no "full complexity" workflow yet)
- Use real geo data files from project data/ directory

### OpenCode's Discretion
- Exact compositor DSL syntax for geo drawing examples
- Which blend modes to document as "recommended for geo"
- Screenshot capture file naming/location

</decisions>

<specifics>
## Specific Ideas

- "Use orx-compositor because it's already battle-tested and documented"
- Keep Phase 4 minimal — just integration and examples, no new infrastructure

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within Phase 4 scope

</deferred>

---

*Phase: 04-layer-system*
*Context gathered: 2026-02-22*