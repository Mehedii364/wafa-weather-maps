package com.example

import com.example.data.model.WeatherCodeHelper
import com.example.ui.util.AppLanguage
import com.example.ui.util.getAppStrings
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testWeatherCodeConversion() {
        assertEquals("Clear Sky", WeatherCodeHelper.getConditionName(0))
        assertEquals("Rain Showers", WeatherCodeHelper.getConditionName(61))
        assertEquals("Thunderstorm", WeatherCodeHelper.getConditionName(95))
    }

    @Test
    fun testBilingualStringsLoad() {
        val en = getAppStrings(AppLanguage.ENGLISH)
        val bn = getAppStrings(AppLanguage.BENGALI)
        assertEquals("Aurora", en.appTitle)
        assertEquals("অরোরা", bn.appTitle)
        assertEquals("Home", en.home)
        assertEquals("হোম", bn.home)
    }
}
