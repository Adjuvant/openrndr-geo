# Phase 6 Research: Projection Fitting Algorithms

**Phase:** 06-fix-projection-api  
**Research Date:** 2026-02-25  
**Focus:** Algorithms for fitting maps to screen via scale/zoom operations  

---

## Executive Summary

This research covers the fundamental algorithms required to implement proper fitBounds() functionality for map projections, specifically addressing:

1. **fitBounds algorithm** - Converting geographic bounding boxes to viewport-fitting projections
2. **Scale/zoom semantics** - The mathematical relationship between zoom levels, scale factors, and meters-per-pixel
3. **Whole-world dataset handling** - Properly fitting global data while handling Mercator pole limits
4. **Site vs data bounds** - Differentiating between the data container and the area of interest
5. **Aspect ratio handling** - Contain strategy to prevent data cropping

**Confidence Level:** HIGH - These algorithms are well-established in mapping libraries (Leaflet, Mapbox GL JS, D3, OpenLayers) with decades of production use.

---

## 1. fitBounds Algorithm

### 1.1 Core Formula

The fitBounds algorithm calculates the optimal projection parameters to fit a geographic bounding box within a viewport:

```kotlin
fun fitBounds(
    bbox: Bounds,           // Geographic bounds (minLat, minLng, maxLat, maxLng)
    viewport: Viewport,     // Screen dimensions (width, height in pixels)
    padding: Int = 0        // Padding in pixels
): ProjectionParameters {
    
    // Step 1: Calculate viewport size minus padding
    val paddedWidth = viewport.width - (padding * 2)
    val paddedHeight = viewport.height - (padding * 2)
    
    // Step 2: Project bbox corners to determine projected bounds
    val sw = project(bbox.minLng, bbox.minLat)  // Southwest corner
    val ne = project(bbox.maxLng, bbox.maxLat)  // Northeast corner
    
    // Step 3: Calculate scale factors for both dimensions
    val scaleX = paddedWidth / (ne.x - sw.x)
    val scaleY = paddedHeight / (ne.y - sw.y)
    
    // Step 4: Use minimum scale to ensure entire bbox fits (contain strategy)
    val scale = min(scaleX, scaleY)
    
    // Step 5: Calculate center point in projected coordinates
    val centerX = (sw.x + ne.x) / 2
    val centerY = (sw.y + ne.y) / 2
    
    // Step 6: Calculate viewport center in screen coordinates
    val viewportCenterX = viewport.width / 2
    val viewportCenterY = viewport.height / 2
    
    // Step 7: Calculate translation to center the projection
    val translateX = viewportCenterX - (centerX * scale)
    val translateY = viewportCenterY - (centerY * scale)
    
    return ProjectionParameters(scale, translateX, translateY)
}
```

### 1.2 Implementation from Leaflet (Reference)

Leaflet's `fitBounds` implementation (simplified):

```javascript
fitBounds: function(bounds, options) {
    bounds = this._latLngBounds(bounds);
    
    var paddingTL = point(options.paddingTopLeft || [0, 0]),
        paddingBR = point(options.paddingBottomRight || [0, 0]),
        padding = point(options.padding || 0),
        pixelBounds = this.getPixelBounds(bounds),
        zoom = this.getBoundsZoom(bounds, false, paddingTL.add(paddingBR).add(padding));
    
    zoom = (typeof options.maxZoom === 'number') ? 
        Math.min(options.maxZoom, zoom) : zoom;
    
    var paddingOffset = paddingBR.subtract(paddingTL).divideBy(2),
        swPoint = this.project(bounds.getSouthWest(), zoom),
        nePoint = this.project(bounds.getNorthEast(), zoom),
        center = this.unproject(swPoint.add(nePoint).divideBy(2).add(paddingOffset), zoom);
    
    return this.setView(center, zoom, options);
}
```

**Key insight:** Leaflet calculates zoom level first, then derives center point. This is the inverse of the scale-first approach.

### 1.3 D3-Geo's fitExtent/fitSize

D3 provides two variants:

```javascript
// fitExtent: fit to arbitrary extent [[x0, y0], [x1, y1]]
projection.fitExtent([[padding, padding], [width - padding, height - padding]], geojson);

// fitSize: fit to size [width, height] with zero padding
projection.fitSize([width, height], geojson);

// fitWidth: fit to width, height adjusts automatically
projection.fitWidth(width, geojson);

// fitHeight: fit to height, width adjusts automatically
projection.fitHeight(height, geojson);
```

