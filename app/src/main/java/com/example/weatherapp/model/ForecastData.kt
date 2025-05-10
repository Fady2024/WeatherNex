package com.example.weatherapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ForecastData(
    val date: String = "",
    val highTemp: Double = 0.0,
    val lowTemp: Double = 0.0,
    val conditions: String = "",
    val description: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val windDirection: Int = 0,
    val precipitation: Int = 0,
    val cloudCover: Int = 0,
    val uvIndex: Int = 0,
    val sunrise: String = "",
    val sunset: String = "",
    val iconType: String = "",
    val snow: Int = 0
) : Parcelable {
    companion object {
        fun empty() = ForecastData(
            date = "",
            highTemp = 0.0,
            lowTemp = 0.0,
            conditions = "",
            description = "",
            humidity = 0,
            windSpeed = 0.0,
            windDirection = 0,
            precipitation = 0,
            cloudCover = 0,
            uvIndex = 0,
            sunrise = "",
            sunset = "",
            iconType = "",
            snow = 0
        )
        
        fun createWithValidTemps(
            date: String,
            highTemp: Double,
            lowTemp: Double,
            conditions: String,
            description: String,
            humidity: Int,
            windSpeed: Double,
            windDirection: Int = 0,
            precipitation: Int,
            cloudCover: Int = 0,
            uvIndex: Int = 0,
            sunrise: String = "",
            sunset: String = "",
            iconType: String = "",
            snow: Int = 0
        ): ForecastData {
            val validLowTemp = if (lowTemp >= highTemp) highTemp * 0.8 else lowTemp
            val validWindSpeed = if (windSpeed < 0.1) 0.1 else windSpeed
            
            return ForecastData(
                date = date,
                highTemp = highTemp,
                lowTemp = validLowTemp,
                conditions = conditions,
                description = description,
                humidity = humidity,
                windSpeed = validWindSpeed,
                windDirection = windDirection,
                precipitation = precipitation,
                cloudCover = cloudCover,
                uvIndex = uvIndex,
                sunrise = sunrise,
                sunset = sunset,
                iconType = iconType,
                snow = snow
            )
        }
    }
}

@Parcelize
data class ForecastResponse(
    val location: String,
    val forecasts: List<ForecastData>
) : Parcelable {
    companion object {
        fun empty() = ForecastResponse(
            location = "",
            forecasts = emptyList()
        )
    }
} 