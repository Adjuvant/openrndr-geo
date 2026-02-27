---
created: 2026-02-27T14:51:54.184Z
title: Add UX improvements for RawProjection and API samples
area: api
files:
  - src/main/kotlin/geo/projection/RawProjection.kt
  - src/main/kotlin/geo/render/DrawerGeoExtensions.kt
---

## Problem

Two nice-to-have UX improvements identified during Phase 9 UAT:

1. **RawProjection off-screen warning**: When users use RawProjection, coordinates pass through unchanged. For CRS like BNG (British National Grid), coordinates are in tens of thousands (e.g., x: 300000, y: 500000), which renders completely off-screen on typical displays. Users get no feedback that their data is invisible.

2. **Professional API sample syntax**: Sample code in tests used `projection = Mercator()` but the correct syntax is `projection = ProjectionMercator { }` (with braces for the builder pattern). This caused confusion during UAT.

## Solution

1. **RawProjection warning**: Add a console warning when RawProjection is used that explains coordinates pass through unchanged and suggests checking if data appears off-screen. Could check coordinate magnitude (>10000 for typical screen coords) and warn proactively.

2. **Improve sample code**: Update test examples and documentation to use correct syntax `ProjectionMercator { }`. Consider adding a compiler error or alias if `Mercator()` is used.
