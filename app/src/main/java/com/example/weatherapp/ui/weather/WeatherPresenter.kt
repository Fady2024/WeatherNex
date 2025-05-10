@file:Suppress("DEPRECATION")

package com.example.weatherapp.ui.weather

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.example.weatherapp.R
import com.example.weatherapp.model.HourlyForecastData
import com.example.weatherapp.model.LocationData
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import java.io.IOException

class WeatherPresenter(
    private val weatherRepository: WeatherRepository,
    private val networkManager: NetworkManager,
    private val cacheManager: CacheManager
) : WeatherContract.Presenter {

    private var view: WeatherContract.View? = null
    private var currentLocation: String? = null
    private var currentWeather: WeatherData? = null
    private var hourlyForecasts: List<HourlyForecastData>? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun attachView(view: WeatherContract.View) {
        this.view = view
        hourlyForecasts?.let { view.updateHourlyForecast(it) }
    }

    override fun detachView() {
        this.view = null
    }

    override fun isViewAttached(): Boolean = view != null

    override fun loadWeatherData(location: String?) {
        currentLocation = location
        if (networkManager.isNetworkAvailable()) {
            refreshWeatherData()
        } else {
            loadCachedData(location ?: "London")
        }
    }

    override fun refreshWeatherData() {
        if (!networkManager.isNetworkAvailable()) {
            loadCachedData(currentLocation ?: "London")
            return
        }

        view?.showLoading()
        
        WeatherDataAsyncTask().execute(currentLocation ?: "London")
    }
    
    @SuppressLint("StaticFieldLeak")
    private inner class WeatherDataAsyncTask : AsyncTask<String, Void, Pair<WeatherResult, List<HourlyForecastData>?>>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): Pair<WeatherResult, List<HourlyForecastData>?> {
            val location = params[0]
            return try {
                val weather = weatherRepository.fetchWeather(location)
                val hourlyData = weatherRepository.fetchHourlyForecast(location)
                Pair(WeatherResult.Success(weather), hourlyData)
            } catch (e: IOException) {
                Pair(WeatherResult.Error(e, R.string.error_network), null)
            } catch (e: Exception) {
                Pair(WeatherResult.Error(e, R.string.error_general), null)
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Pair<WeatherResult, List<HourlyForecastData>?>) {
            if (!isViewAttached()) return
            
            when (val weatherResult = result.first) {
                is WeatherResult.Success -> {
                    currentWeather = weatherResult.data
                    hourlyForecasts = result.second
                    
                    view?.updateWeatherData(weatherResult.data)
                    result.second?.let { view?.updateHourlyForecast(it) }
                    view?.updateLocationName(weatherResult.data.location)
                    view?.hideLoading()
                    view?.hideOfflineBanner()
                    cacheManager.saveCurrentWeather(weatherResult.data)
                    result.second?.let { cacheManager.saveHourlyForecast(it) }
                }
                is WeatherResult.Error -> {
                    handleError(weatherResult.messageResId)
                }
            }
        }
    }
    
    sealed class WeatherResult {
        data class Success(val data: WeatherData) : WeatherResult()
        data class Error(val exception: Exception, val messageResId: Int) : WeatherResult()
    }

    override fun searchLocation(query: String) {
        if (!networkManager.isNetworkAvailable()) {
            view?.showError(view?.context?.getString(R.string.error_offline_search) ?: "")
            return
        }

        if (query.isBlank()) return

        view?.showLoading()
        view?.hideSearchInput()
        
        SearchLocationAsyncTask().execute(query)
    }
    
    @SuppressLint("StaticFieldLeak")
    private inner class SearchLocationAsyncTask : AsyncTask<String, Void, Pair<WeatherResult, List<HourlyForecastData>?>>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): Pair<WeatherResult, List<HourlyForecastData>?> {
            val query = params[0]
            return try {
                val weather = weatherRepository.fetchWeather(query)
                val hourlyData = weatherRepository.fetchHourlyForecast(query)
                Pair(WeatherResult.Success(weather), hourlyData)
            } catch (e: Exception) {
                Pair(WeatherResult.Error(e, R.string.error_general), null)
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Pair<WeatherResult, List<HourlyForecastData>?>) {
            if (!isViewAttached()) return
            
            when (val weatherResult = result.first) {
                is WeatherResult.Success -> {
                    currentWeather = weatherResult.data
                    hourlyForecasts = result.second
                    currentLocation = weatherResult.data.location
                    
                    view?.updateWeatherData(weatherResult.data)
                    result.second?.let { view?.updateHourlyForecast(it) }
                    view?.updateLocationName(weatherResult.data.location)
                    view?.hideLoading()
                    
                    cacheManager.saveCurrentWeather(weatherResult.data)
                    result.second?.let { cacheManager.saveHourlyForecast(it) }
                }
                is WeatherResult.Error -> {
                    handleError(weatherResult.messageResId)
                }
            }
        }
    }

    override fun updateLocation(location: LocationData) {
        Thread {
            try {
                val locationName = weatherRepository.getLocationName(location.latitude, location.longitude)
                mainHandler.post {
                    currentLocation = locationName
                    refreshWeatherData()
                }
            } catch (_: Exception) {
                mainHandler.post {
                    handleError(R.string.error_general)
                }
            }
        }.start()
    }

    override fun handleLocationPermissionResult(granted: Boolean) {
        if (!granted) {
            view?.showError(view?.context?.getString(R.string.error_location_permission) ?: "")
            loadWeatherData(currentLocation)
        }
    }

    override fun loadCachedData(location: String) {
        view?.showLoading()
        val cachedWeather = cacheManager.getCurrentWeather()
        if (cachedWeather != null) {
            currentWeather = cachedWeather
            view?.updateWeatherData(cachedWeather)
            view?.updateLocationName(cachedWeather.location)
        }
        val cachedHourly = cacheManager.getHourlyForecast()
        if (cachedHourly != null) {
            hourlyForecasts = cachedHourly
            view?.updateHourlyForecast(cachedHourly)
        }
        
        view?.hideLoading()
        view?.showOfflineBanner()
    }

    private fun handleError(messageResId: Int) {
        view?.hideLoading()
        view?.showError(view?.context?.getString(messageResId) ?: "")
    }
} 
 
 
 
 