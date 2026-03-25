package uat

import geo.core.*
import geo.layer.*
import geo.projection.GeoProjection
import geo.projection.ProjectionFactory
import geo.projection.ProjectionType
import geo.render.*
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont

/**
 * UAT: Graticule & Layer Features (Phase 20)
 *
 * Visual verification script for:
 * 1. LineString-based graticule with latLines/lngLines
 * 2. Adaptive spacing (1°/10°/30°/90°)
 * 3. Antimeridian line splitting
 * 4. Label generation with N/S/E/W formatting
 * 5. Auto-thinning (20px minimum spacing)
 *
 * Run with: ./gradlew runUAT
 */
fun main() = application {
    configure {
        width = 1400
        height = 900
        title = "UAT - Graticule & Layer Features (Phase 20)"
    }

    program {
        val font = loadFont("data/fonts/default.otf", 14.0)
        val coastlineSource = geoSource("data/geo/coastline.geojson")

        // Pacific bounds: 120°E to 120°W (crossing the antimeridian)
        // TODO: fitBounds doesn't properly handle minX > maxX (antimeridian crossing)
        // This cell is here for when a future phase fixes antimeridian projection support
        val pacificBounds = Bounds(120.0, -60.0, -120.0, 60.0)
        val globalBounds = Bounds(-180.0, -60.0, 180.0, 60.0)
        val regionalBounds = Bounds(-10.0, 40.0, 20.0, 60.0)

        val latFormatTests = listOf(
            45.0 to "45°N",
            -45.0 to "45°S",
            0.0 to "0°",
            90.0 to "90°N",
            -90.0 to "90°S"
        )
        val lonFormatTests = listOf(
            120.0 to "120°E",
            -120.0 to "120°W",
            0.0 to "0°",
            180.0 to "180°"
        )

        val cellWidth = (width - 60) / 2.0
        val cellHeight = (height - 120) / 2.5
        val margin = 20.0
        val cell1X = margin
        val cell1Y = 50.0
        val cell2X = margin + cellWidth + 20
        val cell2Y = 50.0
        val cell3X = margin
        val cell3Y = 50.0 + cellHeight + 30
        val cell4X = margin + cellWidth + 20
        val cell4Y = 50.0 + cellHeight + 30

        val globalProjection = ProjectionFactory.fitBounds(globalBounds, cellWidth, cellHeight, padding = 10.0, projection = ProjectionType.MERCATOR)
        val graticule90 = generateGraticuleLayer(globalBounds, 30.0)

        val regionalProjection = ProjectionFactory.fitBounds(regionalBounds, cellWidth, cellHeight, padding = 10.0, projection = ProjectionType.MERCATOR)
        val graticule10 = generateGraticuleLayer(regionalBounds, 10.0)

        val pacificProjection = ProjectionFactory.fitBounds(pacificBounds, cellWidth, cellHeight, padding = 10.0, projection = ProjectionType.EQUIRECTANGULAR)
        val graticule30 = generateGraticuleLayer(pacificBounds, 30.0)

        val labelBounds = Bounds(-80.0, 20.0, 40.0, 60.0)
        val labelProjection = ProjectionFactory.fitBounds(labelBounds, cellWidth, cellHeight, padding = 10.0, projection = ProjectionType.MERCATOR)
        val graticuleLabels = generateGraticuleLayer(labelBounds, 10.0)
        val labels = generateGraticuleLabels(labelBounds, labelProjection, 10.0, 20.0)

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.fontMap = font
            drawer.fill = ColorRGBa.BLACK
            drawer.text("Phase 20: Graticule & Layer Features - UAT Verification", 20.0, 25.0)

            // Cell 1: Global view with 90° spacing
            drawer.fill = ColorRGBa.GRAY
            drawer.text("1. Global View (30° spacing)", cell1X, cell1Y)
            drawer.translate(cell1X, cell1Y + 10)
            DrawCellOne(coastlineSource, globalProjection, graticule90)
            drawer.translate(-cell1X, -(cell1Y + 10))

            // Cell 2: Regional view with 10° spacing
            drawer.fill = ColorRGBa.GRAY
            drawer.text("2. Regional View (10° spacing)", cell2X, cell2Y)
            drawer.translate(cell2X, cell2Y + 10)
            DrawCellTwo(coastlineSource, regionalProjection, graticule10)
            drawer.translate(-cell2X, -(cell2Y + 10))

            // Cell 3: Pacific view with antimeridian handling
            drawer.fill = ColorRGBa.GRAY
            drawer.text("3. Pacific/Antimeridian (30° spacing)", cell3X, cell3Y)
            drawer.translate(cell3X, cell3Y + 10)
            DrawCellThree(coastlineSource, pacificProjection, graticule30)
            drawer.translate(-cell3X, -(cell3Y + 10))

            // Cell 4: Labels with auto-thinning
            drawer.fill = ColorRGBa.GRAY
            drawer.text("4. Labels with Auto-Thinning (20px min)", cell4X, cell4Y)
            drawer.translate(cell4X, cell4Y + 10)
            DrawCellFour(coastlineSource, labelProjection, graticuleLabels)

            DrawLabelsLegend(labels, cell4X, cell4Y, latFormatTests, lonFormatTests)
        }
    }
}

