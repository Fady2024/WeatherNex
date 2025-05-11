package com.example.weatherapp.ui.weather.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.HourlyForecastData
import com.example.weatherapp.util.Settings

class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {
    private var items = listOf<HourlyForecastData>()
    private var textColor = Color.BLACK
    private var isDarkBackground = false
    private var sunriseTime: String = "06:00"
    private var sunsetTime: String = "18:00"
    
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<HourlyForecastData>) {
        try {
            Log.d("HourlyForecastAdapter", "Submitting ${newItems.size} items")
            
            items = if (newItems.isNotEmpty()) {
                val currentItem = newItems.first()
                val currentHour = extractHourFromTime(currentItem.time)
                val filteredList = newItems.filterIndexed { index, item -> 
                    index == 0 || extractHourFromTime(item.time) != currentHour 
                }
                
                filteredList
            } else {
                newItems
            }
            
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("HourlyForecastAdapter", "Error in submitList", e)
        }
    }
    
    /**
     * Update sunrise and sunset times for night mode detection
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateSunriseSunset(sunrise: String, sunset: String) {
        this.sunriseTime = sunrise
        this.sunsetTime = sunset
        notifyDataSetChanged()
    }
    
    /**
     * Extracts hour from time string like "14:00:00" or "14:00"
     */
    private fun extractHourFromTime(timeStr: String): Int {
        return try {
            timeStr.split(":")[0].toInt()
        } catch (e: Exception) {
            Log.e("HourlyForecastAdapter", "Error parsing time: $timeStr", e)
            -1
        }
    }
    
    /**
     * Updates the text color for all items in the adapter based on background brightness.
     *
     * @param color The color to set for text elements
     * @param isDark Whether the background is dark or not (for adjusting text shadow if needed)
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateTextColor(color: Int, isDark: Boolean) {
        this.textColor = color
        this.isDarkBackground = isDark
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hourly_main, parent, false)
            return ViewHolder(view)
        } catch (e: Exception) {
            Log.e("HourlyForecastAdapter", "Error in onCreateViewHolder", e)
            val emptyView = View(parent.context)
            return ViewHolder(emptyView)
        }
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (position < items.size) {
                holder.bind(items[position], position)
            }
        } catch (e: Exception) {
            Log.e("HourlyForecastAdapter", "Error in onBindViewHolder at position $position", e)
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewHourlyTime: TextView? = itemView.findViewById(R.id.textViewHourlyTime)
        private val textViewHourlyTemp: TextView? = itemView.findViewById(R.id.textViewHourlyTemp)
        private val imageViewHourlyWeather: ImageView? = itemView.findViewById(R.id.imageViewHourlyWeather)
        
        @SuppressLint("SetTextI18n", "UseKtx")
        fun bind(item: HourlyForecastData, position: Int) {
            try {
                if (position == 0) {
                    textViewHourlyTime?.text = "Now"
                } else {
                    textViewHourlyTime?.text = formatTimeBasedOnSettings(item.time)
                }
                
                try {
                    val temperatureUnit = Settings.temperatureUnit
                    val convertedTemp = temperatureUnit.convert(item.temperature)
                    textViewHourlyTemp?.text = "${convertedTemp.toInt()}°"
                } catch (e: Exception) {
                    textViewHourlyTemp?.text = "0°"
                    Log.e("HourlyForecastAdapter", "Error formatting hourly temperature", e)
                }
                
                try {
                    val isNight = isHourAtNight(item.time)
                    imageViewHourlyWeather?.setImageResource(getWeatherIconResource(item.conditions, isNight))
                    imageViewHourlyWeather?.alpha = 0.9f
                    imageViewHourlyWeather?.animate()?.alpha(1.0f)?.setDuration(500)?.start()
                } catch (e: Exception) {
                    imageViewHourlyWeather?.setImageResource(R.drawable.icon_weather_sun_cloud)
                    Log.e("HourlyForecastAdapter", "Error setting hourly weather icon", e)
                }
                
                textViewHourlyTime?.setTextColor(textColor)
                textViewHourlyTemp?.setTextColor(textColor)
                if (!isDarkBackground) {
                    val shadowColor = Color.parseColor("#40000000")
                    textViewHourlyTime?.setShadowLayer(1.5f, 0.5f, 0.5f, shadowColor)
                    textViewHourlyTemp?.setShadowLayer(1.5f, 0.5f, 0.5f, shadowColor)
                } else {
                    textViewHourlyTime?.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                    textViewHourlyTemp?.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                }
                
            } catch (e: Exception) {
                Log.e("HourlyForecastAdapter", "Error binding ViewHolder at position $position", e)
            }
        }
        
        /**
         * Determines if the specified hour is during nighttime
         */
        private fun isHourAtNight(timeStr: String): Boolean {
            try {
                val hour = extractHourFromTime(timeStr)
                val sunriseHour = extractHourFromTime(sunriseTime)
                val sunsetHour = extractHourFromTime(sunsetTime)
                
                return hour < sunriseHour || hour >= sunsetHour
            } catch (e: Exception) {
                Log.e("HourlyForecastAdapter", "Error checking night time", e)
                return false
            }
        }
        
        private fun formatTimeBasedOnSettings(time: String): String {
            try {
                val parts = time.split(":")
                if (parts.isEmpty()) return time
                
                val hour = parts[0].toInt()
                
                return if (Settings.use24HourFormat) {
                    "${hour}:00"
                } else {
                    val hour12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val amPm = if (hour >= 12) "PM" else "AM"
                    "$hour12 $amPm"
                }
            } catch (e: Exception) {
                Log.e("HourlyForecastAdapter", "Error formatting time: $time", e)
                return time
            }
        }
        
        @SuppressLint("DefaultLocale")
        private fun getWeatherIconResource(conditions: String, isNight: Boolean): Int {
            val conditionsLower = conditions.lowercase()
            
            return when {
                isNight -> {
                    when {
                        conditionsLower.contains("rain") ->
                            R.drawable.icon_weather_moon_cloud_rain
                        conditionsLower.contains("snow") || conditionsLower.contains("flurries") ||
                        conditionsLower.contains("ice") || conditionsLower.contains("sleet") ->
                            R.drawable.icon_weather_snow_cloud
                        conditionsLower.contains("cloud") || conditionsLower.contains("overcast") ->
                            R.drawable.icon_weather_moon_cloud
                        conditionsLower.contains("clear") || conditionsLower.contains("sunny") ->
                            R.drawable.icon_weather_moon
                        conditionsLower.contains("thunder") || conditionsLower.contains("storm") ->
                            R.drawable.icon_weather_thunderstorm_cloud
                        conditionsLower.contains("fog") || conditionsLower.contains("mist") ||
                        conditionsLower.contains("haze") ->
                            R.drawable.icon_weather_cloud_fog
                        else -> R.drawable.icon_weather_moon_cloud
                    }
                }
                
                else -> {
                    when {
                        conditionsLower.contains("rain") && conditionsLower.contains("thunder") ->
                            R.drawable.icon_weather_thunderstorm_cloud
                        conditionsLower.contains("rain") && (conditionsLower.contains("partly cloudy") || conditionsLower.contains("partly sunny")) ->
                            R.drawable.icon_weather_sun_rain_cloud
                        conditionsLower.contains("rain") ->
                            R.drawable.icon_weather_rain_cloud
                        conditionsLower.contains("snow") || conditionsLower.contains("flurries") ||
                        conditionsLower.contains("ice") || conditionsLower.contains("sleet") -> 
                            R.drawable.icon_weather_snow_cloud
                        conditionsLower.contains("fog") || conditionsLower.contains("mist") ||
                        conditionsLower.contains("haze") -> 
                            R.drawable.icon_weather_cloud_fog
                        conditionsLower.contains("partly cloudy") || conditionsLower.contains("partly sunny") ->
                            R.drawable.icon_weather_sun_cloud
                        conditionsLower.contains("cloud") || conditionsLower.contains("overcast") ->
                            R.drawable.icon_weather_cloud
                        conditionsLower.contains("sunny") || conditionsLower.contains("clear") ->
                            R.drawable.icon_weather_sun
                        conditionsLower.contains("thunder") || conditionsLower.contains("storm") ->
                            R.drawable.icon_weather_thunderstorm_cloud
                        else -> R.drawable.icon_weather_sun_cloud
                    }
                }
            }
        }
    }
} 
 
 
 
 
 