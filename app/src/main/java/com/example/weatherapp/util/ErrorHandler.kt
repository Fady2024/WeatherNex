package com.example.weatherapp.util

import android.content.Context
import com.example.weatherapp.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling for the application
 * Converts exceptions to user-friendly messages
 */
class ErrorHandler(private val context: Context) {

    sealed class ErrorType {
        object Network : ErrorType()
        object Server : ErrorType()
        object NoLocation : ErrorType()
        object NoCache : ErrorType()
        object Unknown : ErrorType()
        data class Custom(val message: String) : ErrorType()
    }

    fun getErrorMessage(error: Exception): String {
        return when (getErrorType(error)) {
            ErrorType.Network -> context.getString(R.string.error_network)
            ErrorType.Server -> context.getString(R.string.error_server)
            ErrorType.NoLocation -> context.getString(R.string.error_location)
            ErrorType.NoCache -> context.getString(R.string.error_no_cache)
            ErrorType.Unknown -> context.getString(R.string.error_general)
            is ErrorType.Custom -> (getErrorType(error) as ErrorType.Custom).message
        }
    }

    fun getErrorType(error: Exception): ErrorType {
        return when (error) {
            is IOException -> {
                when (error) {
                    is SocketTimeoutException -> ErrorType.Server
                    is UnknownHostException -> ErrorType.Network
                    else -> ErrorType.Network
                }
            }
            is SecurityException -> ErrorType.NoLocation
            is IllegalStateException -> {
                if (error.message?.contains("cache", ignoreCase = true) == true) {
                    ErrorType.NoCache
                } else {
                    ErrorType.Unknown
                }
            }
            else -> ErrorType.Unknown
        }
    }

    fun isNetworkError(error: Exception): Boolean {
        return getErrorType(error) == ErrorType.Network
    }

    fun isServerError(error: Exception): Boolean {
        return getErrorType(error) == ErrorType.Server
    }

    companion object {
        private var instance: ErrorHandler? = null

        fun getInstance(context: Context): ErrorHandler {
            return instance ?: synchronized(this) {
                instance ?: ErrorHandler(context.applicationContext).also { instance = it }
            }
        }
    }
} 