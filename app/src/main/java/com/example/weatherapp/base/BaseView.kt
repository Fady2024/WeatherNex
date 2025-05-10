package com.example.weatherapp.base

interface BaseView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showOfflineBanner(message: String? = null)
    fun hideOfflineBanner()
} 
 
 
 
 
 