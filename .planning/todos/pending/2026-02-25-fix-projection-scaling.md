---
created: 2026-02-25T01:23
title: Fix projection scaling and fitBounds API
area: api
files:
  - src/main/kotlin/geo/projection/ProjectionConfig.kt
  - src/main/kotlin/geo/projection/ProjectionFactory.kt
  - src/main/kotlin/geo/examples/render_BasicRendering.kt:62-70
---

## Problem

Scale parameter in ProjectionConfig is confusing and broken:
- "scale = 10.0" seems to act like 100% rather than intended 0-1 scale
- User comment: "TODO scale fucked"
- fitWorldMercator() is marked as "TODO Broken"
- fitBounds() at 50% broken - "stuff in right scale but lines fuck up at poles"
- Data clamping in Mercator causes issues for whole world visualization

## Impact

Users cannot reliably create projections that fit their data to the viewport. The common use case "load data, put it on screen, muck around with it visually" is difficult.

## Solution

TBD - Needs investigation into:
1. What scale parameter actually represents (document or fix)
2. Why fitWorldMercator is broken
3. Pole handling in Mercator projections
4. Whether to remove clamping for whole-world visualizations

## Context

From commit 19bbdfd: "Projection fitting/scaling is broken. Data clamping in mercator causes issues for whole world viz."
