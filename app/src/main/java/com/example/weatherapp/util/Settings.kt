package com.example.weatherapp.util

import android.content.Context
import android.content.SharedPreferences

object Settings {
    private const val PREFS_NAME = "WeatherAppSettings"
    private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
    private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"
    private const val KEY_PRESSURE_UNIT = "pressure_unit"
    private const val KEY_TIME_FORMAT = "time_format_24h"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var temperatureUnit: TemperatureUnit
        get() = TemperatureUnit.valueOf(prefs.getString(KEY_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name) ?: TemperatureUnit.CELSIUS.name)
        set(value) = prefs.edit().putString(KEY_TEMPERATURE_UNIT, value.name).apply()

    var windSpeedUnit: WindSpeedUnit
        get() = WindSpeedUnit.valueOf(prefs.getString(KEY_WIND_SPEED_UNIT, WindSpeedUnit.KPH.name) ?: WindSpeedUnit.KPH.name)
        set(value) = prefs.edit().putString(KEY_WIND_SPEED_UNIT, value.name).apply()

    var pressureUnit: PressureUnit
        get() = PressureUnit.valueOf(prefs.getString(KEY_PRESSURE_UNIT, PressureUnit.HPA.name) ?: PressureUnit.HPA.name)
        set(value) = prefs.edit().putString(KEY_PRESSURE_UNIT, value.name).apply()
        
    var use24HourFormat: Boolean
        get() = prefs.getBoolean(KEY_TIME_FORMAT, false)
        set(value) = prefs.edit().putBoolean(KEY_TIME_FORMAT, value).apply()

    enum class TemperatureUnit {
        CELSIUS,
        FAHRENHEIT;

        fun convert(fahrenheitValue: Double): Double = when (this) {
            CELSIUS -> (fahrenheitValue - 32) * 5/9
            FAHRENHEIT -> fahrenheitValue
        }

        fun format(value: Double): String = when (this) {
            CELSIUS -> "${value.toInt()}°C"
            FAHRENHEIT -> "${value.toInt()}°F"
        }
    }

    enum class WindSpeedUnit {
        MPS,
        KPH,
        MPH;

        fun convert(kph: Double): Double = when (this) {
            MPS -> kph * 0.277778
            KPH -> kph
            MPH -> kph * 0.621371
        }

        fun format(value: Double): String = when (this) {
            MPS -> "${String.format("%.1f", value)} m/s"
            KPH -> "${value.toInt()} km/h"
            MPH -> "${value.toInt()} mph"
        }
    }

    enum class PressureUnit {
        ATM,
        HPA,
        INHG;

        fun convert(hpa: Double): Double = when (this) {
            ATM -> hpa * 0.000986923
            HPA -> hpa
            INHG -> hpa * 0.02953
        }

        fun format(value: Double): String = when (this) {
            ATM -> "${String.format("%.3f", value)} atm"
            HPA -> "${value.toInt()} hPa"
            INHG -> "${String.format("%.2f", value)} inHg"
        }
    }
} 