package com.example.weatherapp.ui.settings

import com.example.weatherapp.util.Settings

class SettingsPresenter : SettingsContract.Presenter {
    private var view: SettingsContract.View? = null

    override fun attachView(view: SettingsContract.View) {
        this.view = view
        view.updateTemperatureUnit(Settings.temperatureUnit)
        view.updateWindSpeedUnit(Settings.windSpeedUnit)
        view.updatePressureUnit(Settings.pressureUnit)
        view.updateTimeFormat(Settings.use24HourFormat)
    }

    override fun detachView() {
        view = null
    }

    override fun isViewAttached(): Boolean = view != null

    override fun setTemperatureUnit(unit: Settings.TemperatureUnit) {
        Settings.temperatureUnit = unit
        view?.updateTemperatureUnit(unit)
    }

    override fun setWindSpeedUnit(unit: Settings.WindSpeedUnit) {
        Settings.windSpeedUnit = unit
        view?.updateWindSpeedUnit(unit)
    }

    override fun setPressureUnit(unit: Settings.PressureUnit) {
        Settings.pressureUnit = unit
        view?.updatePressureUnit(unit)
    }
    
    override fun setTimeFormat(use24Hour: Boolean) {
        Settings.use24HourFormat = use24Hour
        view?.updateTimeFormat(use24Hour)
    }
} 
 
 
 
 
 
 
 