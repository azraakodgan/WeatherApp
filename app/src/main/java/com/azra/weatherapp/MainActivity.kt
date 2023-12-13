package com.azra.weatherapp

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.drawable.Icon
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import com.azra.weatherapp.databinding.ActivityLoginBinding
import com.azra.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var mRetrofit: ApiInterface

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mRetrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)


        fetchWeatherData("Ankara")
        searchCity()
    }

    private fun searchCity() {
        val search = binding.searchView
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { fetchWeatherData(it) }
                return true;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true;
            }
        })
    }

    fun fetchWeatherData(city: String)
    {
        val response = mRetrofit.getWeatherData(city,"", "metric")

        response.enqueue(object : Callback<WeatherApp>
        {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null)
                {
                    val temperature = response.body()?.main?.temp.toString()
                    binding.txtTemp.text = "$temperature°C"

                    val humidity = responseBody.main.humidity
                    binding.txtHumidityPercent.text = "$humidity%"

                    val windSpeed = responseBody.wind.speed
                    binding.txtWindSpeed.text = "$windSpeed km/h"

                    val sunRise = responseBody.sys.sunrise.toLong()
                    binding.txtSunriseHour.text = time(sunRise)

                    val sunSet = responseBody.sys.sunset.toLong()
                    binding.txtSunsetHour.text = time(sunSet)

                    val altitude = responseBody.main.pressure
                    binding.txtAltitudePercent.text = "$altitude m"

                    val conditions = responseBody.weather.firstOrNull()?.main?: "unknown"
                    binding.txtForecast.text = conditions
                    binding.txtConditions.text = conditions

                    val maxTemp = responseBody.main.temp_max
                    binding.txtMaxTemp.text = "Max: $maxTemp°C"

                    val minTemp = responseBody.main.temp_min
                    binding.txtMinTemp.text = "Min: $minTemp°C"

                    binding.txtDay.text = dayName(System.currentTimeMillis())
                    binding.txtDate.text = date()
                    binding.txtCity.text = city

                    changeIcons(conditions)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable)
            {
                Log.e("HATA:", t.message.toString())
            }
        })
    }

    private fun changeIcons(conditions: String) {
        when (conditions){
            "Clear Sky", "Sunny", "Clear" -> {
                binding.weatherIcon.setImageResource(R.drawable.sunny)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.weatherIcon.setImageResource(R.drawable.cloudy)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.weatherIcon.setImageResource(R.drawable.rainy)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.weatherIcon.setImageResource(R.drawable.sunny)
            }
        }
    }

    @SuppressLint("WeekBasedYear")
    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format((Date()))
    }

    fun time(timestamp: Long): String{
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }

    fun dayName(timestamp: Long): String{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))
    }
}