private fun Program.DrawLabelsLegend(
    labels: GraticuleLabels,
    cell4X: Double,
    cell4Y: Double,
    latFormatTests: List<Pair<Double, String>>,
    lonFormatTests: List<Pair<Double, String>>
) {
    // Draw labels
    drawer.fill = ColorRGBa(0.8, 0.2, 0.2)
    for (latLabel in labels.latitudeLabels) {
        drawer.circle(latLabel.projectedX, latLabel.projectedY, 3.0)
        drawer.text(latLabel.text, latLabel.projectedX + 5, latLabel.projectedY + 4)
    }
    drawer.fill = ColorRGBa(0.2, 0.2, 0.8)
    for (lngLabel in labels.longitudeLabels) {
        drawer.circle(lngLabel.projectedX, lngLabel.projectedY, 3.0)
        drawer.text(lngLabel.text, lngLabel.projectedX + 5, lngLabel.projectedY + 4)
    }

    drawer.translate(-cell4X, -(cell4Y + 10))

    // Bottom: Format verification
    val bottomY = height - 140.0
    drawer.fill = ColorRGBa.GRAY
    drawer.text("5. Format Verification:", 20.0, bottomY)

    var testX = 20.0
    var testY = bottomY + 20.0
    drawer.fill = ColorRGBa.BLACK
    drawer.text("Latitude formats:", testX, testY)
    testX += 120.0
    for ((value, expected) in latFormatTests) {
        val actual = formatLatitude(value)
        val status = if (actual == expected) "PASS" else "FAIL"
        drawer.text("$value -> $actual [$status]", testX, testY)
        testX += 160.0
    }

    testX = 20.0
    testY += 25.0
    drawer.text("Longitude formats:", testX, testY)
    testX += 130.0
    for ((value, expected) in lonFormatTests) {
        val actual = formatLongitude(value)
        val status = if (actual == expected) "PASS" else "FAIL"
        drawer.text("$value -> $actual [$status]", testX, testY)
        testX += 160.0
    }

    // Data structure verification
    testX = 20.0
    testY += 30.0
    drawer.text("6. Data Structures:", testX, testY)
    testX += 150.0
    drawer.text("GraticuleLines: latLines/lngLines GeoSources", testX, testY)
    testX += 350.0
    drawer.text("LabelPosition: text/projectedX/projectedY", testX, testY)
    testX += 350.0
    drawer.text("GraticuleLabels: lat/lng label lists", testX, testY)

    // Legend
    testY += 35.0
    drawer.fill = ColorRGBa(0.3, 0.5, 0.8)
    drawer.circle(30.0, testY - 4, 4.0)
    drawer.fill = ColorRGBa.BLACK
    drawer.text("Latitude lines", 45.0, testY)

    drawer.fill = ColorRGBa(0.2, 0.6, 0.4)
    drawer.circle(180.0, testY - 4, 4.0)
    drawer.fill = ColorRGBa.BLACK
    drawer.text("Longitude lines", 195.0, testY)

    drawer.fill = ColorRGBa(0.8, 0.2, 0.2)
    drawer.circle(340.0, testY - 4, 4.0)
    drawer.fill = ColorRGBa.BLACK
    drawer.text("Latitude labels", 355.0, testY)

    drawer.fill = ColorRGBa(0.2, 0.2, 0.8)
    drawer.circle(500.0, testY - 4, 4.0)
    drawer.fill = ColorRGBa.BLACK
    drawer.text("Longitude labels", 515.0, testY)
}

