package com.example.weatherapp.ui.weather.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.WeatherData

class DailyForecastAdapter : RecyclerView.Adapter<DailyForecastAdapter.ViewHolder>() {
    private var items = listOf<WeatherData>()
    
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<WeatherData>) {
        items = newItems
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }
    
    override fun getItemCount() = items.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewMaxTemp: TextView = itemView.findViewById(R.id.textViewMaxTemp)
        private val textViewConditions: TextView = itemView.findViewById(R.id.textViewConditions)
        private val imageViewWeather: ImageView = itemView.findViewById(R.id.imageViewWeather)
        
        fun bind(item: WeatherData) {
            try {
                textViewDate.text = item.datetime.takeIf { it.isNotEmpty() } ?: "Unknown"
                
                try {
                    textViewMaxTemp.text = item.temperature.toInt().toString()
                } catch (e: Exception) {
                    textViewMaxTemp.text = "0"
                    android.util.Log.e("DailyForecastAdapter", "Error formatting daily temperature", e)
                }
                
                textViewConditions.text = item.conditions.takeIf { it.isNotEmpty() } ?: "Unknown"
                
                try {
                    imageViewWeather.setImageResource(getWeatherIconResource(item.conditions))
                } catch (e: Exception) {
                    imageViewWeather.setImageResource(R.drawable.icon_weather_sun_cloud)
                    android.util.Log.e("DailyForecastAdapter", "Error setting daily weather icon", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("DailyForecastAdapter", "Error binding daily forecast view", e)
            }
        }
        
        private fun getWeatherIconResource(conditions: String): Int {
            val conditionsLower = conditions.toLowerCase()
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
} 