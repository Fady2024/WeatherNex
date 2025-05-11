package com.example.weatherapp.util

import android.annotation.SuppressLint
import com.example.weatherapp.model.WeatherData
import com.example.weatherapp.model.ForecastData
import com.example.weatherapp.model.ForecastResponse
import com.example.weatherapp.model.HourlyForecastData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.util.Log

class WeatherApi {
    companion object {
        private const val TAG = "WeatherAPI"
        private fun getApiKey(): String {
            return try {
                val apiKey = ApiConfig.getApiKey()
                if (apiKey.isBlank()) {
                    Log.e(TAG, "API key is blank or empty")
                    throw Exception("API key is not available")
                }
                Log.d(TAG, "Successfully retrieved API key")
                apiKey
            } catch (e: Exception) {
                Log.e(TAG, "Error getting API key from ApiConfig: ${e.message}", e)
                throw e
            }
        }
        
        private const val BASE_URL = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline"

        @SuppressLint("NewApi")
        fun fetchWeather(location: String): String {
            val encodedLocation = URLEncoder.encode(location, "UTF-8")
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val apiKey = getApiKey()
            
            val urlString = "$BASE_URL/$encodedLocation/$today/$today?key=$apiKey&include=days,hours,current&unitGroup=metric&elements=datetime,temp,humidity,windspeed,winddir,windgust,feelslike,uvindex,pressure,visibility,cloudcover,conditions,description,icon,sunrise,sunset,moonphase,dew,precip,precipprob,precipcover,preciptype,snow,snowdepth,solarradiation,solarenergy,stations,source,tempmin,tempmax,feelslikemin,feelslikemax"
            
            Log.d(TAG, "Fetching weather for: $location on $today")
            return makeApiRequest(urlString)
        }

        @SuppressLint("NewApi")
        fun fetchForecast(location: String): String {
            val encodedLocation = URLEncoder.encode(location, "UTF-8")
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val endDate = tomorrow.plusDays(5)
            val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE
            val apiKey = getApiKey()
            
            val unitGroup = if (Settings.temperatureUnit == Settings.TemperatureUnit.CELSIUS) "metric" else "us"
            
            val urlString = "$BASE_URL/$encodedLocation/${tomorrow.format(dateFormat)}/${endDate.format(dateFormat)}?key=$apiKey&include=days&unitGroup=$unitGroup"
            
            Log.d(TAG, "Fetching forecast for: $location with unitGroup: $unitGroup")
            return makeApiRequest(urlString)
        }

        private fun makeApiRequest(urlString: String): String {
            Log.d(TAG, "Making API request to: $urlString")
            var connection: HttpURLConnection? = null
            
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "GET"
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("Accept", "application/json")

                Log.d(TAG, "Connecting to API...")
                val responseCode = connection.responseCode
                Log.d(TAG, "API response code: $responseCode")
                
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = connection.errorStream
                    val errorResponse = if (errorStream != null) {
                        val reader = BufferedReader(InputStreamReader(errorStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()
                        response.toString()
                    } else {
                        "No error details available"
                    }
                    
                    Log.e(TAG, "HTTP error code: $responseCode, Error details: $errorResponse")
                    throw Exception("HTTP error code: $responseCode")
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                
                reader.close()

                if (response.isEmpty()) {
                    Log.e(TAG, "Empty response from API")
                    throw Exception("Empty response from API")
                }
                
                Log.d(TAG, "API response length: ${response.length} chars")
                return response.toString()
            } catch (e: Exception) {
                Log.e(TAG, "API request failed: ${e.message}", e)
                throw e
            } finally {
                connection?.disconnect()
            }
        }

        @SuppressLint("NewApi")
        fun parseWeatherData(jsonString: String): WeatherData {
            try {
                val jsonObject = JSONObject(jsonString)
                
                if (!jsonObject.has("days") || jsonObject.getJSONArray("days").length() == 0) {
                    Log.e(TAG, "Missing days array in response")
                    return WeatherData.empty()
                }
                
                val today = jsonObject.getJSONArray("days").getJSONObject(0)
                
                saveResponseDataForDebugging(jsonString)
                
                val location = jsonObject.optString("resolvedAddress", "Unknown Location")
                val datetime = today.optString("datetime", "")
                val minTemperature = today.optDouble("tempmin", 0.0)
                val maxTemperature = today.optDouble("tempmax", 0.0)
                val feelsLikeMax = today.optDouble("feelslikemax", 0.0)
                val feelsLikeMin = today.optDouble("feelslikemin", 0.0)
                val sunrise = today.optString("sunrise", "")
                val sunset = today.optString("sunset", "")
                val moonPhase = today.optDouble("moonphase", 0.0)
                val description = today.optString("description", "")
                val precipCover = today.optInt("precipcover", 0)
                val stations = today.optString("stations", "")
                val source = today.optString("source", "")
                var temperature = today.optDouble("temp", 0.0)
                var conditions = today.optString("conditions", "Unknown")
                var humidity = today.optInt("humidity", 0)
                var windSpeed = today.optDouble("windspeed", 0.0)
                var feelsLike = today.optDouble("feelslike", 0.0)
                var uvIndex = today.optInt("uvindex", 0)
                var pressure = today.optDouble("pressure", 0.0)
                var visibility = today.optDouble("visibility", 0.0)
                var cloudCover = today.optInt("cloudcover", 0)
                var dewPoint = today.optDouble("dew", 0.0)
                var precipAmount = today.optDouble("precip", 0.0)
                var precipProbability = today.optInt("precipprob", 0)
                var precipType = today.optString("preciptype", "")
                var snow = today.optInt("snow", 0)
                var snowDepth = today.optDouble("snowdepth", 0.0)
                var windGust = today.optDouble("windgust", 0.0)
                var windDirection = today.optInt("winddir", 0)
                var solarRadiation = today.optDouble("solarradiation", 0.0)
                var solarEnergy = today.optDouble("solarenergy", 0.0)
                var icon = today.optString("icon", "")

                if (jsonObject.has("currentConditions")) {
                    val current = jsonObject.getJSONObject("currentConditions")
                    Log.d(TAG, "Using currentConditions data as primary source for current weather")
                    
                    if (current.has("temp")) temperature = current.optDouble("temp")
                    if (current.has("conditions")) conditions = current.optString("conditions")
                    if (current.has("humidity")) humidity = current.optInt("humidity")
                    if (current.has("windspeed")) windSpeed = current.optDouble("windspeed")
                    if (current.has("winddir")) windDirection = current.optInt("winddir")
                    if (current.has("windgust")) windGust = current.optDouble("windgust")
                    if (current.has("feelslike")) feelsLike = current.optDouble("feelslike")
                    if (current.has("uvindex")) uvIndex = current.optInt("uvindex")
                    if (current.has("pressure")) pressure = current.optDouble("pressure")
                    if (current.has("visibility")) visibility = current.optDouble("visibility")
                    if (current.has("cloudcover")) cloudCover = current.optInt("cloudcover")
                    if (current.has("dew")) dewPoint = current.optDouble("dew")
                    if (current.has("precip")) precipAmount = current.optDouble("precip")
                    if (current.has("precipprob")) precipProbability = current.optInt("precipprob")
                    if (current.has("preciptype")) precipType = current.optString("preciptype")
                    if (current.has("snow")) snow = current.optInt("snow")
                    if (current.has("snowdepth")) snowDepth = current.optDouble("snowdepth")
                    if (current.has("solarradiation")) solarRadiation = current.optDouble("solarradiation")
                    if (current.has("solarenergy")) solarEnergy = current.optDouble("solarenergy")
                    if (current.has("icon")) icon = current.optString("icon")
                    
                    Log.d(TAG, "CURRENT CONDITIONS - Time: ${current.optString("datetime")}, temp: ${temperature}°C, conditions: $conditions")
                    Log.d(TAG, "CURRENT CONDITIONS - windspeed: $windSpeed km/h, visibility: $visibility km")
                }
                else if (today.has("hours")) {
                    try {
                        val hoursArray = today.getJSONArray("hours")
                        val currentHour = LocalTime.now().hour
                        var currentHourData: JSONObject? = null
                        
                        for (i in 0 until hoursArray.length()) {
                            val hourData = hoursArray.getJSONObject(i)
                            val hourTime = hourData.optString("datetime", "00:00:00")
                            
                            val hourValue = try {
                                hourTime.split(":")[0].toInt()
                            } catch (_: Exception) {
                                continue
                            }
                            
                            if (hourValue == currentHour) {
                                currentHourData = hourData
                                break
                            }
                        }
                        
                        if (currentHourData == null) {
                            for (i in 0 until hoursArray.length()) {
                                val hourData = hoursArray.getJSONObject(i)
                                val hourTime = hourData.optString("datetime", "00:00:00")
                                
                                val hourValue = try {
                                    hourTime.split(":")[0].toInt()
                                } catch (_: Exception) {
                                    continue
                                }
                                
                                if (hourValue <= currentHour) {
                                    currentHourData = hourData
                                } else {
                                    break
                                }
                            }
                        }
                        
                        if (currentHourData != null) {
                            Log.d(TAG, "Using hour data as fallback for hour $currentHour")
                            
                            if (currentHourData.has("temp")) temperature = currentHourData.optDouble("temp")
                            if (currentHourData.has("conditions")) conditions = currentHourData.optString("conditions")
                            if (currentHourData.has("humidity")) humidity = currentHourData.optInt("humidity")
                            if (currentHourData.has("windspeed")) windSpeed = currentHourData.optDouble("windspeed")
                            if (currentHourData.has("winddir")) windDirection = currentHourData.optInt("winddir")
                            if (currentHourData.has("windgust")) windGust = currentHourData.optDouble("windgust")
                            if (currentHourData.has("feelslike")) feelsLike = currentHourData.optDouble("feelslike")
                            if (currentHourData.has("uvindex")) uvIndex = currentHourData.optInt("uvindex")
                            if (currentHourData.has("pressure")) pressure = currentHourData.optDouble("pressure")
                            if (currentHourData.has("visibility")) visibility = currentHourData.optDouble("visibility")
                            if (currentHourData.has("cloudcover")) cloudCover = currentHourData.optInt("cloudcover")
                            if (currentHourData.has("dew")) dewPoint = currentHourData.optDouble("dew")
                            if (currentHourData.has("precip")) precipAmount = currentHourData.optDouble("precip")
                            if (currentHourData.has("precipprob")) precipProbability = currentHourData.optInt("precipprob")
                            if (currentHourData.has("preciptype")) precipType = currentHourData.optString("preciptype")
                            if (currentHourData.has("snow")) snow = currentHourData.optInt("snow")
                            if (currentHourData.has("snowdepth")) snowDepth = currentHourData.optDouble("snowdepth")
                            if (currentHourData.has("solarradiation")) solarRadiation = currentHourData.optDouble("solarradiation")
                            if (currentHourData.has("solarenergy")) solarEnergy = currentHourData.optDouble("solarenergy")
                            if (currentHourData.has("icon")) icon = currentHourData.optString("icon")
                            
                            Log.d(TAG, "HOUR DATA - windspeed: $windSpeed km/h, visibility: $visibility km")
                        } else {
                            Log.d(TAG, "No matching hour data found for current hour ${currentHour}, using daily data")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing hours data: ${e.message}")
                    }
                }
                
                Log.d(TAG, "Final parsed weather data: $temperature°C, $conditions, min/max: $minTemperature/$maxTemperature")
                Log.d(TAG, "Detailed data: windspeed=$windSpeed km/h, visibility=$visibility km, pressure=$pressure mb")
                
                return WeatherData(
                    temperature = temperature,
                    conditions = conditions,
                    humidity = humidity,
                    windSpeed = windSpeed,
                    feelsLike = feelsLike,
                    uvIndex = uvIndex,
                    pressure = pressure,
                    visibility = visibility,
                    cloudCover = cloudCover,
                    datetime = datetime,
                    location = location,
                    minTemperature = minTemperature,
                    maxTemperature = maxTemperature,
                    description = description,
                    snow = snow,
                    precipProbability = precipProbability,
                    precipType = precipType,
                    sunrise = sunrise,
                    sunset = sunset,
                    dewPoint = dewPoint,
                    precipAmount = precipAmount,
                    precipCover = precipCover,
                    snowDepth = snowDepth,
                    windGust = windGust,
                    windDirection = windDirection,
                    feelsLikeMax = feelsLikeMax,
                    feelsLikeMin = feelsLikeMin,
                    solarRadiation = solarRadiation,
                    solarEnergy = solarEnergy,
                    moonPhase = moonPhase,
                    icon = icon,
                    stations = stations,
                    source = source
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing weather data", e)
                return WeatherData.empty()
            }
        }

        fun parseForecastData(jsonResponse: String): ForecastResponse {
            try {
                val json = JSONObject(jsonResponse)
                val location = json.optString("resolvedAddress", "Unknown Location")
                
                if (!json.has("days")) {
                    Log.e(TAG, "Missing days in forecast response")
                    return ForecastResponse.empty()
                }
                
                val days = json.getJSONArray("days")
                
                val forecasts = mutableListOf<ForecastData>()
                val startIdx = 0
                val endIdx = days.length().coerceAtMost(startIdx + 5)
                
                for (i in startIdx until endIdx) {
                    try {
                        val day = days.getJSONObject(i)
                        forecasts.add(ForecastData.createWithValidTemps(
                            date = day.optString("datetime", ""),
                            highTemp = day.optDouble("tempmax", 0.0),
                            lowTemp = day.optDouble("tempmin", 0.0),
                            conditions = day.optString("conditions", "Unknown"),
                            description = day.optString("description", ""),
                            humidity = day.optInt("humidity", 0),
                            windSpeed = day.optDouble("windspeed", 0.0),
                            windDirection = day.optInt("winddir", 0),
                            precipitation = day.optInt("precipprob", 0),
                            cloudCover = day.optInt("cloudcover", 0),
                            uvIndex = day.optInt("uvindex", 0),
                            sunrise = day.optString("sunrise", ""),
                            sunset = day.optString("sunset", ""),
                            iconType = day.optString("icon", ""),
                            snow = calculateSnowChance(day)
                        ))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing day at index $i", e)
                        continue
                    }
                }
                
                return ForecastResponse(location, forecasts)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing forecast data", e)
                return ForecastResponse.empty()
            }
        }

        @SuppressLint("NewApi")
        fun parseHourlyForecastData(jsonResponse: String): List<HourlyForecastData> {
            try {
                val json = JSONObject(jsonResponse)
                val forecasts = mutableListOf<HourlyForecastData>()
                
                if (!json.has("days")) {
                    Log.e(TAG, "Missing days in hourly forecast response")
                    return emptyList()
                }
                
                val days = json.getJSONArray("days")
                
                if (days.length() > 0) {
                    val today = days.getJSONObject(0)
                    
                    if (!today.has("hours")) {
                        Log.e(TAG, "Missing hours in today's forecast")
                        return emptyList()
                    }
                    
                    val hours = today.getJSONArray("hours")
                    
                    var currentHour = LocalTime.now().hour
                    var currentTime = ""
                    
                    if (json.has("currentConditions")) {
                        val current = json.getJSONObject("currentConditions")
                        currentTime = current.optString("datetime", "")
                        
                        if (currentTime.isNotEmpty()) {
                            try {
                                currentHour = currentTime.split(":")[0].toInt()
                                Log.d(TAG, "Using location time from API: $currentTime (hour: $currentHour)")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing current hour from API: ${e.message}")
                            }
                        }
                        
                        forecasts.add(HourlyForecastData(
                            time = currentTime.ifEmpty { "${currentHour}:00:00" },
                            temperature = current.optDouble("temp", 0.0),
                            conditions = current.optString("conditions", "Unknown"),
                            precipProbability = current.optInt("precipprob", 0),
                            humidity = current.optInt("humidity", 0),
                            windSpeed = current.optDouble("windspeed", 0.0),
                            feelsLike = current.optDouble("feelslike", 0.0)
                        ))
                    }
                    
                    for (i in 0 until hours.length()) {
                        val hour = hours.getJSONObject(i)
                        val hourTime = hour.optString("datetime", "00:00:00")
                        
                        val hourValue = try {
                            hourTime.split(":")[0].toInt()
                        } catch (_: Exception) {
                            0
                        }
                        
                        if (hourValue > currentHour) {
                            forecasts.add(HourlyForecastData(
                                time = hourTime,
                                temperature = hour.optDouble("temp", 0.0),
                                conditions = hour.optString("conditions", "Unknown"),
                                precipProbability = hour.optInt("precipprob", 0),
                                humidity = hour.optInt("humidity", 0),
                                windSpeed = hour.optDouble("windspeed", 0.0),
                                feelsLike = hour.optDouble("feelslike", 0.0)
                            ))
                        }
                    }
                }
                
                Log.d(TAG, "Parsed ${forecasts.size} hourly forecasts")
                return forecasts
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing hourly forecast data", e)
                return emptyList()
            }
        }

        private fun calculateSnowChance(dayData: JSONObject): Int {
            val precipTypes = try {
                val typesArray = dayData.optJSONArray("preciptype")
                if (typesArray != null) {
                    val types = mutableListOf<String>()
                    for (i in 0 until typesArray.length()) {
                        types.add(typesArray.optString(i, ""))
                    }
                    types
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList<String>()
            }
            
            if (precipTypes.contains("snow")) {
                return dayData.optInt("precipprob", 0)
            }
            
            val conditions = dayData.optString("conditions", "").lowercase()
            if (conditions.contains("snow") || conditions.contains("flurries")) {
                val temp = dayData.optDouble("temp", 20.0)
                return if (temp < 0) 70 else 30
            }
            return 0
        }

        @SuppressLint("NewApi")
        private fun saveResponseDataForDebugging(jsonString: String) {
            try {
                val jsonObject = JSONObject(jsonString)
                val today = jsonObject.getJSONArray("days").getJSONObject(0)
                
                val sb = StringBuilder()
                sb.append("===== API RESPONSE DATA =====\n\n")
                
                sb.append("Location: ${jsonObject.optString("resolvedAddress")}\n")
                sb.append("Date: ${today.optString("datetime")}\n")
                sb.append("Unit Group: metric\n\n")
                
                sb.append("DAILY DATA:\n")
                sb.append("Temperature: ${today.optDouble("temp")}°C\n")
                sb.append("Min Temperature: ${today.optDouble("tempmin")}°C\n")
                sb.append("Max Temperature: ${today.optDouble("tempmax")}°C\n")
                sb.append("Feels Like: ${today.optDouble("feelslike")}°C\n")
                sb.append("Humidity: ${today.optInt("humidity")}%\n")
                sb.append("Wind Speed: ${today.optDouble("windspeed")} km/h\n")
                sb.append("Wind Direction: ${today.optInt("winddir")}°\n")
                sb.append("Visibility: ${today.optDouble("visibility")} km\n")
                sb.append("Pressure: ${today.optDouble("pressure")} mb\n")
                sb.append("Conditions: ${today.optString("conditions")}\n\n")
                
                if (jsonObject.has("currentConditions")) {
                    val current = jsonObject.getJSONObject("currentConditions")
                    sb.append("CURRENT CONDITIONS:\n")
                    sb.append("Time: ${current.optString("datetime")}\n")
                    sb.append("Temperature: ${current.optDouble("temp")}°C\n")
                    sb.append("Feels Like: ${current.optDouble("feelslike")}°C\n")
                    sb.append("Humidity: ${current.optInt("humidity")}%\n")
                    sb.append("Wind Speed: ${current.optDouble("windspeed")} km/h\n")
                    sb.append("Wind Direction: ${current.optInt("winddir")}°\n")
                    sb.append("Visibility: ${current.optDouble("visibility")} km\n")
                    sb.append("Pressure: ${current.optDouble("pressure")} mb\n")
                    sb.append("Conditions: ${current.optString("conditions")}\n\n")
                }
                
                if (today.has("hours")) {
                    val hoursArray = today.getJSONArray("hours")
                    val currentHour = LocalTime.now().hour
                    
                    for (i in 0 until hoursArray.length()) {
                        val hour = hoursArray.getJSONObject(i)
                        val hourTime = hour.optString("datetime", "00:00:00")
                        
                        val hourValue = try {
                            hourTime.split(":")[0].toInt()
                        } catch (_: Exception) {
                            continue
                        }
                        
                        if (hourValue == currentHour) {
                            sb.append("CURRENT HOUR DATA (${hourTime}):\n")
                            sb.append("Temperature: ${hour.optDouble("temp")}°C\n")
                            sb.append("Feels Like: ${hour.optDouble("feelslike")}°C\n")
                            sb.append("Humidity: ${hour.optInt("humidity")}%\n")
                            sb.append("Wind Speed: ${hour.optDouble("windspeed")} km/h\n")
                            sb.append("Wind Direction: ${hour.optInt("winddir")}°\n")
                            sb.append("Visibility: ${hour.optDouble("visibility")} km\n")
                            sb.append("Pressure: ${hour.optDouble("pressure")} mb\n")
                            sb.append("Conditions: ${hour.optString("conditions")}\n\n")
                            break
                        }
                    }
                }
                
                val debugData = sb.toString()
                Log.d(TAG, "Weather API Debug Data:\n$debugData")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error saving debug data: ${e.message}")
            }
        }
    }
} 