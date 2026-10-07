package com.commonmsm.engine

import kotlin.math.*

object SpatialMath {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the great-circle distance between two GPS coordinates using the Haversine formula.
     */
    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2) + cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Computes the initial bearing from point 1 to point 2 in degrees [0, 360).
     */
    fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)
        val dLon = Math.toRadians(lon2 - lon1)

        val y = sin(dLon) * cos(rLat2)
        val x = cos(rLat1) * sin(rLat2) - sin(rLat1) * cos(rLat2) * cos(dLon)
        val bearingRadians = atan2(y, x)
        return (Math.toDegrees(bearingRadians) + 360.0) % 360.0
    }

    /**
     * Converts a bearing in degrees to an 8-point compass cardinal direction.
     */
    fun compassDirection(bearingDegrees: Double): String {
        val normalized = (bearingDegrees % 360.0 + 360.0) % 360.0
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = ((normalized + 22.5) / 45.0).toInt() % 8
        return directions[index]
    }

    /**
     * Formats distance in meters into human-readable compact neo-brutalist notation.
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000.0) {
            "${meters.roundToInt()} M"
        } else {
            String.format("%.1f KM", meters / 1000.0)
        }
    }

    /**
     * Computes compact distance and compass direction string (e.g. "320 M NW" or "2.4 KM E").
     */
    fun formatDistanceAndBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val dist = haversineDistanceMeters(lat1, lon1, lat2, lon2)
        val bearing = calculateBearing(lat1, lon1, lat2, lon2)
        val dir = compassDirection(bearing)
        return "${formatDistance(dist)} $dir"
    }
}
