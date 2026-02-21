# Pitfalls Research

**Domain:** Geospatial Visualization Library (Kotlin/OpenRNDR)
**Researched:** 2026-02-21
**Confidence:** HIGH (multiple GIS/geo sources verified, OS documentation for BNG)

## Critical Pitfalls

### Pitfall 1: CRS Confusion — Running Measurements on Geographic Coordinates

**What goes wrong:**
Performing distance, area, or buffer calculations on coordinates stored in degrees (EPSG:4326/WGS84) instead of a projected coordinate system. A "1 degree buffer" in London is ~111km north-south but only ~69km east-west due to longitude convergence. Area calculations return nonsense values.

**Why it happens:**
- Data arrives in WGS84 (common default)
- Developers assume coordinates are "just numbers"
- CRS metadata is treated as optional or ignored
- Creative coders unfamiliar with GIS fundamentals

**How to avoid:**
- ALWAYS transform to an appropriate projected CRS before spatial operations
- For UK data: use British National Grid (EPSG:27700) for accurate meter-based measurements
- Document which CRS each operation expects
- Add runtime validation: reject spatial operations on geographic CRS

**Warning signs:**
- Buffer distances looking wrong (elliptical instead of circular)
- Area values that seem implausible
- Features overlapping when they shouldn't
- "Why is my point-in-polygon check failing?"

**Phase to address:**
Phase 1 (Data Layer) — CRS handling must be correct from day one

---

### Pitfall 2: Memory Exhaustion — Loading Large GeoPackages Into RAM

**What goes wrong:**
A 12GB GeoPackage loaded entirely into memory crashes the application or causes severe performance degradation. GeoPackage files are SQLite databases that can be queried selectively, but naive libraries load everything.

**Why it happens:**
- Many geo libraries (GeoPandas, etc.) default to `read_file()` which loads entire dataset
- Creative coding environments expect in-memory data structures
- "It worked with my test data" (1MB) then fails with real data (12GB)
- No streaming/chunking strategy in place

**How to avoid:**
- Design for streaming from the start: read by bounding box, by feature count, or by attribute filter
- Use SQLite's spatial index (R-tree) to query only visible features
- Implement level-of-detail (LOD) — fewer features when zoomed out
- Consider memory-mapped file access or database-backed storage
- Add early warning: fail fast if dataset > threshold and no streaming strategy

**Warning signs:**
- OutOfMemoryError with real datasets
- Application freezes on file load
- GC pressure warnings
- "Works on my machine" (with 32GB RAM) but fails on user's 8GB machine

**Phase to address:**
Phase 1 (Data Layer) — Architecture must support large datasets from the start

---

### Pitfall 3: Silent Geometry Errors — Data Corruption That Passes Validation

**What goes wrong:**
Invalid geometries (self-intersecting polygons, wrong winding order, unclosed rings) don't throw errors — they just produce wrong results. A spatial join returns 0 matches. A visualization shows gaps. Metrics in downstream dashboards are silently corrupted.

**Why it happens:**
- Geometry validation is expensive, often skipped
- Many tools "fix" silently or ignore invalid features
- Winding order differs between standards (GeoJSON vs WKB vs some libraries)
- Topology errors can be subtle (tiny self-intersection at coordinate precision limits)

**How to avoid:**
- Validate ALL input geometries on load with explicit error reporting
- Implement `isValid()` checks that tell users WHAT is wrong
- Normalize winding order consistently (recommend right-hand rule for exterior rings)
- Consider topology validation for critical datasets
- Log validation statistics (X features loaded, Y had issues and were fixed/ignored)

**Warning signs:**
- Spatial queries returning unexpected empty results
- Visual artifacts (gaps, overlaps, spikes)
- Downstream analysis producing implausible numbers
- "It worked last month" (data was updated with bad features)

**Phase to address:**
Phase 1 (Data Layer) — Validation is foundational

---

### Pitfall 4: Inaccurate BNG Transformations — Simple Helmert vs OSTN15

