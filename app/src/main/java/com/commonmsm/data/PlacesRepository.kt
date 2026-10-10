package com.commonmsm.data

import android.database.Cursor
import com.commonmsm.data.models.PlaceEntity
import kotlin.math.*

class PlacesRepository {

    fun searchPlaces(
        city: String,
        isVeganOnly: Boolean = false,
        category: String? = null,
        limit: Int = 10
    ): List<PlaceEntity> {
        val db = DatabaseManager.getPlacesDatabase() ?: return emptyList()
        val safeCity = city.trim().take(64)
        val safeLimit = limit.coerceIn(1, 100)

        val selection = StringBuilder("LOWER(city) = LOWER(?)")
        val selectionArgs = mutableListOf(safeCity)

        if (isVeganOnly) {
            selection.append(" AND (is_vegan = 1 OR diet_tags LIKE '%vegan%')")
        }

        if (!category.isNullOrBlank()) {
            selection.append(" AND category = ?")
            selectionArgs.add(category.trim().take(32))
        }

        val cursor = db.query(
            "places",
            null,
            selection.toString(),
            selectionArgs.toTypedArray(),
            null,
            null,
            "fame_score DESC, is_vegan DESC",
            safeLimit.toString()
        )

        val places = cursor.use { extractPlaces(it) }
        if (places.isNotEmpty()) return places

        // Fallback 1: Broad search across city, name, cuisine, diet
        val fallbackPattern = "%$safeCity%"
        val fallbackCursor = db.query(
            "places",
            null,
            "city LIKE ? OR name LIKE ? OR cuisine LIKE ? OR diet_tags LIKE ?",
            arrayOf(fallbackPattern, fallbackPattern, fallbackPattern, fallbackPattern),
            null,
            null,
            "fame_score DESC, is_vegan DESC",
            safeLimit.toString()
        )
        val fallbackPlaces = fallbackCursor.use { extractPlaces(it) }
        if (fallbackPlaces.isNotEmpty()) return fallbackPlaces

        // Fallback 2: Global top-rated recommendations
        val globalCursor = db.query(
            "places",
            null,
            if (isVeganOnly) "is_vegan = 1" else null,
            null,
            null,
            null,
            "fame_score DESC, is_vegan DESC",
            safeLimit.toString()
        )
        return globalCursor.use { extractPlaces(it) }
    }

    fun searchNearCoordinates(
        lat: Double,
        lon: Double,
        radiusKm: Double = 5.0,
        isVeganOnly: Boolean = false,
        limit: Int = 10
    ): List<PlaceEntity> {
        val db = DatabaseManager.getPlacesDatabase() ?: return emptyList()

        val safeLat = lat.coerceIn(-90.0, 90.0)
        val safeLon = lon.coerceIn(-180.0, 180.0)
        val safeRadiusKm = radiusKm.coerceIn(0.1, 500.0)
        val safeLimit = limit.coerceIn(1, 100)

        val latDelta = safeRadiusKm / 111.0
        val lonDelta = safeRadiusKm / (111.0 * cos(Math.toRadians(safeLat)).coerceAtLeast(0.01))

        val selection = StringBuilder("latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ?")
        val selectionArgs = mutableListOf(
            (safeLat - latDelta).toString(),
            (safeLat + latDelta).toString(),
            (safeLon - lonDelta).toString(),
            (safeLon + lonDelta).toString()
        )

        if (isVeganOnly) {
            selection.append(" AND (is_vegan = 1 OR diet_tags LIKE '%vegan%')")
        }

        val cursor = db.query(
            "places",
            null,
            selection.toString(),
            selectionArgs.toTypedArray(),
            null,
            null,
            "fame_score DESC",
            (safeLimit * 3).toString()
        )

        val rawPlaces = cursor.use { extractPlaces(it) }

        return rawPlaces.mapNotNull { place ->
            val distMeters = haversineMeters(safeLat, safeLon, place.latitude, place.longitude)
            if (distMeters <= safeRadiusKm * 1000.0) {
                place.copy(distanceMeters = distMeters)
            } else null
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
         .take(safeLimit)
    }

    private fun extractPlaces(cursor: Cursor): List<PlaceEntity> {
        val list = mutableListOf<PlaceEntity>()
        val idCol = cursor.getColumnIndex("id")
        val nameCol = cursor.getColumnIndex("name")
        val cityCol = cursor.getColumnIndex("city")
        val countryCol = cursor.getColumnIndex("country")
        val latCol = cursor.getColumnIndex("latitude")
        val lonCol = cursor.getColumnIndex("longitude")
        val catCol = cursor.getColumnIndex("category")
        val cuisineCol = cursor.getColumnIndex("cuisine")
        val dietCol = cursor.getColumnIndex("diet_tags")
        val isVeganCol = cursor.getColumnIndex("is_vegan")
        val hoursCol = cursor.getColumnIndex("opening_hours")
        val addrCol = cursor.getColumnIndex("address")
        val wikiCol = cursor.getColumnIndex("wiki_title")
        val fameCol = cursor.getColumnIndex("fame_score")

        while (cursor.moveToNext()) {
            val dietTagsStr = if (dietCol >= 0) cursor.getString(dietCol) ?: "" else ""
            val dietList = dietTagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val isVegan = (isVeganCol >= 0 && cursor.getInt(isVeganCol) == 1) || dietList.contains("vegan")

            list.add(
                PlaceEntity(
                    id = if (idCol >= 0) cursor.getString(idCol) else "",
                    name = if (nameCol >= 0) cursor.getString(nameCol) else "",
                    city = if (cityCol >= 0) cursor.getString(cityCol) else "",
                    country = if (countryCol >= 0) cursor.getString(countryCol) else "",
                    latitude = if (latCol >= 0) cursor.getDouble(latCol) else 0.0,
                    longitude = if (lonCol >= 0) cursor.getDouble(lonCol) else 0.0,
                    category = if (catCol >= 0) cursor.getString(catCol) ?: "venue" else "venue",
                    cuisine = if (cuisineCol >= 0) cursor.getString(cuisineCol) else null,
                    dietTags = dietList,
                    isStrictlyVegan = isVegan,
                    openingHours = if (hoursCol >= 0) cursor.getString(hoursCol) else null,
                    address = if (addrCol >= 0) cursor.getString(addrCol) else null,
                    wikipediaTitle = if (wikiCol >= 0) cursor.getString(wikiCol) else null,
                    fameScore = if (fameCol >= 0) cursor.getDouble(fameCol) else 0.0
                )
            )
        }
        return list
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
