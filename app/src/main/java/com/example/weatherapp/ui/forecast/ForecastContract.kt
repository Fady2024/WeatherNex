package com.example.weatherapp.ui.forecast

import android.content.Context
import com.example.weatherapp.base.BasePresenter
import com.example.weatherapp.base.BaseView
import com.example.weatherapp.model.ForecastResponse

interface ForecastContract {
    interface View : BaseView {
        override fun showLoading()
        override fun hideLoading()
        fun updateForecast(forecast: ForecastResponse)
        fun updateLocationName(location: String)
        override fun showError(message: String)
        override fun showOfflineBanner(message: String?)
        override fun hideOfflineBanner()
        val context: Context
    }

    interface Presenter : BasePresenter<View> {
        override fun attachView(view: View)
        override fun detachView()
        override fun isViewAttached(): Boolean
        fun loadForecastData(location: String?)
        fun refreshForecastData()
    }
} 
 
 
 
 
 