package com.example.weatherapp.ui.weather
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.di.ServiceLocator
import com.example.weatherapp.model.HourlyForecastData
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.ui.forecast.ForecastActivity
import com.example.weatherapp.ui.search.SearchLocationActivity
import com.example.weatherapp.ui.settings.SettingsActivity
import com.example.weatherapp.ui.weather.adapter.DailyForecastAdapter
import com.example.weatherapp.ui.weather.adapter.HourlyForecastAdapter
import com.example.weatherapp.util.BackgroundManager
import com.example.weatherapp.util.CacheManager
import com.example.weatherapp.util.ErrorHandler
import com.example.weatherapp.util.NetworkManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
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
    private lateinit var layoutWeatherTips: LinearLayout
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
    private lateinit var rootLayout: ConstraintLayout
    companion object {
        private const val SEARCH_LOCATION_REQUEST = 1001
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
        private const val FORECAST_REQUEST_CODE = 1003
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
            rootLayout = findViewById(R.id.rootLayout)
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
            layoutWeatherTips = findViewById(R.id.layoutWeatherTips)
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
        try {
            tabToday.setOnClickListener {
                showTodayTab()
            }
            tabNext5Days.setOnClickListener {
                val intent = Intent(this, ForecastActivity::class.java).apply {
                    putExtra("LOCATION", currentLocation)
                }
                startActivityForResult(intent, FORECAST_REQUEST_CODE)
            }
        buttonMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        buttonSearch.setOnClickListener {
            val intent = Intent(this, SearchLocationActivity::class.java)
            startActivityForResult(intent, SEARCH_LOCATION_REQUEST)
        }
        buttonLocation.setOnClickListener {
                requestLocationPermission()
            }
        swipeRefreshLayout.setOnRefreshListener {
                loadWeatherData()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listeners", e)
            showError(e)
        }
    }
    private fun showTodayTab() {
        recyclerViewHourlyForecast.visibility = View.VISIBLE
        recyclerViewDailyForecast.visibility = View.GONE
        val params = tabIndicator.layoutParams as ConstraintLayout.LayoutParams
        params.endToStart = tabNext5Days.id
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET
        tabIndicator.layoutParams = params
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
                val windSpeedUnit = com.example.weatherapp.util.Settings.windSpeedUnit
                Log.d("MainActivity", "Original wind speed: ${weatherData.windSpeed} km/h")
                if (windSpeedUnit == com.example.weatherapp.util.Settings.WindSpeedUnit.KPH) {
                    textViewWind.text = String.format("%.1f km/h", weatherData.windSpeed)
                } else {
                    val convertedWindSpeed = windSpeedUnit.convert(weatherData.windSpeed)
                    textViewWind.text = windSpeedUnit.format(convertedWindSpeed)
                    Log.d("MainActivity", "Converted wind speed: $convertedWindSpeed ${windSpeedUnit.name}")
                }
                textViewWindDirection.text = com.example.weatherapp.util.WindDirectionConverter.formatWindDirection(weatherData.windDirection)
                if (windSpeedUnit == com.example.weatherapp.util.Settings.WindSpeedUnit.KPH) {
                    textViewWindGust.text = String.format("%.1f km/h", weatherData.windGust)
                } else {
                    val convertedWindGust = windSpeedUnit.convert(weatherData.windGust)
                    textViewWindGust.text = windSpeedUnit.format(convertedWindGust)
                }
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
            try {
                Log.d("MainActivity", "Original visibility: ${weatherData.visibility} km")
                textViewVisibility.text = "${weatherData.visibility.toInt()} km"
            } catch (e: Exception) {
                textViewVisibility.text = "0 km"
                Log.e("MainActivity", "Error formatting visibility", e)
            }
            textViewCloudCover.text = "${weatherData.cloudCover}%"
            textViewPrecipitation.text = "${weatherData.precipProbability}%"
            val snowChance = weatherData.snow
            val displaySnowChance = if (snowChance == 0 && weatherData.conditions.toLowerCase().contains("snow")) 5 else snowChance
            textViewSnow.text = "${displaySnowChance}%"
            val convertedSnowDepth = com.example.weatherapp.util.Settings.lengthUnit.convert(weatherData.snowDepth)
            textViewSnowDepth.text = com.example.weatherapp.util.Settings.lengthUnit.format(convertedSnowDepth)
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
            hourlyForecastAdapter.updateSunriseSunset(
                weatherData.sunrise.ifEmpty { "06:00" },
                weatherData.sunset.ifEmpty { "18:00" }
            )
            try {
                imageViewWeatherIcon.setImageResource(getWeatherIconResource(weatherData.conditions))
            } catch (e: Exception) {
                imageViewWeatherIcon.setImageResource(R.drawable.icon_weather_sun_cloud) 
                Log.e("MainActivity", "Error setting weather icon", e)
            }
            applyWeatherTips(weatherData)
            updateUIBasedOnWeatherCondition(weatherData)
            BackgroundManager.setDynamicBackground(this, weatherData, rootLayout)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating weather UI", e)
        }
    }
    private fun updateHourlyForecast(hourlyForecast: List<HourlyForecastData>) {
        try {
            Log.d(TAG, "Updating hourly forecast with ${hourlyForecast.size} items")
            val sunrise = currentWeather?.sunrise ?: "06:00"
            val sunset = currentWeather?.sunset ?: "18:00"
            hourlyForecastAdapter.updateSunriseSunset(sunrise, sunset)
            hourlyForecastAdapter.submitList(hourlyForecast)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating hourly forecast", e)
        }
    }
    private fun updateLocationName() {
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
        when (requestCode) {
            SEARCH_LOCATION_REQUEST -> {
                if (resultCode == RESULT_OK) {
            data?.getStringExtra("LOCATION")?.let { newLocation ->
                currentLocation = newLocation
                loadWeatherData()
            }
        }
    }
            FORECAST_REQUEST_CODE -> {
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
            val use24Hour = com.example.weatherapp.util.Settings.use24HourFormat
            return if (use24Hour) {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time: $time", e)
            return time
        }
    }
    @SuppressLint("NotifyDataSetChanged")
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
                        applyWeatherTips(cachedWeather)
                        if (!cachedWeather.sunrise.isEmpty()) {
                            textViewSunrise.text = formatTimeBasedOnSettings(cachedWeather.sunrise)
                        }
                        if (!cachedWeather.sunset.isEmpty()) {
                            textViewSunset.text = formatTimeBasedOnSettings(cachedWeather.sunset)
                        }
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
                        hourlyForecastAdapter.notifyDataSetChanged()
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
    @SuppressLint("UseKtx", "SetTextI18n")
    private fun applyWeatherTips(weatherData: WeatherData) {
        try {
            val weatherContentLayout = findViewById<ViewGroup>(R.id.weatherContentLayout)
            var tipsContainer = findViewById<LinearLayout>(R.id.layoutWeatherTips)
            if (tipsContainer == null) {
                tipsContainer = LinearLayout(this)
                tipsContainer.id = R.id.layoutWeatherTips
                tipsContainer.orientation = LinearLayout.VERTICAL
                tipsContainer.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                weatherContentLayout?.addView(tipsContainer)
                } else {
                tipsContainer.removeAllViews()
            }
                val backgroundResId = BackgroundManager.getCurrentBackgroundResourceId(weatherData, isNightTime())
            val isDarkBackground = when (backgroundResId) {
                R.drawable.background_night_thunderstorm,
                R.drawable.background_night_foggy,
                R.drawable.background_night_snowy,
                R.drawable.background_night_rainy,
                R.drawable.background_night_cloudy,
                R.drawable.background_night_clear -> true
                R.drawable.background_day_thunderstorm,
                R.drawable.background_day_rainy -> true
                else -> false
            }
                val titleTextColor = if (isDarkBackground) {
                    ContextCompat.getColor(this, R.color.white)
                } else {
                    ContextCompat.getColor(this, R.color.black)
                }
                val titleTextView = TextView(this)
                titleTextView.text = "Weather Tips"
                titleTextView.setTextColor(titleTextColor)
                titleTextView.textSize = 22f
                titleTextView.setPadding(16, 32, 16, 16)
                titleTextView.typeface = Typeface.DEFAULT_BOLD
            if (!isDarkBackground) {
                titleTextView.setShadowLayer(1.5f, 0.5f, 0.5f, Color.parseColor("#50000000"))
                } else {
                titleTextView.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                }
                tipsContainer.addView(titleTextView)
            val tipsList = mutableListOf<Pair<View, Boolean>>()
            var highPriorityCount = 0
            val isHighUV = weatherData.uvIndex > 5
            if (isHighUV) highPriorityCount++
            tipsList.add(Pair(createTipView(getUVIndexTip(weatherData.uvIndex, weatherData.sunrise, weatherData.sunset), isHighUV), isHighUV))
            val windSpeedKmh = weatherData.windSpeed * 3.6
            val isStrongWind = windSpeedKmh > 38
            if (isStrongWind) highPriorityCount++
            tipsList.add(Pair(createTipView(getWindSpeedTip(weatherData.windSpeed), isStrongWind), isStrongWind))
            val isHighRain = weatherData.precipProbability > 60
            if (isHighRain) highPriorityCount++
            tipsList.add(Pair(createTipView(getPrecipitationTip(weatherData.precipProbability), isHighRain), isHighRain))
            val isExtremeHumidity = weatherData.humidity > 75 || weatherData.humidity < 25
            if (isExtremeHumidity) highPriorityCount++
            tipsList.add(Pair(createTipView(getHumidityTip(weatherData.humidity), isExtremeHumidity), isExtremeHumidity))
            val tempDiff = abs(weatherData.temperature - weatherData.feelsLike)
            val isSignificantFeelsLike = tempDiff > 5
            if (isSignificantFeelsLike) highPriorityCount++
            tipsList.add(Pair(createTipView(getFeelsLikeTip(weatherData.temperature, weatherData.feelsLike), isSignificantFeelsLike), isSignificantFeelsLike))
            tipsList.add(Pair(createTipView(getTimeOfDayTip(weatherData.sunrise, weatherData.sunset), false), false))
            tipsList.sortByDescending { it.second }
            val maxTips = 6
            val tipsToShow = if (tipsList.size > maxTips) {
                val highPriorityTips = tipsList.filter { it.second }.map { it.first }
                val normalPriorityTips = tipsList.filter { !it.second }.map { it.first }
                if (highPriorityTips.size >= maxTips) {
                    highPriorityTips.take(maxTips)
                } else {
                    highPriorityTips + normalPriorityTips.take(maxTips - highPriorityTips.size)
                }
            } else {
                tipsList.map { it.first }
            }
            for (i in tipsToShow.indices) {
                val view = tipsToShow[i]
                tipsContainer.addView(view)
                view.alpha = 0f
                view.translationY = 50f
                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(i * 100L)
                    .start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error applying weather tips", e)
        }
    }
    @SuppressLint("UseKtx", "DefaultLocale")
    private fun createTipView(tip: Pair<String, Int>, isHighPriority: Boolean): View {
        val cardView = CardView(this)
        cardView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
        }
        cardView.radius = 24f
        cardView.cardElevation = if (isHighPriority) 4f else 2f
        val startColor: Int
        val endColor: Int
        var forceDarkText = false
        when (tip.second) {
            R.drawable.ic_wb_sunny -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_sunny_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_sunny_gradient_end)
                forceDarkText = true
            }
            R.drawable.ic_warning -> {
                startColor = ColorUtils.blendARGB(
                    ContextCompat.getColor(this, R.color.error),
                    ContextCompat.getColor(this, R.color.weather_card_sunny_gradient_start),
                    0.2f
                )
                endColor = ContextCompat.getColor(this, R.color.error)
            }
            R.drawable.ic_water_drop -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_day_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_day_gradient_end)
                val precipText = tip.first.toLowerCase()
                if (precipText.contains("very low") || precipText.contains("low chance")) {
                    forceDarkText = true
                }
            }
            R.drawable.ic_opacity -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_cloudy_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_cloudy_gradient_end)
                forceDarkText = true
            }
            R.drawable.ic_thermostat -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_sunny_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_sunny_gradient_end)
                forceDarkText = true
            }
            R.drawable.ic_air -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_cloudy_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_cloudy_gradient_end)
                forceDarkText = true
            }
            R.drawable.ic_nightlight -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_night_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_night_gradient_end)
            }
            else -> {
                startColor = ContextCompat.getColor(this, R.color.weather_card_day_gradient_start)
                endColor = ContextCompat.getColor(this, R.color.weather_card_day_gradient_end)
            }
        }
        val finalStartColor = if (isHighPriority) {
            ColorUtils.blendARGB(startColor, Color.WHITE, 0.15f)
        } else {
            startColor
        }
        val finalEndColor = if (isHighPriority) {
            ColorUtils.blendARGB(endColor, Color.BLACK, 0.1f)
        } else {
            endColor
        }
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(finalStartColor, finalEndColor)
        )
        gradientDrawable.cornerRadius = 24f
        val isDarkBackground = when {
            forceDarkText -> false
            tip.second == R.drawable.ic_nightlight -> true
            tip.second == R.drawable.ic_warning -> true
            tip.second == R.drawable.ic_water_drop && !forceDarkText -> true
            isHighPriority && tip.second == R.drawable.ic_opacity -> true
            else -> false
        }
        val textColor = if (isDarkBackground) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.HORIZONTAL
        contentLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        contentLayout.setPadding(24, 20, 24, 20)
        contentLayout.background = gradientDrawable
        contentLayout.gravity = Gravity.CENTER_VERTICAL
        val iconContainer = FrameLayout(this)
        val iconSize = 48.dpToPx()
        iconContainer.layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
            marginEnd = 16.dpToPx()
        }
        val circleDrawable = GradientDrawable()
        circleDrawable.shape = GradientDrawable.OVAL
        circleDrawable.setColor(if (isDarkBackground) Color.WHITE else Color.parseColor("#F5F5F5"))
        circleDrawable.alpha = 220
        iconContainer.background = circleDrawable
        val iconView = ImageView(this)
        iconView.layoutParams = FrameLayout.LayoutParams(
            (iconSize * 0.6f).toInt(),
            (iconSize * 0.6f).toInt(),
            Gravity.CENTER
        )
        iconView.setImageResource(tip.second)
        val iconColor = when (tip.second) {
            R.drawable.ic_wb_sunny -> ContextCompat.getColor(this, R.color.icon_sun_color)
            R.drawable.ic_warning -> ContextCompat.getColor(this, R.color.error)
            R.drawable.ic_water_drop -> ContextCompat.getColor(this, R.color.icon_rain_color)
            R.drawable.ic_opacity -> ContextCompat.getColor(this, R.color.primary_light)
            R.drawable.ic_thermostat -> ContextCompat.getColor(this, R.color.accent)
            R.drawable.ic_air -> ContextCompat.getColor(this, R.color.icon_cloud_shadow)
            R.drawable.ic_nightlight -> ContextCompat.getColor(this, R.color.weather_card_night)
            else -> ContextCompat.getColor(this, R.color.primary)
        }
        iconView.setColorFilter(iconColor)
        iconContainer.addView(iconView)
        val textLayout = LinearLayout(this)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        if (isHighPriority) {
            val warningIndicator = TextView(this)
            warningIndicator.text = if (tip.second == R.drawable.ic_warning) "⚠ IMPORTANT" else "• IMPORTANT"
            warningIndicator.setTextColor(textColor)
            warningIndicator.alpha = 0.9f
            warningIndicator.textSize = 12f
            warningIndicator.setTypeface(warningIndicator.typeface, Typeface.BOLD)
            warningIndicator.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4.dpToPx()
            }
            if (!isDarkBackground) {
                warningIndicator.setShadowLayer(1.5f, 0.5f, 0.5f, Color.parseColor("#50000000"))
            } else {
                warningIndicator.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
            }
            textLayout.addView(warningIndicator)
        }
        val textView = TextView(this)
        textView.text = tip.first
        textView.setTextColor(textColor)
        textView.textSize = 16f
        if (isHighPriority) {
            textView.setTypeface(textView.typeface, Typeface.BOLD)
        }
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        if (!isDarkBackground) {
            textView.setShadowLayer(1.5f, 0.5f, 0.5f, Color.parseColor("#50000000"))
        } else {
            textView.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
        textLayout.addView(textView)
        contentLayout.addView(iconContainer)
        contentLayout.addView(textLayout)
        cardView.addView(contentLayout)
        val outValue = TypedValue()
        this.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        cardView.foreground = ContextCompat.getDrawable(this, outValue.resourceId)
        return cardView
    }
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    private fun getUVIndexTip(uvIndex: Int, sunrise: String, sunset: String): Pair<String, Int> {
        val sunriseTime = parseTimeString(sunrise)
        val sunsetTime = parseTimeString(sunset)
        val sunriseHour = sunriseTime.get(Calendar.HOUR_OF_DAY)
        val sunsetHour = sunsetTime.get(Calendar.HOUR_OF_DAY)
        val peakStartHour = (sunriseHour + 2).coerceAtMost(sunsetHour - 3)
        val peakEndHour = (sunsetHour - 2).coerceAtLeast(peakStartHour + 2)
        val timeFormat = if (com.example.weatherapp.util.Settings.use24HourFormat) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("h:mm a", Locale.getDefault())
        }
        val peakStartCal = Calendar.getInstance()
        peakStartCal.set(Calendar.HOUR_OF_DAY, peakStartHour)
        peakStartCal.set(Calendar.MINUTE, 0)
        val peakEndCal = Calendar.getInstance()
        peakEndCal.set(Calendar.HOUR_OF_DAY, peakEndHour)
        peakEndCal.set(Calendar.MINUTE, 0)
        val peakStartTime = timeFormat.format(peakStartCal.time)
        val peakEndTime = timeFormat.format(peakEndCal.time)
        return when {
            uvIndex <= 2 -> Pair(
                "Low UV Index ($uvIndex). No protection needed for most skin types.", 
                R.drawable.ic_wb_sunny
            )
            uvIndex <= 5 -> Pair(
                "Moderate UV Index ($uvIndex). Apply SPF 30+ sunscreen when outside.", 
                R.drawable.ic_wb_sunny
            )
            uvIndex <= 7 -> Pair(
                "High UV Index ($uvIndex). Apply sunscreen regularly and seek shade during midday hours ($peakStartTime-$peakEndTime).", 
                R.drawable.ic_warning
            )
            uvIndex <= 10 -> Pair(
                "Very High UV Index ($uvIndex). Minimize sun exposure between $peakStartTime-$peakEndTime.", 
                R.drawable.ic_warning
            )
            else -> Pair(
                "Extreme UV Index ($uvIndex). Avoid outdoor activities during peak hours ($peakStartTime-$peakEndTime).", 
                R.drawable.ic_warning
            )
        }
    }
    private fun getWindSpeedTip(windSpeed: Double): Pair<String, Int> {
        val windSpeedUnit = com.example.weatherapp.util.Settings.windSpeedUnit
        val convertedSpeed = windSpeedUnit.convert(windSpeed)
        val formattedSpeed = windSpeedUnit.format(convertedSpeed)
        val lowThreshold = when(windSpeedUnit) {
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPS -> 5.5
            com.example.weatherapp.util.Settings.WindSpeedUnit.KPH -> 20.0
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPH -> 12.5
        }
        val mediumThreshold = when(windSpeedUnit) {
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPS -> 10.5
            com.example.weatherapp.util.Settings.WindSpeedUnit.KPH -> 38.0
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPH -> 23.5
        }
        val highThreshold = when(windSpeedUnit) {
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPS -> 17.0
            com.example.weatherapp.util.Settings.WindSpeedUnit.KPH -> 62.0
            com.example.weatherapp.util.Settings.WindSpeedUnit.MPH -> 38.5
        }
        return when {
            convertedSpeed < lowThreshold -> Pair(
                "Light wind ($formattedSpeed). Perfect for outdoor activities.", 
                R.drawable.ic_air
            )
            convertedSpeed < mediumThreshold -> Pair(
                "Moderate wind ($formattedSpeed). May affect light outdoor activities.", 
                R.drawable.ic_air
            )
            convertedSpeed < highThreshold -> Pair(
                "Strong wind ($formattedSpeed). Be careful of flying objects and debris.", 
                R.drawable.ic_warning
            )
            else -> Pair(
                "High wind ($formattedSpeed). Consider staying indoors for safety.", 
                R.drawable.ic_warning
            )
        }
    }
    private fun getHumidityTip(humidity: Int): Pair<String, Int> {
        return when {
            humidity < 30 -> Pair(
                "Low humidity ($humidity%). Stay hydrated to prevent dry skin and eyes.", 
                R.drawable.ic_opacity
            )
            humidity in 30..50 -> Pair(
                "Comfortable humidity level ($humidity%). Ideal conditions for outdoor activities.", 
                R.drawable.ic_opacity
            )
            humidity in 51..70 -> Pair(
                "Moderate humidity ($humidity%). May feel slightly muggy outdoors.", 
                R.drawable.ic_opacity
            )
            else -> Pair(
                "High humidity ($humidity%). May cause discomfort and excess sweating.", 
                R.drawable.ic_opacity
            )
        }
    }
    private fun getFeelsLikeTip(actual: Double, feelsLike: Double): Pair<String, Int> {
        val tempUnit = com.example.weatherapp.util.Settings.temperatureUnit
        val actualTemp = tempUnit.convert(actual).toInt()
        val feelsLikeTemp = tempUnit.convert(feelsLike).toInt()
        val diff = feelsLikeTemp - actualTemp
        val formattedActualTemp = tempUnit.format(tempUnit.convert(actual))
        val formattedFeelsLikeTemp = tempUnit.format(tempUnit.convert(feelsLike))
        return when {
            abs(diff) < 2 -> Pair(
                "Temperature feels like actual ($formattedFeelsLikeTemp).", 
                R.drawable.ic_thermostat
            )
            diff > 2 -> Pair(
                "Feels warmer ($formattedFeelsLikeTemp) than actual ($formattedActualTemp).", 
                R.drawable.ic_thermostat
            )
            else -> Pair(
                "Feels colder ($formattedFeelsLikeTemp) than actual ($formattedActualTemp).", 
                R.drawable.ic_thermostat
            )
        }
    }
    private fun getPrecipitationTip(precipProb: Int): Pair<String, Int> {
        return when {
            precipProb < 10 -> Pair(
                "Very low chance of rain ($precipProb%). Expected to stay dry.", 
                R.drawable.ic_water_drop
            )
            precipProb < 30 -> Pair(
                "Low chance of precipitation ($precipProb%). Unlikely to need an umbrella.", 
                R.drawable.ic_water_drop
            )
            precipProb < 60 -> Pair(
                "Moderate chance of rain ($precipProb%). Consider bringing a rain jacket.", 
                R.drawable.ic_water_drop
            )
            else -> Pair(
                "High chance of precipitation ($precipProb%). Umbrella recommended.", 
                R.drawable.ic_water_drop
            )
        }
    }
    private fun getTimeOfDayTip(sunrise: String, sunset: String): Pair<String, Int> {
        val currentTime = Calendar.getInstance()
        val sunriseTime = parseTimeString(sunrise)
        val sunsetTime = parseTimeString(sunset)
        val midday = Calendar.getInstance()
        midday.set(Calendar.HOUR_OF_DAY, (sunriseTime.get(Calendar.HOUR_OF_DAY) + sunsetTime.get(Calendar.HOUR_OF_DAY)) / 2)
        midday.set(Calendar.MINUTE, (sunriseTime.get(Calendar.MINUTE) + sunsetTime.get(Calendar.MINUTE)) / 2)
        val use24Hour = com.example.weatherapp.util.Settings.use24HourFormat
        val currentTimeFormat = if (use24Hour) {
            SimpleDateFormat("HH:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("h:mm a", Locale.getDefault())
        }
        val currentTimeFormatted = currentTimeFormat.format(currentTime.time)
        val formattedSunrise = formatTimeBasedOnSettings(sunrise)
        val formattedSunset = formatTimeBasedOnSettings(sunset)
        return when {
            currentTime.before(sunriseTime) -> Pair(
                "Pre-dawn at $currentTimeFormatted. Sunrise at $formattedSunrise.", 
                R.drawable.ic_nightlight
            )
            currentTime.before(midday) -> Pair(
                "Morning at $currentTimeFormatted. UV protection recommended.", 
                R.drawable.ic_wb_sunny
            )
            currentTime.before(sunsetTime) -> Pair(
                "Afternoon at $currentTimeFormatted. Stay hydrated.", 
                R.drawable.ic_wb_sunny
            )
            else -> Pair(
                "Evening at $currentTimeFormatted. Sunset was at $formattedSunset.", 
                R.drawable.ic_nightlight
            )
        }
    }
    @SuppressLint("DefaultLocale", "UseKtx")
    private fun updateUIBasedOnWeatherCondition(weatherData: WeatherData) {
        try {
            val backgroundResId = when {
                weatherData.conditions.toLowerCase().contains("thunder") || 
                weatherData.conditions.toLowerCase().contains("storm") || 
                weatherData.conditions.toLowerCase().contains("lightning") -> {
                    if (isNightTime()) R.drawable.background_night_thunderstorm else R.drawable.background_day_thunderstorm
                }
                weatherData.conditions.toLowerCase().contains("fog") || 
                weatherData.conditions.toLowerCase().contains("mist") || 
                weatherData.conditions.toLowerCase().contains("haze") -> {
                    if (isNightTime()) R.drawable.background_night_foggy else R.drawable.background_day_foggy
                }
                weatherData.conditions.toLowerCase().contains("snow") || 
                weatherData.conditions.toLowerCase().contains("sleet") || 
                weatherData.conditions.toLowerCase().contains("ice") || 
                weatherData.conditions.toLowerCase().contains("blizzard") -> {
                    if (isNightTime()) R.drawable.background_night_snowy else R.drawable.background_day_snowy
                }
                weatherData.conditions.toLowerCase().contains("rain") || 
                weatherData.conditions.toLowerCase().contains("drizzle") || 
                weatherData.conditions.toLowerCase().contains("shower") -> {
                    if (isNightTime()) R.drawable.background_night_rainy else R.drawable.background_day_rainy
                }
                weatherData.conditions.toLowerCase().contains("cloud") || 
                weatherData.conditions.toLowerCase().contains("overcast") -> {
                    if (isNightTime()) R.drawable.background_night_cloudy else R.drawable.background_day_cloudy
                }
                else -> {
                    if (isNightTime()) R.drawable.background_night_clear else R.drawable.background_day_clear
                }
            }
            rootLayout.setBackgroundResource(backgroundResId)
            window.statusBarColor = Color.TRANSPARENT
            val isDarkBackground = when (backgroundResId) {
                R.drawable.background_night_thunderstorm,
                R.drawable.background_night_foggy,
                R.drawable.background_night_snowy,
                R.drawable.background_night_rainy,
                R.drawable.background_night_cloudy,
                R.drawable.background_night_clear -> true
                R.drawable.background_day_thunderstorm,
                R.drawable.background_day_rainy -> true
                else -> false
            }
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
            val shouldAddShadow = !isDarkBackground
            val allViews = listOf(
                textViewLocation,
                textViewCountry,
                textViewDate,
                textViewTemperature,
                textViewDegree,
                textViewWeatherDescription,
                textViewDetailedDescription,
                textViewHumidity,
                textViewWind,
                textViewPressure,
                textViewTempMin,
                textViewTempMax,
                textViewFeelsLike,
                textViewSunrise,
                textViewSunset,
                textViewUVIndex,
                textViewVisibility,
                textViewCloudCover,
                textViewPrecipitation,
                textViewSnow,
                textViewSnowDepth,
                textViewDewPoint,
                textViewWindDirection,
                textViewWindGust,
                textViewSolarRadiation,
                textViewMoonPhase,
                tabToday,
                tabNext5Days
            )
            allViews.forEach { view ->
                view.setTextColor(textColor)
                if (shouldAddShadow) {
                    view.setShadowLayer(1.5f, 0.5f, 0.5f, Color.parseColor("#50000000"))
                } else {
                    view.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                }
            }
            tabNext5Days.compoundDrawables.forEach { drawable ->
                drawable?.setTint(textColor)
            }
            tabIndicator.setBackgroundColor(textColor)
            val labelTextViews = listOf(
                findViewById<TextView>(R.id.textTempMaxLabel),
                findViewById<TextView>(R.id.textTempMinLabel),
                findViewById<TextView>(R.id.textFeelsLikeLabel),
                findViewById<TextView>(R.id.textSunriseLabel),
                findViewById<TextView>(R.id.textSunsetLabel),
                findViewById<TextView>(R.id.textUVIndexLabel),
                findViewById<TextView>(R.id.textHumidityLabel),
                findViewById<TextView>(R.id.textWindLabel),
                findViewById<TextView>(R.id.textPressureLabel),
                findViewById<TextView>(R.id.textVisibilityLabel),
                findViewById<TextView>(R.id.textCloudCoverLabel),
                findViewById<TextView>(R.id.textPrecipitationLabel),
                findViewById<TextView>(R.id.textSnowLabel),
                findViewById<TextView>(R.id.textSnowDepthLabel),
                findViewById<TextView>(R.id.textDewPointLabel),
                findViewById<TextView>(R.id.textWindDirectionLabel),
                findViewById<TextView>(R.id.textWindGustLabel),
                findViewById<TextView>(R.id.textSolarRadiationLabel),
                findViewById<TextView>(R.id.textMoonPhaseLabel)
            )
            labelTextViews.forEach { label ->
                try {
                    label?.let {
                        it.setTextColor(textColor)
                        if (shouldAddShadow) {
                            it.setShadowLayer(1.5f, 0.5f, 0.5f, Color.parseColor("#50000000"))
                        } else {
                            it.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating label color: ${e.message}")
                }
            }
            val iconViews = listOf(buttonSearch, buttonMenu, buttonLocation)
            iconViews.forEach { icon ->
                icon.setColorFilter(textColor)
                if (shouldAddShadow) {
                    icon.elevation = 4f
                } else {
                    icon.elevation = 0f
                }
            }
            hourlyForecastAdapter.updateTextColor(textColor, isDarkBackground)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }
} 