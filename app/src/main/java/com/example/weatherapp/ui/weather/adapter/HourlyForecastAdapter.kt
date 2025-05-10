package com.example.weatherapp.ui.weather.adapter

import android.annotation.SuppressLint
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
    
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<HourlyForecastData>) {
        try {
            Log.d("HourlyForecastAdapter", "Submitting ${newItems.size} items")
            items = newItems
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("HourlyForecastAdapter", "Error in submitList", e)
        }
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
                holder.bind(items[position])
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
        
        @SuppressLint("SetTextI18n")
        fun bind(item: HourlyForecastData) {
            try {
                textViewHourlyTime?.text = formatTimeBasedOnSettings(item.time)
                try {
                    val temperatureUnit = Settings.temperatureUnit
                    val convertedTemp = temperatureUnit.convert(item.temperature)
                    textViewHourlyTemp?.text = "${convertedTemp.toInt()}°"
                } catch (e: Exception) {
                    textViewHourlyTemp?.text = "0°"
                    Log.e("HourlyForecastAdapter", "Error formatting hourly temperature", e)
                }
                try {
                    imageViewHourlyWeather?.setImageResource(getWeatherIconResource(item.conditions))
                    imageViewHourlyWeather?.alpha = 0.9f
                    imageViewHourlyWeather?.animate()?.alpha(1.0f)?.setDuration(500)?.start()
                } catch (e: Exception) {
                    imageViewHourlyWeather?.setImageResource(R.drawable.icon_weather_sun_cloud)
                    Log.e("HourlyForecastAdapter", "Error setting hourly weather icon", e)
                }
            } catch (e: Exception) {
                Log.e("HourlyForecastAdapter", "Error binding hourly forecast item", e)
            }
        }
        
        @SuppressLint("DefaultLocale")
        private fun formatTimeBasedOnSettings(time: String): String {
            try {
                val parts = time.split(":")
                if (parts.size < 2) return time

                val hour = parts[0].toInt()
                val minute = parts[1].padStart(2, '0')

                return if (Settings.use24HourFormat) {
                    String.format("%02d:%s", hour, minute)
                } else {
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    String.format("%d:%s %s", hour12, minute, amPm)
                }
            } catch (e: Exception) {
                Log.e("HourlyForecastAdapter", "Error formatting time", e)
                return time
            }
        }
    }
    
    private fun getWeatherIconResource(conditions: String): Int {
        val conditionsLower = conditions.lowercase()
        return when {
            conditionsLower.contains("rain") && conditionsLower.contains("thunder") ->
                R.drawable.icon_weather_thunderstorm_cloud
            conditionsLower.contains("rain") && conditionsLower.contains("partly cloudy") -> 
                R.drawable.icon_weather_sun_rain_cloud
            conditionsLower.contains("rain") -> 
                R.drawable.icon_weather_rain_cloud
            conditionsLower.contains("snow") || conditionsLower.contains("flurries") || 
            conditionsLower.contains("ice") || conditionsLower.contains("sleet") -> 
                R.drawable.icon_weather_snow_cloud
            conditionsLower.contains("thunder") || conditionsLower.contains("storm") ->
                R.drawable.icon_weather_thunderstorm_cloud
            conditionsLower.contains("fog") || conditionsLower.contains("mist") ||
            conditionsLower.contains("haze") -> 
                R.drawable.icon_weather_cloud_fog
            conditionsLower.contains("partly cloudy") ->
                R.drawable.icon_weather_sun_cloud
            conditionsLower.contains("cloudy") || conditionsLower.contains("overcast") ->
                R.drawable.icon_weather_cloud
            conditionsLower.contains("clear") || conditionsLower.contains("sunny") ->
                R.drawable.icon_weather_sun
            else -> R.drawable.icon_weather_sun_cloud
        }
    }
} 
 
 
 
 
 