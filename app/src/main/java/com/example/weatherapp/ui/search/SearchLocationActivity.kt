@file:Suppress("DEPRECATION")

package com.example.weatherapp.ui.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.model.LocationData
import com.example.weatherapp.model.SearchHistoryItem
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.network.WeatherRepository
import com.example.weatherapp.util.NetworkManager
import com.example.weatherapp.util.SearchHistoryManager
import java.io.IOException

class SearchLocationActivity : AppCompatActivity(), SearchLocationAdapter.SearchItemClickListener {
    
    private lateinit var searchEditText: EditText
    private lateinit var cancelButton: TextView
    private lateinit var clearButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchLocationAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    
    private lateinit var searchHistoryManager: SearchHistoryManager
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var networkManager: NetworkManager
    private lateinit var locationManager: LocationManager
    
    private var currentLocation: LocationData? = null
    private var searchTask: AsyncTask<String, Void, SearchResult>? = null
    
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1002
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_location)
        searchHistoryManager = SearchHistoryManager(this)
        weatherRepository = WeatherRepository(this)
        networkManager = NetworkManager(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        initializeViews()
        setupListeners()
        loadSearchHistory()

        if (hasLocationPermission()) {
            getCurrentLocation()
        }
    }
    
    private fun initializeViews() {
        searchEditText = findViewById(R.id.editTextSearch)
        cancelButton = findViewById(R.id.textViewCancel)
        clearButton = findViewById(R.id.buttonClear)
        recyclerView = findViewById(R.id.recyclerViewSearchHistory)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.textViewError)
        adapter = SearchLocationAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        cancelButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        
        clearButton.setOnClickListener {
            searchEditText.text.clear()
        }
        
        findViewById<TextView>(R.id.buttonClearHistory).setOnClickListener {
            searchHistoryManager.clearSearchHistory()
            loadSearchHistory()
        }
        
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }
    
    private fun loadSearchHistory() {
        val historyItems = mutableListOf<SearchHistoryItem>()
        
        if (currentLocation != null) {
            historyItems.add(SearchHistoryItem(getString(R.string.current_location), true))
        }
        
        val searchHistory = searchHistoryManager.getSearchHistory()
        for (item in searchHistory) {
            historyItems.add(SearchHistoryItem(item))
        }
        
        adapter.submitList(historyItems)
    }
    
    private fun performSearch(query: String) {
        if (!networkManager.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.error_offline_search), Toast.LENGTH_SHORT).show()
            return
        }
        
        searchTask?.cancel(true)
        
        showLoading()
        
        searchTask = SearchAsyncTask().apply {
            execute(query)
        }
    }
    
    @SuppressLint("StaticFieldLeak")
    private inner class SearchAsyncTask : AsyncTask<String, Void, SearchResult>() {
        private var searchQuery: String = ""
        
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: String): SearchResult {
            searchQuery = params[0]
            
            return try {
                val weather = weatherRepository.fetchWeather(searchQuery)
                SearchResult.Success(weather)
            } catch (_: IOException) {
                SearchResult.Error(R.string.error_network)
            } catch (_: Exception) {
                SearchResult.Error(R.string.error_general)
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: SearchResult) {
            hideLoading()
            
            when (result) {
                is SearchResult.Success -> {
                    searchHistoryManager.addSearchQuery(result.data.location)
                    
                    searchHistoryManager.saveLastSelectedLocation(result.data.location)
                    
                    val resultIntent = Intent()
                    resultIntent.putExtra("LOCATION", result.data.location)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                is SearchResult.Error -> {
                    errorText.text = getString(result.messageResId)
                    errorText.visibility = View.VISIBLE
                }
            }
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
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            if (lastLocation != null) {
                currentLocation = LocationData(lastLocation.latitude, lastLocation.longitude)
                
                loadSearchHistory()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
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
            }
        }
    }
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
    
    override fun onItemClick(item: SearchHistoryItem) {
        if (item.isCurrentLocation) {
            if (currentLocation != null) {
                GetLocationNameTask().execute(currentLocation!!)
            }
        } else {
            searchHistoryManager.saveLastSelectedLocation(item.name)
            
            val resultIntent = Intent()
            resultIntent.putExtra("LOCATION", item.name)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    
    override fun onAddClick(item: SearchHistoryItem) {
    }
    
    override fun onRemoveClick(item: SearchHistoryItem) {
        if (!item.isCurrentLocation) {
            searchHistoryManager.removeSearchQuery(item.name)
            loadSearchHistory()
        }
    }
    
    @SuppressLint("StaticFieldLeak")
    private inner class GetLocationNameTask : AsyncTask<LocationData, Void, String>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: LocationData): String {
            val location = params[0]
            return try {
                weatherRepository.getLocationName(location.latitude, location.longitude)
            } catch (_: Exception) {
                getString(R.string.default_location)
            }
        }
        
        @Deprecated("Deprecated in Java")
        override fun onPostExecute(locationName: String) {
            searchHistoryManager.saveLastSelectedLocation(locationName)
            val resultIntent = Intent()
            resultIntent.putExtra("LOCATION", locationName)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    
    sealed class SearchResult {
        data class Success(val data: WeatherData) : SearchResult()
        data class Error(val messageResId: Int) : SearchResult()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
} 
 