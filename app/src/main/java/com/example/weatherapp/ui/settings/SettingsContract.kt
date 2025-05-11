package com.example.weatherapp.ui.settings

import com.example.weatherapp.base.BasePresenter
import com.example.weatherapp.base.BaseView
import com.example.weatherapp.util.Settings

interface SettingsContract {
    interface View : BaseView {
        fun updateTemperatureUnit(unit: Settings.TemperatureUnit)
        fun updateWindSpeedUnit(unit: Settings.WindSpeedUnit)
        fun updatePressureUnit(unit: Settings.PressureUnit)
        fun updateLengthUnit(unit: Settings.LengthUnit)
        fun updateTimeFormat(use24Hour: Boolean)
    }

    interface Presenter : BasePresenter<View> {
        fun setTemperatureUnit(unit: Settings.TemperatureUnit)
        fun setWindSpeedUnit(unit: Settings.WindSpeedUnit)
        fun setPressureUnit(unit: Settings.PressureUnit)
        fun setLengthUnit(unit: Settings.LengthUnit)
        fun setTimeFormat(use24Hour: Boolean)
    }
} 
 
 
 
 
 