package com.example.data.repository

import com.example.data.api.NetworkClient
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.WeatherCodeHelper
import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {
    private val openMeteoApi = NetworkClient.openMeteoApi

    // Cached state
    private var lastCachedWeather: WeatherData? = null

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        locationName: String
    ): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val response = openMeteoApi.getForecast(
                latitude = latitude,
                longitude = longitude
            )

            val current = response.currentWeather
            val currentTemp = current?.temperature ?: 28.0f
            val currentCode = current?.weathercode ?: 0
            val windSpeed = current?.windspeed ?: 12.0f

            // Parse Hourly
            val hourlyItems = mutableListOf<HourlyForecast>()
            val hourlyDto = response.hourly
            if (hourlyDto != null && hourlyDto.time.isNotEmpty()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                val outputFormat = SimpleDateFormat("ha", Locale.getDefault())
                val count = minOf(24, hourlyDto.time.size)
                for (i in 0 until count) {
                    val rawTime = hourlyDto.time[i]
                    val timeLabel = try {
                        val date = inputFormat.parse(rawTime)
                        if (date != null) outputFormat.format(date).lowercase() else rawTime
                    } catch (e: Exception) {
                        "${i}:00"
                    }
                    val temp = hourlyDto.temperature2m.getOrElse(i) { currentTemp }
                    val code = hourlyDto.weathercode.getOrElse(i) { currentCode }
                    val precip = hourlyDto.precipitationProbability.getOrElse(i) { 10 }
                    hourlyItems.add(
                        HourlyForecast(
                            timeLabel = if (i == 0) "Now" else timeLabel,
                            temperature = temp,
                            conditionCode = code,
                            precipitationProbability = precip
                        )
                    )
                }
            }

            // Parse Daily
            val dailyItems = mutableListOf<DailyForecast>()
            val dailyDto = response.daily
            if (dailyDto != null && dailyDto.time.isNotEmpty()) {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val count = minOf(7, dailyDto.time.size)
                for (i in 0 until count) {
                    val rawDate = dailyDto.time[i]
                    val dayLabel = try {
                        val date = inputFormat.parse(rawDate)
                        if (i == 0) "Today" else if (date != null) dayFormat.format(date) else rawDate
                    } catch (e: Exception) {
                        "Day ${i + 1}"
                    }
                    val maxTemp = dailyDto.temperatureMax.getOrElse(i) { currentTemp + 4 }
                    val minTemp = dailyDto.temperatureMin.getOrElse(i) { currentTemp - 4 }
                    val code = dailyDto.weathercode.getOrElse(i) { currentCode }
                    val precip = dailyDto.precipitationProbabilityMax.getOrElse(i) { 15 }

                    dailyItems.add(
                        DailyForecast(
                            dayLabel = dayLabel,
                            maxTemp = maxTemp,
                            minTemp = minTemp,
                            conditionCode = code,
                            precipitationProbability = precip,
                            conditionDescription = WeatherCodeHelper.getConditionName(code)
                        )
                    )
                }
            }

            val todayMax = dailyItems.firstOrNull()?.maxTemp ?: (currentTemp + 3f)
            val todayMin = dailyItems.firstOrNull()?.minTemp ?: (currentTemp - 4f)
            val currentHumidity = hourlyDto?.relativeHumidity2m?.firstOrNull() ?: 65
            val currentPrecip = hourlyDto?.precipitationProbability?.firstOrNull() ?: 15
            val currentUv = dailyDto?.uvIndexMax?.firstOrNull()?.toInt() ?: 6

            val result = WeatherData(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                temperatureCelsius = currentTemp,
                conditionDescription = WeatherCodeHelper.getConditionName(currentCode),
                conditionCode = currentCode,
                humidityPercentage = currentHumidity,
                windSpeedKmh = windSpeed,
                precipitationProbability = currentPrecip,
                uvIndex = currentUv,
                feelsLikeCelsius = currentTemp + 1.5f,
                highCelsius = todayMax,
                lowCelsius = todayMin,
                hourlyForecast = hourlyItems,
                dailyForecast = dailyItems,
                isLive = true,
                lastUpdated = System.currentTimeMillis()
            )
            lastCachedWeather = result
            Result.success(result)
        } catch (e: Exception) {
            val fallback = lastCachedWeather ?: WeatherData(
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                isLive = false
            )
            Result.success(fallback)
        }
    }
}