**Implementation pattern (from d3-geo/src/projection/fit.js):**

```javascript
export function fitExtent(projection, extent, object) {
    var w = extent[1][0] - extent[0][0],
        h = extent[1][1] - extent[0][1],
        clip = projection.clipExtent && projection.clipExtent();
    
    projection.scale(150).translate([0, 0]);  // Reset to base
    
    var bounds = path.bounds(object),         // Calculate geographic bounds
        k = Math.min(w / (bounds[1][0] - bounds[0][0]), 
                     h / (bounds[1][1] - bounds[0][1])),
        x = +extent[0][0] + (w - k * (bounds[1][0] + bounds[0][0])) / 2,
        y = +extent[1][1] + (h - k * (bounds[1][1] + bounds[0][1])) / 2;
    
    projection.scale(k * 150).translate([x, y]);
    if (clip) projection.clipExtent(clip);
    return projection;
}
```

**Key insight:** D3 uses path.bounds() to calculate bounding box dynamically, supporting any geometry type.

---

## 2. Scale/Zoom Semantics

### 2.1 Mercator Zoom Level Formula

The relationship between zoom level and scale factor in Mercator projections:

```
scale = 256 * 2^zoom
```

Where:
- `scale` is the projection scale factor (256 = 1:1 at zoom 0)
- `zoom` is the zoom level (0 = world, higher = more zoomed in)

**Reverse formula:**
```
zoom = log2(scale / 256)
```

### 2.2 Meters-Per-Pixel Calculation

At zoom level `z`, the meters-per-pixel at latitude `φ`:

```
metersPerPixel = (cos(φ) * 2 * π * 6378137) / (256 * 2^z)
```

Where:
- `6378137` is Earth's radius in meters (WGS84)
- `φ` is latitude in radians
- `256` is tile size in pixels

**At equator (φ = 0):**
```
metersPerPixel = 156543.03392 / 2^z
```

**Zoom level reference:**
| Zoom | m/px (equator) | Description |
|------|----------------|-------------|
| 0 | 156,543 | Whole world |
| 5 | 4,893 | Country scale |
| 10 | 153 | City scale |
| 15 | 4.8 | Street scale |
| 20 | 0.15 | Building scale |

### 2.3 Scale Factor vs Zoom Level

**For OpenRNDR implementation:**

```kotlin
// Convert zoom level to scale factor
fun zoomToScale(zoom: Double, baseScale: Double = 1.0): Double {
    return baseScale * 2.0.pow(zoom)
}

// Convert scale factor to zoom level
fun scaleToZoom(scale: Double, baseScale: Double = 1.0): Double {
    return log2(scale / baseScale)
}

// Calculate scale for specific meters-per-pixel at latitude
fun metersPerPixelToScale(
    metersPerPixel: Double, 
    latitude: Double = 0.0,
    tileSize: Int = 256
): Double {
    val equatorialCircumference = 2 * PI * 6378137  // Earth's circumference
    val pixelCircumference = equatorialCircumference / metersPerPixel
    return pixelCircumference / tileSize
}
```

### 2.4 Recommendation for openrndr-geo

**Use "zoom level" semantics rather than arbitrary scale values:**

```kotlin
// Current (confusing):
scale = 10.0  // What does this mean?

// Recommended (clear):
zoomLevel = 5.0  // Clear meaning: country scale
// OR
metersPerPixel = 5000.0  // Clear meaning: 5km per pixel
```

---

## 3. Whole-World Dataset Handling

### 3.1 Valid World Bounds in WGS84

**For Mercator projection, valid world bounds are:**
- Longitude: -180° to +180° (full wraparound)
- Latitude: -85.05112878° to +85.05112878° (Mercator limit)

**NOT -90° to +90°** - The poles project to infinity in Mercator.

### 3.2 Mercator Latitude Clamping

```kotlin
val MAX_MERCATOR_LAT = 85.0511287798066  // radians: 1.4844222297483063

fun clampLatitude(latitude: Double): Double {
    return latitude.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
}
```

**Why this value?**
- `tan(π/4 + φ/2)` where φ = 85.05112878° ≈ π
- This makes the Mercator projection a square at zoom 0
- Enables seamless tile pyramid

### 3.3 Algorithm for Whole-World Fitting

