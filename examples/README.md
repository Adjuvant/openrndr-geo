# OPENRNDR Geo Examples

Welcome to the OPENRNDR Geo Examples — a collection of runnable examples demonstrating how to work with geographic data using the openrndr-geo library.

## Overview

The openrndr-geo library provides tools for loading, transforming, and rendering geographic data in OPENRNDR applications. It decouples data operations from rendering, allowing for rapid prototyping of visual ideas.

## Categories

This examples directory is organized into the following categories:

- **[core/](core/)**: Data loading and inspection — Learn how to load GeoJSON and GPKG files, inspect feature properties, and understand data structures.

- **[render/](render/)**: Rendering geographic features — Explore techniques for drawing points, lines, and polygons with proper styling.

- **[proj/](proj/)**: Projections and coordinate systems — Understand coordinate transformations, projections, and working with different CRS.

- **[anim/](anim/)**: Animation patterns — Create dynamic visualizations with time-based updates and smooth transitions.

- **[layer/](layer/)**: Layer composition and overlays — Build complex maps by combining multiple layers with different styles.

## Running Examples

Each example can be run using Gradle:

```bash
./gradlew run -Popenrndr.application=examples.category.FileNameKt
```

For example, to run an example in the core category:

```bash
./gradlew run -Popenrndr.application=examples.core.ExampleNameKt
```

## Sample Data

Sample geographic data files are located in `data/geo/`:

- `sample.geojson` — Simple example features
- `coastline.geojson` — Coastline data
- `ocean.geojson` — Ocean polygons
- `rivers_lakes.geojson` — Rivers and lakes
- `populated_places.geojson` — City locations
- `catchment-topo.geojson` — Topographic catchment areas
- `UK-terr50-land_water_boundary.gpkg` — UK terrain data
- `ness-vectors.gpkg` — Vector field data

## Getting Started

1. Explore the category folders to find examples relevant to your needs
2. Read the category README for an overview of key concepts
3. Run an example to see it in action
4. Modify the code to experiment with your own data
