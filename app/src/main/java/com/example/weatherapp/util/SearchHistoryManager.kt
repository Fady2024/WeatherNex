package com.example.weatherapp.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException

/**
 * Manager for handling search history operations
 * Stores the search history in SharedPreferences as a JSON array
 */
class SearchHistoryManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "WeatherSearchHistory"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val KEY_LAST_LOCATION = "last_selected_location"
        private const val MAX_HISTORY_ITEMS = 10
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the list of recent search history
     * @return List of search strings ordered from most recent to oldest
     */
    fun getSearchHistory(): List<String> {
        val historyJson = prefs.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        val result = mutableListOf<String>()
        
        try {
            val jsonArray = JSONArray(historyJson)
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        
        return result
    }

    /**
     * Add a new search query to the history
     * If the query already exists in history, it will be moved to the top (most recent)
     * @param query The search query to add
     */
    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        history.add(0, query)
        while (history.size > MAX_HISTORY_ITEMS) {
            history.removeAt(history.size - 1)
        }
        saveSearchHistory(history)
    }

    /**
     * Remove a specific search query from history
     * @param query The search query to remove
     */
    fun removeSearchQuery(query: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        saveSearchHistory(history)
    }

    /**
     * Clear all search history
     */
    fun clearSearchHistory() {
        prefs.edit().remove(KEY_SEARCH_HISTORY).apply()
    }

    /**
     * Save the search history list to SharedPreferences
     */
    private fun saveSearchHistory(history: List<String>) {
        val jsonArray = JSONArray()
        for (item in history) {
            jsonArray.put(item)
        }
        
        prefs.edit().putString(KEY_SEARCH_HISTORY, jsonArray.toString()).apply()
    }
    
    /**
     * Save the last selected location
     * This will be used as the default location when the app starts
     * @param location The location name to save
     */
    fun saveLastSelectedLocation(location: String) {
        if (location.isBlank()) return
        prefs.edit().putString(KEY_LAST_LOCATION, location).apply()
    }
    
    /**
     * Get the last selected location
     * @param defaultLocation The default location to return if no location was saved
     * @return The last selected location or the default if none was saved
     */
    fun getLastSelectedLocation(defaultLocation: String): String {
        return prefs.getString(KEY_LAST_LOCATION, defaultLocation) ?: defaultLocation
    }
} 
 
 
 
 
 