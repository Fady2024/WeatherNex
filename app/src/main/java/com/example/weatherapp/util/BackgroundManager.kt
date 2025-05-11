package com.example.weatherapp.util

import android.app.Activity
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData
import java.util.Calendar
import android.graphics.Color
import androidx.core.graphics.ColorUtils

/**
 * Utility class to manage dynamic backgrounds based on weather conditions and time of day
 */
@Suppress("DEPRECATION")
class BackgroundManager {
    companion object {
        private const val TAG = "BackgroundManager"
        
        /**
         * Determines if a background color is dark
         */
        fun isBackgroundDark(activity: Activity, backgroundResId: Int): Boolean {
            val startColor = when (backgroundResId) {
                R.drawable.background_day_clear -> ContextCompat.getColor(activity, R.color.status_bar_day_clear)
                R.drawable.background_night_clear -> ContextCompat.getColor(activity, R.color.status_bar_night_clear)
                R.drawable.background_day_cloudy -> ContextCompat.getColor(activity, R.color.status_bar_day_cloudy)
                R.drawable.background_night_cloudy -> ContextCompat.getColor(activity, R.color.status_bar_night_cloudy)
                R.drawable.background_day_rainy -> ContextCompat.getColor(activity, R.color.status_bar_day_rainy)
                R.drawable.background_night_rainy -> ContextCompat.getColor(activity, R.color.status_bar_night_rainy)
                R.drawable.background_day_snowy -> ContextCompat.getColor(activity, R.color.status_bar_day_snowy)
                R.drawable.background_night_snowy -> ContextCompat.getColor(activity, R.color.status_bar_night_snowy)
                R.drawable.background_day_thunderstorm -> ContextCompat.getColor(activity, R.color.status_bar_day_thunderstorm)
                R.drawable.background_night_thunderstorm -> ContextCompat.getColor(activity, R.color.status_bar_night_thunderstorm)
                R.drawable.background_day_foggy -> ContextCompat.getColor(activity, R.color.status_bar_day_foggy)
                R.drawable.background_night_foggy -> ContextCompat.getColor(activity, R.color.status_bar_night_foggy)
                else -> Color.WHITE
            }
            
            val luminance = ColorUtils.calculateLuminance(startColor)
            return luminance < 0.5
        }
        
        /**
         * Determines the appropriate background resource ID based on weather conditions and time of day
         * without applying it to any view. This is useful for checking background characteristics.
         * 
         * @param weatherData The current weather data
         * @param isNight Whether it is night time
         * @return The resource ID of the appropriate background
         */
        fun getCurrentBackgroundResourceId(weatherData: WeatherData, isNight: Boolean): Int {
            val conditions = weatherData.conditions.lowercase()
                
            return when {
                conditions.contains("thunder") || conditions.contains("storm") ||
                conditions.contains("lightning") -> {
                    if (isNight) R.drawable.background_night_thunderstorm else R.drawable.background_day_thunderstorm
                }
                
                conditions.contains("rain") || conditions.contains("drizzle") ||
                conditions.contains("shower") || weatherData.precipProbability > 80 -> {
                    if (isNight) R.drawable.background_night_rainy else R.drawable.background_day_rainy
                }
                
                conditions.contains("fog") || conditions.contains("mist") ||
                conditions.contains("haze") -> {
                    if (isNight) R.drawable.background_night_foggy else R.drawable.background_day_foggy
                }
                
                conditions.contains("snow") || conditions.contains("sleet") ||
                conditions.contains("ice") || conditions.contains("flurries") -> {
                    if (isNight) R.drawable.background_night_snowy else R.drawable.background_day_snowy
                }
                
                conditions.contains("cloud") || conditions.contains("overcast") ||
                weatherData.cloudCover > 60 -> {
                    if (isNight) R.drawable.background_night_cloudy else R.drawable.background_day_cloudy
                }
                
                else -> {
                    if (isNight) R.drawable.background_night_clear else R.drawable.background_day_clear
                }
            }
        }
        
        /**
         * Sets the appropriate background for the activity based on weather conditions and time of day
         * 
         * @param activity The activity to set the background for
         * @param weatherData The current weather data
         * @param rootView The root view to apply the background to
         */
        fun setDynamicBackground(activity: Activity, weatherData: WeatherData, rootView: View) {
            try {
                val isNight = isNightTime(weatherData.sunrise, weatherData.sunset)
                val backgroundResId = getCurrentBackgroundResourceId(weatherData, isNight)
                
                val background = ContextCompat.getDrawable(activity, backgroundResId)
                
                if (rootView is ConstraintLayout) {
                    rootView.background = background
                } else {
                    rootView.setBackgroundResource(backgroundResId)
                }
                
                updateStatusBarColor(activity, background)
                
                Log.d(TAG, "Set background to ${getBackgroundName(backgroundResId)}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting dynamic background", e)
            }
        }
        
        /**
         * Sets the appropriate background for the activity based on a forecast
         */
        fun setForecastBackground(activity: Activity, conditions: String, isNight: Boolean, rootView: View) {
            try {
                val conditionsLower = conditions.lowercase()
                
                val backgroundResId = when {
                    conditionsLower.contains("thunder") || conditionsLower.contains("storm") ||
                    conditionsLower.contains("lightning") -> {
                        if (isNight) R.drawable.background_night_thunderstorm else R.drawable.background_day_thunderstorm
                    }
                    
                    conditionsLower.contains("rain") || conditionsLower.contains("drizzle") ||
                    conditionsLower.contains("shower") -> {
                        if (isNight) R.drawable.background_night_rainy else R.drawable.background_day_rainy
                    }
                    
                    conditionsLower.contains("fog") || conditionsLower.contains("mist") ||
                    conditionsLower.contains("haze") -> {
                        if (isNight) R.drawable.background_night_foggy else R.drawable.background_day_foggy
                    }
                    
                    conditionsLower.contains("snow") || conditionsLower.contains("sleet") ||
                    conditionsLower.contains("ice") || conditionsLower.contains("flurries") -> {
                        if (isNight) R.drawable.background_night_snowy else R.drawable.background_day_snowy
                    }
                    
                    conditionsLower.contains("cloud") || conditionsLower.contains("overcast") -> {
                        if (isNight) R.drawable.background_night_cloudy else R.drawable.background_day_cloudy
                    }
                    
                    else -> {
                        if (isNight) R.drawable.background_night_clear else R.drawable.background_day_clear
                    }
                }
                
                val background = ContextCompat.getDrawable(activity, backgroundResId)
                
                if (rootView is ConstraintLayout) {
                    rootView.background = background
                } else {
                    rootView.setBackgroundResource(backgroundResId)
                }
                updateStatusBarColor(activity, background)
                
                Log.d(TAG, "Set forecast background to ${getBackgroundName(backgroundResId)}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting forecast background", e)
            }
        }
        
        /**
         * Sets a default background for loading screens
         */
        fun setDefaultBackground(activity: Activity, rootView: View) {
            try {
                val isNight = isNightTimeNow()
                val backgroundResId = if (isNight) R.drawable.background_night_clear else R.drawable.background_day_clear
                
                val background = ContextCompat.getDrawable(activity, backgroundResId)
                
                if (rootView is ConstraintLayout) {
                    rootView.background = background
                } else {
                    rootView.setBackgroundResource(backgroundResId)
                }
                
                updateStatusBarColor(activity, background)
                
                Log.d(TAG, "Set default background to ${getBackgroundName(backgroundResId)}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting default background", e)
            }
        }
        
        /**
         * Update status bar color to match the top of the gradient background
         */
        private fun updateStatusBarColor(activity: Activity, background: Drawable?) {
            try {
                val window = activity.window
                when (background?.constantState?.newDrawable()?.constantState) {
                    ContextCompat.getDrawable(activity, R.drawable.background_day_clear)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_clear)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_clear)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_clear)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_day_cloudy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_cloudy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_cloudy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_cloudy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_day_rainy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_rainy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_rainy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_rainy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_day_snowy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_snowy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_snowy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_snowy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_day_thunderstorm)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_thunderstorm)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_thunderstorm)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_thunderstorm)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_day_foggy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_day_foggy)
                    }
                    ContextCompat.getDrawable(activity, R.drawable.background_night_foggy)?.constantState -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.status_bar_night_foggy)
                    }
                    else -> {
                        window.statusBarColor = ContextCompat.getColor(activity, R.color.primary_dark)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating status bar color", e)
            }
        }
        
        /**
         * Check if it's currently night time based on sunrise and sunset
         */
        private fun isNightTime(sunrise: String, sunset: String): Boolean {
            try {
                val currentTime = Calendar.getInstance()
                val sunriseCalendar = Calendar.getInstance()
                val sunriseParts = sunrise.split(":")
                if (sunriseParts.size >= 2) {
                    sunriseCalendar.set(Calendar.HOUR_OF_DAY, sunriseParts[0].toInt())
                    sunriseCalendar.set(Calendar.MINUTE, sunriseParts[1].toInt())
                } else {
                    sunriseCalendar.set(Calendar.HOUR_OF_DAY, 6)
                    sunriseCalendar.set(Calendar.MINUTE, 0)
                }
                
                val sunsetCalendar = Calendar.getInstance()
                val sunsetParts = sunset.split(":")
                if (sunsetParts.size >= 2) {
                    sunsetCalendar.set(Calendar.HOUR_OF_DAY, sunsetParts[0].toInt())
                    sunsetCalendar.set(Calendar.MINUTE, sunsetParts[1].toInt())
                } else {
                    sunsetCalendar.set(Calendar.HOUR_OF_DAY, 18)
                    sunsetCalendar.set(Calendar.MINUTE, 0)
                }
                
                return currentTime.before(sunriseCalendar) || currentTime.after(sunsetCalendar)
            } catch (e: Exception) {
                Log.e(TAG, "Error determining night time", e)
                return false
            }
        }
        
        /**
         * Check if it's currently night time based on hour of day
         */
        private fun isNightTimeNow(): Boolean {
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return currentHour < 6 || currentHour >= 19
        }
        
        /**
         * Get a human-readable name for a background resource ID for logging
         */
        private fun getBackgroundName(backgroundResId: Int): String {
            return when (backgroundResId) {
                R.drawable.background_day_clear -> "Day Clear"
                R.drawable.background_night_clear -> "Night Clear"
                R.drawable.background_day_cloudy -> "Day Cloudy"
                R.drawable.background_night_cloudy -> "Night Cloudy"
                R.drawable.background_day_rainy -> "Day Rainy"
                R.drawable.background_night_rainy -> "Night Rainy"
                R.drawable.background_day_snowy -> "Day Snowy"
                R.drawable.background_night_snowy -> "Night Snowy"
                R.drawable.background_day_thunderstorm -> "Day Thunderstorm"
                R.drawable.background_night_thunderstorm -> "Night Thunderstorm"
                R.drawable.background_day_foggy -> "Day Foggy"
                R.drawable.background_night_foggy -> "Night Foggy"
                else -> "Unknown"
            }
        }
    }
} 