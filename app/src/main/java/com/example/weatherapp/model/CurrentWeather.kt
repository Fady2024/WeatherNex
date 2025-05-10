package com.example.weatherapp.model

data class CurrentWeather(
    val temperature: Double,
    val conditions: String,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double,
    val uvIndex: Int,
    val pressure: Double,
    val visibility: Double,
    val cloudCover: Int,
    val datetime: String,
    val location: String
) {
    companion object {
        fun empty() = CurrentWeather(
            temperature = 0.0,
            conditions = "",
            humidity = 0,
            windSpeed = 0.0,
            feelsLike = 0.0,
            uvIndex = 0,
            pressure = 0.0,
            visibility = 0.0,
            cloudCover = 0,
            datetime = "",
            location = ""
        )
    }
} 