**What goes wrong:**
Using a simple 7-parameter Helmert transformation between OSGB36 (BNG) and WGS84 introduces 3-5 meter errors. For UK mapping, this is visible — features don't align with basemaps. OSTN15 (the official Ordnance Survey transformation) is a 1km grid-based interpolation that achieves ~1cm accuracy.

**Why it happens:**
- Simple transformations are faster and easier to implement
- Many libraries default to Helmert for "OSGB36 to WGS84"
- Developers don't realize there are multiple transformation options
- "Close enough" mentality, not understanding precision requirements

**How to avoid:**
- Use OSTN15/OSGM15 for all British National Grid transformations
- Cache the transformation grid in memory (it's ~1MB)
- Provide both options but default to accurate transformation
- Document transformation accuracy expectations

**Warning signs:**
- BNG data appearing offset from WGS84 basemaps by several meters
- Features lining up only after manual adjustment
- Users reporting "GPS coordinates don't match map"

**Phase to address:**
Phase 1 (Data Layer) — CRS transformations are core functionality

---

### Pitfall 5: Renderer-Data Coupling — Architecture That Can't Scale

**What goes wrong:**
A monolithic architecture where data loading, CRS transformation, styling, and rendering are tightly coupled makes it impossible to:
- Optimize one layer without breaking others
- Add new data formats
- Support different rendering backends
- Test components in isolation

**Why it happens:**
- "Just make it work" MVP thinking
- Creative coding frameworks often encourage immediate-mode rendering
- No clear separation between "geo logic" and "graphics logic"
- Incremental feature additions without refactoring

**How to avoid:**
- Enforce clean architecture layers:
  1. **Data Layer**: Load, validate, transform CRS, spatial index
  2. **Model Layer**: Geometry types, feature collections, spatial operations
  3. **Style Layer**: Symbology, color ramps, classification
  4. **Render Layer**: OpenRNDR drawing, GPU buffers, animation
- Each layer has clear interfaces and no upward dependencies
- Data Layer never knows about OpenRNDR
- Render Layer never knows about GeoPackage

**Warning signs:**
- "To add a new format, I need to modify the renderer"
- Unit tests require a graphics context
- Can't run headless data processing
- Animation frame drops because data loading happens every frame

**Phase to address:**
Phase 1 (Architecture) — Must be designed in from the start

---

### Pitfall 6: No Spatial Index — O(n²) Performance

**What goes wrong:**
Finding features in a viewport or testing point-in-polygon across millions of features without a spatial index causes O(n) or O(n²) operations. The application becomes unresponsive on pan/zoom.

**Why it happens:**
- Spatial indexes seem like "premature optimization"
- Many datasets are small enough that brute force works... until they aren't
- Creative coders may not know about R-tree, Quadtree, or H3 indexing

**How to avoid:**
- Build spatial index on load for any dataset > 1000 features
- R-tree is the standard choice for geospatial data
- SQLite GeoPackages include R-tree spatial indexes — use them
- For creative coding use cases, consider simpler grid-based indexing

**Warning signs:**
- Pan/zoom lag increases with dataset size
- Point-in-polygon test taking > 100ms
- "Adding more data made everything slow"

**Phase to address:**
Phase 2 (Performance) — After basic functionality works

---

## Technical Debt Patterns

Shortcuts that seem reasonable but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Load all data into memory | Simple code, fast access | OOM with real datasets | Demo with <100MB data only |
| Skip geometry validation | Faster load time | Silent data corruption, wrong results | Never |
| Use EPSG:4326 for everything | No transformations needed | Wrong measurements, distorted visuals | Only for visualization (no analysis) |
| Simple Helmert for BNG→WGS84 | Faster transformation | 3-5m error, misaligned features | Never for UK data |
| Hardcode CRS assumptions | Less code | Fails with new data sources | Never |
| Skip spatial index | Simpler implementation | Unusable at scale | Datasets < 1000 features only |

## Integration Gotchas

Common mistakes when connecting to external data sources.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| GeoPackage | Load entire file at once | Use SQLite queries with spatial filter |
| GeoPackage | Ignore spatial index | Leverage built-in R-tree for viewport queries |
| Mixed CRS data | Assume all layers same CRS | Transform all to common CRS on load |
| GeoJSON | Assume winding order is correct | Normalize to right-hand rule |
| OS Data (BNG) | Use generic WGS84 transformation | Use OSTN15 transformation grid |
| Large files | Read synchronously | Background loading with progress |

## Performance Traps

Patterns that work at small scale but fail as usage grows.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| In-memory data structures | OOM, GC pauses | Streaming, chunking, spatial index | > 2GB datasets |
| No LOD strategy | Frame drops at zoom out | Feature simplification at low zoom | > 10k visible features |
| Redraw everything each frame | Low FPS | Dirty region tracking, incremental updates | > 60fps requirement |
| Complex geometries at all zooms | Rendering bottleneck | Douglas-Peucker simplification | > 1000 complex polygons |
| CRS transform per frame | Animation stutter | Cache transformed coordinates | Animated pan/zoom |

## UX Pitfalls

Common user experience mistakes in geospatial visualization.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Spatial bias (choropleths) | Large regions dominate perception | Use cartograms or normalize by population |
| Projection distortion | Wrong mental model of distances | Show scale bar, use appropriate projection |
| Overwhelming detail | Users can't find what matters | Progressive disclosure, layer controls |
| No coordinate feedback | Users can't report location issues | Show coordinates on hover/click |
| Silent load failures | Users think app is broken | Loading indicators, error messages |
| No scale awareness | Misinterpretation of zoom levels | Zoom level indicator, appropriate detail |

## "Looks Done But Isn't" Checklist

Things that appear complete but are missing critical pieces.

- [ ] **CRS Transformation:** Often only works for one CRS pair — test BNG→WGS84 AND WGS84→BNG
- [ ] **Large File Support:** Works with test data — verify with 12GB production dataset
- [ ] **Geometry Validation:** Shows valid features — but does it handle/report invalid ones?
- [ ] **Animation:** Smooth at rest — test during data loading, pan/zoom
- [ ] **Spatial Index:** Features display — but does query performance degrade with dataset size?
- [ ] **Memory Management:** Works initially — test for memory leaks over extended sessions

## Recovery Strategies

When pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Wrong CRS architecture | HIGH | Refactor data layer, add CRS metadata throughout |
| Memory issues | MEDIUM | Implement streaming, may need architecture changes |
| Invalid geometries | LOW | Add validation, provide fix utilities |
| Slow spatial queries | MEDIUM | Add spatial index, may need data restructure |
| Transformation inaccuracy | LOW | Swap transformation implementation |

## Pitfall-to-Phase Mapping

How roadmap phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| CRS Confusion | Phase 1 (Data Layer) | Unit tests with known coordinate pairs |
| Memory Exhaustion | Phase 1 (Data Layer) | Load test with 12GB file, monitor memory |
| Silent Geometry Errors | Phase 1 (Data Layer) | Validation tests with invalid geometries |
| BNG Transformation | Phase 1 (Data Layer) | Compare against OS Coordinate Transformer |
| Renderer-Data Coupling | Phase 1 (Architecture) | Dependency analysis, headless tests |
| No Spatial Index | Phase 2 (Performance) | Benchmark queries at 1k, 10k, 100k features |

## Sources

- **GIS Measurement Failures**: LinkedIn/Matt Forrest - CRS confusion as most common trap
- **GeoPackage Memory Issues**: GitHub OSGeo/gdal #6563, GIS StackExchange multiple threads
- **Silent Geometry Errors**: LinkedIn/Gokul Ganesan, PostGIS Validity workshop
- **BNG Transformation**: Ordnance Survey documentation on OSTN15 vs Helmert
- **Architecture Patterns**: MapLibre architecture docs, deck.gl performance docs
- **Spatial Indexing**: CockroachDB spatial index docs, academic papers on R-tree performance
- **Animation Performance**: MapLibre, ArcGIS performance considerations
- **GIS Common Mistakes**: Geoinfotech 2025, multiple community sources

---
*Pitfalls research for: Kotlin/OpenRNDR Geospatial Visualization Library*
*Researched: 2026-02-21*
