package com.example.weatherapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    private lateinit var cacheManager: CacheManager
    private lateinit var context: android.content.Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        cacheManager = CacheManager(context)
    }
    
    @Test
    fun testCacheWritePerformance() {
        val weatherData = WeatherData.empty().copy(
            temperature = 20.0,
            conditions = "Sunny",
            location = "London"
        )
        
        val writeTime = measureTimeMillis {
            repeat(100) {
                cacheManager.saveCurrentWeather(weatherData)
            }
        }
        assert(writeTime < 1000) { "Cache write performance is too slow: $writeTime ms" }
    }
    
    @Test
    fun testCacheReadPerformance() {
        val readTime = measureTimeMillis {
            repeat(100) {
                cacheManager.getCurrentWeather()
            }
        }
        assert(readTime < 500) { "Cache read performance is too slow: $readTime ms" }
    }
    
    @Test
    fun testNetworkRequestPerformance() {
        val weatherRepository = WeatherRepository(context)
        
        val requestTime = measureTimeMillis {
            weatherRepository.fetchWeather("London")
        }
        assert(requestTime < 5000) { "Network request is too slow: $requestTime ms" }
    }
} 