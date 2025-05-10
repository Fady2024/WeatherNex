package com.example.weatherapp.ui.forecast

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.di.ServiceLocator
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.ui.forecast.adapter.ForecastAdapter
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.ErrorHandler
import com.example.weatherapp.util.CacheManager.CacheResult
import com.google.android.material.snackbar.Snackbar

class ForecastActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "ForecastActivity"
    }
    
    private lateinit var textViewLocation: TextView
    private lateinit var buttonBack: ImageView
    private lateinit var recyclerViewForecast: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewError: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewOfflineBanner: TextView
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var cacheManager: CacheManager
    private lateinit var networkManager: NetworkManager
    private lateinit var errorHandler: ErrorHandler
    private var currentLocation: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        
        try {
            weatherRepository = ServiceLocator.provideWeatherRepository()
            cacheManager = ServiceLocator.provideCacheManager()
            networkManager = ServiceLocator.provideNetworkManager()
            errorHandler = (application as WeatherApplication).errorHandler
            
            initializeViews()
            setupListeners()
            currentLocation = intent.getStringExtra("LOCATION") ?: getString(R.string.default_location)
            textViewLocation.text = currentLocation
            textViewTitle.text = getString(R.string.next_5_days)
            loadForecastData()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ForecastActivity", e)
            Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initializeViews() {
        textViewLocation = findViewById(R.id.textViewLocation)
        buttonBack = findViewById(R.id.buttonBack)
        recyclerViewForecast = findViewById(R.id.recyclerViewForecast)
        progressBar = findViewById(R.id.progressBar)
        textViewError = findViewById(R.id.textViewError)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewOfflineBanner = findViewById(R.id.textViewOfflineBanner)
        recyclerViewForecast.layoutManager = LinearLayoutManager(this)
        forecastAdapter = ForecastAdapter()
        recyclerViewForecast.adapter = forecastAdapter
    }
    
    private fun setupListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
    }
    
    private fun loadForecastData() {
        try {
            showLoading()
            hideError()
            
            if (!networkManager.isNetworkAvailable()) {
                loadCachedData()
                hideLoading()
                showOfflineBanner(getString(R.string.offline_using_cached_data))
                return
            }
            
            Thread {
                try {
                    val forecastResponse = weatherRepository.fetchForecast(currentLocation)
                    cacheManager.saveForecast(forecastResponse)
                    
                    runOnUiThread {
                        if (forecastResponse.forecasts.isNotEmpty()) {
                            updateForecastUI(forecastResponse.forecasts)
                            hideError()
                        } else {
                            showError(getString(R.string.error_no_data))
                            showOfflineFallback()
                        }
                        hideLoading()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading forecast data", e)
                    runOnUiThread {
                        handleError(e)
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadForecastData", e)
            hideLoading()
            showError(getString(R.string.error_general))
            loadCachedData()
        }
    }

    private fun handleError(error: Exception) {
        try {
            hideLoading()
            val message = errorHandler.getErrorMessage(error)
            showError(message)
            
            if (errorHandler.isNetworkError(error)) {
                loadCachedData()
                showOfflineBanner(getString(R.string.offline_using_cached_data))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling error", e)
            showError(getString(R.string.error_general))
        }
    }

    private fun loadCachedData() {
        try {
            val cacheResult = cacheManager.getForecast()
            when (cacheResult) {
                is CacheResult.Success -> {
                    updateForecastUI(cacheResult.data.forecasts)
                }
                is CacheResult.Expired -> {
                    showError(getString(R.string.error_no_cache))
                }
                is CacheResult.NotFound -> {
                    showError(getString(R.string.error_offline_no_cache))
                }
                is CacheResult.Error -> {
                    showError(getString(R.string.error_general))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached data", e)
            showError(getString(R.string.error_general))
        }
    }
    
    private fun updateForecastUI(forecastData: List<ForecastData>) {
        recyclerViewForecast.visibility = View.VISIBLE
        val fiveDayForecast = forecastData.take(5)
        forecastAdapter.submitList(fiveDayForecast)
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerViewForecast.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        textViewError.text = message
        textViewError.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        textViewError.visibility = View.GONE
    }

    private fun showOfflineBanner(message: String) {
        textViewOfflineBanner.text = message
        textViewOfflineBanner.visibility = View.VISIBLE
    }

    private fun hideOfflineBanner() {
        textViewOfflineBanner.visibility = View.GONE
    }

    private fun showOfflineFallback() {
        val cacheResult = cacheManager.getForecast()
        if (cacheResult is CacheResult.Success) {
            updateForecastUI(cacheResult.data.forecasts)
            showOfflineBanner(getString(R.string.offline_using_cached_data))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::forecastAdapter.isInitialized) {
            forecastAdapter.notifyDataSetChanged()
        }
    }
} 