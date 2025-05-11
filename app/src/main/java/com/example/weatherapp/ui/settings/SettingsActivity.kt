package com.example.weatherapp.ui.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
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
    private lateinit var textLengthUnit: TextView

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
        textLengthUnit = findViewById(R.id.textLengthUnit)
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
        val options = arrayOf("mb", "hPa", "atm", "mmHg", "inHg")
        val currentUnit = textPressureUnit.text.toString()
        val initialSelection = options.indexOf(currentUnit)
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val unit = when (index) {
                0 -> Settings.PressureUnit.MB
                1 -> Settings.PressureUnit.HPA
                2 -> Settings.PressureUnit.ATM
                3 -> Settings.PressureUnit.MMHG
                4 -> Settings.PressureUnit.INHG
                else -> Settings.PressureUnit.MB
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

    fun onLengthUnitClick(view: View) {
        val options = arrayOf("mm", "cm", "m", "in", "ft")
        val currentUnit = textLengthUnit.text.toString()
        val initialSelection = options.indexOf(currentUnit)
        
        showAnimatedDropdown(view, options, initialSelection) { index ->
            val unit = when (index) {
                0 -> Settings.LengthUnit.MM
                1 -> Settings.LengthUnit.CM
                2 -> Settings.LengthUnit.M
                3 -> Settings.LengthUnit.INCH
                4 -> Settings.LengthUnit.FEET
                else -> Settings.LengthUnit.CM
            }
            presenter.setLengthUnit(unit)
            Toast.makeText(this, "Length unit changed to ${options[index]}", Toast.LENGTH_SHORT).show()
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
        ).apply {
            setBackgroundDrawable(null)
            isOutsideTouchable = true
        }

        val container = menuView.findViewById<ViewGroup>(R.id.optionContainer)
        container.removeAllViews()
        
        val optionViews = mutableListOf<TextView>()
        
        val valueText = when (anchor.id) {
            R.id.temperatureUnitSelector -> findViewById<TextView>(R.id.textTemperatureUnit)
            R.id.windSpeedUnitSelector -> findViewById<TextView>(R.id.textWindSpeedUnit)
            R.id.pressureUnitSelector -> findViewById<TextView>(R.id.textPressureUnit)
            R.id.timeFormatSelector -> findViewById<TextView>(R.id.textTimeFormat)
            R.id.lengthUnitSelector -> findViewById<TextView>(R.id.textLengthUnit)
            else -> findViewById<TextView>(R.id.textTimeFormat)
        }
        
        container.setPadding(8, 4, 8, 4)
        
        for (i in options.indices) {
            val optionView = layoutInflater.inflate(R.layout.dropdown_option_item, container, false) as TextView
            optionView.apply {
                text = options[i]
                id = View.generateViewId()
                textSize = 16f
                setPadding(16, 12, 16, 12)
                minWidth = 120
                gravity = android.view.Gravity.CENTER
                
                val selectedTextColor = valueText?.currentTextColor ?: ContextCompat.getColor(this@SettingsActivity, android.R.color.black)
                val normalTextColor = Color.parseColor("#757575")
                
                setTextColor(if (i == initialSelection) selectedTextColor else normalTextColor)
                
                if (i == initialSelection) {
                    typeface = Typeface.DEFAULT_BOLD
                }
                
                setOnClickListener {
                    onItemSelected(i)
                    popupWindow.dismiss()
                }
            }
            
            if (i > 0) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        setMargins(8, 0, 8, 0)
                    }
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                }
                container.addView(divider)
            }
            
            container.addView(optionView)
            optionViews.add(optionView)
        }

        popupWindow.animationStyle = R.style.DropdownAnimation

        val location = IntArray(2)
        valueText?.getLocationInWindow(location)
        
        val dropdownWidth = (valueText?.width ?: 120) + 120
        popupWindow.width = dropdownWidth

        val xOffset = (anchor.width - dropdownWidth)
        
        val yOffset = 16
        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
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
            Settings.PressureUnit.MB -> "mb"
            Settings.PressureUnit.HPA -> "hPa"
            Settings.PressureUnit.ATM -> "atm"
            Settings.PressureUnit.MMHG -> "mmHg"
            Settings.PressureUnit.INHG -> "inHg"
        }
    }
    
    override fun updateTimeFormat(use24Hour: Boolean) {
        textTimeFormat.text = if (use24Hour) "24-hour" else "12-hour"
    }

    override fun updateLengthUnit(unit: Settings.LengthUnit) {
        textLengthUnit.text = when (unit) {
            Settings.LengthUnit.MM -> "mm"
            Settings.LengthUnit.CM -> "cm"
            Settings.LengthUnit.M -> "m"
            Settings.LengthUnit.INCH -> "in"
            Settings.LengthUnit.FEET -> "ft"
        }
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
 
 