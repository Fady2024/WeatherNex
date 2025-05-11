package com.example.weatherapp.ui.forecast

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.di.ServiceLocator
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.ui.forecast.adapter.ForecastAdapter
import com.example.weatherapp.util.BackgroundManager
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.ErrorHandler
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.Settings
import java.util.Calendar

@Suppress("DEPRECATION")
class ForecastActivity : AppCompatActivity(), ForecastContract.View {
    
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
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var cacheManager: CacheManager
    private lateinit var networkManager: NetworkManager
    private lateinit var errorHandler: ErrorHandler
    private lateinit var presenter: ForecastPresenter
    private var currentLocation: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        
        try {
            weatherRepository = ServiceLocator.provideWeatherRepository()
            cacheManager = ServiceLocator.provideCacheManager()
            networkManager = ServiceLocator.provideNetworkManager()
            errorHandler = (application as WeatherApplication).errorHandler
            presenter = ForecastPresenter(weatherRepository, networkManager, cacheManager)
            initializeViews()
            currentLocation = intent.getStringExtra("LOCATION") ?: getString(R.string.default_location)
            setupUI()
            presenter.attachView(this)
            presenter.loadForecastData(currentLocation)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ForecastActivity", e)
            Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
    
    private fun initializeViews() {
        try {
            rootLayout = findViewById(R.id.forecastRootLayout)
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
            buttonBack.setOnClickListener {
                val intent = Intent().apply {
                    putExtra("LOCATION", currentLocation)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }
    
    private fun setupUI() {
        try {
            window.statusBarColor = Color.TRANSPARENT
            val backgroundResId = R.drawable.background_day_clear
            rootLayout.setBackgroundResource(backgroundResId)
            val isDarkBackground = BackgroundManager.isBackgroundDark(this, backgroundResId)
            window.decorView.systemUiVisibility = if (!isDarkBackground) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                0
            }
            val textColor = if (isDarkBackground) {
                ContextCompat.getColor(this, R.color.white)
            } else {
                ContextCompat.getColor(this, R.color.black)
            }
            
            textViewLocation.setTextColor(textColor)
            textViewTitle.setTextColor(textColor)
            textViewError.setTextColor(textColor)
            buttonBack.setColorFilter(textColor)
            textViewLocation.text = currentLocation
            textViewTitle.text = getString(R.string.next_5_days)
            
            BackgroundManager.setDefaultBackground(this, rootLayout)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UI", e)
            throw e
        }
    }

    override val context: Context
        get() = this

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerViewForecast.visibility = View.INVISIBLE
        textViewError.visibility = View.GONE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
        if (textViewError.visibility != View.VISIBLE) {
            recyclerViewForecast.visibility = View.VISIBLE
        }
    }

    override fun updateForecast(forecast: ForecastResponse) {
        updateForecastUI(forecast.forecasts)
    }

    override fun updateLocationName(location: String) {
        textViewLocation.text = location
    }

    override fun showError(message: String) {
        textViewError.text = message
        textViewError.visibility = View.VISIBLE
        recyclerViewForecast.visibility = View.INVISIBLE
    }

    override fun showOfflineBanner(message: String?) {
        textViewOfflineBanner.text = message ?: getString(R.string.offline_using_cached_data)
        textViewOfflineBanner.visibility = View.VISIBLE
    }

    override fun hideOfflineBanner() {
        textViewOfflineBanner.visibility = View.GONE
    }

    private fun updateForecastUI(forecastList: List<ForecastData>) {
        try {
            if (forecastList.isEmpty()) {
                showError(getString(R.string.error_no_data))
                return
            }
            
            recyclerViewForecast.visibility = View.VISIBLE
            forecastAdapter.updateForecast(forecastList)
            
            if (forecastList.isNotEmpty()) {
                val firstDay = forecastList[0]
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val isNight = currentHour < 6 || currentHour >= 19
                
                BackgroundManager.setForecastBackground(this, firstDay.conditions, isNight, rootLayout)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating forecast UI", e)
            showError(getString(R.string.error_general))
        }
    }
    
    /**
     * Refreshes the forecast UI with the current settings
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun refreshForecastUI() {
        if (::forecastAdapter.isInitialized) {
            forecastAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        
        try {
            if (::presenter.isInitialized && presenter.haveSettingsChanged()) {
                Log.d(TAG, "Settings have changed, updating tracked settings and refreshing UI")
                presenter.updateTrackedSettings()
                Log.d(TAG, "Current settings - Temp: ${Settings.temperatureUnit}, Wind: ${Settings.windSpeedUnit}")
                refreshForecastUI()
            } else {
                Log.d(TAG, "No settings changes detected")
                if (::forecastAdapter.isInitialized) {
                    forecastAdapter.refreshUnitsDisplay()
                }
            }
            
            if (!networkManager.isNetworkAvailable()) {
                showOfflineBanner(getString(R.string.offline_using_cached_data))
            } else {
                hideOfflineBanner()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("LOCATION", currentLocation)
        }
        setResult(RESULT_OK, intent)
        super.onBackPressed()
    }
} 