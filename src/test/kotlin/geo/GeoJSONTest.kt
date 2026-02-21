package geo

import org.junit.Test
import org.junit.Assert.*

class GeoJSONTest {

    @Test
    fun `should parse Point feature from GeoJSON`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [10.0, 20.0]
                        },
                        "properties": {
                            "name": "Test Point"
                        }
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is Point)
        val point = features[0].geometry as Point
        assertEquals(10.0, point.x, 0.001)
        assertEquals(20.0, point.y, 0.001)
        assertEquals("Test Point", features[0].stringProperty("name"))
    }

    @Test
    fun `should parse LineString feature from GeoJSON`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [[0.0, 0.0], [10.0, 10.0], [20.0, 0.0]]
                        },
                        "properties": {}
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is LineString)
        val lineString = features[0].geometry as LineString
        assertEquals(3, lineString.points.size)
    }

    @Test
    fun `should parse Polygon feature from GeoJSON`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [
                                [[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]]
                            ]
                        },
                        "properties": {}
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is Polygon)
        val polygon = features[0].geometry as Polygon
        assertEquals(5, polygon.exterior.size)
    }

    @Test
    fun `should parse multiple features from GeoJSON`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [0.0, 0.0]
                        },
                        "properties": {"id": 1}
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [10.0, 10.0]
                        },
                        "properties": {"id": 2}
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(2, features.size)
        assertEquals(1, features[0].intProperty("id"))
        assertEquals(2, features[1].intProperty("id"))
    }

    @Test
    fun `should skip malformed features`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [0.0, 0.0]
                        },
                        "properties": {}
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Unknown",
                            "coordinates": [0.0, 0.0]
                        },
                        "properties": {}
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(1, features.size)
    }

    @Test
    fun `should parse single Feature`() {
        val geoJson = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [5.0, 15.0]
                },
                "properties": {"type": "single"}
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val features = source.listFeatures()

        assertEquals(1, features.size)
        assertTrue(features[0].geometry is Point)
    }

    @Test
    fun `convenience features should return same features as load`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [10.0, 20.0]
                        },
                        "properties": {"name": "Point 1"}
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [30.0, 40.0]
                        },
                        "properties": {"name": "Point 2"}
                    }
                ]
            }
        """.trimIndent()

        val source = GeoJSON.loadString(geoJson)
        val sourceFeatures = source.listFeatures()
        
        val convenienceFeatures = GeoJSON.featuresString(geoJson).toList()

        assertEquals(sourceFeatures.size, convenienceFeatures.size)
        assertEquals(2, convenienceFeatures.size)
        assertEquals("Point 1", convenienceFeatures[0].stringProperty("name"))
        assertEquals("Point 2", convenienceFeatures[1].stringProperty("name"))
    }

    @Test
    fun `convenience featuresString should return empty sequence for empty collection`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": []
            }
        """.trimIndent()

        val features = GeoJSON.featuresString(geoJson).toList()

        assertTrue(features.isEmpty())
    }

    @Test
    fun `convenience featuresString should parse multiple geometry types`() {
        val geoJson = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [0.0, 0.0]
                        },
                        "properties": {"type": "point"}
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [[0.0, 0.0], [10.0, 10.0]]
                        },
                        "properties": {"type": "line"}
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Polygon",
                            "coordinates": [[[0.0, 0.0], [10.0, 0.0], [10.0, 10.0], [0.0, 10.0], [0.0, 0.0]]]
                        },
                        "properties": {"type": "polygon"}
                    }
                ]
            }
        """.trimIndent()

        val features = GeoJSON.featuresString(geoJson).toList()

        assertEquals(3, features.size)
        assertTrue(features[0].geometry is Point)
        assertTrue(features[1].geometry is LineString)
        assertTrue(features[2].geometry is Polygon)
        assertEquals("point", features[0].stringProperty("type"))
        assertEquals("line", features[1].stringProperty("type"))
        assertEquals("polygon", features[2].stringProperty("type"))
    }
}
