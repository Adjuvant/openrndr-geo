package geo.examples

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import geo.Point
import geo.animation.interpolators.haversineInterpolate
import geo.animation.interpolators.Position
import geo.render.Shape
import geo.render.drawPoint
import geo.render.Style
import geo.projection.ProjectionFactory
import geo.projection.toScreen

/**
 * Haversine Interpolation Visualizer
 *
 * This example visualizes the Haversine interpolation for great-circle paths:
 * 1. Shows start (London) and end (Tokyo) points
 * 2. Draws the curved great-circle path
 * 3. Shows intermediate interpolated points with debug info
 *
 * To run this example:
 * ./gradlew run --main=geo.examples.HaversineDemo
 *
 * Debug output appears on-screen showing:
 * - Current animation progress (t)
 * - Coordinates of interpolated point
 * - Distance from start point
 */
fun main() = application {
    configure {
        width = 1200
        height = 800
    }

    program {
        // Define geographic positions
        val london = Position(51.5074, -0.1278)  // lat, lng
        val tokyo = Position(35.6762, 139.6503)

        // Create world map projection
        // Using zoomLevel: 0 = whole world, higher = more zoomed
        // scale = 256 * 2^zoom, so zoomLevel 2 gives scale ~1024
        val projection = ProjectionFactory.mercator(
            width = width.toDouble(),
            height = height.toDouble(),
            center = Vector2(42.0, 60.0),
            zoomLevel = 1.0
        )

        // Calculate world map bounds to center viewport
        val londonScreen = Point(london.longitude, london.latitude).toScreen(projection)
        val tokyoScreen = Point(tokyo.longitude, tokyo.latitude).toScreen(projection)

        extend {
            drawer.clear(ColorRGBa.fromHex("#1a1a2e"))

            // Animation progress (0.0 to 1.0 oscillating)
            val t = (Math.sin(seconds * 0.5) + 1.0) / 2.0

            // Calculate interpolated position
            val currentPos = haversineInterpolate(london, tokyo, t)
            val currentScreen = Point(currentPos.longitude, currentPos.latitude).toScreen(projection)

            val londonScreen = Point(london.longitude, london.latitude).toScreen(projection)
            val tokyoScreen = Point(tokyo.longitude, tokyo.latitude).toScreen(projection)

            // Draw great-circle path (sampled with 100 points)
            val pathPoints = (0..100).map { i ->
                val pathT = i / 100.0
                val pathPos = haversineInterpolate(london, tokyo, pathT)
                Point(pathPos.longitude, pathPos.latitude).toScreen(projection)
            }

            drawer.stroke = ColorRGBa.fromHex("#00d4ff").opacify(0.6)
            drawer.strokeWeight = 3.0
            drawer.lineStrip(pathPoints)

            // Draw city points with labels
            drawCityPoint(drawer, londonScreen, "London", ColorRGBa.GREEN)
            drawCityPoint(drawer, tokyoScreen, "Tokyo", ColorRGBa.MAGENTA)

            // Draw animated point
            drawer.fill = ColorRGBa.YELLOW
            drawer.stroke = ColorRGBa.WHITE
            drawer.strokeWeight = 3.0
            drawer.circle(currentScreen.x, currentScreen.y, 10.0)

            // Debug information panel
            val debugPanelX = 20.0
            var debugPanelY = 20.0
            val lineHeight = 24.0

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null

            // Debug text
            drawer.text("HAVERSINE INTERPOLATION DEBUG", debugPanelX, debugPanelY)
            debugPanelY += lineHeight

            drawer.text("Progress (t): ${String.format("%.3f", t)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            drawer.text("Start Point (London):", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lat: ${String.format("%.4f", london.latitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lng: ${String.format("%.4f", london.longitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            drawer.text("End Point (Tokyo):", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lat: ${String.format("%.4f", tokyo.latitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lng: ${String.format("%.4f", tokyo.longitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            drawer.text("Current Interpolated Point:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lat: ${String.format("%.4f", currentPos.latitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("  Lng: ${String.format("%.4f", currentPos.longitude)}", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            // Calculate approximate distance (Haversine formula output)
            val distanceKm = calculateDistance(london, tokyo) / 1000.0
            val currentDistance = calculateDistance(london, currentPos) / 1000.0

            drawer.text("Total Distance: ${String.format("%.1f", distanceKm)} km", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("Current Distance: ${String.format("%.1f", currentDistance)} km (${String.format("%.1f", (currentDistance / distanceKm) * 100.0)}%)\n", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 1.5

            drawer.text("Visual Elements:", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Cyan line: Great-circle path (curved)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Green circle: Start point (London)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Magenta circle: End point (Tokyo)", debugPanelX, debugPanelY)
            debugPanelY += lineHeight
            drawer.text("• Yellow circle: Current interpolated point", debugPanelX, debugPanelY)
            debugPanelY += lineHeight * 2.0

            drawer.text("Press ESC to exit", debugPanelX, debugPanelY)
        }
    }
}

fun calculateDistance(from: Position, to: Position): Double {
    val lat1 = Math.toRadians(from.latitude)
    val lon1 = Math.toRadians(from.longitude)
    val lat2 = Math.toRadians(to.latitude)
    val lon2 = Math.toRadians(to.longitude)

    val dLat = lat2 - lat1
    val dLon = lon2 - lon1
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    val c = 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))

    return 6371000.0 * c  // Earth radius in meters
}

fun drawCityPoint(drawer: org.openrndr.draw.Drawer, screen: org.openrndr.math.Vector2, name: String, color: ColorRGBa) {
    drawer.fill = color
    drawer.stroke = ColorRGBa.WHITE
    drawer.strokeWeight = 2.0
    drawer.circle(screen.x, screen.y, 8.0)

    drawer.fill = ColorRGBa.WHITE
    drawer.stroke = null
    drawer.text(name, screen.x + 12.0, screen.y + 4.0)
}