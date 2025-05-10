package com.example.weatherapp.model

data class HourlyForecast(
    val timestamp: Long,
    val temperature: Double,
    val weatherCode: Int,
    val weatherDescription: String
) {
    companion object {
        fun fromHourlyForecastData(data: HourlyForecastData): HourlyForecast {
            val timeString = data.time.split(":")
            val hour = if (timeString.isNotEmpty()) timeString[0].toIntOrNull() ?: 0 else 0
            val now = System.currentTimeMillis()
            val timestamp = now - (now % 86400000) + (hour * 3600000)
            return HourlyForecast(
                timestamp = timestamp,
                temperature = data.temperature,
                weatherCode = 0, // Default code, you may want to map this better
                weatherDescription = data.conditions
            )
        }
    }
} 