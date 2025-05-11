package com.example.weatherapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.text.DecimalFormat

/**
 * Global app settings class
 */
object Settings {
    // Default values
    private var prefs: SharedPreferences? = null
    
    // Temperature settings
    private var _temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS
    val temperatureUnit: TemperatureUnit get() = _temperatureUnit
    
    // Time format settings
    private var _use24HourFormat: Boolean = true
    val use24HourFormat: Boolean get() = _use24HourFormat
    
    // Wind speed settings
    private var _windSpeedUnit: WindSpeedUnit = WindSpeedUnit.KPH
    val windSpeedUnit: WindSpeedUnit get() = _windSpeedUnit
    
    // Pressure settings
    private var _pressureUnit: PressureUnit = PressureUnit.MB
    val pressureUnit: PressureUnit get() = _pressureUnit
    
    // Length settings (for snow depth, etc.)
    private var _lengthUnit: LengthUnit = LengthUnit.MM
    val lengthUnit: LengthUnit get() = _lengthUnit
    
    // Notification settings
    private var _notificationsEnabled: Boolean = true

    /**
     * Initialize settings from SharedPreferences
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)
        
        // Load saved settings
        val tempUnitString = prefs?.getString("temp_unit", TemperatureUnit.CELSIUS.name) ?: TemperatureUnit.CELSIUS.name
        _temperatureUnit = TemperatureUnit.valueOf(tempUnitString)
        
        val windSpeedUnitString = prefs?.getString("wind_speed_unit", WindSpeedUnit.KPH.name) ?: WindSpeedUnit.KPH.name
        _windSpeedUnit = WindSpeedUnit.valueOf(windSpeedUnitString)
        
        val pressureUnitString = prefs?.getString("pressure_unit", PressureUnit.MB.name) ?: PressureUnit.MB.name
        _pressureUnit = PressureUnit.valueOf(pressureUnitString)
        
        val lengthUnitString = prefs?.getString("length_unit", LengthUnit.MM.name) ?: LengthUnit.MM.name
        _lengthUnit = LengthUnit.valueOf(lengthUnitString)
        
        _use24HourFormat = prefs?.getBoolean("use_24_hour_format", true) != false
        
        _notificationsEnabled = prefs?.getBoolean("notifications_enabled", true) != false
    }
    
    /**
     * Set temperature unit
     */
    @SuppressLint("UseKtx")
    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit = unit
        prefs?.edit()?.putString("temp_unit", unit.name)?.apply()
    }
    
    /**
     * Set wind speed unit
     */
    @SuppressLint("UseKtx")
    fun setWindSpeedUnit(unit: WindSpeedUnit) {
        _windSpeedUnit = unit
        prefs?.edit()?.putString("wind_speed_unit", unit.name)?.apply()
    }
    
    /**
     * Set pressure unit
     */
    @SuppressLint("UseKtx")
    fun setPressureUnit(unit: PressureUnit) {
        _pressureUnit = unit
        prefs?.edit()?.putString("pressure_unit", unit.name)?.apply()
    }
    
    /**
     * Set length unit
     */
    @SuppressLint("UseKtx")
    fun setLengthUnit(unit: LengthUnit) {
        _lengthUnit = unit
        prefs?.edit()?.putString("length_unit", unit.name)?.apply()
    }
    
    /**
     * Set time format
     */
    @SuppressLint("UseKtx")
    fun setUse24HourFormat(use24Hour: Boolean) {
        _use24HourFormat = use24Hour
        prefs?.edit()?.putBoolean("use_24_hour_format", use24Hour)?.apply()
    }

    /**
     * Temperature unit enum with conversion methods
     * Note: API returns values in Celsius when unitGroup=metric is used
     */
    enum class TemperatureUnit {
        CELSIUS,
        FAHRENHEIT;

        /**
         * Convert temperature value to the selected unit
         * @param value The temperature value in Celsius (as received from API with unitGroup=metric)
         * @return The temperature value converted to the selected unit
         */
        fun convert(value: Double): Double {
            return when (this) {
                CELSIUS -> value // No conversion needed as API sends in Celsius
                FAHRENHEIT -> value * 9.0 / 5.0 + 32.0
            }
        }
        
        fun format(value: Double): String {
            val df = DecimalFormat("#.#")
            return when (this) {
                CELSIUS -> "${df.format(value)}°C"
                FAHRENHEIT -> "${df.format(value)}°F"
            }
        }
    }
    
    /**
     * Wind speed unit enum with conversion methods
     * Note: API returns values in km/h when unitGroup=metric is used
     */
    enum class WindSpeedUnit {
        KPH,
        MPH,
        MPS;

        /**
         * Convert wind speed to the selected unit
         * @param value The wind speed in km/h (as received from API with unitGroup=metric)
         * @return The wind speed value converted to the selected unit
         */
        fun convert(value: Double): Double {
            return when (this) {
                KPH -> value // No conversion needed as API sends in km/h
                MPH -> value * 0.621371 // km/h to mph
                MPS -> value / 3.6 // km/h to m/s
            }
        }
        
        fun format(value: Double): String {
            val df = DecimalFormat("#.#")
            return when (this) {
                KPH -> "${df.format(value)} km/h"
                MPH -> "${df.format(value)} mph"
                MPS -> "${df.format(value)} m/s"
            }
        }
    }
    
    /**
     * Pressure unit enum with conversion methods
     * Note: API returns values in millibars/hPa when unitGroup=metric is used
     */
    enum class PressureUnit {
        MB,
        HPA,
        ATM,
        MMHG,
        INHG; // Added back for backward compatibility

        /**
         * Convert pressure value to the selected unit
         * @param value The pressure in millibars/hPa (as received from API with unitGroup=metric)
         * @return The pressure value converted to the selected unit
         */
        fun convert(value: Double): Double {
            return when (this) {
                MB -> value // No conversion needed
                HPA -> value // mb = hPa (they are the same)
                ATM -> value / 1013.25 // mb to atm
                MMHG -> value * 0.75006 // mb to mmHg
                INHG -> value * 0.02953 // mb to inHg
            }
        }
        
        fun format(value: Double): String {
            val df = DecimalFormat("#.#")
            return when (this) {
                MB -> "${df.format(value)} mb"
                HPA -> "${df.format(value)} hPa" 
                ATM -> "${df.format(value)} atm"
                MMHG -> "${df.format(value)} mmHg"
                INHG -> "${df.format(value)} inHg"
            }
        }
    }
    
    /**
     * Length unit enum with conversion methods for snow depth and other length measurements
     * Note: API returns snow depth in cm when unitGroup=metric is used
     */
    enum class LengthUnit {
        MM,
        CM,
        M,
        INCH,
        FEET;

        /**
         * Convert length value to the selected unit
         * @param value The length in cm (as received from API with unitGroup=metric for snow depth)
         * @return The length value converted to the selected unit
         */
        fun convert(value: Double): Double {
            return when (this) {
                MM -> value * 10.0 // cm to mm
                CM -> value // No conversion needed as API sends snow depth in cm
                M -> value / 100.0 // cm to m
                INCH -> value / 2.54 // cm to inches
                FEET -> value / 30.48 // cm to feet
            }
        }
        
        fun format(value: Double): String {
            val df = DecimalFormat("#.#")
            return when (this) {
                MM -> "${df.format(value)} mm"
                CM -> "${df.format(value)} cm"
                M -> "${df.format(value)} m"
                INCH -> "${df.format(value)} in"
                FEET -> "${df.format(value)} ft"
            }
        }
    }
} 