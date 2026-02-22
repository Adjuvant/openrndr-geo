# Phase 5: Animation - Context

**Gathered:** 2026-02-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Users can create animated visualizations by animating geo structures over time. This includes:
1. Animating geo structures along defined paths
2. Smoothly tweening geometry properties (position, color, size) over time
3. Applying procedural motion effects to geo primitives

Scope does NOT include: interactive controls for animation, real-time data streaming, or non-geo animation use cases.

**Reference inspiration:** Anime.js (timeline-based, property tweening, easing functions, stagger effects) — adapted for Kotlin/OpenRNDR DSL style with geo data focus.

</domain>

<decisions>
## Implementation Decisions

### Animation trigger mode
- **OpenRNDR-native integration** — use `extend()` pattern consistent with Phases 3-4 layer system
- **Dual scope support** — both single global animator (one controller for all) AND per-feature animator (independent animations per geo feature)
- Lifecycle hooks and time control: OpenCode's discretion based on OpenRNDR patterns

### Easing and interpolation
- **Use orx-easing library** — leverage existing OpenRNDR extension for easing functions (linear, easeIn/Out/InOut variants, elastic, bounce, etc.)
- **Pluggable interpolators** — sensible defaults (position, color, size), with ability for users to provide custom interpolation functions
- **Path-based animations** — user choice between constant velocity and constant speed; constant speed as default
- **Per-property easing** — each animated property can have its own easing function (position uses easeOut, color uses linear, etc.)

### Animation scope and composition
- **Both feature and layer animation** — animate individual GeoFeature properties (position, color, size) AND layer-level properties (opacity, offset, zoom)
- **Both stagger types** — index-based stagger (delay by feature order) AND spatial stagger (delay by distance from geographic origin point for ripple effects)
- **Dual composition style** — timeline-based composition (`timeline().add(anim1).add(anim2, offset = 200)`) AND chain-based fluent API (`animate { }.then { }.then { }`)
- **Both coordinate systems** — projected screen coordinates (default for performance) AND geographic coordinates (option for accuracy)

### Temporal data integration
- **OpenCode's discretion** — "whatever is easy and sensible for DSL integration"
- Preference for helper utilities over core integration: `filterByTime()`, `animateTimeWindow()`
- Support both continuous time animation and discrete event-based time handling

### OpenCode's Discretion
- Time control mechanism (scrubbing, pausing) — choose based on OpenRNDR patterns
- Lifecycle hook implementation (callback vs DSL vs coroutine/Flow)
- Exact temporal data integration approach
- Default interpolator implementations
- Timeline vs chain API syntax details

</decisions>

<specifics>
## Specific Ideas

- "Copy anime.js but for Kotlin/OpenRNDR DSL style, focusing on geo data not general UI"
- Anime.js features to adapt: timeline-based control, property tweening, easing functions, stagger effects
- Keep consistent with Phase 3-4 `extend()` patterns (e.g., `extend(GeoAnimator)`)
- Spatial stagger for geographic ripple effects (e.g., earthquake shockwave visualizations)
- Per-feature animation enables particle-like effects with geo data

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. All ideas (interactive controls, real-time streaming, advanced temporal analysis) would be new capabilities belonging in future phases.

</deferred>

---

*Phase: 05-animation*
*Context gathered: 2026-02-22*