```kotlin
fun fitWorldMercator(viewport: Viewport): ProjectionParameters {
    // World bounds in WGS84
    val worldBounds = Bounds(
        minLat = -85.05112878,
        maxLat = 85.05112878,
        minLng = -180.0,
        maxLng = 180.0
    )
    
    // Use standard fitBounds with world bounds
    return fitBounds(worldBounds, viewport, padding = 0)
}
```

### 3.4 Handling Pole Overflow

**Problem:** Data with latitude ±90° causes infinite y values in Mercator.

**Solution:** Pre-project clamping

```kotlin
fun projectWithClamping(lng: Double, lat: Double): Point {
    val clampedLat = lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
    return project(lng, clampedLat)
}
```

**For geometry processing:**

```kotlin
fun clampGeometryToMercator(geometry: Geometry): Geometry {
    return when (geometry) {
        is Point -> Point(
            lng = geometry.lng,
            lat = geometry.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)
        )
        is Polygon -> Polygon(
            exterior = geometry.exterior.map { 
                Point(it.lng, it.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT))
            },
            holes = geometry.holes.map { ring ->
                ring.map { Point(it.lng, it.lat.coerceIn(-MAX_MERCATOR_LAT, MAX_MERCATOR_LAT)) }
            }
        )
        // ... etc for other geometry types
    }
}
```

---

## 4. Site vs Data Bounds

### 4.1 Mental Model

**Data bounds** (`dataBounds`):
- Bounding box of all features in the dataset
- Determined by data file contents
- Example: UK coastline file spans all of UK

**Site bounds** (`siteBounds`):
- User's area of interest for visualization
- Determined by use case intent
- Example: River catchment within UK

### 4.2 API Design Pattern

```kotlin
// Option 1: Explicit parameters (recommended)
projection.fitTo(siteBounds)  // Fit to site (what user wants to see)
projection.fit(dataBounds)    // Fit to data (what file contains)

// Option 2: Contextual methods
projection.fitToView(siteBounds, viewport)
projection.fitToData(dataBounds, viewport)

// Option 3: Modifier chain
projection.fitted(dataBounds).focus(siteBounds)
```

### 4.3 Use Case: UK River Analysis

```kotlin
// User has files covering all of UK
val coastline = load("uk-coastline.json")      // dataBounds = all UK
val cities = load("uk-cities.json")            // dataBounds = all UK  
val ocean = load("world-ocean.json")           // dataBounds = whole world

// But wants to focus on specific river catchment
val riverCatchment = Bounds(
    minLat = 51.2, maxLat = 51.8,
    minLng = -1.5, maxLng = -0.8
)

// API should support:
val projection = ProjectionMercator {
    width = 800
    height = 600
    fitTo(riverCatchment, padding = 20)  // Focus on site, not data
}

// All layers rendered with same projection
// Ocean data visible but zoomed to river catchment
```

---

## 5. Aspect Ratio Handling

### 5.1 Contain Strategy (Recommended)

**Principle:** Fit entire bbox in viewport, maintaining aspect ratio, with letterboxing if needed.

```kotlin
fun calculateContainScale(
    bbox: Bounds,
    viewport: Viewport,
    project: (Double, Double) -> Point
): Double {
    val sw = project(bbox.minLng, bbox.minLat)
    val ne = project(bbox.maxLng, bbox.maxLat)
    
    val bboxWidth = ne.x - sw.x
    val bboxHeight = ne.y - sw.y
    
    val scaleX = viewport.width / bboxWidth
    val scaleY = viewport.height / bboxHeight
    
    // Use minimum to ensure entire bbox fits
    return min(scaleX, scaleY)
}
```

### 5.2 Centering with Letterboxing

```kotlin
fun calculateTranslation(
    bbox: Bounds,
    viewport: Viewport,
    scale: Double,
    project: (Double, Double) -> Point
): Point {
    val sw = project(bbox.minLng, bbox.minLat)
    val ne = project(bbox.maxLng, bbox.maxLat)
    
    val centerX = (sw.x + ne.x) / 2
    val centerY = (sw.y + ne.y) / 2
    
    val viewportCenterX = viewport.width / 2
    val viewportCenterY = viewport.height / 2
    
    return Point(
        x = viewportCenterX - (centerX * scale),
        y = viewportCenterY - (centerY * scale)
    )
}
```

### 5.3 Why Never Crop User Data

