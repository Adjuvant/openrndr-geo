---
created: 2026-02-25T01:23
title: Reduce API boilerplate for common rendering workflows
area: api
status: scheduled
roadmap_phase: 15
files:
  - src/main/kotlin/geo/examples/render_BasicRendering.kt
  - src/main/kotlin/geo/render/
---

## Problem

Current API requires too much boilerplate for the most common use case: load data → create projection → render.

Recent change (commit 19bbdfd): "feeding projection config into draw functions to reduce visible boiler plate when just want to draw something"

Also noted: "TODO Nice to have: join multiple imports into a single data monolith, then use features crawl as it."

## Impact

Creative coding workflow friction. Users need to write many lines of setup code before seeing visual output.

## Solution

TBD - Ideas from recent changes:
1. Pass projection directly to draw functions (already started)
2. Single-import API: `import geo.*` gets everything needed
3. Streamlined workflow: `GeoJSON.load().render(projection, drawer)`
4. Conventions over configuration for common cases

## Context

From commit 19bbdfd: "CHANGE: feeding projection config into draw functions to reduce visible boiler plate when just want to draw something."

Related to simplify-crs-handling-api todo (2026-02-22).

## Roadmap

**Status:** Scheduled for Phase 15 (API Ergonomics) — v1.4.0 Developer Experience milestone

**ROADMAP.md:** See Phase 15: API Ergonomics — Reduce Boilerplate
**Requirements:** API-01, API-02, API-03, API-04

This todo will be addressed when planning Phase 15. The goal is to reduce the common workflow from 3+ lines to 1-2 lines of code.
