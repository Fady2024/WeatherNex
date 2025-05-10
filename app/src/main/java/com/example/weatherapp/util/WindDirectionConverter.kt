package com.example.weatherapp.util

object WindDirectionConverter {
    /**
     * Converts wind direction in degrees to cardinal direction
     * @param degrees Wind direction in degrees (0-360)
     * @return Cardinal direction (N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW)
     */
    fun degreesToCardinal(degrees: Int): String {
        val normalizedDegrees = ((degrees % 360 + 360) % 360)

        return when (normalizedDegrees) {
            in 349..360, in 0..11 -> "N"
            in 12..33 -> "NNE"
            in 34..56 -> "NE"
            in 57..78 -> "ENE"
            in 79..101 -> "E"
            in 102..123 -> "ESE"
            in 124..146 -> "SE"
            in 147..168 -> "SSE"
            in 169..191 -> "S"
            in 192..213 -> "SSW"
            in 214..236 -> "SW"
            in 237..258 -> "WSW"
            in 259..281 -> "W"
            in 282..303 -> "WNW"
            in 304..326 -> "NW"
            in 327..348 -> "NNW"
            else -> "N"
        }
    }
    
    /**
     * Converts wind direction in degrees to a user-friendly format showing both cardinal direction and degrees
     * @param degrees Wind direction in degrees (0-360)
     * @return Formatted string like "NE (45°)"
     */
    fun formatWindDirection(degrees: Int): String {
        return "${degreesToCardinal(degrees)} (${degrees}°)"
    }
} 