**Creative coding context:**
- Users may have artistic reasons for aspect ratio mismatches
- Cropping can hide important features
- Letterboxing is acceptable and expected
- User can adjust viewport or bbox if cropping is desired

---

## 6. Implementation Patterns

### 6.1 Three-Variant API

Based on Kotlin conventions and OpenRNDR patterns:

```kotlin
interface GeoProjection {
    // Variant 1: Mutate in-place (80% use case)
    fun fit(bbox: Bounds, padding: Int = 0): GeoProjection
    
    // Variant 2: Return new instance (functional approach)
    fun fitted(bbox: Bounds, padding: Int = 0): GeoProjection
    
    // Variant 3: Return parameters (for animation)
    fun fitParameters(bbox: Bounds, padding: Int = 0): TransformParameters
}

// Usage:
val proj1 = ProjectionMercator { width = 800; height = 600 }
proj1.fit(ukBounds)  // Mutates proj1

val proj2 = ProjectionMercator { width = 800; height = 600 }
    .fitted(ukBounds)  // Returns new instance

val params = ProjectionMercator { width = 800; height = 600 }
    .fitParameters(ukBounds)  // Returns scale/translate for animation
```

### 6.2 Zoom Constraints Integration

```kotlin
data class ZoomConstraints(
    val minZoom: Double? = null,
    val maxZoom: Double? = null
)

fun fitBounds(
    bbox: Bounds,
    viewport: Viewport,
    padding: Int = 0,
    constraints: ZoomConstraints = ZoomConstraints()
): ProjectionParameters {
    
    // Calculate unconstrained scale
    var params = calculateFit(bbox, viewport, padding)
    
    // Apply zoom constraints
    val zoom = scaleToZoom(params.scale)
    val constrainedZoom = when {
        constraints.minZoom != null && zoom < constraints.minZoom -> constraints.minZoom
        constraints.maxZoom != null && zoom > constraints.maxZoom -> constraints.maxZoom
        else -> zoom
    }
    
    params = params.copy(scale = zoomToScale(constrainedZoom))
    return params
}
```

### 6.3 Pixel-Based Padding

```kotlin
fun fitWithPadding(
    bbox: Bounds,
    viewport: Viewport,
    padding: Int
): ProjectionParameters {
    // Reduce effective viewport by padding
    val effectiveViewport = Viewport(
        width = viewport.width - (padding * 2),
        height = viewport.height - (padding * 2)
    )
    
    // Calculate fit for reduced viewport
    val params = calculateFit(bbox, effectiveViewport)
    
    // Adjust translation to account for padding offset
    return params.copy(
        translateX = params.translateX + padding,
        translateY = params.translateY + padding
    )
}
```

---

## 7. Mercator-Specific Considerations

### 7.1 Scale Varies with Latitude

**Critical issue:** Mercator is not conformal in scale - it preserves angles but distorts area.

**Implication for fitBounds:**
- Scale calculation at bbox center latitude
- Or use average scale across bbox
- D3 uses adaptive resampling to handle this

```kotlin
fun calculateMercatorScale(
    bbox: Bounds,
    viewport: Viewport,
    latitude: Double? = null  // null = use bbox center
): Double {
    val refLat = latitude ?: (bbox.minLat + bbox.maxLat) / 2
    
    // Project at reference latitude
    val y = mercatorY(refLat)
    val scaleAtEquator = calculateEquatorScale(bbox, viewport)
    
    // Adjust for latitude scale distortion
    return scaleAtEquator / cos(refLat.toRadians())
}
```

### 7.2 Handling Dateline Wrapping

**Problem:** Bbox spanning -180°/+180° (e.g., Pacific Ocean data)

**Solution:** Normalize longitude or split geometry

```kotlin
fun normalizeLongitude(lng: Double): Double {
    var normalized = lng
    while (normalized < -180) normalized += 360
    while (normalized > 180) normalized -= 360
    return normalized
}

// Or use modulo for cleaner code
fun normalizeLongitude(lng: Double): Double {
    return ((lng + 180) % 360 + 360) % 360 - 180
}
```

### 7.3 Pole Artifacts Prevention

**"Lines fuck up at poles"** happens when:
1. Geometry includes latitudes > ±85.05112878°
2. Projection attempts to project these to infinity
3. Line segments to/from infinity create visual artifacts

