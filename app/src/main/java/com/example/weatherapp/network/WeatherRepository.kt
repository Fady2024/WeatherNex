package com.example.weatherapp.network

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.model.HourlyForecastData
import com.example.weatherapp.util.WeatherApi
import com.example.weatherapp.util.CacheManager
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository class that handles all weather-related data operations.
 * This class serves as a single source of truth for weather data,
 * abstracting the data source from the rest of the app.
 */
class WeatherRepository(private val context: Context) {
    private val cacheManager = CacheManager(context)
    private val TAG = "WeatherRepository"
    
    /**
     * Checks if the device has an active internet connection
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Fetches current weather data for the specified location.
     * If offline, returns cached data if available and not expired.
     * @param location The location to fetch weather for (city name or coordinates)
     * @return WeatherData object containing current weather information
     * @throws IOException if there's a network error and no cache is available
     */
    fun fetchWeather(location: String): WeatherData {
        return try {
            if (isNetworkAvailable()) {
                Log.d(TAG, "Fetching weather data from API for $location")
                try {
                    val jsonResponse = WeatherApi.fetchWeather(location)
                    val weatherData = WeatherApi.parseWeatherData(jsonResponse)
                    cacheManager.saveCurrentWeather(weatherData)
                    weatherData
                } catch (e: Exception) {
                    Log.e(TAG, "API call failed for location '$location': ${e.message}", e)
                    // Try to use cached data as fallback
                    cacheManager.getCurrentWeather()?.also {
                        Log.d(TAG, "Using cached weather data as fallback after API error")
                        return it
                    } ?: throw IOException("API call failed and no cached data available: ${e.message}", e)
                }
            } else {
                Log.d(TAG, "No network available, trying to get cached weather data")
                cacheManager.getCurrentWeather()?.also {
                    Log.d(TAG, "Using cached weather data for ${it.location}")
                    return it
                } ?: throw IOException("No network connection and no cached data available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data for '$location': ${e.message}", e)
            cacheManager.getCurrentWeather()?.also {
                Log.d(TAG, "Using cached weather data as fallback")
                return it
            } ?: throw e
        }
    }

    /**
     * Fetches forecast data for the specified location.
     * @param location The location to fetch forecast for (city name or coordinates)
     * @return ForecastResponse object containing forecast information
     * @throws IOException if there's a network error
     */
    fun fetchForecast(location: String): ForecastResponse {
        try {
            Log.d(TAG, "Fetching forecast data for $location")
            val jsonResponse = WeatherApi.fetchForecast(location)
            return WeatherApi.parseForecastData(jsonResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching forecast data for '$location': ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Fetches hourly forecast data for today.
     * If offline, returns cached data if available and not expired.
     * @param location The location to fetch hourly forecast for (city name or coordinates)
     * @return List of HourlyForecastData objects for today's hours
     * @throws IOException if there's a network error and no cache is available
     */
    fun fetchHourlyForecast(location: String): List<HourlyForecastData> {
        return try {
            if (isNetworkAvailable()) {
                Log.d(TAG, "Fetching hourly forecast from API for $location")
                try {
                    val jsonResponse = WeatherApi.fetchWeather(location)
                    val hourlyData = WeatherApi.parseHourlyForecastData(jsonResponse)
                    cacheManager.saveHourlyForecast(hourlyData)
                    hourlyData
                } catch (e: Exception) {
                    Log.e(TAG, "API call failed for hourly forecast: ${e.message}", e)
                    // Try to use cached data as fallback
                    cacheManager.getHourlyForecast()?.also {
                        Log.d(TAG, "Using cached hourly forecast as fallback after API error")
                        return it
                    } ?: throw IOException("API call failed and no cached hourly forecast available: ${e.message}", e)
                }
            } else {
                Log.d(TAG, "No network available, trying to get cached hourly forecast")
                cacheManager.getHourlyForecast()?.also {
                    Log.d(TAG, "Using cached hourly forecast with ${it.size} hours")
                    return it
                } ?: throw IOException("No network connection and no cached data available")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching hourly forecast for '$location': ${e.message}", e)
            cacheManager.getHourlyForecast()?.also {
                Log.d(TAG, "Using cached hourly forecast as fallback")
                return it
            } ?: throw e
        }
    }

    /**
     * Gets the city name from coordinates using reverse geocoding.
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return The city name, or "Cairo" as fallback
     */
    fun getLocationName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.locality ?: "Cairo"
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location name", e)
            "Cairo" // Fallback to default city if geocoding fails
        }
    }
} 
 
 
 
 