package com.example.weatherapp.ui.weather

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weatherapp.R
import com.example.weatherapp.model.HourlyForecastData
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.ui.forecast.ForecastActivity
import com.example.weatherapp.ui.search.SearchLocationActivity
import com.example.weatherapp.ui.settings.SettingsActivity
import com.example.weatherapp.ui.weather.adapter.DailyForecastAdapter
import com.example.weatherapp.ui.weather.adapter.HourlyForecastAdapter
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.NetworkManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.FrameLayout
import java.util.Calendar
import android.util.Log
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.di.ServiceLocator
import com.example.weatherapp.util.ErrorHandler

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    
    
    private lateinit var textViewLocation: TextView
    private lateinit var textViewCountry: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewDegree: TextView
    private lateinit var textViewWeatherDescription: TextView
    private lateinit var textViewDetailedDescription: TextView
    private lateinit var textViewHumidity: TextView
    private lateinit var textViewWind: TextView
    private lateinit var textViewPressure: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private lateinit var buttonMenu: ImageButton
    private lateinit var buttonSearch: ImageButton
    private lateinit var buttonLocation: ImageButton
    private lateinit var recyclerViewHourlyForecast: RecyclerView
    private lateinit var recyclerViewDailyForecast: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewError: TextView
    private lateinit var textViewOfflineBanner: TextView
    private lateinit var tabToday: TextView
    private lateinit var tabNext5Days: TextView
    private lateinit var tabIndicator: View
    private lateinit var tabContentFrame: FrameLayout
    
    
    private lateinit var textViewTempMin: TextView
    private lateinit var textViewTempMax: TextView
    private lateinit var textViewFeelsLike: TextView
    private lateinit var textViewSunrise: TextView
    private lateinit var textViewSunset: TextView
    private lateinit var textViewUVIndex: TextView
    private lateinit var textViewVisibility: TextView
    private lateinit var textViewCloudCover: TextView
    private lateinit var textViewPrecipitation: TextView
    private lateinit var textViewSnow: TextView
    private lateinit var textViewSnowDepth: TextView
    private lateinit var textViewDewPoint: TextView
    private lateinit var textViewWindDirection: TextView
    private lateinit var textViewWindGust: TextView
    private lateinit var textViewSolarRadiation: TextView
    private lateinit var textViewMoonPhase: TextView
    
    
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    
    
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var cacheManager: CacheManager
    private lateinit var networkManager: NetworkManager
    
    
    private var currentWeather: WeatherData? = null
    private var currentLocation: String = "Cairo"

    private lateinit var errorHandler: ErrorHandler
    
    companion object {
        private const val SEARCH_LOCATION_REQUEST = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "MainActivity onCreate started")
            setContentView(R.layout.activity_main)
            
            try {
                weatherRepository = ServiceLocator.provideWeatherRepository()
                cacheManager = ServiceLocator.provideCacheManager()
                networkManager = ServiceLocator.provideNetworkManager()
                errorHandler = (application as WeatherApplication).errorHandler
                
                initializeViews()
                setupListeners()
                
                currentLocation = intent.getStringExtra("LOCATION") ?: getString(R.string.default_location)
                Log.d(TAG, "Current location set to: $currentLocation")
                
                loadWeatherData()
                
                if (!networkManager.isNetworkAvailable()) {
                    showOfflineBanner()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing MainActivity", e)
                Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_LONG).show()
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate", e)
            Toast.makeText(this, "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initializeViews() {
        try {
            Log.d(TAG, "Initializing views")
            textViewLocation = findViewById(R.id.textViewLocation)
            textViewCountry = findViewById(R.id.textViewCountry)
            textViewDate = findViewById(R.id.textViewDate)
            buttonMenu = findViewById(R.id.buttonMenu)
            buttonSearch = findViewById(R.id.buttonSearch)
            buttonLocation = findViewById(R.id.buttonLocation)
            textViewTemperature = findViewById(R.id.textViewTemperature)
            textViewDegree = findViewById(R.id.textViewDegree)
            textViewWeatherDescription = findViewById(R.id.textViewWeatherDescription)
            textViewDetailedDescription = findViewById(R.id.textViewDetailedDescription)
            textViewHumidity = findViewById(R.id.textViewHumidity)
            textViewWind = findViewById(R.id.textViewWind)
            textViewPressure = findViewById(R.id.textViewPressure)
            imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)
            textViewTempMin = findViewById(R.id.textViewTempMin)
            textViewTempMax = findViewById(R.id.textViewTempMax)
            textViewFeelsLike = findViewById(R.id.textViewFeelsLike)
            textViewSunrise = findViewById(R.id.textViewSunrise)
            textViewSunset = findViewById(R.id.textViewSunset)
            textViewUVIndex = findViewById(R.id.textViewUVIndex)
            textViewVisibility = findViewById(R.id.textViewVisibility)
            textViewCloudCover = findViewById(R.id.textViewCloudCover)
            textViewPrecipitation = findViewById(R.id.textViewPrecipitation)
            textViewSnow = findViewById(R.id.textViewSnow)
            textViewSnowDepth = findViewById(R.id.textViewSnowDepth)
            textViewDewPoint = findViewById(R.id.textViewDewPoint)
            textViewWindDirection = findViewById(R.id.textViewWindDirection)
            textViewWindGust = findViewById(R.id.textViewWindGust)
            textViewSolarRadiation = findViewById(R.id.textViewSolarRadiation)
            textViewMoonPhase = findViewById(R.id.textViewMoonPhase)
            tabToday = findViewById(R.id.tabToday)
            tabNext5Days = findViewById(R.id.tabNext5Days)
            tabIndicator = findViewById(R.id.tabIndicator)
            tabContentFrame = findViewById(R.id.tabContentFrame)
            recyclerViewHourlyForecast = findViewById(R.id.recyclerViewHourlyForecast)
            recyclerViewDailyForecast = findViewById(R.id.recyclerViewDailyForecast)
            progressBar = findViewById(R.id.progressBar)
            textViewError = findViewById(R.id.textViewError)
            textViewOfflineBanner = findViewById(R.id.textViewOfflineBanner)
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

            try {
                Log.d(TAG, "Initializing adapters")
                hourlyForecastAdapter = HourlyForecastAdapter()
                dailyForecastAdapter = DailyForecastAdapter()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing adapters", e)
            }
            
            updateDateDisplay()
            
            try {
                Log.d(TAG, "Setting up RecyclerViews")
                recyclerViewHourlyForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                recyclerViewHourlyForecast.adapter = hourlyForecastAdapter
                
                try {
                    hourlyForecastAdapter.submitList(emptyList<HourlyForecastData>())
                } catch (e: Exception) {
                    Log.e(TAG, "Error submitting empty list to hourlyForecastAdapter", e)
                }
                
                recyclerViewDailyForecast.layoutManager = LinearLayoutManager(this)
                recyclerViewDailyForecast.adapter = dailyForecastAdapter
                
                try {
                    dailyForecastAdapter.submitList(emptyList<WeatherData>())
                } catch (e: Exception) {
                    Log.e(TAG, "Error submitting empty list to dailyForecastAdapter", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up RecyclerViews", e)
            }
            
            setupTabListeners()
            
            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error initializing app: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupListeners() {
        buttonMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        buttonSearch.setOnClickListener {
            val intent = Intent(this, SearchLocationActivity::class.java)
            startActivityForResult(intent, SEARCH_LOCATION_REQUEST)
        }

        buttonLocation.setOnClickListener {
            if (hasLocationPermission()) {
                getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
        
        swipeRefreshLayout.setOnRefreshListener {
            refreshWeatherData()
        }
    }
    
    private fun loadWeatherData() {
        try {
            showLoading()
            hideError()
            
            
            loadCachedData()
            
            
            if (networkManager.isNetworkAvailable()) {
                refreshWeatherData()
            } else {
                hideLoading()
                showOfflineBanner()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading weather data", e)
            hideLoading()
            showError(e)
            loadCachedData() 
        }
    }
    
    private fun loadCachedData() {
        
        val cachedWeather = cacheManager.getCurrentWeather()
        if (cachedWeather != null) {
            updateWeatherData(cachedWeather)
            updateLocationName()
        }
        
        
        val cachedHourly = cacheManager.getHourlyForecast()
        if (cachedHourly != null) {
            updateHourlyForecast(cachedHourly)
        }
    }
    
    private fun refreshWeatherData() {
        showLoading()
        hideError()
        
        Thread {
            try {
                
                if (!networkManager.isNetworkAvailable()) {
                    runOnUiThread {
                        hideLoading()
                        showOfflineBanner()
                        swipeRefreshLayout.isRefreshing = false
                        loadCachedData() 
                    }
                    return@Thread
                }
                
                
                val weatherData = weatherRepository.fetchWeather(currentLocation)
                
                
                val hourlyForecast = weatherRepository.fetchHourlyForecast(currentLocation)
                
                
                runOnUiThread {
                    updateWeatherData(weatherData)
                    updateHourlyForecast(hourlyForecast)
                    updateLocationName()
                    hideLoading()
                    hideOfflineBanner()
                    swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing weather data", e)
                runOnUiThread {
                    hideLoading()
                    showOfflineBanner()
                    swipeRefreshLayout.isRefreshing = false
                    showError(e)
                    loadCachedData() 
                }
            }
        }.start()
    }
    
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateWeatherData(weatherData: WeatherData) {
        try {
            currentWeather = weatherData
            
            
            val locationParts = weatherData.location.split(",")
            if (locationParts.isNotEmpty()) {
                
                textViewLocation.text = locationParts[0].trim().takeIf { it.isNotEmpty() } ?: getString(R.string.default_location)
                
                
                if (locationParts.size > 1) {
                    textViewCountry.text = locationParts[1].trim()
                } else {
                    textViewCountry.text = "United Kingdom" 
                }
            } else {
                textViewLocation.text = getString(R.string.default_location)
                textViewCountry.text = "United Kingdom"
            }
            
            
            val temperatureUnit = com.example.weatherapp.util.Settings.temperatureUnit
            textViewDegree.text = if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "°C" else "°F"
            
            
            try {
                val convertedTemp = temperatureUnit.convert(weatherData.temperature)
                val temperatureInt = convertedTemp.toInt()
                textViewTemperature.text = temperatureInt.toString()
                
                
                val minTemp = temperatureUnit.convert(weatherData.minTemperature).toInt()
                val maxTemp = temperatureUnit.convert(weatherData.maxTemperature).toInt()
                textViewTempMax.text = "${maxTemp}°${if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "C" else "F"}"
                textViewTempMin.text = "${minTemp}°${if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "C" else "F"}"
                
                
                val feelsLike = temperatureUnit.convert(weatherData.feelsLike).toInt()
                textViewFeelsLike.text = "${feelsLike}°${if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "C" else "F"}"
                
                
                val dewPoint = temperatureUnit.convert(weatherData.dewPoint).toInt()
                textViewDewPoint.text = "${dewPoint}°${if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "C" else "F"}"
            } catch (e: Exception) {
                textViewTemperature.text = "0"
                textViewTempMin.text = "0°"
                textViewTempMax.text = "0°"
                textViewFeelsLike.text = "0°"
                textViewDewPoint.text = "0°"
                Log.e("MainActivity", "Error formatting temperature", e)
            }
            
            
            textViewWeatherDescription.text = weatherData.conditions.takeIf { it.isNotEmpty() } ?: "Unknown"
            
            
            textViewDetailedDescription.text = weatherData.description.takeIf { it.isNotEmpty() } ?: "No additional description available"
            
            
            try {
                textViewHumidity.text = "${weatherData.humidity}%"
            } catch (e: Exception) {
                textViewHumidity.text = "0%"
                Log.e("MainActivity", "Error formatting humidity", e)
            }
            
            
            try {
                val convertedWindSpeed = com.example.weatherapp.util.Settings.windSpeedUnit.convert(weatherData.windSpeed)
                textViewWind.text = com.example.weatherapp.util.Settings.windSpeedUnit.format(convertedWindSpeed)
                
                
                textViewWindDirection.text = com.example.weatherapp.util.WindDirectionConverter.formatWindDirection(weatherData.windDirection)
                
                
                val convertedWindGust = com.example.weatherapp.util.Settings.windSpeedUnit.convert(weatherData.windGust)
                textViewWindGust.text = com.example.weatherapp.util.Settings.windSpeedUnit.format(convertedWindGust)
            } catch (e: Exception) {
                textViewWind.text = "0.0 m/s"
                textViewWindDirection.text = "N (0°)"
                textViewWindGust.text = "0.0 m/s"
                Log.e("MainActivity", "Error formatting wind data", e)
            }
            
            
            try {
                val convertedPressure = com.example.weatherapp.util.Settings.pressureUnit.convert(weatherData.pressure)
                textViewPressure.text = com.example.weatherapp.util.Settings.pressureUnit.format(convertedPressure)
            } catch (e: Exception) {
                textViewPressure.text = "0.0atm"
                Log.e("MainActivity", "Error formatting pressure", e)
            }
            
            
            val uvIndex = weatherData.uvIndex
            val uvDescription = when {
                uvIndex <= 2 -> "Low"
                uvIndex <= 5 -> "Moderate"
                uvIndex <= 7 -> "High"
                uvIndex <= 10 -> "Very High"
                else -> "Extreme"
            }
            textViewUVIndex.text = "$uvIndex ($uvDescription)"
            
            
            textViewVisibility.text = "${weatherData.visibility.toInt()} km"
            
            
            textViewCloudCover.text = "${weatherData.cloudCover}%"
            
            
            textViewPrecipitation.text = "${weatherData.precipProbability}%"
            
            
            val snowChance = weatherData.snow
            
            val displaySnowChance = if (snowChance == 0 && weatherData.conditions.toLowerCase().contains("snow")) 5 else snowChance
            textViewSnow.text = "${displaySnowChance}%"
            
            
            textViewSnowDepth.text = "${weatherData.snowDepth} mm"
            
            
            textViewSolarRadiation.text = "${weatherData.solarRadiation} W/m²"
            
            
            val moonPhaseText = when {
                weatherData.moonPhase < 0.05 -> "New Moon"
                weatherData.moonPhase < 0.25 -> "Waxing Crescent"
                weatherData.moonPhase < 0.30 -> "First Quarter"
                weatherData.moonPhase < 0.45 -> "Waxing Gibbous"
                weatherData.moonPhase < 0.55 -> "Full Moon"
                weatherData.moonPhase < 0.70 -> "Waning Gibbous"
                weatherData.moonPhase < 0.80 -> "Last Quarter"
                weatherData.moonPhase < 0.95 -> "Waning Crescent"
                else -> "New Moon"
            }
            textViewMoonPhase.text = moonPhaseText
            
            
            if (weatherData.sunrise.isNotEmpty()) {
                textViewSunrise.text = formatTimeBasedOnSettings(weatherData.sunrise)
            } else {
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 6)
                calendar.set(Calendar.MINUTE, 45)
                textViewSunrise.text = formatTimeBasedOnSettings("06:45")
            }
            
            if (weatherData.sunset.isNotEmpty()) {
                textViewSunset.text = formatTimeBasedOnSettings(weatherData.sunset)
            } else {
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 17)
                calendar.set(Calendar.MINUTE, 30)
                textViewSunset.text = formatTimeBasedOnSettings("17:30")
            }
            
            
            try {
                imageViewWeatherIcon.setImageResource(getWeatherIconResource(weatherData.conditions))
            } catch (e: Exception) {
                imageViewWeatherIcon.setImageResource(R.drawable.icon_weather_sun_cloud) 
                Log.e("MainActivity", "Error setting weather icon", e)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating weather UI", e)
            
        }
    }
    
    private fun updateHourlyForecast(hourlyForecast: List<HourlyForecastData>) {
        try {
            Log.d(TAG, "Updating hourly forecast with ${hourlyForecast.size} items")
            hourlyForecastAdapter.submitList(hourlyForecast)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating hourly forecast", e)
        }
    }
    
    private fun updateLocationName() {
        // Update the location name in the toolbar or action bar
    }
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
    
    private fun showError(error: Exception) {
        try {
            val message = errorHandler.getErrorMessage(error)
            textViewError.text = message
            textViewError.visibility = View.VISIBLE
            
            if (errorHandler.isNetworkError(error)) {
                showOfflineBanner()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error message", e)
            textViewError.text = getString(R.string.error_general)
            textViewError.visibility = View.VISIBLE
        }
    }
    
    private fun hideError() {
        textViewError.visibility = View.GONE
    }
    
    private fun showOfflineBanner() {
        textViewOfflineBanner.visibility = View.VISIBLE
    }
    
    private fun hideOfflineBanner() {
        textViewOfflineBanner.visibility = View.GONE
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == SEARCH_LOCATION_REQUEST && resultCode == RESULT_OK) {
            data?.getStringExtra("LOCATION")?.let { newLocation ->
                
                currentLocation = newLocation
                loadWeatherData()
            }
        }
    }
    
    
    @SuppressLint("DefaultLocale")
    private fun getWeatherIconResource(conditions: String): Int {
        val conditionsLower = conditions.toLowerCase()
        
        
        val isNightTime = isNightTime()
        
        return when {
            
            isNightTime -> {
                when {
                    
                    conditionsLower.contains("rain") -> R.drawable.icon_weather_moon_cloud_rain
                    
                    conditionsLower.contains("cloud") || conditionsLower.contains("overcast") -> 
                        R.drawable.icon_weather_moon_cloud
                    
                    conditionsLower.contains("clear") -> R.drawable.icon_weather_moon
                    
                    else -> R.drawable.icon_weather_moon_cloud
                }
            }
            
            
            else -> {
                when {
                    
                    conditionsLower.contains("rain") && conditionsLower.contains("thunder") -> 
                        R.drawable.icon_weather_thunderstorm_cloud
                    conditionsLower.contains("rain") && conditionsLower.contains("partly cloudy") -> 
                        R.drawable.icon_weather_sun_rain_cloud
                    conditionsLower.contains("rain") && conditionsLower.contains("cloudy") -> 
                        R.drawable.icon_weather_rain_cloud
                    
                    
                    conditionsLower.contains("snow") || conditionsLower.contains("flurries") || 
                    conditionsLower.contains("ice") || conditionsLower.contains("sleet") -> 
                        R.drawable.icon_weather_snow_cloud
                    
                    
                    conditionsLower.contains("fog") || conditionsLower.contains("mist") || 
                    conditionsLower.contains("haze") -> 
                        R.drawable.icon_weather_cloud_fog
                    
                    
                    conditionsLower.contains("partly cloudy") || conditionsLower.contains("partly sunny") -> 
                        R.drawable.icon_weather_sun_cloud
                    
                    
                    conditionsLower.contains("cloud") || conditionsLower.contains("overcast") -> 
                        R.drawable.icon_weather_cloud
                    
                    
                    conditionsLower.contains("sunny") || conditionsLower.contains("clear") -> 
                        R.drawable.icon_weather_sun
                    
                    
                    conditionsLower.contains("thunder") || conditionsLower.contains("storm") -> 
                        R.drawable.icon_weather_thunderstorm_cloud
                    
                    
                    else -> R.drawable.icon_weather_sun_cloud
                }
            }
        }
    }

    private fun isNightTime(): Boolean {
        try {
            val currentTime = Calendar.getInstance()
            val sunriseTime = parseTimeString(currentWeather?.sunrise ?: "06:00")
            val sunsetTime = parseTimeString(currentWeather?.sunset ?: "18:00")
            
            
            return currentTime.before(sunriseTime) || currentTime.after(sunsetTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking night time", e)
            return false
        }
    }

    private fun parseTimeString(timeStr: String): Calendar {
        val calendar = Calendar.getInstance()
        try {
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                calendar.set(Calendar.MINUTE, parts[1].toInt())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time string: $timeStr", e)
        }
        return calendar
    }
    
    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val currentDate = sdf.format(Date())
        textViewDate.text = currentDate
    }
    
    private fun setupTabListeners() {
        tabToday.setOnClickListener {
            
            recyclerViewHourlyForecast.visibility = View.VISIBLE
            recyclerViewDailyForecast.visibility = View.GONE
            
            
            val params = tabIndicator.layoutParams as ConstraintLayout.LayoutParams
            params.endToStart = tabNext5Days.id
            params.endToEnd = ConstraintLayout.LayoutParams.UNSET
            tabIndicator.layoutParams = params
        }
        
        tabNext5Days.setOnClickListener {
            
            val intent = Intent(this, ForecastActivity::class.java)
            intent.putExtra("LOCATION", currentLocation)
            startActivity(intent)
        }
    }
    
    @SuppressLint("DefaultLocale")
    private fun formatTimeBasedOnSettings(time: String): String {
        try {
            
            val parts = time.split(":")
            if (parts.size < 2) return time

            val hour = parts[0].toInt()
            val minute = parts[1].padStart(2, '0')

            return if (com.example.weatherapp.util.Settings.use24HourFormat) {
                
                String.format("%02d:%s", hour, minute)
            } else {
                
                val amPm = if (hour >= 12) "PM" else "AM"
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format("%d:%s %s", hour12, minute, amPm)
            }
        } catch (_: Exception) {
            return time
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        try {
            Log.d(TAG, "MainActivity onResume")
            
            if (::weatherRepository.isInitialized && ::cacheManager.isInitialized) {

                val temperatureUnit = com.example.weatherapp.util.Settings.temperatureUnit
                textViewDegree.text = if (temperatureUnit == com.example.weatherapp.util.Settings.TemperatureUnit.CELSIUS) "°C" else "°F"
                
                try {
                    val cachedWeather = cacheManager.getCurrentWeather()
                    if (cachedWeather != null) {
                        Log.d(TAG, "Updating UI with cached weather data")
                        updateWeatherData(cachedWeather)
                    } else {
                        Log.d(TAG, "No cached weather data available")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating weather data in onResume", e)
                }
                
                try {
                    val cachedHourlyForecast = cacheManager.getHourlyForecast()
                    if (cachedHourlyForecast != null) {
                        Log.d(TAG, "Updating UI with cached hourly forecast")
                        updateHourlyForecast(cachedHourlyForecast)
                    } else {
                        Log.d(TAG, "No cached hourly forecast available")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating hourly forecast in onResume", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onResume", e)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastLocation != null) {
                
                Thread {
                    try {
                        val locationName = weatherRepository.getLocationName(lastLocation.latitude, lastLocation.longitude)
                        runOnUiThread {
                            currentLocation = locationName
                            loadWeatherData()
                        }
                    } catch (_: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, getString(R.string.error_location), Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, getString(R.string.error_location), Toast.LENGTH_SHORT).show()
            }
        } catch (_: SecurityException) {
            Toast.makeText(this, getString(R.string.error_location_permission), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, getString(R.string.error_location_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }
} 