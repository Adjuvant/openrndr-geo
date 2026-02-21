package geo.exception

/**
 * Thrown when coordinate transformation exceeds projection limits.
 * Example: Mercator projection at latitude ±90° (poles).
 */
class ProjectionOverflowException(message: String) : Exception(message)

/**
 * Thrown when coordinates are outside valid transformation range.
 * Provides rich feedback about transformation limits.
 * Example: BNG coordinates outside UK grid area for OSTN15.
 */
class AccuracyWarningException(message: String) : Exception(message)

/**
 * Thrown when coordinates are unrepresentable in target projection.
 * Example: Exact latitude ±90° for Mercator (cannot be represented).
 */
class ProjectionUnrepresentableException(message: String) : Exception(message)

/**
 * Thrown when coordinate system transformation is invalid.
 * Example: Using lat/lng projection with BNG coordinates.
 */
class CoordinateSystemException(message: String) : Exception(message)