**Prevention:**
```kotlin
fun validateMercatorBounds(geometry: Geometry): Boolean {
    return geometry.allCoordinates { coord ->
        coord.latitude in -85.05112878..85.05112878
    }
}

fun safeProject(geometry: Geometry, projection: MercatorProjection): Geometry {
    return if (validateMercatorBounds(geometry)) {
        projection.project(geometry)
    } else {
        projection.project(clampGeometryToMercator(geometry))
    }
}
```

---

## 8. Code Examples

### 8.1 Complete fitBounds Implementation

```kotlin
class ProjectionMercator(
    var width: Int,
    var height: Int,
    var scale: Double = 1.0,
    var translateX: Double = 0.0,
    var translateY: Double = 0.0
) : GeoProjection {
    
    companion object {
        const val MAX_LAT = 85.0511287798066
        const val TILE_SIZE = 256
    }
    
    override fun fit(bbox: Bounds, padding: Int): ProjectionMercator {
        val params = calculateFit(bbox, padding)
        this.scale = params.scale
        this.translateX = params.translateX
        this.translateY = params.translateY
        return this
    }
    
    override fun fitted(bbox: Bounds, padding: Int): ProjectionMercator {
        val params = calculateFit(bbox, padding)
        return ProjectionMercator(
            width = width,
            height = height,
            scale = params.scale,
            translateX = params.translateX,
            translateY = params.translateY
        )
    }
    
    override fun fitParameters(bbox: Bounds, padding: Int): TransformParameters {
        return calculateFit(bbox, padding)
    }
    
    private fun calculateFit(bbox: Bounds, padding: Int): TransformParameters {
        // Calculate effective viewport
        val effectiveWidth = (width - padding * 2).toDouble()
        val effectiveHeight = (height - padding * 2).toDouble()
        
        // Project bbox corners
        val sw = project(bbox.minLng, bbox.minLat)
        val ne = project(bbox.maxLng, bbox.maxLat)
        
        // Calculate scale (minimum to ensure fit)
        val dx = ne.x - sw.x
        val dy = ne.y - sw.y
        val scale = min(effectiveWidth / dx, effectiveHeight / dy)
        
        // Calculate center
        val centerX = (sw.x + ne.x) / 2
        val centerY = (sw.y + ne.y) / 2
        
        // Calculate translation
        val viewportCenterX = width / 2.0
        val viewportCenterY = height / 2.0
        
        return TransformParameters(
            scale = scale,
            translateX = viewportCenterX - centerX * scale + padding,
            translateY = viewportCenterY - centerY * scale + padding
        )
    }
    
    private fun project(lng: Double, lat: Double): Point {
        val clampedLat = lat.coerceIn(-MAX_LAT, MAX_LAT)
        val x = Math.toRadians(lng)
        val y = ln(tan(Math.PI / 4 + Math.toRadians(clampedLat) / 2))
        return Point(x, y)
    }
}

// Convenience extensions
fun ProjectionMercator.fitWorld(): ProjectionMercator {
    return fit(Bounds.worldMercator())
}

fun ProjectionMercator.fitToSite(siteBounds: Bounds, dataBounds: Bounds): ProjectionMercator {
    // Use site bounds for fitting, but validate within data bounds
    require(siteBounds.intersects(dataBounds)) {
        "Site bounds must intersect with data bounds"
    }
    return fit(siteBounds)
}
```

### 8.2 Usage Examples

```kotlin
// Example 1: Simple world map
val worldMap = ProjectionMercator(800, 600)
    .fit(Bounds.worldMercator())

// Example 2: Focus on specific region with padding
val ukMap = ProjectionMercator(800, 600)
    .fit(ukBounds, padding = 20)

// Example 3: Site analysis (focus on river catchment)
val projection = ProjectionMercator(1200, 800)
val dataBounds = coastline.bounds  // All UK
val siteBounds = riverCatchmentBounds  // Specific area
projection.fitToSite(siteBounds, dataBounds)

// Example 4: Animation preparation
val startParams = ProjectionMercator(800, 600).fitParameters(worldBounds)
val endParams = ProjectionMercator(800, 600).fitParameters(cityBounds)
// Animate from startParams to endParams

// Example 5: Immutable chain
val projection = ProjectionMercator(800, 600)
    .fitted(ukBounds, padding = 20)
    .copy(width = 1600, height = 1200)  // Resize without refitting
```

---

## 9. Edge Cases and Solutions

### 9.1 Bbox with Zero Area

**Problem:** All points have same lat/lng (degenerate bbox)

**Solution:** Use default scale or zoom level

