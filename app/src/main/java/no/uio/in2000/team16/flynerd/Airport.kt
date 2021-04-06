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

    suspend fun getData() {
        val baseURL = "https://api.met.no/weatherapi/locationforecast/2.0/compact?"
        val geoLocation = "lat=$latitude&lon=$longtitude"
        val requestUrl = "$baseURL$geoLocation"

        try {
            val response = Fuel.get(requestUrl).header("User-Agent", "FLyNerd gjchocopasta@gmail.com").awaitString()
            val data = gson.fromJson(response, Forecast::class.java)
            weatherForecast = data
            forecastToString(weatherForecast!!)
//            val response = Fuel.get(requestUrl).header("User-Agent", "FLyNerd gjchocopasta@gmail.com")
//                .awaitString()
//            Log.d("API Respons: ", response)
//            return response
        }
        catch(exception: FuelError) {
            Log.d("Fuel", "ERROR FETCHING DATA")
        }

    }

    fun forecastToString(fc : Forecast){
        println("Time: ${fc.properties.timeseries[0].time.padStart(10)}\n" +
                "Sky: ${fc.properties.timeseries[0].data.next_1_hours.summary.symbol_code.padStart(11)}")
    }

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