package com.example.weatherapp.ui.forecast.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.util.Settings
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ForecastAdapter : ListAdapter<ForecastData, ForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    @SuppressLint("NewApi")
    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.textViewDate)
        private val conditionsText: TextView = itemView.findViewById(R.id.textViewConditions)
        private val descriptionText: TextView = itemView.findViewById(R.id.textViewDescription)
        private val humidityText: TextView = itemView.findViewById(R.id.textViewHumidity)
        private val windText: TextView = itemView.findViewById(R.id.textViewWind)
        private val precipitationText: TextView = itemView.findViewById(R.id.textViewPrecipitation)
        private val snowText: TextView = itemView.findViewById(R.id.textViewSnow)
        private val maxTempText: TextView = itemView.findViewById(R.id.textViewMaxTemp)
        private val minTempText: TextView = itemView.findViewById(R.id.textViewMinTemp)
        private val maxTempUnitText: TextView = itemView.findViewById(R.id.textMaxTempUnit)
        private val minTempUnitText: TextView = itemView.findViewById(R.id.textMinTempUnit)
        private val weatherIcon: ImageView = itemView.findViewById(R.id.imageViewWeather)

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(forecast: ForecastData) {
            try {
                val date = LocalDate.parse(forecast.date)
                dateText.text = date.format(DateTimeFormatter.ofPattern("EEEE"))
                conditionsText.text = forecast.conditions
                descriptionText.text = forecast.description
                humidityText.text = "${forecast.humidity}%"
                val windSpeed = Settings.windSpeedUnit.convert(forecast.windSpeed)
                windText.text = Settings.windSpeedUnit.format(windSpeed)
                val precipChance = forecast.precipitation
                precipitationText.text = "${precipChance}%"
                val snowAmount = forecast.snow
                snowText.text = "${snowAmount}%"
                val highTemp = Settings.temperatureUnit.convert(forecast.highTemp).toInt()
                val lowTemp = Settings.temperatureUnit.convert(forecast.lowTemp).toInt()
                maxTempText.text = "${highTemp}"
                minTempText.text = "${lowTemp}"
                val unitSymbol = if (Settings.temperatureUnit == Settings.TemperatureUnit.CELSIUS) "°C" else "°F"
                maxTempUnitText.text = unitSymbol
                minTempUnitText.text = unitSymbol
                
                weatherIcon.setImageResource(getWeatherIconResource(forecast.conditions))
                
            } catch (e: Exception) {
                android.util.Log.e("ForecastAdapter", "Error binding forecast", e)
            }
        }

        @SuppressLint("DefaultLocale")
        private fun getWeatherIconResource(conditions: String): Int {
            val conditionsLower = conditions.lowercase()
            return when {
                conditionsLower.contains("rain") && conditionsLower.contains("thunder") -> 
                    R.drawable.icon_weather_thunderstorm_cloud
                conditionsLower.contains("rain") && conditionsLower.contains("partly cloudy") -> 
                    R.drawable.icon_weather_sun_rain_cloud
                conditionsLower.contains("rain") -> 
                    R.drawable.icon_weather_rain_cloud
                conditionsLower.contains("snow") && conditionsLower.contains("partly cloudy") ->
                    R.drawable.icon_weather_snow_cloud
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

    class ForecastDiffCallback : DiffUtil.ItemCallback<ForecastData>() {
        override fun areItemsTheSame(oldItem: ForecastData, newItem: ForecastData): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: ForecastData, newItem: ForecastData): Boolean {
            return oldItem == newItem
        }
    }
} 
 
 
 
 