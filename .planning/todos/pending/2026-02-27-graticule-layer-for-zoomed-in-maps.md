---
created: 2026-02-27T21:30:51.608Z
title: Graticule layer for zoomed-in maps
area: layer
files:
  - examples/layer/01-graticule.kt
---

## Problem

The graticule layer example fails - this is a known issue. Graticule lines need some thought for more zoomed-in maps. A lot of mapping work will be sight-based (local/regional) rather than globe-spanning. The current implementation assumes global-scale rendering and doesn't work well for zoomed-in views.

Current state:
- Graticule example exists at examples/layer/01-graticule.kt
- Example was created during Phase 10 but fails at runtime
- User explicitly noted this is a known issue planned for v1.3.0

## Solution

TBD - needs research and thought

Approach considerations:
- Support variable graticule density based on zoom level
- Consider viewport-based graticule generation (only draw lines within visible area)
- Different line spacing strategies for local vs global scales
- May need configurable interval parameters (degrees vs pixels)
- Research how other mapping libraries handle this (Leaflet, Mapbox, etc.)

This is related to the layer composition feature area and will need API design work.
