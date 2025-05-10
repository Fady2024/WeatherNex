package com.example.weatherapp.ui.weather

import android.content.Context
import com.example.weatherapp.base.BasePresenter
import com.example.weatherapp.base.BaseView
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.model.LocationData
import com.example.weatherapp.model.HourlyForecastData

interface WeatherContract {
    interface View : BaseView {
        override fun showLoading()
        override fun hideLoading()
        fun updateWeatherData(weather: WeatherData)
        fun updateHourlyForecast(hourlyForecasts: List<HourlyForecastData>)
        fun updateLocationName(location: String)
        override fun showError(message: String)
        override fun showOfflineBanner(message: String?)
        override fun hideOfflineBanner()
        fun showSearchInput()
        fun hideSearchInput()
        val context: Context
    }

    interface Presenter : BasePresenter<View> {
        override fun attachView(view: View)
        override fun detachView()
        override fun isViewAttached(): Boolean
        fun loadWeatherData(location: String?)
        fun refreshWeatherData()
        fun searchLocation(query: String)
        fun updateLocation(location: LocationData)
        fun handleLocationPermissionResult(granted: Boolean)
        fun loadCachedData(location: String)
    }
} 
 
 
 
 
 