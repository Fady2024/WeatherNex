package com.example.weatherapp.model

/**
 * Data class representing a search history item
 *
 * @property name The name of the location
 * @property isCurrentLocation Whether this is the user's current GPS location
 */
data class SearchHistoryItem(
    val name: String,
    val isCurrentLocation: Boolean = false
) 