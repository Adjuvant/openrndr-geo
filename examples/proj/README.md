# Projection Examples

This category covers coordinate reference systems, projections, and transforming data between different coordinate systems.

## Examples

| # | Example | Description |
|---|---------|-------------|
| 01 | [01-mercator.kt](01-mercator.kt) | World-scale Mercator projection using ProjectionFactory.fitWorldMercator() |
| 02 | [02-fit-bounds.kt](02-fit-bounds.kt) | Automatic projection fitting to data bounds with padding |
| 03 | [03-crs-transform.kt](03-crs-transform.kt) | CRS transformation between EPSG codes using Proj4J |

## Key Concepts

- **ProjectionFactory.fitWorldMercator()**: Creates a Mercator projection fitted to show the entire world. At zoom=0, the world bounds fit exactly in the viewport.

- **ProjectionFactory.fitBounds()**: Automatically calculates zoom level and center to fit geographic data to the viewport. Supports padding parameter for margins.

- **CRS Transformation**: Using CRSTransformer to convert coordinates between different Coordinate Reference Systems (e.g., WGS84 to British National Grid).

- **Projection Types**: Equirectangular (simple lat/lng mapping) vs Mercator (standard web map projection with area distortion at poles).

## Running Examples

```bash
# Run a specific example
./gradlew run -Popenrndr.application=examples.proj.Mercator

# Or use the main class directly
./gradlew run --main=examples.proj.Mercator
```

## Package

All examples use package `examples.proj`.
