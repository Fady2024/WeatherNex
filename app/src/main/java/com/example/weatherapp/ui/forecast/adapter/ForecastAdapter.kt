package com.example.weatherapp.ui.forecast.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.util.BackgroundManager
import com.example.weatherapp.util.Settings
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.text.DecimalFormat

class ForecastAdapter : ListAdapter<ForecastData, ForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {
    private var lastTemperatureUnit: Settings.TemperatureUnit = Settings.temperatureUnit
    private var lastWindSpeedUnit: Settings.WindSpeedUnit = Settings.windSpeedUnit
    private var lastPressureUnit: Settings.PressureUnit = Settings.pressureUnit
    private var lastLengthUnit: Settings.LengthUnit = Settings.lengthUnit
    private var lastTimeFormat: Boolean = Settings.use24HourFormat
    
    private val forecastItems = mutableListOf<ForecastData>()

    /**
     * Updates the forecast data
     */
    fun updateForecast(items: List<ForecastData>) {
        forecastItems.clear()
        forecastItems.addAll(items)
        submitList(items.toList())
    }
    
    /**
     * Checks if units have changed since the last refresh
     * @return true if any units changed and display needs to be refreshed
     */
    @SuppressLint("NotifyDataSetChanged")
    fun refreshUnitsDisplay(): Boolean {
        var hasChanges = false
        
        if (lastTemperatureUnit != Settings.temperatureUnit) {
            lastTemperatureUnit = Settings.temperatureUnit
            hasChanges = true
            android.util.Log.d("ForecastAdapter", "Temperature unit changed to: ${Settings.temperatureUnit}")
        }
        
        if (lastWindSpeedUnit != Settings.windSpeedUnit) {
            lastWindSpeedUnit = Settings.windSpeedUnit
            hasChanges = true
            android.util.Log.d("ForecastAdapter", "Wind speed unit changed to: ${Settings.windSpeedUnit}")
        }
        
        if (lastPressureUnit != Settings.pressureUnit) {
            lastPressureUnit = Settings.pressureUnit
            hasChanges = true
            android.util.Log.d("ForecastAdapter", "Pressure unit changed to: ${Settings.pressureUnit}")
        }
        
        if (lastLengthUnit != Settings.lengthUnit) {
            lastLengthUnit = Settings.lengthUnit
            hasChanges = true
            android.util.Log.d("ForecastAdapter", "Length unit changed to: ${Settings.lengthUnit}")
        }
        
        if (lastTimeFormat != Settings.use24HourFormat) {
            lastTimeFormat = Settings.use24HourFormat
            hasChanges = true
            android.util.Log.d("ForecastAdapter", "Time format changed to 24-hour: ${Settings.use24HourFormat}")
        }
        
        if (hasChanges) {
            android.util.Log.d("ForecastAdapter", "Units changed, refreshing display")
            notifyDataSetChanged()
        }
        
        return hasChanges
    }

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
        private val headerLayout: ConstraintLayout = itemView.findViewById(R.id.forecastHeaderLayout)

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(forecast: ForecastData) {
            try {
                android.util.Log.d("ForecastAdapter", "Binding forecast for date: ${forecast.date}")
                android.util.Log.d("ForecastAdapter", "Current settings - Temp: ${Settings.temperatureUnit}, Wind: ${Settings.windSpeedUnit}")
                val date = LocalDate.parse(forecast.date)
                dateText.text = date.format(DateTimeFormatter.ofPattern("EEEE"))
                conditionsText.text = forecast.conditions
                descriptionText.text = forecast.description
                humidityText.text = "${forecast.humidity}%"
                val windSpeed = Settings.windSpeedUnit.convert(forecast.windSpeed)
                android.util.Log.d("ForecastAdapter", "Wind Speed: Original=${forecast.windSpeed} km/h, Converted=$windSpeed ${Settings.windSpeedUnit}")
                windText.text = Settings.windSpeedUnit.format(windSpeed)
                val precipChance = forecast.precipitation
                precipitationText.text = "${precipChance}%"
                val snowAmount = forecast.snow.toDouble()

                if (snowAmount > 0) {
                    val convertedSnow = Settings.lengthUnit.convert(snowAmount)
                    snowText.text = Settings.lengthUnit.format(convertedSnow)
                } else {
                    snowText.text = "0%"
                }
                val highTemp = forecast.highTemp
                val lowTemp = forecast.lowTemp
                
                android.util.Log.d("ForecastAdapter", "Temperatures: ${forecast.highTemp}/${forecast.lowTemp} ${Settings.temperatureUnit}")
                
                val df = DecimalFormat("#.#")
                maxTempText.text = df.format(highTemp)
                minTempText.text = df.format(lowTemp)
                
                val unitSymbol = if (Settings.temperatureUnit == Settings.TemperatureUnit.CELSIUS) "°C" else "°F"
                maxTempUnitText.text = unitSymbol
                minTempUnitText.text = unitSymbol
                
                android.util.Log.d("ForecastAdapter", "Applied unit symbol: $unitSymbol")
                weatherIcon.setImageResource(getWeatherIconResource(forecast.conditions))
                setCardBackground(forecast.conditions)
                
            } catch (e: Exception) {
                android.util.Log.e("ForecastAdapter", "Error binding forecast", e)
            }
        }

