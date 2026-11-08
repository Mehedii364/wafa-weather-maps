package com.example

import com.example.data.model.ActionType
import com.example.data.model.ChatMessage
import com.example.data.model.LocationPoint
import com.example.data.model.PlaceCategory
import com.example.data.model.PlaceItem
import com.example.data.model.WeatherCodeHelper
import com.example.data.model.WeatherData
import com.example.ui.util.AppLanguage
import com.example.ui.util.getAppStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWeatherCodeHelper() {
        assertEquals("Clear Sky", WeatherCodeHelper.getConditionName(0))
        assertEquals("Partly Cloudy", WeatherCodeHelper.getConditionName(1))
        assertEquals("Foggy / Mist", WeatherCodeHelper.getConditionName(45))
        assertEquals("Rain Showers", WeatherCodeHelper.getConditionName(61))
        assertEquals("Snowfall", WeatherCodeHelper.getConditionName(71))
        assertEquals("Thunderstorm", WeatherCodeHelper.getConditionName(95))
    }

    @Test
    fun testBilingualStrings() {
        val en = getAppStrings(AppLanguage.ENGLISH)
        val bn = getAppStrings(AppLanguage.BENGALI)

        assertEquals("Aurora", en.appTitle)
        assertEquals("অরোরা", bn.appTitle)

        assertEquals("Home", en.home)
        assertEquals("হোম", bn.home)

        assertEquals("Maps", en.maps)
        assertEquals("ম্যাপস", bn.maps)

        assertEquals("Weather", en.weather)
        assertEquals("আবহাওয়া", bn.weather)

        assertEquals("AI Assistant", en.assistant)
        assertEquals("এআই সহকারী", bn.assistant)
    }

    @Test
    fun testPlaceItemCreation() {
        val place = PlaceItem(
            id = "cafe_1",
            title = "Aurora Sky Lounge",
            subtitle = "Artisanal espresso",
            latitude = 23.812,
            longitude = 90.415,
            category = PlaceCategory.CAFE,
            rating = 4.9f,
            isSaved = true
        )
        assertEquals("Aurora Sky Lounge", place.title)
        assertEquals(PlaceCategory.CAFE, place.category)
        assertTrue(place.isSaved)
        assertEquals(4.9f, place.rating)
    }

    @Test
    fun testChatMessageActionTypes() {
        val chatMsg = ChatMessage(
            text = "Here is the weather in Dhaka",
            isUser = false,
            actionType = ActionType.SHOW_WEATHER,
            actionPayload = "Dhaka, Bangladesh"
        )
        assertEquals(ActionType.SHOW_WEATHER, chatMsg.actionType)
        assertEquals("Dhaka, Bangladesh", chatMsg.actionPayload)
        assertNotNull(chatMsg.id)
    }

    @Test
    fun testWeatherDataDefaultValues() {
        val weather = WeatherData()
        assertEquals(28.5f, weather.temperatureCelsius)
        assertTrue(weather.isLive)
        assertTrue(weather.humidityPercentage in 0..100)
    }
}
