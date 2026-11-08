package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.NetworkClient
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.LocationPoint
import com.example.data.model.PlaceCategory
import com.example.data.model.PlaceItem
import com.example.data.model.PromptSuggestion
import com.example.data.model.RouteInfo
import com.example.data.model.WeatherData
import com.example.data.repository.AssistantRepository
import com.example.data.repository.MapsRepository
import com.example.data.repository.WeatherRepository
import com.example.ui.util.AppLanguage
import com.example.ui.util.StringsDefinition
import com.example.ui.util.getAppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppDestination(val route: String) {
    HOME("home"),
    MAPS("maps"),
    WEATHER("weather"),
    ASSISTANT("assistant"),
    PROFILE("profile"),
    SETTINGS("settings")
}

data class RecentActivity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val time: String,
    val destination: AppDestination
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val preferences = AppPreferences(application)
    private val weatherRepo = WeatherRepository()
    private val mapsRepo = MapsRepository(db.savedPlaceDao())
    private val assistantRepo = AssistantRepository(db.chatMessageDao(), weatherRepo)

    // Navigation & Global UI
    private val _currentDestination = MutableStateFlow(AppDestination.HOME)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    val strings: StringsDefinition
        get() = getAppStrings(_language.value)

    private val _backendUrl = MutableStateFlow("")
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _backendStatus = MutableStateFlow<String?>(null)
    val backendStatus: StateFlow<String?> = _backendStatus.asStateFlow()

    // Weather State
    private val _weatherData = MutableStateFlow(WeatherData())
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private val _isLoadingWeather = MutableStateFlow(false)
    val isLoadingWeather: StateFlow<Boolean> = _isLoadingWeather.asStateFlow()

    // Maps State
    private val _currentLocation = MutableStateFlow(LocationPoint(23.8103, 90.4125, "Dhaka", "Bangladesh"))
    val currentLocation: StateFlow<LocationPoint> = _currentLocation.asStateFlow()

    private val _mapSearchQuery = MutableStateFlow("")
    val mapSearchQuery: StateFlow<String> = _mapSearchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<PlaceItem>>(emptyList())
    val searchResults: StateFlow<List<PlaceItem>> = _searchResults.asStateFlow()

    private val _isSearchingMaps = MutableStateFlow(false)
    val isSearchingMaps: StateFlow<Boolean> = _isSearchingMaps.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PlaceCategory.ALL)
    val selectedCategory: StateFlow<PlaceCategory> = _selectedCategory.asStateFlow()

    private val _nearbyPlaces = MutableStateFlow<List<PlaceItem>>(emptyList())
    val nearbyPlaces: StateFlow<List<PlaceItem>> = _nearbyPlaces.asStateFlow()

    private val _selectedPlace = MutableStateFlow<PlaceItem?>(null)
    val selectedPlace: StateFlow<PlaceItem?> = _selectedPlace.asStateFlow()

    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> = _routeInfo.asStateFlow()

    val savedPlaces = mapsRepo.savedPlaces

    // Assistant State
    val chatMessages = assistantRepo.messages
    val suggestedPrompts: List<PromptSuggestion> = assistantRepo.suggestedPrompts

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Recent Activity
    private val _recentActivities = MutableStateFlow<List<RecentActivity>>(
        listOf(
            RecentActivity(title = "Checked Dhaka Live Weather", subtitle = "28°C • Clear Sky", time = "10 min ago", destination = AppDestination.WEATHER),
            RecentActivity(title = "Viewed Aurora Sky Lounge", subtitle = "Artisanal Coffee & Views", time = "25 min ago", destination = AppDestination.MAPS),
            RecentActivity(title = "AI Query: Weekend Plan", subtitle = "Outdoor itinerary generated", time = "1 hr ago", destination = AppDestination.ASSISTANT)
        )
    )
    val recentActivities: StateFlow<List<RecentActivity>> = _recentActivities.asStateFlow()

    init {
        // Load preferences
        viewModelScope.launch {
            _isDarkMode.value = preferences.isDarkMode.first()
            val langCode = preferences.languageCode.first()
            _language.value = if (langCode == "bn") AppLanguage.BENGALI else AppLanguage.ENGLISH
            _backendUrl.value = preferences.backendUrl.first()

            // Initial weather fetch
            refreshWeather()
            // Initial nearby places
            refreshNearbyPlaces()
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        viewModelScope.launch { preferences.setDarkMode(enabled) }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        viewModelScope.launch { preferences.setLanguage(lang.code) }
    }

    fun setBackendUrl(url: String) {
        _backendUrl.value = url
        viewModelScope.launch { preferences.setBackendUrl(url) }
    }

    fun testBackendConnection() {
        viewModelScope.launch {
            _backendStatus.value = "Pinging backend..."
            try {
                val api = NetworkClient.createBackendApi(_backendUrl.value)
                val res = api.checkHealth()
                _backendStatus.value = "Online: ${res.service ?: "Aurora API Service"}"
            } catch (e: Exception) {
                _backendStatus.value = "Offline: Using resilient local/direct providers"
            }
        }
    }

    fun refreshWeather(lat: Double = _currentLocation.value.latitude, lon: Double = _currentLocation.value.longitude, name: String = _currentLocation.value.name) {
        viewModelScope.launch {
            _isLoadingWeather.value = true
            val result = weatherRepo.fetchWeather(lat, lon, name)
            result.onSuccess { data ->
                _weatherData.value = data
            }
            _isLoadingWeather.value = false
        }
    }

    fun setLocation(lat: Double, lon: Double, name: String) {
        _currentLocation.value = LocationPoint(lat, lon, name)
        refreshWeather(lat, lon, name)
        refreshNearbyPlaces()
    }

    fun onMapSearchQueryChange(query: String) {
        _mapSearchQuery.value = query
        if (query.length >= 2) {
            viewModelScope.launch {
                _isSearchingMaps.value = true
                val res = mapsRepo.searchPlaces(query)
                res.onSuccess { _searchResults.value = it }
                _isSearchingMaps.value = false
            }
        } else {
            _searchResults.value = emptyList()
        }
    }

    fun selectCategory(category: PlaceCategory) {
        _selectedCategory.value = category
        refreshNearbyPlaces()
    }

    private fun refreshNearbyPlaces() {
        viewModelScope.launch {
            val places = mapsRepo.getNearbyPlaces(
                _currentLocation.value.latitude,
                _currentLocation.value.longitude,
                _selectedCategory.value
            )
            _nearbyPlaces.value = places
        }
    }

    fun selectPlace(place: PlaceItem) {
        _selectedPlace.value = place
        // Calculate route automatically
        val start = _currentLocation.value
        val end = LocationPoint(place.latitude, place.longitude, place.title, place.subtitle)
        _routeInfo.value = mapsRepo.calculateRoute(start, end)
    }

    fun clearSelectedPlace() {
        _selectedPlace.value = null
        _routeInfo.value = null
    }

    fun toggleSavePlace(place: PlaceItem) {
        viewModelScope.launch {
            mapsRepo.toggleSavePlace(place)
            refreshNearbyPlaces()
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiThinking.value = true
            val response = assistantRepo.sendMessage(text, _backendUrl.value, _language.value.code)
            _isAiThinking.value = false

            // Add to recent activities
            val newActivity = RecentActivity(
                title = "AI Query: \"${text.take(28)}...\"",
                subtitle = response.text.take(35) + "...",
                time = "Just now",
                destination = AppDestination.ASSISTANT
            )
            _recentActivities.value = listOf(newActivity) + _recentActivities.value.take(4)
        }
    }

    fun handleActionChip(actionType: ActionType, payload: String) {
        when (actionType) {
            ActionType.SHOW_WEATHER -> {
                navigateTo(AppDestination.WEATHER)
            }
            ActionType.SHOW_MAP_PLACE, ActionType.ROUTE_DIRECTIONS -> {
                navigateTo(AppDestination.MAPS)
                val targetPlace = _nearbyPlaces.value.firstOrNull { it.id == payload || it.title.contains(payload, ignoreCase = true) }
                    ?: _nearbyPlaces.value.firstOrNull()
                if (targetPlace != null) {
                    selectPlace(targetPlace)
                }
            }
            ActionType.NONE -> Unit
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            assistantRepo.clearHistory()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            db.savedPlaceDao().clearAll()
            db.chatMessageDao().clearChat()
            refreshNearbyPlaces()
        }
    }
}
