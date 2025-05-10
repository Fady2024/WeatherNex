package com.example.weatherapp.di

import android.content.Context
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.SearchHistoryManager
import com.example.weatherapp.ui.weather.WeatherPresenter
import com.example.weatherapp.ui.forecast.ForecastPresenter
import com.example.weatherapp.ui.settings.SettingsPresenter

/**
 * Service Locator pattern implementation for dependency injection
 * Provides singleton instances of dependencies throughout the app
 */
object ServiceLocator {
    private var instance: ServiceLocator? = null
    private var applicationContext: Context? = null

    // Lazy initialized dependencies
    private lateinit var networkManager: NetworkManager
    private lateinit var cacheManager: CacheManager
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var searchHistoryManager: SearchHistoryManager

    fun init(context: Context) {
        applicationContext = context.applicationContext
        networkManager = NetworkManager(context)
        cacheManager = CacheManager(context)
        weatherRepository = WeatherRepository(context)
        searchHistoryManager = SearchHistoryManager(context)
    }

    fun provideWeatherPresenter(): WeatherPresenter {
        checkInit()
        return WeatherPresenter(
            weatherRepository = weatherRepository,
            networkManager = networkManager,
            cacheManager = cacheManager
        )
    }

    fun provideForecastPresenter(): ForecastPresenter {
        checkInit()
        return ForecastPresenter(
            weatherRepository = weatherRepository,
            networkManager = networkManager,
            cacheManager = cacheManager
        )
    }

    fun provideSettingsPresenter(): SettingsPresenter {
        checkInit()
        return SettingsPresenter()
    }

    fun provideNetworkManager(): NetworkManager {
        checkInit()
        return networkManager
    }

    fun provideCacheManager(): CacheManager {
        checkInit()
        return cacheManager
    }

    fun provideWeatherRepository(): WeatherRepository {
        checkInit()
        return weatherRepository
    }

    fun provideSearchHistoryManager(): SearchHistoryManager {
        checkInit()
        return searchHistoryManager
    }

    private fun checkInit() {
        check(applicationContext != null) { "ServiceLocator must be initialized with init(Context) before using" }
    }

    fun reset() {
        applicationContext = null
    }
} 