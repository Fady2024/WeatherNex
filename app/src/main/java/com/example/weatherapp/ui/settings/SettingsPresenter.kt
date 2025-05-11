package com.example.weatherapp.ui.settings

import com.example.weatherapp.util.Settings

class SettingsPresenter : SettingsContract.Presenter {
    private var view: SettingsContract.View? = null

    override fun attachView(view: SettingsContract.View) {
        this.view = view
        view.updateTemperatureUnit(Settings.temperatureUnit)
        view.updateWindSpeedUnit(Settings.windSpeedUnit)
        view.updatePressureUnit(Settings.pressureUnit)
        view.updateLengthUnit(Settings.lengthUnit)
        view.updateTimeFormat(Settings.use24HourFormat)
    }

    override fun detachView() {
        view = null
    }

    override fun isViewAttached(): Boolean = view != null

    override fun setTemperatureUnit(unit: Settings.TemperatureUnit) {
        Settings.setTemperatureUnit(unit)
        view?.updateTemperatureUnit(unit)
    }

    override fun setWindSpeedUnit(unit: Settings.WindSpeedUnit) {
        Settings.setWindSpeedUnit(unit)
        view?.updateWindSpeedUnit(unit)
    }

    override fun setPressureUnit(unit: Settings.PressureUnit) {
        Settings.setPressureUnit(unit)
        view?.updatePressureUnit(unit)
    }
    
    override fun setLengthUnit(unit: Settings.LengthUnit) {
        Settings.setLengthUnit(unit)
        view?.updateLengthUnit(unit)
    }
    
    override fun setTimeFormat(use24Hour: Boolean) {
        Settings.setUse24HourFormat(use24Hour)
        view?.updateTimeFormat(use24Hour)
    }
} 
 
 
 
 
 
 
 