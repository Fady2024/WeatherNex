package com.example.weatherapp.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.model.HourlyForecastData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CacheManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "WeatherAppCache"
        private const val KEY_CURRENT_WEATHER = "current_weather"
        private const val KEY_FORECAST = "forecast"
        private const val KEY_HOURLY_FORECAST = "hourly_forecast"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val CACHE_DURATION = 30 * 60 * 1000
        private const val TAG = "CacheManager"
        
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCurrentWeather(weatherData: WeatherData) {
        try {
            val jsonObject = JSONObject().apply {
                put("temperature", weatherData.temperature)
                put("conditions", weatherData.conditions)
                put("humidity", weatherData.humidity)
                put("windSpeed", weatherData.windSpeed)
                put("feelsLike", weatherData.feelsLike)
                put("uvIndex", weatherData.uvIndex)
                put("pressure", weatherData.pressure)
                put("visibility", weatherData.visibility)
                put("cloudCover", weatherData.cloudCover)
                put("datetime", weatherData.datetime)
                put("location", weatherData.location)
                put("minTemperature", weatherData.minTemperature)
                put("maxTemperature", weatherData.maxTemperature)
                put("description", weatherData.description)
                put("snow", weatherData.snow)
                put("precipProbability", weatherData.precipProbability)
                put("precipType", weatherData.precipType)
                put("sunrise", weatherData.sunrise)
                put("sunset", weatherData.sunset)
                put("dewPoint", weatherData.dewPoint)
                put("precipAmount", weatherData.precipAmount)
                put("precipCover", weatherData.precipCover)
                put("snowDepth", weatherData.snowDepth)
                put("windGust", weatherData.windGust)
                put("windDirection", weatherData.windDirection)
                put("feelsLikeMax", weatherData.feelsLikeMax)
                put("feelsLikeMin", weatherData.feelsLikeMin)
                put("solarRadiation", weatherData.solarRadiation)
                put("solarEnergy", weatherData.solarEnergy)
                put("moonPhase", weatherData.moonPhase)
                put("icon", weatherData.icon)
                put("stations", weatherData.stations)
                put("source", weatherData.source)
            }
            
            val timestamp = System.currentTimeMillis()
            prefs.edit()
                .putString(KEY_CURRENT_WEATHER, jsonObject.toString())
                .putLong(KEY_LAST_UPDATE, timestamp)
                .apply()
            
            Log.d(TAG, "Weather data cached at ${dateFormat.format(Date(timestamp))}")
        } catch (e: JSONException) {
            Log.e(TAG, "Error saving weather to cache", e)
        }
    }

    fun getCurrentWeather(): WeatherData? {
        val json = prefs.getString(KEY_CURRENT_WEATHER, null) ?: return null
        val timestamp = prefs.getLong(KEY_LAST_UPDATE, 0)
        
        if (isCacheExpired(timestamp)) {
            Log.d(TAG, "Cache expired, last updated at ${dateFormat.format(Date(timestamp))}")
            return null
        }
        
        return try {
            val jsonObject = JSONObject(json)
            WeatherData(
                temperature = jsonObject.getDouble("temperature"),
                conditions = jsonObject.getString("conditions"),
                humidity = jsonObject.getInt("humidity"),
                windSpeed = jsonObject.getDouble("windSpeed"),
                feelsLike = jsonObject.getDouble("feelsLike"),
                uvIndex = jsonObject.getInt("uvIndex"),
                pressure = jsonObject.getDouble("pressure"),
                visibility = jsonObject.getDouble("visibility"),
                cloudCover = jsonObject.getInt("cloudCover"),
                datetime = jsonObject.getString("datetime"),
                location = jsonObject.getString("location"),
                minTemperature = jsonObject.getDouble("minTemperature"),
                maxTemperature = jsonObject.getDouble("maxTemperature"),
                description = jsonObject.getString("description"),
                snow = jsonObject.getInt("snow"),
                precipProbability = jsonObject.getInt("precipProbability"),
                precipType = jsonObject.getString("precipType"),
                sunrise = jsonObject.getString("sunrise"),
                sunset = jsonObject.getString("sunset"),
                dewPoint = jsonObject.getDouble("dewPoint"),
                precipAmount = jsonObject.getDouble("precipAmount"),
                precipCover = jsonObject.getInt("precipCover"),
                snowDepth = jsonObject.getDouble("snowDepth"),
                windGust = jsonObject.getDouble("windGust"),
                windDirection = jsonObject.getInt("windDirection"),
                feelsLikeMax = jsonObject.getDouble("feelsLikeMax"),
                feelsLikeMin = jsonObject.getDouble("feelsLikeMin"),
                solarRadiation = jsonObject.getDouble("solarRadiation"),
                solarEnergy = jsonObject.getDouble("solarEnergy"),
                moonPhase = jsonObject.getDouble("moonPhase"),
                icon = jsonObject.getString("icon"),
                stations = jsonObject.getString("stations"),
                source = jsonObject.getString("source")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving weather from cache", e)
            null
        }
    }

    fun saveForecast(forecast: ForecastResponse) {
        try {
            val jsonObject = JSONObject().apply {
                put("location", forecast.location)
                
                val forecastsArray = JSONArray()
                forecast.forecasts.forEach { forecastData ->
                    val forecastObject = JSONObject().apply {
                        put("date", forecastData.date)
                        put("highTemp", forecastData.highTemp)
                        put("lowTemp", forecastData.lowTemp)
                        put("conditions", forecastData.conditions)
                        put("humidity", forecastData.humidity)
                        put("windSpeed", forecastData.windSpeed)
                        put("windDirection", forecastData.windDirection)
                        put("precipitation", forecastData.precipitation)
                    }
                    forecastsArray.put(forecastObject)
                }
                
                put("forecasts", forecastsArray)
            }
            
            val timestamp = Date().time
            prefs.edit()
                .putString(KEY_FORECAST, jsonObject.toString())
                .putLong(KEY_LAST_UPDATE, timestamp)
                .apply()
        } catch (e: JSONException) {
            Log.e(TAG, "Error saving forecast to cache", e)
        }
    }

    fun getForecast(): CacheResult<ForecastResponse> {
        val json = prefs.getString(KEY_FORECAST, null) ?: return CacheResult.NotFound()
        val timestamp = prefs.getLong(KEY_LAST_UPDATE, 0)
        
        if (isCacheExpired(timestamp)) {
            return CacheResult.Expired(getFormattedTimestamp(timestamp))
        }
        
        return try {
            val jsonObject = JSONObject(json)
            val location = jsonObject.getString("location")
            val forecastsArray = jsonObject.getJSONArray("forecasts")
            
            val forecasts = mutableListOf<ForecastData>()
            for (i in 0 until forecastsArray.length()) {
                val forecastObject = forecastsArray.getJSONObject(i)
                forecasts.add(
                    ForecastData(
                        date = forecastObject.getString("date"),
                        highTemp = forecastObject.getDouble("highTemp"),
                        lowTemp = forecastObject.getDouble("lowTemp"),
                        conditions = forecastObject.getString("conditions"),
                        humidity = forecastObject.getInt("humidity"),
                        windSpeed = forecastObject.getDouble("windSpeed"),
                        windDirection = forecastObject.getInt("windDirection"),
                        precipitation = forecastObject.getInt("precipitation")
                    )
                )
            }
            
            val data = ForecastResponse(location, forecasts)
            CacheResult.Success(data, getFormattedTimestamp(timestamp))
        } catch (e: Exception) {
            CacheResult.Error(e)
        }
    }

    fun saveHourlyForecast(hourlyForecasts: List<HourlyForecastData>) {
        try {
            val jsonArray = JSONArray()
            for (forecast in hourlyForecasts) {
                val jsonObject = JSONObject().apply {
                    put("time", forecast.time)
                    put("temperature", forecast.temperature)
                    put("conditions", forecast.conditions)
                    put("precipProbability", forecast.precipProbability)
                    put("humidity", forecast.humidity)
                    put("windSpeed", forecast.windSpeed)
                    put("feelsLike", forecast.feelsLike)
                }
                jsonArray.put(jsonObject)
            }
            
            val timestamp = System.currentTimeMillis()
            prefs.edit()
                .putString(KEY_HOURLY_FORECAST, jsonArray.toString())
                .putLong(KEY_LAST_UPDATE, timestamp)
                .apply()
            
            Log.d(TAG, "Saved ${hourlyForecasts.size} hourly forecasts to cache")
        } catch (e: JSONException) {
            Log.e(TAG, "Error saving hourly forecast to cache", e)
        }
    }

    fun getHourlyForecast(): List<HourlyForecastData>? {
        val json = prefs.getString(KEY_HOURLY_FORECAST, null) ?: return null
        val timestamp = prefs.getLong(KEY_LAST_UPDATE, 0)
        
        if (isCacheExpired(timestamp)) {
            Log.d(TAG, "Hourly forecast cache expired, last updated at ${dateFormat.format(Date(timestamp))}")
            return null
        }
        
        return try {
            val forecasts = mutableListOf<HourlyForecastData>()
            val jsonArray = JSONArray(json)
            
            for (i in 0 until jsonArray.length()) {
                val hourObject = jsonArray.getJSONObject(i)
                forecasts.add(
                    HourlyForecastData(
                        time = hourObject.getString("time"),
                        temperature = hourObject.getDouble("temperature"),
                        conditions = hourObject.getString("conditions"),
                        precipProbability = hourObject.getInt("precipProbability"),
                        humidity = hourObject.getInt("humidity"),
                        windSpeed = hourObject.getDouble("windSpeed"),
                        feelsLike = hourObject.getDouble("feelsLike")
                    )
                )
            }
            
            forecasts
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving hourly forecast from cache", e)
            null
        }
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        val isExpired = age > CACHE_DURATION
        if (isExpired) {
            Log.d(TAG, "Cache expired. Age: ${age/1000} seconds")
        }
        return isExpired
    }

    private fun getFormattedTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun clearCache() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Cache cleared")
    }

    sealed class CacheResult<out T> {
        data class Success<T>(val data: T, val timestamp: String) : CacheResult<T>()
        data class Expired(val timestamp: String) : CacheResult<Nothing>()
        class NotFound : CacheResult<Nothing>()
        data class Error(val exception: Exception) : CacheResult<Nothing>()
    }
} 
 
 
 
 
 