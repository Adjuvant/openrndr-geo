# UAT Report: Phase 6 Fixes

**Phase:** 06-fix-projection-api  
**Date:** 2026-02-26  
**Tester:** Code Review

---

## Summary

Phase 6 delivered the planned features but **user acceptance testing reveals remaining issues** that violate documented behavior. The API doesn't work as users would expect based on documentation and TODO comments in code.

**Status:** ⚠️ PARTIAL - Requires fix phase

---

## Tests Conducted

### 1. fitBounds API ✅ FUNCTIONAL
- **Test:** `ProjectionFactory.fitBounds(bounds, width, height, padding, MERCATOR)`
- **Result:** Creates projection
- **Issue:** Padding changed from percentage to pixels (good) but semantics still unclear

### 2. Zoom Level Semantics ❌ BROKEN
- **Test:** Create projection with `zoomLevel = 0`
- **Expected:** Whole world visible (per KDoc: "0 = whole world")
- **Actual:** Need `zoomLevel = -2` to see whole world
- **Evidence:** 
  ```kotlin
  // From render_BasicRendering.kt:
  zoomLevel = -2.0, // 0 = whole world view
  // TODO Zoom level works, but 0 is not whole world view -2 is.
  ```
- **Root Cause:** Formula `zoom = log2(scale/256)` assumes tiled map semantics

### 3. fitWorldMercator ❌ BROKEN
- **Test:** `ProjectionFactory.fitWorldMercator(width, height)`
- **Expected:** Fits entire world in viewport
- **Actual:** Just creates config with zoom=0, center=(0,0), doesn't actually calculate fit
- **Evidence:** 
  ```kotlin
  // From render_BasicRendering.kt:
  // TODO Error, too zoomed in
  // val projection = ProjectionFactory.fitWorldMercator(width.toDouble(), height.toDouble())
  ```

### 4. Example TODOs ❌ NOT RESOLVED
Multiple examples have TODOs indicating the API doesn't work:
- `render_BasicRendering.kt:67` - "Zoom level works, but 0 is not whole world view -2 is"
- `render_BasicRendering.kt:69-70` - fitWorldMercator marked as broken
- `render_BasicRendering.kt:93` - "shouldn't we move everything to screen space?"
- `layer_Graticule.kt:122` - "think this is broken"

### 5. Pixel-Based Padding ✅ FIXED
- **Previous:** `padding = 0.8` meant 80% scale multiplier (confusing)
- **Current:** `padding = 20.0` means 20px on each side (clear)
- **Status:** Fixed in recent commit

---

## Issues Summary

| Issue | Severity | Location | Status |
|-------|----------|----------|--------|
| Zoom semantics wrong | HIGH | ProjectionConfig/Factory | ❌ Unfixed |
| fitWorldMercator broken | HIGH | ProjectionFactory | ❌ Unfixed |
| Example TODOs remain | MEDIUM | Multiple examples | ❌ Unfixed |
| Pixel padding done | - | ProjectionFactory | ✅ Fixed |

---

## Fix Required

Need Phase 6-FIX or Phase 7 to address:

1. **Fix zoom level calculation** - Ensure zoom=0 actually shows whole world
2. **Fix fitWorldMercator** - Actually calculate world-fitting projection
3. **Verify all examples work** - Remove TODOs, confirm API works as documented
4. **Update documentation** - Ensure KDoc matches actual behavior

---

*UAT conducted as part of milestone verification*
</content>