        @SuppressLint("UseKtx")
        private fun setCardBackground(conditions: String) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val isNight = currentHour < 6 || currentHour >= 19
            
            val backgroundResId = when {
                conditions.lowercase().contains("thunder") ||
                conditions.lowercase().contains("storm") || 
                conditions.lowercase().contains("lightning") -> {
                    if (isNight) R.drawable.background_night_thunderstorm else R.drawable.background_day_thunderstorm
                }
                
                conditions.lowercase().contains("rain") ||
                conditions.lowercase().contains("drizzle") || 
                conditions.lowercase().contains("shower") -> {
                    if (isNight) R.drawable.background_night_rainy else R.drawable.background_day_rainy
                }
                
                conditions.lowercase().contains("fog") ||
                conditions.lowercase().contains("mist") || 
                conditions.lowercase().contains("haze") -> {
                    if (isNight) R.drawable.background_night_foggy else R.drawable.background_day_foggy
                }
                
                conditions.lowercase().contains("snow") ||
                conditions.lowercase().contains("sleet") || 
                conditions.lowercase().contains("ice") -> {
                    if (isNight) R.drawable.background_night_snowy else R.drawable.background_day_snowy
                }
                
                conditions.lowercase().contains("cloud") ||
                conditions.lowercase().contains("overcast") -> {
                    if (isNight) R.drawable.background_night_cloudy else R.drawable.background_day_cloudy
                }
                
                else -> {
                    if (isNight) R.drawable.background_night_clear else R.drawable.background_day_clear
                }
            }
            
            headerLayout.setBackgroundResource(backgroundResId)
            
            val isDarkBackground = BackgroundManager.isBackgroundDark(itemView.context as Activity, backgroundResId)
            val textColor = if (isDarkBackground) {
                ContextCompat.getColor(itemView.context, R.color.white)
            } else {
                ContextCompat.getColor(itemView.context, R.color.black)
            }
            
            dateText.setTextColor(textColor)
            conditionsText.setTextColor(textColor)
            descriptionText.setTextColor(textColor)
            maxTempText.setTextColor(textColor)
            minTempText.setTextColor(textColor)
            maxTempUnitText.setTextColor(textColor)
            minTempUnitText.setTextColor(textColor)
            
            if (!isDarkBackground) {
                val shadowColor = Color.parseColor("#40000000")
                dateText.setShadowLayer(2f, 1f, 1f, shadowColor)
                conditionsText.setShadowLayer(2f, 1f, 1f, shadowColor)
                descriptionText.setShadowLayer(2f, 1f, 1f, shadowColor)
            } else {
                dateText.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                conditionsText.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                descriptionText.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
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
 
 
 
 