```kotlin
fun calculateFit(bbox: Bounds, viewport: Viewport): TransformParameters {
    val sw = project(bbox.minLng, bbox.minLat)
    val ne = project(bbox.maxLng, bbox.maxLat)
    
    val dx = ne.x - sw.x
    val dy = ne.y - sw.y
    
    if (dx < 1e-10 || dy < 1e-10) {
        // Degenerate bbox, use default scale
        return TransformParameters(
            scale = 1.0,
            translateX = viewport.width / 2.0,
            translateY = viewport.height / 2.0
        )
    }
    
    // Normal calculation...
}
```

### 9.2 Dateline Crossing

**Problem:** Bbox from 170° to -170° (should span 20°, not 340°)

**Solution:** Detect and normalize

```kotlin
fun normalizeBbox(bbox: Bounds): Bounds {
    return if (bbox.maxLng < bbox.minLng) {
        // Crosses dateline
        bbox.copy(maxLng = bbox.maxLng + 360)
    } else {
        bbox
    }
}
```

### 9.3 Pole-Crossing Geometries

**Problem:** Geometry includes ±90° latitude

**Solution:** Clamp before projection

```kotlin
fun projectSafe(lng: Double, lat: Double): Point {
    return project(lng, lat.coerceIn(-MAX_LAT, MAX_LAT))
}
```

### 9.4 Empty or Invalid Bbox

**Problem:** NaN or infinite bounds values

**Solution:** Validate and fallback

```kotlin
fun validateBbox(bbox: Bounds): Boolean {
    return bbox.minLat.isFinite() && bbox.maxLat.isFinite() &&
           bbox.minLng.isFinite() && bbox.maxLng.isFinite() &&
           bbox.minLat < bbox.maxLat &&
           bbox.minLng < bbox.maxLng
}
```

---

## 10. Confidence Levels

| Algorithm | Confidence | Rationale |
|-----------|-----------|-----------|
| fitBounds core formula | **VERY HIGH** | Used in Leaflet, Mapbox, D3 for 10+ years |
| Scale/zoom conversion | **VERY HIGH** | Standard Mercator tile math (OSM, Google Maps) |
| Mercator clamping | **VERY HIGH** | Universal constant ±85.05112878° |
| Site vs data bounds | **HIGH** | Established UX pattern in QGIS, ArcGIS |
| Aspect ratio handling | **VERY HIGH** | Standard contain strategy used everywhere |
| Three-variant API | **HIGH** | Follows Kotlin/OpenRNDR conventions |
| Zoom constraints | **HIGH** | Common feature in mapping libraries |
| Pole artifact prevention | **MEDIUM** | Implementation-specific, needs testing |

---

## 11. References

### 11.1 Libraries Referenced

- **Leaflet** (https://leafletjs.com/) - `fitBounds()` implementation
- **Mapbox GL JS** (https://docs.mapbox.com/mapbox-gl-js/) - Camera fitting
- **D3-Geo** (https://github.com/d3/d3-geo) - `fitExtent()`, `fitSize()`
- **OpenLayers** (https://openlayers.org/) - View fitting
- **Turf.js** (https://turfjs.org/) - Bounding box calculations

### 11.2 Mathematical References

- Mercator projection formula: Snyder, J.P. (1987). "Map Projections: A Working Manual"
- Zoom level calculations: https://wiki.openstreetmap.org/wiki/Zoom_levels
- Web Mercator specification: https://epsg.io/3857

### 11.3 Code References

- Leaflet `fitBounds`: https://github.com/Leaflet/Leaflet/blob/main/src/map/Map.js#L500
- D3-Geo `fit.js`: https://github.com/d3/d3-geo/blob/main/src/projection/fit.js
- Mapbox `camera.ts`: https://github.com/mapbox/mapbox-gl-js/blob/main/src/ui/camera.ts

---

## 12. Next Steps for Implementation

1. **Implement core fitBounds()** with three variants (fit, fitted, fitParameters)
2. **Replace scale parameter** with zoomLevel or metersPerPixel
3. **Add latitude clamping** to Mercator projection (±85.05112878°)
4. **Implement site vs data bounds** distinction in API
5. **Add contain strategy** with letterboxing support
6. **Create integration tests** with whole-world ocean data
7. **Verify pole handling** doesn't cause artifacts

---

*Research completed: 2026-02-25*  
*Confidence: HIGH - All algorithms are well-established in production mapping libraries*
