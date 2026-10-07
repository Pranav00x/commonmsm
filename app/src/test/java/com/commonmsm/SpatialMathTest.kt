package com.commonmsm

import com.commonmsm.engine.SpatialMath
import org.junit.Assert.*
import org.junit.Test

class SpatialMathTest {

    @Test
    fun testHaversineDistanceBetweenLisbonPoints() {
        // Lisbon Praça do Comércio to Castelo de São Jorge (~1.0 km)
        val lat1 = 38.7075
        val lon1 = -9.1364
        val lat2 = 38.7139
        val lon2 = -9.1335

        val distance = SpatialMath.haversineDistanceMeters(lat1, lon1, lat2, lon2)
        assertTrue("Distance should be between 700m and 1100m, was $distance", distance in 700.0..1100.0)
    }

    @Test
    fun testSamePointDistanceIsZero() {
        val distance = SpatialMath.haversineDistanceMeters(38.7075, -9.1364, 38.7075, -9.1364)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testBearingAndCardinalDirections() {
        // Due North
        assertEquals("N", SpatialMath.compassDirection(0.0))
        assertEquals("N", SpatialMath.compassDirection(355.0))
        assertEquals("N", SpatialMath.compassDirection(5.0))

        // Due East
        assertEquals("E", SpatialMath.compassDirection(90.0))

        // Due South
        assertEquals("S", SpatialMath.compassDirection(180.0))

        // Due West
        assertEquals("W", SpatialMath.compassDirection(270.0))

        // North-West
        assertEquals("NW", SpatialMath.compassDirection(315.0))
    }

    @Test
    fun testFormatDistanceNotation() {
        assertEquals("250 M", SpatialMath.formatDistance(250.0))
        assertEquals("999 M", SpatialMath.formatDistance(999.0))
        assertEquals("1.0 KM", SpatialMath.formatDistance(1000.0))
        assertEquals("2.5 KM", SpatialMath.formatDistance(2500.0))
    }

    @Test
    fun testFormatDistanceAndBearing() {
        val lat1 = 38.7075
        val lon1 = -9.1364
        val lat2 = 38.7139
        val lon2 = -9.1335
        val formatted = SpatialMath.formatDistanceAndBearing(lat1, lon1, lat2, lon2)
        assertNotNull(formatted)
        assertTrue(formatted.contains("M") || formatted.contains("KM"))
    }
}
