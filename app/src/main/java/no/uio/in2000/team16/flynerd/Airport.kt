package no.uio.in2000.team16.flynerd

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson

class Airport(val ICAO : String,
              val name : String,
              val country : String,
              val latitude : Double,
              val longtitude : Double,
              var weatherForecast: Forecast? = null,
              val gson : Gson = Gson()) {

    suspend fun callForecastAPI() {
        val baseURL = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact?"
        val geoLocation = "lat=$latitude&lon=$longtitude"
        val requestUrl = "$baseURL$geoLocation"

        try {
            val response = Fuel.get(requestUrl).header("User-Agent", "FLyNerd gjchocopasta@gmail.com").awaitString()
            Log.d("output", response)
            val data = gson.fromJson(response, Forecast::class.java)
            weatherForecast = data
            println(forecastToString())
        }
        catch(exception: FuelError) {
            Log.d("Fuel", "ERROR FETCHING DATA")
        }
    }

    fun emptyWeatherForecast(): Boolean{
        if (weatherForecast == null) return true
        return false
    }

    //GETTER METHODS FOR SPECIFIC WEATHER DATA
    //error handling: check if weatherforecast property exists
    fun getCurrentWeather() : String{
        return (weatherForecast!!.properties.timeseries[0].data.next_1_hours.summary.symbol_code).replace("_", " ")
    }

    fun getTemperature() : String{
        return "${weatherForecast!!.properties.timeseries[0].data.instant.details.air_temperature} ${weatherForecast!!.properties.meta.units.air_temperature}"
    }

    fun getWindForce() : String{
        return "${weatherForecast!!.properties.timeseries[0].data.instant.details.wind_speed} ${weatherForecast!!.properties.meta.units.wind_speed}"
    }

    fun getPrecipationAmount() : String{
        return "${weatherForecast!!.properties.timeseries[0].data.next_1_hours.details.precipitation_amount} ${weatherForecast!!.properties.meta.units.precipitation_amount}"
    }

    fun forecastToString() : String{
        if (emptyWeatherForecast()) Log.d("Forecast Data", "Forecast property empty, unable to execute function")
        return "Time: ${weatherForecast!!.properties.timeseries[0].time.padStart(10)}\n" +
                "Sky: ${getCurrentWeather().padStart(11)}\n" +
                "Temp: ${getTemperature().padStart(10)}\n" +
                "Wind: ${getWindForce().padStart(10)}\n" +
                "Precipation: ${getPrecipationAmount().padStart(3)}"
    }

    //Helper Mmethod for displaying Airport data
    override fun toString(): String {
        return  "ICAO" + ":".padStart(10) + "$ICAO\n" +
                "name" + ":".padStart(10) + "$name\n" +
                "country" + ":".padStart(7) + "$country\n" +
                "latitude" + ":".padStart(6) + "$latitude\n" +
                "longtitude" + ":".padStart(4) + "$longtitude\n"
//        +
//                "forecast" + ":".padStart(6) + "$";
    }


}