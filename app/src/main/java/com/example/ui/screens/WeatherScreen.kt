package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuroraCard
import com.example.ui.components.AuroraPill
import com.example.ui.components.AuroraSearchBar
import com.example.ui.theme.AuroraDarkCyan
import com.example.ui.theme.AuroraDarkElevated
import com.example.ui.theme.AuroraDarkPrimary
import com.example.ui.theme.AuroraDarkSuccess
import com.example.ui.theme.AuroraDarkViolet
import com.example.ui.viewmodel.AppViewModel

data class QuickCity(val name: String, val lat: Double, val lon: Double)

@Composable
fun WeatherScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoadingWeather.collectAsState()
    val strings = viewModel.strings

    var citySearchText by remember { mutableStateOf("") }

    val popularCities = listOf(
        QuickCity("Dhaka", 23.8103, 90.4125),
        QuickCity("New York", 40.7128, -74.0060),
        QuickCity("London", 51.5074, -0.1278),
        QuickCity("Tokyo", 35.6762, 139.6503),
        QuickCity("Paris", 48.8566, 2.3522),
        QuickCity("Dubai", 25.2048, 55.2708)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_screen_content"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Search Bar & Refresh Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AuroraSearchBar(
                        query = citySearchText,
                        onQueryChange = { citySearchText = it },
                        onSearch = {
                            if (citySearchText.isNotBlank()) {
                                viewModel.refreshWeather(weather.latitude, weather.longitude, citySearchText)
                                citySearchText = ""
                            }
                        },
                        placeholder = "Search city or location...",
                        onClear = { citySearchText = "" }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.refreshWeather() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .testTag("weather_refresh_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AuroraDarkCyan)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Quick City Selection Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularCities) { city ->
                    val isSelected = weather.locationName.contains(city.name, ignoreCase = true)
                    AuroraPill(
                        text = city.name,
                        selected = isSelected,
                        onClick = { viewModel.refreshWeather(city.lat, city.lon, city.name) }
                    )
                }
            }
        }

        // Hero Weather Card with Aurora Gradient Glow
        item {
            AuroraCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_hero_card"),
                borderGlow = true
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AuroraDarkViolet.copy(alpha = 0.28f),
                                    AuroraDarkPrimary.copy(alpha = 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = AuroraDarkCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = weather.locationName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Icon(
                            imageVector = getWeatherIcon(weather.conditionCode),
                            contentDescription = weather.conditionDescription,
                            tint = if (weather.conditionCode == 0) Color(0xFFFFB300) else AuroraDarkCyan,
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${weather.temperatureCelsius.toInt()}°C",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 58.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = weather.conditionDescription,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AuroraDarkCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${strings.feelsLike} ${weather.feelsLikeCelsius.toInt()}°C • H: ${weather.highCelsius.toInt()}° L: ${weather.lowCelsius.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Metric 2x2 Grid (Humidity, Wind, Precipitation, UV Index)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = strings.humidity,
                    value = "${weather.humidityPercentage}%",
                    icon = Icons.Default.WaterDrop,
                    iconColor = AuroraDarkCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = strings.wind,
                    value = "${weather.windSpeedKmh.toInt()} km/h",
                    icon = Icons.Default.Air,
                    iconColor = AuroraDarkViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = strings.rainProb,
                    value = "${weather.precipitationProbability}%",
                    icon = Icons.Default.Cloud,
                    iconColor = AuroraDarkPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = strings.uvIndex,
                    value = "${weather.uvIndex} (Mod)",
                    icon = Icons.Default.DeviceThermostat,
                    iconColor = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 24-Hour Forecast Horizontal Strip
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.forecast24h,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weather.hourlyForecast) { hourly ->
                        AuroraCard(
                            modifier = Modifier.width(82.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = hourly.timeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Icon(
                                    imageVector = getWeatherIcon(hourly.conditionCode),
                                    contentDescription = null,
                                    tint = AuroraDarkCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${hourly.temperature.toInt()}°",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${hourly.precipitationProbability}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuroraDarkViolet
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7-Day Extended Forecast Card
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.forecast7d,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuroraCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        weather.dailyForecast.forEachIndexed { index, day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(65.dp)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getWeatherIcon(day.conditionCode),
                                        contentDescription = null,
                                        tint = AuroraDarkCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${day.precipitationProbability}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AuroraDarkViolet
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${day.minTemp.toInt()}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(55.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                Brush.horizontalGradient(listOf(AuroraDarkCyan, AuroraDarkPrimary))
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${day.maxTemp.toInt()}°",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    AuroraCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun getWeatherIcon(code: Int): ImageVector {
    return when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.WbCloudy
        45, 48 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.WbCloudy
    }
}