private fun Program.DrawCellFour(
    coastlineSource: GeoSource,
    labelProjection: GeoProjection,
    graticuleLabels: GeoLayer
) {
    drawer.geo(coastlineSource) {
        projection = labelProjection
        stroke = ColorRGBa(0.7, 0.7, 0.7)
        strokeWeight = 0.5
    }
    graticuleLabels.latLines?.let { latLines ->
        drawer.geo(latLines) {
            projection = labelProjection
            stroke = ColorRGBa(0.6, 0.3, 0.3)
            strokeWeight = 0.5
        }
    }
    graticuleLabels.lngLines?.let { lngLines ->
        drawer.geo(lngLines) {
            projection = labelProjection
            stroke = ColorRGBa(0.6, 0.3, 0.3)
            strokeWeight = 0.5
        }
    }
}

private fun Program.DrawCellThree(
    coastlineSource: GeoSource,
    pacificProjection: GeoProjection,
    graticule30: GeoLayer
) {
    drawer.geo(coastlineSource) {
        projection = pacificProjection
        fill = ColorRGBa(0.85, 0.85, 0.85)
        stroke = ColorRGBa(0.85, 0.5, 0.5)
        strokeWeight = 0.5
    }
    graticule30.latLines?.let { latLines ->
        drawer.geo(latLines) {
            projection = pacificProjection
            stroke = ColorRGBa(0.2, 0.6, 0.4)
            strokeWeight = 0.5
        }
    }
    graticule30.lngLines?.let { lngLines ->
        drawer.geo(lngLines) {
            projection = pacificProjection
            stroke = ColorRGBa(0.2, 0.6, 0.4)
            strokeWeight = 0.5
        }
    }
}

private fun Program.DrawCellTwo(
    coastlineSource: GeoSource,
    regionalProjection: GeoProjection,
    graticule10: GeoLayer
) {
    drawer.geo(coastlineSource) {
        projection = regionalProjection
        stroke = ColorRGBa(0.7, 0.7, 0.7)
        strokeWeight = 0.5
    }
    graticule10.latLines?.let { latLines ->
        drawer.geo(latLines) {
            projection = regionalProjection
            stroke = ColorRGBa(0.2, 0.4, 0.7)
            strokeWeight = 0.5
        }
    }
    graticule10.lngLines?.let { lngLines ->
        drawer.geo(lngLines) {
            projection = regionalProjection
            stroke = ColorRGBa(0.2, 0.4, 0.7)
            strokeWeight = 0.5
        }
    }
}

private fun Program.DrawCellOne(
    coastlineSource: GeoSource,
    globalProjection: GeoProjection,
    graticule90: GeoLayer
) {


    drawer.geo(coastlineSource) {
        projection = globalProjection
        stroke = ColorRGBa(0.7, 0.7, 0.7)
        strokeWeight = 0.5
    }
    graticule90.latLines?.let { latLines ->
        drawer.geo(latLines) {
            projection = globalProjection
            stroke = ColorRGBa(0.3, 0.5, 0.8)
            strokeWeight = 0.3
        }
    }
    graticule90.lngLines?.let { lngLines ->
        drawer.geo(lngLines) {
            projection = globalProjection
            stroke = ColorRGBa(0.3, 0.5, 0.8)
            strokeWeight = 0.3
        }
    }
}
