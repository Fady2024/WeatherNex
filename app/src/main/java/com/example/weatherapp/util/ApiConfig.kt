package com.example.weatherapp.util

import android.content.Context
import android.util.Log

/**
 * Utility class to access API configuration values
 */
object ApiConfig {
    private const val TAG = "ApiConfig"
    
    // Note: Best security practice is not to hardcode the API key in the source code
    // but we're using this method for simplicity
    private const val API_KEY = "U5WXPBRLBQH5BT77ER9Q73MN8"
    
    private var initialized = false
    
    /**
     * Initialize the API access module
     */
    fun init(context: Context?) {
        if (initialized) {
            Log.d(TAG, "ApiConfig already initialized")
            return
        }
        
        try {
            Log.d(TAG, "API configuration initialized successfully")
            initialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ApiConfig: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Get the API key
     */
    fun getApiKey(): String {
        if (!initialized) {
            Log.w(TAG, "ApiConfig not initialized, initializing now")
            init(null)
        }
        
        return API_KEY
    }
} 