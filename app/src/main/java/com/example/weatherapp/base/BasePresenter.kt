package com.example.weatherapp.base

interface BasePresenter<V> {
    fun attachView(view: V)
    fun detachView()
    fun isViewAttached(): Boolean
} 
 
 
 
 
 