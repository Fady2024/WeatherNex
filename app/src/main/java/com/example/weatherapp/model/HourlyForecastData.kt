package com.example.weatherapp.model

data class HourlyForecastData(
    val time: String,
    val temperature: Double,
    val conditions: String,
    val precipProbability: Int,
    val humidity: Int,
    val windSpeed: Double,
    val feelsLike: Double
) 
 
 
 
 
 