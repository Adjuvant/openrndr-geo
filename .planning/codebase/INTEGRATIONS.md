# External Integrations

**Analysis Date:** 2026-02-21

## APIs & External Services

**None detected** - This is a standalone creative coding application with no external API integrations.

The following are available as optional dependencies but are currently disabled:

- **OSC (Open Sound Control):** `orx-osc` - Available but not enabled
- **MIDI:** `orx-midi` - Available but not enabled
- **Runway ML:** `orx-runway` - Available but not enabled
- **Chataigne:** `orx-chataigne` - Available but not enabled

## Data Storage

**Databases:**
- None - No database connectivity

**File Storage:**
- Local filesystem only
- Assets loaded from `data/` directory relative to working directory
- Images: PNG, JPG formats supported
- Fonts: OTF format supported
- Video: FFmpeg-based (when enabled)

**Caching:**
- None - No caching layer implemented

## Authentication & Identity

**Auth Provider:**
- Not applicable - Desktop application with no user authentication

## Monitoring & Observability

**Error Tracking:**
- None - No external error tracking service

**Logs:**
- SLF4J with Log4j2 backend (FULL logging mode)
- Configured in `build.gradle.kts`:
  ```kotlin
  when (applicationLogging) {
      Logging.NONE -> runtimeOnly(libs.slf4j.nop)
      Logging.SIMPLE -> runtimeOnly(libs.slf4j.simple)
      Logging.FULL -> runtimeOnly(libs.log4j.slf4j2)
  }
  ```

## CI/CD & Deployment

**Hosting:**
- None - Distributes as downloadable executables via GitHub Releases

**CI Pipeline:**
- GitHub Actions (`.github/workflows/`)
  - `build-on-commit.yaml` - Builds on push to master/next-version
  - `publish-binaries.yaml` - Publishes releases on version tags

**Release Process:**
1. Tag commit with version (e.g., `v1.0.0`)
2. Push tag to origin
3. Matrix build runs on ubuntu, windows, macos
4. jpackage creates platform-specific executables
5. Release created with zipped artifacts

## Environment Configuration

**Required env vars:**
- None - Application runs without environment configuration

**Optional build properties:**
- `openrndr.application` - Override main class to run
- `targetPlatform` - Cross-compile for different OS (windows, macos, linux-x64, linux-arm64)

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

**Video (optional):**
- `openrndr-ffmpeg-natives-{platform}` - FFmpeg native libraries
- Disabled on ARM architectures

---

*Integration audit: 2026-02-21*
