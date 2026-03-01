---
phase: quick
task: 9
plan: 01
type: execute
wave: 1
depends_on: []
files_modified: [src/main/kotlin/geo/GeoStack.kt, examples/render/07-geostack-render.kt]
autonomous: true
requirements: []
must_haves:
  truths:
    - GeoStack tracks view bounds independently from data bounds
    - User can zoom to specific bounds with zoomTo()
    - User can center the view on a point with centerOn()
    - User can zoom by a factor with zoom()
    - User can pan the view with pan()
    - User can reset to full data view with reset()
    - render() uses current viewBounds, not totalBoundingBox
  artifacts:
    - path: src/main/kotlin/geo/GeoStack.kt
      provides: GeoStack class with view manipulation methods
      adds: viewBounds, zoomTo(), centerOn(), zoom(), pan(), reset()
  key_links:
    - from: zoomTo/centerOn/zoom/pan
      to: viewBounds property
      pattern: update viewBounds with new calculated bounds
    - from: render(drawer)
      to: viewBounds
      pattern: use viewBounds or fallback to totalBoundingBox()
---

<objective>
Add zoom and pan helpers to GeoStack for iterative map exploration. Users can zoom to specific regions, pan around, and reset to full view without creating manual projections.

Purpose: Enable fluid, interactive exploration workflow — load data, see full view, then progressively zoom into areas of interest.
Output: GeoStack with viewBounds state and manipulation methods
</objective>

<execution_context>
@~/.config/opencode/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/STATE.md
@src/main/kotlin/geo/GeoStack.kt
@src/main/kotlin/geo/Bounds.kt
@src/main/kotlin/geo/projection/ProjectionFactory.kt
@examples/render/07-geostack-render.kt
</context>

<tasks>

<task type="auto">
  <name>Add viewBounds and manipulation methods to GeoStack</name>
  <files>src/main/kotlin/geo/GeoStack.kt</files>
  <action>
Add the following to GeoStack class:

1. **Mutable viewBounds property** (defaults to null):
   ```kotlin
   private var viewBounds: Bounds? = null
   ```

2. **Public getter** for current view bounds:
   ```kotlin
   fun getCurrentViewBounds(): Bounds = viewBounds ?: totalBoundingBox()
   ```

3. **View manipulation methods**:
   ```kotlin
   /**
    * Zoom to fit specific geographic bounds.
    * @param bounds Target bounds to show
    */
   fun zoomTo(bounds: Bounds) {
       viewBounds = bounds
   }

   /**
    * Center the view on a specific point.
    * @param x Longitude/lat in current CRS
    * @param y Latitude/lng in current CRS
    */
   fun centerOn(x: Double, y: Double) {
       val current = getCurrentViewBounds()
       val halfWidth = current.width / 2.0
       val halfHeight = current.height / 2.0
       viewBounds = Bounds(
           minX = x - halfWidth,
           minY = y - halfHeight,
           maxX = x + halfWidth,
           maxY = y + halfHeight
       )
   }

   /**
    * Zoom in/out by a factor.
    * @param factor Zoom factor (< 1 = zoom out, > 1 = zoom in)
    */
   fun zoom(factor: Double) {
       val current = getCurrentViewBounds()
       val centerX = current.center.first
       val centerY = current.center.second
       val newWidth = current.width / factor
       val newHeight = current.height / factor
       viewBounds = Bounds(
           minX = centerX - newWidth / 2.0,
           minY = centerY - newHeight / 2.0,
           maxX = centerX + newWidth / 2.0,
           maxY = centerY + newHeight / 2.0
       )
   }

   /**
    * Pan the view by offset.
    * @param dx Offset in X (negative = left, positive = right)
    * @param dy Offset in Y (negative = down, positive = up)
    */
   fun pan(dx: Double, dy: Double) {
       val current = getCurrentViewBounds()
       viewBounds = Bounds(
           minX = current.minX + dx,
           minY = current.minY + dy,
           maxX = current.maxX + dx,
           maxY = current.maxY + dy
       )
   }

   /**
    * Reset view to show all data.
    */
   fun reset() {
       viewBounds = null
   }
   ```

4. **Update render(drawer)** to use viewBounds:
   ```kotlin
   fun render(drawer: Drawer) {
       val bounds = viewBounds ?: totalBoundingBox()  // Use viewBounds if set
       val projection = geo.projection.ProjectionFactory.fitBounds(...)
       render(drawer, projection)
   }
   ```

