package geo.internal.batch

import geo.projection.GeoProjection
import org.openrndr.math.Vector2

/**
 * Batch transformation utilities for coordinate arrays.
 * 
 * Per RESEARCH.md Pattern 2: Inline functions to eliminate lambda overhead.
 * All functions use indexed loops instead of for-each or map operations.
 */

/**
 * Generic batch transformer that applies a transformation function to all coordinates.
 * 
 * Uses inline to eliminate lambda allocation overhead at call sites.
 * Uses indexed loops for optimal performance.
 * 
 * @param x Input x coordinates
 * @param y Input y coordinates  
 * @param outX Output x coordinates (pre-allocated)
 * @param outY Output y coordinates (pre-allocated)
 * @param transform Function that takes (x, y) and returns Pair(transformedX, transformedY)
 */
internal inline fun batchTransform(
    x: DoubleArray,
    y: DoubleArray,
    outX: DoubleArray,
    outY: DoubleArray,
    transform: (Double, Double) -> Pair<Double, Double>
) {
    require(x.size == y.size && x.size == outX.size && x.size == outY.size) {
        "All arrays must have the same size: x=${x.size}, y=${y.size}, outX=${outX.size}, outY=${outY.size}"
    }
    
    for (i in x.indices) {
        val (tx, ty) = transform(x[i], y[i])
        outX[i] = tx
        outY[i] = ty
    }
}

/**
 * Projects coordinate arrays to screen space using a GeoProjection.
 * Fills the provided output array with projected Vector2 points.
 * 
 * @param x Input x coordinates
 * @param y Input y coordinates
 * @param projection The projection to use for coordinate transformation
 * @param output Pre-allocated output array to fill with projected coordinates
 * @return The output array (for chaining)
 */
internal fun batchProject(
    x: DoubleArray,
    y: DoubleArray,
    projection: GeoProjection,
    output: Array<Vector2>
): Array<Vector2> {
    require(x.size == y.size) {
        "Input arrays must have the same size: x=${x.size}, y=${y.size}"
    }
    require(x.size <= output.size) {
        "Output array must be at least as large as input: input=${x.size}, output=${output.size}"
    }
    
    for (i in x.indices) {
        output[i] = projection.project(Vector2(x[i], y[i]))
    }
    
    return output
}

/**
 * Projects coordinate arrays to screen space, writing results to output arrays.
 * 
 * This variant writes to DoubleArray outputs instead of Array<Vector2>,
 * useful for intermediate transformations or when further batch processing is needed.
 * 
 * @param x Input x coordinates
 * @param y Input y coordinates
 * @param projection The projection to use for coordinate transformation
 * @param outX Output x coordinates (screen space)
 * @param outY Output y coordinates (screen space)
 */
internal fun batchProjectToArrays(
    x: DoubleArray,
    y: DoubleArray,
    projection: GeoProjection,
    outX: DoubleArray,
    outY: DoubleArray
) {
    require(x.size == y.size && x.size == outX.size && x.size == outY.size) {
        "All arrays must have the same size: x=${x.size}, y=${y.size}, outX=${outX.size}, outY=${outY.size}"
    }
    
    for (i in x.indices) {
        val projected = projection.project(Vector2(x[i], y[i]))
        outX[i] = projected.x
        outY[i] = projected.y
    }
}

/**
 * Projects a CoordinateBatch to screen space, returning a new Array<Vector2>.
 * 
 * This is a convenience wrapper around batchProject for use with CoordinateBatch.
 * 
 * @param batch The coordinate batch to project
 * @param projection The projection to use
 * @param output Pre-allocated output array (optional, will be created if null)
 * @return Array of projected Vector2 coordinates
 */
internal fun batchProjectBatch(
    batch: CoordinateBatch,
    projection: GeoProjection,
    output: Array<Vector2>? = null
): Array<Vector2> {
    val result = output ?: Array(batch.size) { Vector2.ZERO }
    return batchProject(batch.x, batch.y, projection, result)
}
