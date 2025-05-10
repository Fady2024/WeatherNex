package com.example.weatherapp

import android.app.Application
import android.util.Log
import com.example.weatherapp.di.ServiceLocator
import com.example.weatherapp.util.ApiConfig
import com.example.weatherapp.util.ErrorHandler
import com.example.weatherapp.util.Settings
import java.lang.Thread.UncaughtExceptionHandler

/**
 * Custom Application class for the Weather App
 * 
 * This class is responsible for:
 * 1. Initializing core components of the application
 * 2. Setting up dependency injection
 * 3. Configuring global settings
 * 4. Managing application lifecycle
 *
 * The initialization happens in the following order:
 * 1. ServiceLocator - for dependency injection
 * 2. Settings - for user preferences
 * 3. ErrorHandler - for centralized error handling
 */
class WeatherApplication : Application() {
    
    /**
     * Global error handler instance
     * Used throughout the app for consistent error handling
     */
    lateinit var errorHandler: ErrorHandler
        private set

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
        
        try {
            Log.d("WeatherApp", "Starting application initialization")
            
            try {
                Log.d("WeatherApp", "Initializing ServiceLocator")
                ServiceLocator.init(this)
                Log.d("WeatherApp", "ServiceLocator initialized successfully")
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error initializing ServiceLocator", e)
                throw e
            }
            
            try {
                Log.d("WeatherApp", "Initializing Settings")
                Settings.init(this)
                Log.d("WeatherApp", "Settings initialized successfully")
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error initializing Settings", e)
                throw e
            }
            
            try {
                Log.d("WeatherApp", "Loading API configuration")
                initializeApiConfig()
                Log.d("WeatherApp", "API configuration loaded successfully")
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error loading API configuration", e)
            }
            
            try {
                Log.d("WeatherApp", "Creating ErrorHandler")
                errorHandler = ErrorHandler.getInstance(this)
                Log.d("WeatherApp", "ErrorHandler created successfully")
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error creating ErrorHandler", e)
                throw e
            }
            
            Log.d("WeatherApp", "Application initialization completed successfully")
        } catch (e: Exception) {
            Log.e("WeatherApp", "CRITICAL ERROR: Application initialization failed", e)
        }
    }

    private fun initializeApiConfig() {
        var attempt = 1
        val maxAttempts = 3
        
        while (attempt <= maxAttempts) {
            try {
                Log.d("WeatherApp", "API configuration initialization attempt $attempt of $maxAttempts")
                ApiConfig.init(this)
                val apiKey = ApiConfig.getApiKey()
                if (apiKey.isBlank()) {
                    Log.e("WeatherApp", "API key is blank after initialization, retrying...")
                    attempt++
                    continue
                }
                
                Log.d("WeatherApp", "ApiConfig initialized successfully")
                return
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error initializing ApiConfig on attempt $attempt: ${e.message}", e)
                if (attempt < maxAttempts) {
                    attempt++
                    Thread.sleep(500)
                } else {
                    Log.e("WeatherApp", "Failed to initialize ApiConfig after $maxAttempts attempts")
                    throw e
                }
            }
        }
    }

    override fun onTerminate() {
        Log.d("WeatherApp", "Application terminating")
        super.onTerminate()
        ServiceLocator.reset()
    }
    
    /**
     * Custom exception handler to log uncaught exceptions
     */
    private inner class CustomExceptionHandler : UncaughtExceptionHandler {
        private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            try {
                Log.e("WeatherApp", "FATAL CRASH in thread: ${thread.name}", throwable)
                throwable.stackTrace.forEach {
                    Log.e("WeatherApp", "    at $it")
                }
                
                if (throwable.cause != null) {
                    Log.e("WeatherApp", "Caused by:", throwable.cause)
                    throwable.cause?.stackTrace?.forEach {
                        Log.e("WeatherApp", "    at $it")
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherApp", "Error in custom exception handler", e)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
} 