package no.uio.in2000.team16.flynerd

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import no.uio.in2000.team16.flynerd.airportweatherdata.Forecast
import java.io.Serializable

/**
 * Airport class responsible for creation of forecast objects
 * contains methods for fetching and manipulation of forecast related data.
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
 */
class Airport(
    val ICAO: String,
    val city: String?,
    val name: String,
    val country: String,
    val latitude: Double,
    val longtitude: Double,
    var weatherForecast: Forecast? = null
) : Serializable {

    /**
     * method: callForecastAPI
     * makes api call to locationforecast api offered by MET
     */
    suspend fun callForecastAPI() {
        val baseURL = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact?"
        val geoLocation = "lat=$latitude&lon=$longtitude"
        val requestURL = "$baseURL$geoLocation"
        val gson = Gson()

        try {
            val response =
                Fuel.get(requestURL).header("User-Agent", "FLyNerd gjchocopasta@gmail.com")
                    .awaitString()
            Log.d("output", response)
            weatherForecast = gson.fromJson(response, Forecast::class.java)
        } catch (exception: FuelError) {
            Log.d("Fuel", "ERROR FETCHING DATA")
        }
    }

    /**
     * Getter method section
     * all gather methods using the forecast object reference
     * all data is returned as a string.
     */
    fun getCurrentWeather(): String {
        return (weatherForecast!!.properties.timeseries[0].data.next_1_hours.summary.symbol_code).replace(
            "_",
            " "
        )
    }

    fun getTemperature(): String {
        return "${weatherForecast!!.properties.timeseries[0].data.instant.details.air_temperature} ${weatherForecast!!.properties.meta.units.air_temperature}"
    }

    fun getWindForce(): String {
        return "${weatherForecast!!.properties.timeseries[0].data.instant.details.wind_speed} ${weatherForecast!!.properties.meta.units.wind_speed}"
    }

    fun getPrecipationAmount(): String {
        return "${weatherForecast!!.properties.timeseries[0].data.next_1_hours.details.precipitation_amount} ${weatherForecast!!.properties.meta.units.precipitation_amount}"
    }
}