5. **Update class KDoc** to document the new view manipulation methods in the usage examples.
  </action>
  <verify>
    <automated>cd /Users/thomas/Documents/workspace/gsd-projects/openrndr-geo && grep -q "viewBounds" src/main/kotlin/geo/GeoStack.kt && grep -q "zoomTo" src/main/kotlin/geo/GeoStack.kt && grep -q "centerOn" src/main/kotlin/geo/GeoStack.kt && grep -q "fun zoom(factor" src/main/kotlin/geo/GeoStack.kt && grep -q "fun pan" src/main/kotlin/geo/GeoStack.kt && grep -q "fun reset()" src/main/kotlin/geo/GeoStack.kt && echo "All methods added"</automated>
    <sampling_rate>run after task completes</sampling_rate>
  </verify>
  <done>GeoStack has viewBounds property and all manipulation methods (zoomTo, centerOn, zoom, pan, reset)</done>
</task>

<task type="auto">
  <name>Update GeoStack example to demonstrate zoom/pan</name>
  <files>examples/render/07-geostack-render.kt</files>
  <action>
Update the 07-geostack-render.kt example to demonstrate the new view manipulation features. Add keybindings to showcase the iterative exploration workflow:

```kotlin
extend {
    // Clear with black background
    drawer.clear(ColorRGBa.BLACK)
    
    // Render all features with current view
    stack.render(drawer)
}

// Key bindings for iterative exploration
keyboard.keyDown.listen {
    when (it.key) {
        // Zoom to full view
        KEY_F -> {
            stack.reset()
            println("Reset to full view")
        }
        // Zoom in
        KEY_I -> {
            stack.zoom(1.5)
            println("Zoomed in")
        }
        // Zoom out
        KEY_O -> {
            stack.zoom(0.75)
            println("Zoomed out")
        }
        // Pan directions (arrow keys if available, or WASD)
        KEY_ARROW_LEFT, KEY_A -> {
            stack.pan(-stack.getCurrentViewBounds().width * 0.2, 0.0)
            println("Panned left")
        }
        KEY_ARROW_RIGHT, KEY_D -> {
            stack.pan(stack.getCurrentViewBounds().width * 0.2, 0.0)
            println("Panned right")
        }
        KEY_ARROW_UP, KEY_W -> {
            stack.pan(0.0, stack.getCurrentViewBounds().height * 0.2)
            println("Panned up")
        }
        KEY_ARROW_DOWN, KEY_S -> {
            stack.pan(0.0, -stack.getCurrentViewBounds().height * 0.2)
            println("Panned down")
        }
        else -> {}
    }
}
```

Also add keyboard import: `import org.openrndr.Keyboard.KEY_*` constants and update the header comment to document the keybindings.
  </action>
  <verify>
    <automated>grep -q "stack.zoom(1.5)" examples/render/07-geostack-render.kt && grep -q "stack.pan(" examples/render/07-geostack-render.kt && grep -q "stack.reset()" examples/render/07-geostack-render.kt && echo "Example updated with zoom/pan demo"</automated>
    <sampling_rate>run after task completes</sampling_rate>
  </verify>
  <done>Example demonstrates zoom/pan with keyboard controls</done>
</task>

<task type="checkpoint:human-verify">
  <name>Verify zoom/pan behavior works interactively</name>
  <what-built>
GeoStack with view manipulation methods and an example demonstrating keyboard-controlled zoom/pan
  </what-built>
  <how-to-verify>
Run the example and test the interactive features:

1. Run: `./gradlew run -Popenrndr.application=examples.render.GeoStackRenderKt`

2. Verify initial view shows all data (coastline, cities, rivers/lakes)

3. Test zoom in: Press 'I' - view should zoom in by 1.5x

4. Test zoom out: Press 'O' - view should zoom out by 0.75x

5. Test panning: Press arrow keys or WASD - view should pan in that direction

6. Test reset: Press 'F' - view should return to showing all data

7. Verify features remain visible during all transformations
  </how-to-verify>
  <resume-signal>Type "verified" or describe any issues with zoom/pan behavior</resume-signal>
</task>

</tasks>

<verification>
- GeoStack has `viewBounds` property that can be null (defaults to total data bounds)
- All manipulation methods exist: `zoomTo(bounds)`, `centerOn(x, y)`, `zoom(factor)`, `pan(dx, dy)`, `reset()`
- `render(drawer)` uses `viewBounds ?: totalBoundingBox()` for view extent
- Example demonstrates the features with keyboard controls
- Interactive testing confirms zoom and pan work correctly
</verification>

<success_criteria>
- User can load data with GeoStack, see full view, then progressively zoom into specific areas
- View state persists across render calls until modified
- Simple method calls (not manual projection creation) control the view
- Example provides working template for interactive exploration
</success_criteria>

<output>
After completion, create `.planning/quick/9-add-zoom-pan-helpers-to-geostack-for-ite/9-SUMMARY.md`
</output>
