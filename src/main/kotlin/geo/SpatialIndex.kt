package geo

private const val MAX_CAPACITY = 16

/**
 * A spatial index for efficient geographic feature queries.
 *
 * The Quadtree is a tree data structure where each node represents a bounding box
 * and contains either a list of features (if it's a leaf node) or four child nodes
 * (if it has been subdivided).
 *
 * Features are inserted into the quadtree and can be queried by bounding box,
 * returning only those features that intersect the query bounds.
 *
 * @param bounds The bounding box that this quadtree node covers
 * @param parent The parent quadtree node (null for root)
 */
class Quadtree(
    private val bounds: Bounds,
    private val parent: Quadtree? = null
) {
    private val features = mutableListOf<Feature>()
    private var children: Array<Quadtree>? = null

    /**
     * Inserts a feature into the quadtree.
     * The feature is inserted into this node if it's a leaf and has capacity,
     * or into the appropriate child node(s) if this node has been subdivided.
     *
     * @param feature The feature to insert
     * @return true if the feature was inserted, false if it's outside this node's bounds
     */
    fun insert(feature: Feature): Boolean {
        val bbox = feature.geometry.boundingBox
        if (!bounds.intersects(bbox)) return false

        if (children != null) {
            // Insert into appropriate child
            return children!!.any { it.insert(feature) }
        }

        if (features.size < MAX_CAPACITY) {
            features.add(feature)
            return true
        }

        // Split and redistribute
        subdivide()
        val toRedistribute = features.toList()
        features.clear()
        for (f in toRedistribute) {
            insert(f)
        }
        return insert(feature)
    }

    /**
     * Queries the quadtree for features that intersect the given bounds.
     *
     * @param queryBounds The bounding box to query
     * @return List of features whose bounding boxes intersect the query bounds
     */
    fun query(queryBounds: Bounds): List<Feature> {
        if (!bounds.intersects(queryBounds)) return emptyList()

        val results = mutableListOf<Feature>()

        if (children != null) {
            for (child in children!!) {
                results.addAll(child.query(queryBounds))
            }
        } else {
            for (feature in features) {
                if (feature.geometry.boundingBox.intersects(queryBounds)) {
                    results.add(feature)
                }
            }
        }

        return results
    }

    /**
     * Returns all features in this quadtree (and all subtrees).
     *
     * @return List of all features stored in the quadtree
     */
    fun allFeatures(): List<Feature> {
        if (children != null) {
            return children!!.flatMap { it.allFeatures() }
        }
        return features.toList()
    }

    /**
     * Subdivides this node into four children.
     * This happens when the node exceeds its capacity.
     */
    private fun subdivide() {
        if (children != null) return

        val halfWidth = (bounds.maxX - bounds.minX) / 2
        val halfHeight = (bounds.maxY - bounds.minY) / 2
        val midX = bounds.minX + halfWidth
        val midY = bounds.minY + halfHeight

        children = arrayOf(
            Quadtree(Bounds(bounds.minX, bounds.minY, midX, midY), this),
            Quadtree(Bounds(midX, bounds.minY, bounds.maxX, midY), this),
            Quadtree(Bounds(bounds.minX, midY, midX, bounds.maxY), this),
            Quadtree(Bounds(midX, midY, bounds.maxX, bounds.maxY), this)
        )
    }
}

/**
 * Filters a sequence of features to include only those that intersect the given bounds.
 *
 * @param bounds The bounding box to filter by
 * @return A sequence containing only features whose bounding boxes intersect the given bounds
 */
infix fun Sequence<Feature>.within(bounds: Bounds): Sequence<Feature> =
    filter { feature ->
        feature.geometry.boundingBox.intersects(bounds)
    }

/**
 * Checks if this feature is within (intersects) the given bounds.
 *
 * @param bounds The bounding box to test against
 * @return true if this feature's bounding box intersects the given bounds
 */
infix fun Feature.within(bounds: Bounds): Boolean =
    geometry.boundingBox.intersects(bounds)
