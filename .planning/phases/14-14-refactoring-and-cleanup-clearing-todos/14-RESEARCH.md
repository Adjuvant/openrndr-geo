# Phase 14: Refactoring and Cleanup - Research

**Research Date:** 2026-03-07
**Phase:** 14 - Refactoring and Cleanup, Clearing TODOs

## Executive Summary

This phase addresses 7 TODO markers across 5 files in the codebase. The work is categorized into four sequential waves following the user's specified order: directory structure changes, method/code refactoring, broken feature fixes, and AOB (any other business).

## TODO Inventory

### 1. App.kt (2 TODOs) - Chain API Exploration
**Location:** `src/main/kotlin/App.kt:77,84`
**Type:** Documentation/Exploration

```kotlin
// TODO need the chain ordering explained, and need to see if shorter chains can work
// TODO try to take and render only cities in top half of screen
```

**Analysis:**
- These are exploration notes in the main application file
- Not critical bugs but represent incomplete understanding/testing of chain API
- Should be documented in proper API documentation or removed if resolved
- The "top half of screen" TODO is a demonstration/experiment, not a feature request

**Recommendation:**
- Convert to documentation comments explaining the chain API
- Remove experimental code or move to proper examples
- The App.kt file appears to be a scratch/experimentation file

### 2. GeoAnimator.kt (1 TODO) - Architecture Decision
**Location:** `src/main/kotlin/geo/animation/GeoAnimator.kt:114`
**Type:** Architecture/Design

```kotlin
// TODO Not sure animator should be a singleton, can only animate one thing?
```

**Analysis:**
- This is a fundamental design question about the animation system
- Current implementation uses singleton pattern via companion object
- The concern is valid: singleton limits concurrent animations
- However, for creative coding use case, this may be intentional (one animation at a time)

**Recommendation:**
- Evaluate if multiple simultaneous animations are needed
- If yes: Convert to non-singleton, allow instance creation
- If no: Document the design decision and remove TODO
- Check existing usage patterns in codebase

### 3. render_BasicRendering.kt (1 TODO) - Example Quality
**Location:** `src/main/kotlin/geo/examples/render_BasicRendering.kt:84`
**Type:** Example Improvement

```kotlin
// TODO this isn't basic now, this is feature rendering, intermediate. 
// Replace with dump geojson to screen.
```

**Analysis:**
- Example file name doesn't match content complexity
- Currently demonstrates feature iteration and rendering
- Suggests a simpler "dump geojson" example would be more appropriate for "basic"

**Recommendation:**
- Either rename file to reflect actual content (render_FeatureIteration.kt)
- Or create a truly basic example that just renders GeoJSON without feature processing
- Keep current example but with better naming

### 4. layer_BlendModes.kt (1 TODO) - API Enhancement
**Location:** `src/main/kotlin/geo/examples/layer_BlendModes.kt:91`
**Type:** API Enhancement

```kotlin
// TODO promote helper function to API feature, quite reusable.
```

**Analysis:**
- `drawDataQuadrant()` helper function is identified as reusable
- Currently private to the example file
- Suggests it should be part of the public API

**Recommendation:**
- Extract `drawDataQuadrant()` to a proper API location
- Consider if it belongs in `GeoSource` as `renderQuadrant()` or similar
- Document the API addition

### 5. GeoSource.kt (1 TODO) - Code Clarification
**Location:** `src/main/kotlin/geo/GeoSource.kt:192`
**Type:** Code Fix/Clarification

```kotlin
// TODO I though padding had changed to pixels?
padding = 0.9,
```

**Analysis:**
- Padding is being passed as 0.9 (likely a ratio/proportion)
- Comment suggests confusion about whether padding should be in pixels
- This is either a bug or needs documentation clarification

**Recommendation:**
- Verify `ProjectionFactory.fitBounds()` padding parameter semantics
- Check if 0.9 is correct (likely means 10% padding as ratio)
- Either fix the value or clarify with better comment
- Consistency check: ensure all padding usages follow same units

### 6. TemplateProgram.kt (2 TODOs) - Duplicate of App.kt
**Location:** `src/main/kotlin/TemplateProgram.kt:77,84`
**Type:** Duplicate

**Analysis:**
- Contains identical TODOs to App.kt
- Likely App.kt was copied from TemplateProgram.kt
- Both files appear to be examples/starting points

**Recommendation:**
- Treat as same TODOs as App.kt
- Consider if both files should exist or if one should be removed

## Categorization by Work Type

### Directory Structure Changes (Wave 1)
- **Potential:** Examples directory reorganization
- **Potential:** Consolidate App.kt and TemplateProgram.kt if redundant

### Method and Code Refactoring (Wave 2)
- GeoAnimator.kt: Convert TODO to design decision or refactor singleton
- GeoSource.kt: Clarify padding semantics and fix or document

### Broken Feature Fixes (Wave 3)
- layer_BlendModes.kt: Promote helper function to API
- render_BasicRendering.kt: Fix example naming/content mismatch

### AOB - Any Other Business (Wave 4)
- App.kt: Clean up experimental comments or document properly
- TemplateProgram.kt: Align with App.kt changes
- Final review for any missed TODOs

## Risk Assessment

| TODO | Risk Level | Impact |
|------|------------|--------|
| App.kt chain API | Low | Documentation only |
| GeoAnimator singleton | Medium | Architecture change if refactored |
| BasicRendering example | Low | Example rename only |
| BlendModes helper | Low | New API addition |
| GeoSource padding | Low | Small fix or documentation |

**Overall Risk: Medium** - Most changes are low risk, but GeoAnimator singleton decision requires careful consideration.

## Dependencies

- No external dependencies for this phase
- Internal: May need to review ProjectionFactory padding semantics
- Internal: Check GeoAnimator usage patterns before architectural change

## Validation Strategy

1. **After each wave:** Build passes
2. **After each wave:** Tests pass
3. **Final verification:**
   - Zero TODOs remaining in codebase
   - All FIXMEs addressed
   - Build and tests pass
   - Code review complete

## Notes

- This is a cleanup phase with no new features
- Focus is on code quality and technical debt reduction
- User specified "medium" risk tolerance with interactive fixing
- Changes should be made in sensible order per user's requirements
