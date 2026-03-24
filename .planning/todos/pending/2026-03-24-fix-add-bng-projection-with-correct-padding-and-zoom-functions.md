---
created: 2026-03-24T22:28:11.254Z
title: Fix/add BNG projection with correct padding and zoom functions
area: projection
files:
  - src/main/kotlin/geo/projection/ProjectionBNG.kt
  - src/main/kotlin/geo/projection/ProjectionFactory.kt
  - src/main/kotlin/geo/projection/ProjectionConfig.kt
---

## Problem

BNG (British National Grid) projection is missing or broken. Other projections (Mercator, etc.) have padding and zoom functions that BNG lacks. This needs to be fixed so BNG behaves consistently with other projections in the codebase.

## Solution

TBD — requires analysis of existing projection implementations (Mercator, etc.) to understand the padding and zoom function patterns, then applying same patterns to ProjectionBNG.kt.

Check ProjectionFactory.kt and ProjectionConfig.kt to understand how projections are registered and configured, then ensure BNG has equivalent functionality.
