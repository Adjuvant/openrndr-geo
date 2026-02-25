---
created: 2026-02-25T01:23
title: Fix MultiPolygon rendering for ocean/whole-world data
area: rendering
files:
  - src/main/kotlin/geo/render/MultiRenderer.kt:132-141
  - src/main/kotlin/geo/examples/render_BasicRendering.kt:131-134
---

## Problem

MultiPolygon rendering fails on ocean data because it covers beyond min/max lat/lng bounds. 

User comment: "TODO fails on ocean as it covers beyond min max lat longs"

Also from commit: "MultiPolygon not working as hoped."

## Impact

Cannot visualize global ocean data or any MultiPolygon that spans projection limits.

## Solution

TBD - Likely needs:
1. Geometry clipping before projection
2. Better handling of coordinates outside valid projection bounds
3. Alternative rendering strategy for spanning geometries

## Context

From commit 19bbdfd: "MultiPolygon not working as hoped."
