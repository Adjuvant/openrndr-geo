package geo.render

/**
 * Shape types for point rendering.
 *
 * Provides primitive shape variety for different visualization needs.
 * Users can configure shape per-style or per-feature.
 *
 * @property Circle Circular point marker (default)
 * @property Square Square point marker
 * @property Triangle Triangle point marker (equilateral)
 */
enum class Shape {
    Circle,
    Square,
    Triangle
}
