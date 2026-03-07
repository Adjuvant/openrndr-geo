---
created: 2026-02-26T17:23
title: Improve polygon interior/exterior ring handling
area: rendering
status: scheduled
roadmap_phase: 16
files:
  - src/main/kotlin/geo/render/
---

## Problem

Current polygon handling uses a simplified exterior-only method that is described as a "hack". The oceans.geojson file renders strangely with this approach because it doesn't properly handle interior rings (holes). MultiPolygon features with complex ring structures need better rendering support.

## Solution

TBD - Leverage OpenRNDR's shape construction capabilities which are well-suited for complex shape handling:
1. Implement proper interior/exterior ring distinction in polygon rendering
2. Use OpenRNDR's CompoundShape or similar for multi-ring polygons
3. Handle winding order and ring classification correctly
4. Test with oceans.geojson and other complex polygon datasets

## Context

Current implementation only renders exterior rings, causing visual artifacts with ocean data. OpenRNDR provides robust shape construction primitives that should handle this more elegantly than the current workaround.

## Roadmap

**Status:** Scheduled for Phase 16 (Rendering Improvements) — v1.4.0 Developer Experience milestone

**ROADMAP.md:** See Phase 16: Rendering Improvements — Fix MultiPolygon and polygon ring handling
**Requirements:** RENDER-02
