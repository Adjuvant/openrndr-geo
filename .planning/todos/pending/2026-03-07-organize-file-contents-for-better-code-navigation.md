---
created: 2026-03-07T01:27:20.473Z
title: Organize file contents for better code navigation
area: tooling
files:
  - src/main/kotlin/geo/
---

## Problem

Code organization within individual files could be improved to help new users absorb the mental model of the library when scanning code. Currently, file contents may not follow a logical order that makes the library's architecture immediately clear. This creates friction for developers trying to understand:
- How classes relate to each other
- What the core abstractions are
- Where to find related functionality

## Goals

- Ensure content within files follows a logical order
- Group related concepts together (don't split too eagerly)
- Make the library's mental model clear when scanning code
- Improve ease of navigation for new users

## Scope Guidelines

**DON'T:** Split things into separate files too eagerly
**DO:** Keep related things together if they make sense as a unit

This is about *organization*, not *fragmentation*. A file should tell a coherent story.

## Heuristics for Organization

### Order of declarations (typical):
1. Package declaration and imports
2. File-level KDoc describing the module
3. Public interfaces/abstract classes (the contracts)
4. Primary implementations
5. Extension functions that operate on the types
6. Helper/internal functions
7. Companion objects with factory methods

### Grouping principles:
- **By abstraction level:** High-level APIs first, implementation details later
- **By usage frequency:** Most commonly used items should appear early
- **By dependency:** Things that depend on each other should be close
- **By cohesion:** Keep related functionality in proximity

### Code organization within classes:
1. Properties (primary constructor, then body)
2. Init blocks
3. Secondary constructors
4. Public API methods
5. Internal/protected methods
6. Private helper methods
7. Companion object

## Solution

**Phase 1: Audit**
1. Review each major file in `src/main/kotlin/geo/` and subdirectories
2. Identify files where order is confusing or illogical
3. Note missing KDoc or unclear module boundaries

**Phase 2: Restructure**
For each file needing work:
1. Reorder declarations to follow logical flow
2. Add/improve file-level KDoc explaining the module
3. Group related functions/classes together
4. Add section comments if file is large
5. Ensure imports are organized and minimal

**Phase 3: Verify**
1. Build compiles successfully
2. All tests pass
3. Spot-check navigation experience (can a new dev understand the structure?)

## Candidate Files to Review

Priority order (most impactful first):
- Core domain files: `Geometry.kt`, `Feature.kt`, `Bounds.kt`
- Data sources: `GeoSource.kt`, `GeoJSON.kt`, `GeoPackage.kt`
- Rendering: `layer/` subpackage contents
- Projections: `projection/` and `crs/` subpackages
- Utilities: `ProjectionExtensions.kt`, `SpatialIndex.kt`

## Success Criteria

- [ ] Core files follow consistent organization patterns
- [ ] File-level KDoc explains module purpose and key types
- [ ] Navigation requires less jumping around when reading
- [ ] New users can understand architecture from file structure alone
- [ ] No functionality changes (pure refactoring)
