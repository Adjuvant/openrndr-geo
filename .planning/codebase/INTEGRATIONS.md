# External Integrations

**Analysis Date:** 2026-03-02

## APIs & External Services

**None detected** - This is a standalone creative coding application with no external API integrations.

The following OpenRNDR extensions are available but not currently enabled:

- **OSC (Open Sound Control):** `orx-osc` - Available in ORX catalog
- **MIDI:** `orx-midi` - Available in ORX catalog
- **Runway ML:** `orx-runway` - Available in ORX catalog
- **Chataigne:** `orx-chataigne` - Available in ORX catalog

## Data Storage

**Databases:**
- **GeoPackage** (via `mil.nga.geopackage:geopackage:6.6.5`)
  - OGC GeoPackage Encoding Standard support
  - File-based geospatial database format
  - Used in: `src/main/kotlin/geo/GeoPackage.kt`
  - Features: Spatial indexing, multiple feature tables

**File Storage:**
- Local filesystem only
- Assets loaded from `data/` directory relative to working directory
- Images: PNG, JPG formats supported
- Fonts: OTF format supported
- GeoPackage: .gpkg files
- GeoJSON: .json files
- Video: FFmpeg-based (when enabled)

**Caching:**
- None - No distributed caching layer
- In-memory spatial indexing available (`src/main/kotlin/geo/SpatialIndex.kt`)

## Authentication & Identity

**Auth Provider:**
- Not applicable - Desktop application with no user authentication

## Monitoring & Observability

**Error Tracking:**
- None - No external error tracking service

**Logs:**
- SLF4J with slf4j-simple backend (default)
- Optional Log4j2 backend (FULL logging mode)
- Configured in `build.gradle.kts`:
  ```kotlin
  runtimeOnly(libs.bundles.logging.simple)  // Default
  ```

## CI/CD & Deployment

**Hosting:**
- None - Distributes as downloadable executables via GitHub Releases

**CI Pipeline:**
- GitHub Actions (`.github/workflows/`)
  - `build-on-commit.yaml` - Builds on push to master/next-version branches
  - `publish-binaries.yaml` - Publishes releases on version tags (v1.*, v1.*.*)

**Release Process:**
1. Tag commit with version (e.g., `v1.2.0`)
2. Push tag to origin
3. Matrix build runs on ubuntu, windows, macos
4. jpackage creates platform-specific executables
5. Release created with zipped artifacts via `ncipollo/release-action@v1.14.0`

## Environment Configuration

**Required env vars:**
- None - Application runs without environment configuration

**Optional build properties:**
- `openrndr.application` - Override main class to run
- `targetPlatform` - Cross-compile for different OS (not currently used)

**Secrets location:**
- GitHub Actions: `GITHUB_TOKEN` (automatic, used for releases)
- No other secrets required

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

## Native Libraries

**OpenGL:**
- `openrndr-gl3-natives-{platform}` - Platform-specific OpenGL bindings

**Audio:**
- `openrndr-openal-natives-{platform}` - Platform-specific OpenAL bindings

**Video:**
- `openrndr-ffmpeg-natives-{platform}` - FFmpeg native libraries
- Available but may be excluded on ARM architectures

## Coordinate Reference Systems

**CRS Transformation:**
- **Proj4j** (`org.locationtech.proj4j:proj4j:1.4.1`)
  - Java port of PROJ.4 library
  - EPSG database via `proj4j-epsg`
  - Used for coordinate reference system transformations
  - Implementation: `src/main/kotlin/geo/projection/CRSTransformer.kt`

**Supported CRS:**
- WGS84 (EPSG:4326) - GPS standard
- Web Mercator (EPSG:3857) - Web map standard
- British National Grid (EPSG:27700) - UK Ordnance Survey
- Custom CRS via EPSG codes

---

*Integration audit: 2026-03-02*
