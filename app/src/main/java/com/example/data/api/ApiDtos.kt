package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String? = null,
    val service: String? = null,
    val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class BackendChatRequest(
    val message: String,
    val history: List<ChatHistoryItem> = emptyList(),
    val language: String = "en"
)

@JsonClass(generateAdapter = true)
data class ChatHistoryItem(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class BackendChatResponse(
    val reply: String? = null,
    val actionType: String? = null,
    val actionPayload: String? = null,
    val status: String? = "success"
)

// Open-Meteo Direct Live Weather API DTOs (Real weather provider fallback)
@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double? = null,
    val longitude: Double? = null,
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto? = null,
    val hourly: HourlyWeatherDto? = null,
    val daily: DailyWeatherDto? = null
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Float = 0f,
    val weathercode: Int = 0,
    val time: String? = null
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Float> = emptyList(),
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Int> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    val weathercode: List<Int> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    val time: List<String> = emptyList(),
    @Json(name = "weathercode") val weathercode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperatureMax: List<Float> = emptyList(),
    @Json(name = "temperature_2m_min") val temperatureMin: List<Float> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Float> = emptyList()
)

// Nominatim Geocoding DTO
@JsonClass(generateAdapter = true)
data class NominatimResultDto(
    @Json(name = "place_id") val placeId: Long = 0,
    val lat: String = "0",
    val lon: String = "0",
    @Json(name = "display_name") val displayName: String = "",
    val name: String? = null,
    val type: String? = null,
    val category: String? = null
)
