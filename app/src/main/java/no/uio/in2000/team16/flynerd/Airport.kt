package no.uio.in2000.team16.flynerd

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson


/**
 * Airport class
 * class creation warranted by expert principle
 * responsible for creation of airport objects and contains
 * methods for fetching and manipulation of data concerning this object.
 *
 * Used in: AirportsList.kt
 *
 * @param ICAO - airport ID as per international standard
 * @param name - name of the airport
 * @param country - country of airport
 * @param latitude -  geographic coordinate that specifies the north–south position
 *                    of the airport on the earths surface (need to limit decimals)
 * @param longtitude - a geographic coordinate that specifies the east–west position
 *                     of a point on the Earth's surface (need to limit decimals)
 * @param weatherForecast - holds forecast object reference.
 *                          object contains weather forecast data
 *                          null at initialization. we only want to call api when we need forecast information
 * @param gson - holds gson object. Needed when when parsing json data from resulting from api call.
 */
class Airport(val ICAO : String,
              val city : String?,
              val name : String,
              val country : String,
              val latitude : Double,
              val longtitude : Double,
              var weatherForecast: Forecast? = null,
              val gson : Gson = Gson()) {

    /**
     * method: callForecastAPI
     * makes api call to locationforecast api offered by MET
     */

    suspend fun callForecastAPI() {
        val baseURL = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact?"
        val geoLocation = "lat=$latitude&lon=$longtitude"
        val requestURL = "$baseURL$geoLocation"

        try {
            val response = Fuel.get(requestURL).header("User-Agent", "FLyNerd gjchocopasta@gmail.com").awaitString()
            Log.d("output", response)
            weatherForecast = gson.fromJson(response, Forecast::class.java)
            println(forecastToString()) //for testing purposes
        }
        catch(exception: FuelError) {
            Log.d("Fuel", "ERROR FETCHING DATA")
        }
    }

    fun emptyWeatherForecast(): Boolean{
        if (weatherForecast == null) return true
        return false
    }

    /**
     * Getter method section
     * all gather methods using the forecast object reference
     * all data is returned as a string.
     */
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

    /**
     * Getter method section
     * all gather methods using the Airport object reference
     * all data is returned as a string.
     */

    internal fun getIcao() : String {
        return "$ICAO\n"
    }

    internal fun getCity() : String? {
        return "$city\n"
    }

    internal fun getCountry() : String {
        return "$country\n"
    }

    internal fun getAirportName() : String {
        return "$name\n"
    }

    internal fun getLatitude() : Double {
        return latitude
    }

    internal fun getLongtitude() : Double {
        return longtitude
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
                "city" + ":".padStart(10) + "$city\n" +
                "name" + ":".padStart(10) + "$name\n" +
                "country" + ":".padStart(7) + "$country\n" +
                "latitude" + ":".padStart(6) + "$latitude\n" +
                "longtitude" + ":".padStart(4) + "$longtitude\n" +
                "weather" + ":".padStart(5) + "$weatherForecast"
//        +
//                "forecast" + ":".padStart(6) + "$";
    }


}