package com.example.data.model

data class WeatherData(
    val locationName: String = "Dhaka, Bangladesh",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val temperatureCelsius: Float = 28.5f,
    val conditionDescription: String = "Clear Sky",
    val conditionCode: Int = 0,
    val humidityPercentage: Int = 62,
    val windSpeedKmh: Float = 14.2f,
    val precipitationProbability: Int = 10,
    val uvIndex: Int = 6,
    val feelsLikeCelsius: Float = 29.8f,
    val highCelsius: Float = 32.0f,
    val lowCelsius: Float = 22.0f,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
    val isLive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class HourlyForecast(
    val timeLabel: String,
    val temperature: Float,
    val conditionCode: Int,
    val precipitationProbability: Int
)

data class DailyForecast(
    val dayLabel: String,
    val maxTemp: Float,
    val minTemp: Float,
    val conditionCode: Int,
    val precipitationProbability: Int,
    val conditionDescription: String
)

object WeatherCodeHelper {
    fun getConditionName(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy / Mist"
            51, 53, 55, 61, 63, 65 -> "Rain Showers"
            71, 73, 75, 77 -> "Snowfall"
            80, 81, 82 -> "Heavy Rain"
            95, 96, 99 -> "Thunderstorm"
            else -> "Partly Cloudy"
        }
    }
}
