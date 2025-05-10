package com.example.weatherapp.ui.splash

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.model.LocationData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.ui.weather.MainActivity
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.SearchHistoryManager

class SplashActivity : AppCompatActivity() {
    private val LOCATION_PERMISSION_REQUEST = 1001
    private lateinit var networkManager: NetworkManager
    private lateinit var cacheManager: CacheManager
    private lateinit var weatherRepository: WeatherRepository
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var loadingText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        networkManager = NetworkManager(this)
        cacheManager = CacheManager(this)
        weatherRepository = WeatherRepository(this)
        loadingText = findViewById(R.id.textViewLoading)
        
        if (hasLocationPermission()) {
            loadWeatherData()
        } else {
            requestLocationPermission()
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadWeatherData()
            } else {
                loadWeatherWithDefaultLocation()
            }
        }
    }
    
    private fun loadWeatherData() {
        if (networkManager.isNetworkAvailable()) {
            updateLoadingText(getString(R.string.loading_location))
            getCurrentLocation { location ->
                if (location != null) {
                    preloadWeatherData(location)
                } else {
                    loadWeatherWithDefaultLocation()
                }
            }
        } else {
            updateLoadingText(getString(R.string.loading_cached_data))
            loadCachedData()
        }
    }
    
    private fun getCurrentLocation(callback: (LocationData?) -> Unit) {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            
            if (!hasLocationPermission()) {
                callback(null)
                return
            }
            
            val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            if (lastKnown != null) {
                callback(LocationData(lastKnown.latitude, lastKnown.longitude))
            } else {
                callback(null)
            }
        } catch (e: Exception) {
            callback(null)
        }
    }
    
    private fun preloadWeatherData(location: LocationData) {
        updateLoadingText(getString(R.string.getting_location_name))
        Thread {
            try {
                val locationName = weatherRepository.getLocationName(location.latitude, location.longitude)
                handler.post {
                    updateLoadingText(getString(R.string.loading_weather_for, locationName))
                }
                val weatherData = weatherRepository.fetchWeather(locationName)
                handler.post {
                    updateLoadingText(getString(R.string.loading_forecast))
                }
                
                val hourlyForecast = weatherRepository.fetchHourlyForecast(locationName)
                cacheManager.saveCurrentWeather(weatherData)
                cacheManager.saveHourlyForecast(hourlyForecast)
                handler.post {
                    navigateToMain(locationName)
                }
            } catch (e: Exception) {
                handler.post {
                    updateLoadingText(getString(R.string.loading_error_using_cache))
                    navigateToMainWithCachedData()
                }
            }
        }.start()
    }
    
    private fun loadWeatherWithDefaultLocation() {
        val searchHistoryManager = SearchHistoryManager(this)
        val defaultLocation = searchHistoryManager.getLastSelectedLocation("Cairo")
        
        if (networkManager.isNetworkAvailable()) {
            updateLoadingText(getString(R.string.loading_weather_for, defaultLocation))
            
            Thread {
                try {
                    val weatherData = weatherRepository.fetchWeather(defaultLocation)
                    
                    handler.post {
                        updateLoadingText(getString(R.string.loading_forecast))
                    }
                    
                    val hourlyForecast = weatherRepository.fetchHourlyForecast(defaultLocation)
                    
                    cacheManager.saveCurrentWeather(weatherData)
                    cacheManager.saveHourlyForecast(hourlyForecast)
                    
                    handler.post {
                        navigateToMain(defaultLocation)
                    }
                } catch (e: Exception) {
                    handler.post {
                        updateLoadingText(getString(R.string.loading_error_using_cache))
                        navigateToMainWithCachedData()
                    }
                }
            }.start()
        } else {
            navigateToMainWithCachedData()
        }
    }
    
    private fun navigateToMainWithCachedData() {
        val cachedWeather = cacheManager.getCurrentWeather()
        if (cachedWeather != null) {
            navigateToMain(cachedWeather.location)
        } else {
            val searchHistoryManager = SearchHistoryManager(this)
            val lastLocation = searchHistoryManager.getLastSelectedLocation(getString(R.string.default_location))
            Toast.makeText(
                this,
                getString(R.string.error_using_cached_data),
                Toast.LENGTH_SHORT
            ).show()
            
            navigateToMain(lastLocation)
        }
    }
    
    private fun updateLoadingText(message: String) {
        loadingText.text = message
    }
    
    private fun navigateToMain(location: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("LOCATION", location)
        }
        startActivity(intent)
        finish()
    }
    
    private fun loadCachedData() {
        val cachedWeather = cacheManager.getCurrentWeather()
        if (cachedWeather != null) {
            navigateToMain(cachedWeather.location)
        } else {
            navigateToMain("Cairo")
        }
    }
} 
 
 
 
 
 