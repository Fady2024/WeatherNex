package com.example.weatherapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.weatherapp.ui.weather.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun testWeatherDataDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.textViewTemperature)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.textViewWeatherDescription)).check(matches(isDisplayed()))
    }

    @Test
    fun testMenuButtonIsDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.buttonMenu)).check(matches(isDisplayed()))
    }

    @Test
    fun testRefreshWeatherData() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.swipeRefreshLayout)).perform(swipeDown())
    }

    @Test
    fun testSearchButtonIsDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.buttonSearch)).check(matches(isDisplayed()))
    }
} 