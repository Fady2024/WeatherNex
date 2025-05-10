package com.example.weatherapp.ui.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.weatherapp.R
import com.example.weatherapp.util.Settings

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity(), SettingsContract.View {
    private lateinit var presenter: SettingsContract.Presenter
    private lateinit var backButton: ImageButton
    
    private lateinit var textTemperatureUnit: TextView
    private lateinit var textWindSpeedUnit: TextView
    private lateinit var textPressureUnit: TextView
    private lateinit var textTimeFormat: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        presenter = SettingsPresenter()
        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.buttonBack)
        textTemperatureUnit = findViewById(R.id.textTemperatureUnit)
        textWindSpeedUnit = findViewById(R.id.textWindSpeedUnit)
        textPressureUnit = findViewById(R.id.textPressureUnit)
        textTimeFormat = findViewById(R.id.textTimeFormat)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    fun onTemperatureUnitClick(view: View) {
        val options = arrayOf("°C", "°F")
        val currentUnit = textTemperatureUnit.text.toString()
        val initialSelection = options.indexOf(currentUnit)
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val unit = when (index) {
                0 -> Settings.TemperatureUnit.CELSIUS
                1 -> Settings.TemperatureUnit.FAHRENHEIT
                else -> Settings.TemperatureUnit.CELSIUS
            }
            presenter.setTemperatureUnit(unit)
            Toast.makeText(this, "Temperature unit changed to ${options[index]}", Toast.LENGTH_SHORT).show()
        }
    }

    fun onWindSpeedUnitClick(view: View) {
        val options = arrayOf("m/s", "km/h", "mph")
        val currentUnit = textWindSpeedUnit.text.toString()
        val initialSelection = options.indexOf(currentUnit)
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val unit = when (index) {
                0 -> Settings.WindSpeedUnit.MPS
                1 -> Settings.WindSpeedUnit.KPH
                2 -> Settings.WindSpeedUnit.MPH
                else -> Settings.WindSpeedUnit.MPS
            }
            presenter.setWindSpeedUnit(unit)
            Toast.makeText(this, "Wind speed unit changed to ${options[index]}", Toast.LENGTH_SHORT).show()
        }
    }

    fun onPressureUnitClick(view: View) {
        val options = arrayOf("atm", "hPa", "inHg")
        val currentUnit = textPressureUnit.text.toString()
        val initialSelection = options.indexOf(currentUnit)
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val unit = when (index) {
                0 -> Settings.PressureUnit.ATM
                1 -> Settings.PressureUnit.HPA
                2 -> Settings.PressureUnit.INHG
                else -> Settings.PressureUnit.ATM
            }
            presenter.setPressureUnit(unit)
            Toast.makeText(this, "Pressure unit changed to ${options[index]}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun onTimeFormatClick(view: View) {
        val options = arrayOf("12-hour (AM/PM)", "24-hour")
        val initialSelection = if (Settings.use24HourFormat) 1 else 0
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val use24Hour = index == 1
            presenter.setTimeFormat(use24Hour)
            Toast.makeText(this, "Time format changed to ${options[index]}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("UseKtx", "InflateParams")
    private fun showAnimatedDropdown(anchor: View, options: Array<String>, initialSelection: Int, onItemSelected: (Int) -> Unit) {
        val menuView = layoutInflater.inflate(R.layout.dropdown_menu, null)
        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val option1 = menuView.findViewById<TextView>(R.id.option1)
        val option2 = menuView.findViewById<TextView>(R.id.option2)
        
        option1.text = options[0]
        option2.text = options[1]

        val valueText = when (anchor.id) {
            R.id.temperatureUnitSelector -> anchor.findViewById<TextView>(R.id.textTemperatureUnit)
            R.id.windSpeedUnitSelector -> anchor.findViewById<TextView>(R.id.textWindSpeedUnit)
            R.id.pressureUnitSelector -> anchor.findViewById<TextView>(R.id.textPressureUnit)
            else -> anchor.findViewById<TextView>(R.id.textTimeFormat)
        }

        val selectedTextColor = valueText?.currentTextColor ?: ContextCompat.getColor(this, android.R.color.black)
        val normalTextColor = Color.parseColor("#757575")
        
        option1.setTextColor(if (initialSelection == 0) selectedTextColor else normalTextColor)
        option2.setTextColor(if (initialSelection == 1) selectedTextColor else normalTextColor)

        option1.setOnClickListener {
            onItemSelected(0)
            popupWindow.dismiss()
        }
        option2.setOnClickListener {
            onItemSelected(1)
            popupWindow.dismiss()
        }

        popupWindow.animationStyle = R.style.DropdownAnimation

        try {
            val paint = Paint()
            paint.textSize = option1.textSize
            
            val option1Width = paint.measureText(options[0])
            val option2Width = paint.measureText(options[1])
            val maxOptionWidth = maxOf(option1Width, option2Width)
            val totalWidth = (maxOptionWidth + 48).toInt()
            val minWidth = (valueText?.width ?: 0) + 48
            val finalWidth = maxOf(totalWidth, minWidth)
            popupWindow.width = finalWidth
            val xOffset = valueText?.left ?: 0
            popupWindow.showAsDropDown(anchor, xOffset, 0)
            
        } catch (_: Exception) {
            popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT
            popupWindow.showAsDropDown(anchor)
        }
    }

    override fun updateTemperatureUnit(unit: Settings.TemperatureUnit) {
        textTemperatureUnit.text = when (unit) {
            Settings.TemperatureUnit.CELSIUS -> "°C"
            Settings.TemperatureUnit.FAHRENHEIT -> "°F"
        }
    }

    override fun updateWindSpeedUnit(unit: Settings.WindSpeedUnit) {
        textWindSpeedUnit.text = when (unit) {
            Settings.WindSpeedUnit.MPS -> "m/s"
            Settings.WindSpeedUnit.KPH -> "km/h"
            Settings.WindSpeedUnit.MPH -> "mph"
        }
    }

    override fun updatePressureUnit(unit: Settings.PressureUnit) {
        textPressureUnit.text = when (unit) {
            Settings.PressureUnit.ATM -> "atm"
            Settings.PressureUnit.HPA -> "hPa"
            Settings.PressureUnit.INHG -> "inHg"
        }
    }
    
    override fun updateTimeFormat(use24Hour: Boolean) {
        textTimeFormat.text = if (use24Hour) "24-hour" else "12-hour"
    }

    override fun showLoading() {
        // Not needed for settings screen
    }

    override fun hideLoading() {
        // Not needed for settings screen
    }

    override fun showError(message: String) {
        // Not needed for settings screen
    }

    override fun showOfflineBanner(message: String?) {
        // Not needed for settings screen
    }

    override fun hideOfflineBanner() {
        // Not needed for settings screen
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
} 
 
 