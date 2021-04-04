package no.uio.in2000.team16.flynerd

import android.util.Log
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.MainActivity.Companion.context
import java.io.InputStream
import kotlinx.coroutines.*

class AirportsList {
    var airports : MutableList<Airport?> = mutableListOf()

    fun readAirports() {
        val tsvReader = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

        tsvReader.open(context?.resources!!.openRawResource(R.raw.domair)) {
            readAllAsSequence().forEach { row ->
                append_to_domesticAirportList(create_Airport(row[0], row[2], row[1], row[3].toDouble(), row[4].toDouble()))
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun callAPI() {
        CoroutineScope(Dispatchers.IO).launch {
            for (airport in airports) {
                val responseAPI = getData(airport!!.ICAO)

                // Handling HTTP 404 exception
                if (responseAPI == "no data") {
                    airport.forecastString = "no data"
                    continue
                }

                val inputStream: InputStream = responseAPI.byteInputStream()
                val listOfForecasts = ForecastParser().parse(inputStream)


                // Handling exception when the server responds but there is no data we are interested in
                if (listOfForecasts.isEmpty()) {
                    airport.forecastString = "no data"
                    continue
                }

                val earliestForecast = listOfForecasts[0]
                Log.d("ForecastString: ", earliestForecast.toString())
                airport.forecastString = earliestForecast.toString().trim()
            }
        }
    }

    fun append_to_domesticAirportList(airportObject : Airport?) {
        airports.add(airportObject)
    }

    suspend fun getData(icaoCode: String): String {
        val baseURL = "https://api.met.no/weatherapi/tafmetar/1.0/tafmetar.xml?icao="
        val requestUrl = "$baseURL$icaoCode"

        try {
            val response = Fuel.get(requestUrl).header("User-Agent", "tafmarApp gjchocopasta@gmail.com")
                .awaitString()
            return response
        }
        catch(exception: FuelError) {
            return "no data"
        }

    }
    fun printAirports() {
        for (airport in airports){
            println(airport.toString())
        }
    }

    // TODO remove redundant function
    fun create_Airport(ICAO : String, name : String, country : String, latitude : Double, longtitude : Double) : Airport? {
        var tmpAirport : Airport? = Airport(ICAO, name, country, latitude, longtitude, null, null);
        return tmpAirport
    }
}

// TODO add translations from TAF to 