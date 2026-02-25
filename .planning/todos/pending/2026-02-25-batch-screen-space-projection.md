---
created: 2026-02-25T01:23
title: Batch screen space projection for rendering efficiency
area: performance
files:
  - src/main/kotlin/geo/render/MultiRenderer.kt
  - src/main/kotlin/geo/Geometry.kt
  - src/main/kotlin/geo/examples/render_BasicRendering.kt:92-93
---

## Problem

Rendering is very wasteful - have to project every time you need points. Current API requires calling `.toScreen(projection)` on every geometry in every frame.

For the common use case (load data → put it on screen → muck around with it visually), this creates unnecessary overhead.

User comment: "TODO shouldn't we move everything to screen space by this stage? Or would that cause issues (data triangulation)?"

## Impact

Performance overhead for large datasets. Every frame re-projects all geometries even when data hasn't changed.

## Solution

TBD - Options to investigate:
1. Cache projected coordinates after first projection
2. Add `projected()` method to create screen-space copy
3. Batch projection API: `data.toScreenSpace(projection)` returns projected dataset
4. Smart caching in render functions

## Context

From commit 19bbdfd: "Rendering is very wasteful, have to project everytime you need points, need to batch screen space for a common use case"

Also relates to: Simplify CRS handling API todo (2026-02-22) - both address API friction for common workflows.
