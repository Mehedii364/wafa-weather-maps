package com.example.data.model

enum class PlaceCategory(val displayName: String, val iconName: String) {
    ALL("All", "place"),
    CAFE("Cafes", "local_cafe"),
    RESTAURANT("Dining", "restaurant"),
    TRANSIT("Transit", "directions_subway"),
    PARK("Parks", "park"),
    HOSPITAL("Healthcare", "local_hospital")
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val address: String = ""
)

data class PlaceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val category: PlaceCategory,
    val distanceMeters: Int = 0,
    val isSaved: Boolean = false,
    val rating: Float = 4.8f
)

data class RouteStep(
    val instruction: String,
    val distanceText: String,
    val iconType: String = "straight"
)

data class RouteInfo(
    val destinationName: String,
    val distanceKm: Float,
    val durationMinutes: Int,
    val startLocation: LocationPoint,
    val endLocation: LocationPoint,
    val steps: List<RouteStep>
)
