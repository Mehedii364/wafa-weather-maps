package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.local.dao.SavedPlaceDao
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.model.LocationPoint
import com.example.data.model.PlaceCategory
import com.example.data.model.PlaceItem
import com.example.data.model.RouteInfo
import com.example.data.model.RouteStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class MapsRepository(
    private val savedPlaceDao: SavedPlaceDao
) {
    private val nominatimApi = NetworkClient.nominatimApi

    val savedPlaces: Flow<List<PlaceItem>> = savedPlaceDao.getAllSavedPlaces().map { entities ->
        entities.map { entity ->
            PlaceItem(
                id = entity.id,
                title = entity.title,
                subtitle = entity.subtitle,
                latitude = entity.latitude,
                longitude = entity.longitude,
                category = try {
                    PlaceCategory.valueOf(entity.category)
                } catch (e: Exception) {
                    PlaceCategory.ALL
                },
                rating = entity.rating,
                isSaved = true
            )
        }
    }

    suspend fun searchPlaces(query: String): Result<List<PlaceItem>> = withContext(Dispatchers.IO) {
        try {
            val results = nominatimApi.searchPlaces(query = query)
            val items = results.map { dto ->
                val lat = dto.lat.toDoubleOrNull() ?: 0.0
                val lon = dto.lon.toDoubleOrNull() ?: 0.0
                val shortTitle = dto.name ?: dto.displayName.split(",").firstOrNull() ?: "Location"
                PlaceItem(
                    id = "nom_${dto.placeId}",
                    title = shortTitle,
                    subtitle = dto.displayName,
                    latitude = lat,
                    longitude = lon,
                    category = mapTypeToCategory(dto.type),
                    rating = 4.7f,
                    isSaved = savedPlaceDao.isPlaceSaved("nom_${dto.placeId}")
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            // Return curated locations matching query if offline
            Result.success(getCuratedPlaces(23.8103, 90.4125).filter {
                it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true)
            })
        }
    }

    suspend fun getNearbyPlaces(lat: Double, lon: Double, category: PlaceCategory): List<PlaceItem> = withContext(Dispatchers.IO) {
        val curated = getCuratedPlaces(lat, lon)
        val filtered = if (category == PlaceCategory.ALL) curated else curated.filter { it.category == category }
        // Compute actual distances
        filtered.map { place ->
            val dist = calculateDistanceMeters(lat, lon, place.latitude, place.longitude)
            place.copy(
                distanceMeters = dist,
                isSaved = savedPlaceDao.isPlaceSaved(place.id)
            )
        }.sortedBy { it.distanceMeters }
    }

    suspend fun toggleSavePlace(place: PlaceItem) = withContext(Dispatchers.IO) {
        val isCurrentlySaved = savedPlaceDao.isPlaceSaved(place.id)
        if (isCurrentlySaved) {
            savedPlaceDao.deletePlaceById(place.id)
        } else {
            savedPlaceDao.insertPlace(
                SavedPlaceEntity(
                    id = place.id,
                    title = place.title,
                    subtitle = place.subtitle,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    category = place.category.name,
                    rating = place.rating
                )
            )
        }
    }

    fun calculateRoute(start: LocationPoint, end: LocationPoint): RouteInfo {
        val distanceMeters = calculateDistanceMeters(start.latitude, start.longitude, end.latitude, end.longitude)
        val distanceKm = (distanceMeters / 1000f * 10f).roundToInt() / 10f
        val durationMins = maxOf(4, (distanceKm * 3.5f).roundToInt())

        val steps = listOf(
            RouteStep("Head north towards main avenue", "200 m", "straight"),
            RouteStep("Turn right at the junction onto Grand Boulevard", "${(distanceKm * 400).roundToInt()} m", "turn_right"),
            RouteStep("Continue straight across the metro plaza", "${(distanceKm * 300).roundToInt()} m", "straight"),
            RouteStep("Turn left towards ${end.name}", "150 m", "turn_left"),
            RouteStep("Arrive at ${end.name}", "Destination reached", "flag")
        )

        return RouteInfo(
            destinationName = end.name,
            distanceKm = maxOf(0.8f, distanceKm),
            durationMinutes = durationMins,
            startLocation = start,
            endLocation = end,
            steps = steps
        )
    }

    private fun mapTypeToCategory(type: String?): PlaceCategory {
        return when (type?.lowercase()) {
            "cafe", "coffee_shop" -> PlaceCategory.CAFE
            "restaurant", "fast_food" -> PlaceCategory.RESTAURANT
            "station", "subway", "bus_stop" -> PlaceCategory.TRANSIT
            "park", "garden" -> PlaceCategory.PARK
            "hospital", "clinic", "pharmacy" -> PlaceCategory.HOSPITAL
            else -> PlaceCategory.ALL
        }
    }

    private fun getCuratedPlaces(baseLat: Double, baseLon: Double): List<PlaceItem> {
        return listOf(
            PlaceItem(
                id = "poi_1",
                title = "Aurora Sky Lounge & Cafe",
                subtitle = "Artisanal espresso & panoramic rooftop views",
                latitude = baseLat + 0.0035,
                longitude = baseLon + 0.0028,
                category = PlaceCategory.CAFE,
                rating = 4.9f
            ),
            PlaceItem(
                id = "poi_2",
                title = "Central Metro Interchange",
                subtitle = "Lines 1 & 6 Rapid Transit Hub",
                latitude = baseLat + 0.0048,
                longitude = baseLon - 0.0032,
                category = PlaceCategory.TRANSIT,
                rating = 4.6f
            ),
            PlaceItem(
                id = "poi_3",
                title = "The Obsidian Bistro",
                subtitle = "Contemporary fusion dining & dessert bar",
                latitude = baseLat - 0.0042,
                longitude = baseLon + 0.0045,
                category = PlaceCategory.RESTAURANT,
                rating = 4.8f
            ),
            PlaceItem(
                id = "poi_4",
                title = "Botanical Eco Gardens",
                subtitle = "Scenic walking trails & lotus lake",
                latitude = baseLat + 0.0075,
                longitude = baseLon + 0.0062,
                category = PlaceCategory.PARK,
                rating = 4.9f
            ),
            PlaceItem(
                id = "poi_5",
                title = "Metropolitan Medical Center",
                subtitle = "24/7 Emergency Care & Specialty Clinic",
                latitude = baseLat - 0.0060,
                longitude = baseLon - 0.0051,
                category = PlaceCategory.HOSPITAL,
                rating = 4.7f
            ),
            PlaceItem(
                id = "poi_6",
                title = "Cyber Pulse Espresso Lab",
                subtitle = "Single-origin pour-overs & tech coworking",
                latitude = baseLat - 0.0020,
                longitude = baseLon - 0.0038,
                category = PlaceCategory.CAFE,
                rating = 4.8f
            )
        )
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).roundToInt()
    }
}
