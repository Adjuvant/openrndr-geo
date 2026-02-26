---
created: 2026-02-26T17:19
title: Add GeoSource summary function
area: api
files:
  - src/main/kotlin/geo/GeoSource.kt
---

## Problem

Need a way to inspect GeoSource data structure at runtime for debugging and drawing support. Currently developers can't easily see what geometry types, properties, and structure their data contains. Common issue: expecting LineString features but finding MultiLineString (or other mismatches between assumed and actual geometry types).

## Solution

TBD - Add a summary() function to GeoSource that outputs sensible, formatted information to console including:
- Geometry type(s) present
- Feature count
- Property keys/values (sample)
- CRS information
- Bounding box

## Context

This surfaced during exploration of geo datasets where the actual geometry type differs from expectations (e.g., all features are MultiLineString when assumed to be LineString). Having a quick inspection method would help prototype visual ideas faster.
