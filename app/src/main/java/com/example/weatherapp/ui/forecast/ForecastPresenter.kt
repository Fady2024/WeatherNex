package com.example.weatherapp.ui.forecast

import android.os.Handler
import android.os.Looper
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.Settings

class ForecastPresenter(
    private val weatherRepository: WeatherRepository,
    private val networkManager: NetworkManager,
    private val cacheManager: CacheManager
) : ForecastContract.Presenter {

    private var view: ForecastContract.View? = null
    private var currentLocation: String? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastTemperatureUnit: Settings.TemperatureUnit = Settings.temperatureUnit
    private var lastWindSpeedUnit: Settings.WindSpeedUnit = Settings.windSpeedUnit
    private var lastPressureUnit: Settings.PressureUnit = Settings.pressureUnit
    private var lastLengthUnit: Settings.LengthUnit = Settings.lengthUnit
    private var lastTimeFormat: Boolean = Settings.use24HourFormat

    override fun attachView(view: ForecastContract.View) {
        this.view = view
        
        lastTemperatureUnit = Settings.temperatureUnit
        lastWindSpeedUnit = Settings.windSpeedUnit
        lastPressureUnit = Settings.pressureUnit
        lastLengthUnit = Settings.lengthUnit
        lastTimeFormat = Settings.use24HourFormat
    }

    override fun detachView() {
        this.view = null
    }

    override fun isViewAttached(): Boolean = view != null
    
    /**
     * Checks if any settings have changed since last time
     * @return true if settings have changed
     */
    fun haveSettingsChanged(): Boolean {
        return lastTemperatureUnit != Settings.temperatureUnit ||
               lastWindSpeedUnit != Settings.windSpeedUnit ||
               lastPressureUnit != Settings.pressureUnit ||
               lastLengthUnit != Settings.lengthUnit ||
               lastTimeFormat != Settings.use24HourFormat
    }
    
    /**
     * Updates tracked settings to current values
     */
    fun updateTrackedSettings() {
        lastTemperatureUnit = Settings.temperatureUnit
        lastWindSpeedUnit = Settings.windSpeedUnit
        lastPressureUnit = Settings.pressureUnit
        lastLengthUnit = Settings.lengthUnit
        lastTimeFormat = Settings.use24HourFormat
    }

    override fun loadForecastData(location: String?) {
        currentLocation = location
        if (networkManager.isNetworkAvailable()) {
            refreshForecastData()
        } else {
            loadCachedData()
            view?.showOfflineBanner("No internet connection. Showing cached data.")
        }
    }

    override fun refreshForecastData() {
        view?.showLoading()
        
        if (!networkManager.isNetworkAvailable()) {
            loadCachedData()
            view?.hideLoading()
            view?.showOfflineBanner("No internet connection. Showing cached data.")
            return
        }
        
        Thread {
            try {
                val locationToUse = currentLocation ?: "London"
                val forecastData = weatherRepository.fetchForecast(locationToUse)
                cacheManager.saveForecast(forecastData)
                
                mainHandler.post {
                    if (isViewAttached()) {
                        view?.updateForecast(forecastData)
                        view?.updateLocationName(forecastData.location)
                        view?.hideLoading()
                        view?.hideOfflineBanner()
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    if (isViewAttached()) {
                        view?.hideLoading()
                        loadCachedData()
                        view?.showError("Error loading forecast: ${e.localizedMessage}")
                    }
                }
            }
        }.start()
    }
    
    private fun loadCachedData() {
        val cacheResult = cacheManager.getForecast()
        when (cacheResult) {
            is CacheManager.CacheResult.Success -> {
                view?.updateForecast(cacheResult.data)
                view?.updateLocationName(cacheResult.data.location)
            }
            is CacheManager.CacheResult.Expired -> {
                view?.showError("Cached data has expired. Please refresh when back online.")
            }
            is CacheManager.CacheResult.NotFound -> {
                view?.showError("No cached data available. Please connect to the internet.")
            }
            is CacheManager.CacheResult.Error -> {
                view?.showError("Error loading cached data: ${cacheResult.exception.localizedMessage}")
            }
        }
    }
} 