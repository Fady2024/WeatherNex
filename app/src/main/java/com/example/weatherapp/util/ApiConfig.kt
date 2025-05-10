package com.example.weatherapp.util

import android.content.Context
import android.util.Log
import java.io.IOException
import java.util.Properties

/**
 * Utility class to access API configuration values
 */
object ApiConfig {
    private const val TAG = "ApiConfig"
    private const val ENV_FILE = ".env"
    private const val API_KEY_PROP = "API_KEY"
    private const val DEFAULT_API_KEY = "U5WXPBRLBQH5BT77ER9Q73MN8"
    
    private var apiKey: String? = null
    private var initialized = false
    
    /**
     * Load API configuration from .env file
     */
    fun init(context: Context) {
        if (initialized) {
            Log.d(TAG, "ApiConfig already initialized")
            return
        }
        
        try {
            Log.d(TAG, "Loading API configuration from $ENV_FILE")
            val properties = Properties()
            
            try {
                context.assets.open(ENV_FILE).use { inputStream ->
                    properties.load(inputStream)
                    Log.d(TAG, "Successfully loaded .env file")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Could not open $ENV_FILE from assets: ${e.message}", e)
                throw e
            }
            
            apiKey = properties.getProperty(API_KEY_PROP)
            
            if (apiKey.isNullOrBlank()) {
                Log.w(TAG, "API key is null or blank in .env file, using default key")
                apiKey = DEFAULT_API_KEY
            } else {
                Log.d(TAG, "API key loaded successfully")
            }
            
            initialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error loading API configuration, using default values: ${e.message}", e)
            apiKey = DEFAULT_API_KEY
            initialized = true
        }
    }
    
    /**
     * Get the API key
     */
    fun getApiKey(): String {
        if (!initialized) {
            Log.w(TAG, "ApiConfig not initialized! Using default API key")
            return DEFAULT_API_KEY
        }
        
        val key = apiKey
        if (key.isNullOrBlank()) {
            Log.w(TAG, "API key is null or blank, using default key")
            return DEFAULT_API_KEY
        }
        
        return key
    }
} 