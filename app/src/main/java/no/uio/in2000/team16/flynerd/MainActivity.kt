package no.uio.in2000.team16.flynerd

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.InputStream
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class MainActivity : AppCompatActivity() {

    var domesticAirportList : MutableList<Airport?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        // Getting API request for TAF
        val baseUrL = "https://api.met.no/weatherapi/tafmetar/1.0/tafmetar.xml?icao="
        val airportIcao = "ENGM"
        val requestUrl = "https://api.met.no/weatherapi/tafmetar/1.0/tafmetar.xml?icao=ENGM"

//        suspend fun getData(): String {
//            return Fuel.get(requestUrl).header("User-Agent", "tafmarApp gjchocopasta@gmail.com")
//                .awaitString()
//        }
//
//        CoroutineScope(IO).launch {
//            val responseAPI = getData()
////            Log.d("API response: ", responseAPI)
//
//            runOnUiThread {
//                val inputStream: InputStream = responseAPI.byteInputStream()
//                val listOfForecasts = ForecastParser().parse(inputStream)
//
//                for (forecast in listOfForecasts) {
//                    forecast as Forecast // I do this casting to be able to address objects of this class
//                    println(forecast.forecastString)
//                }
//
////                Log.d("List of Forecasts: ", listOfForecasts.toString())
//                Log.d("Size of the list: ", listOfForecasts.size.toString())
//            }
//        }

        // --------------------------------------------------------------
        val initializer = AirportsList()
        initializer.readAirports()

//
//        val tsvReader = csvReader {
//            charset = "UTF-8"
//            quoteChar = '"'
//            delimiter = ';'
//            escapeChar = '\\'
//        }
//
//        tsvReader.open(resources.openRawResource(R.raw.domair)) {
//            readAllAsSequence().forEach { row ->
//                append_to_domesticAirportList(create_Airport(row[0], row[2], row[1], row[3].toDouble(), row[4].toDouble()))
//            }
//        }
//
////        for (airport in domesticAirportList){
//////            println(airport.toString())
////        }
//    }
//
//    fun append_to_domesticAirportList(airportObject : Airport?) {
//        domesticAirportList.add(airportObject)
//    }
//
//    fun create_Airport(ICAO : String, name : String, country : String, latitude : Double, longtitude : Double) : Airport? {
//        var tmpAirport : Airport? = Airport(ICAO, name, country, latitude, longtitude, null, null);
//        return tmpAirport
//    }
    }
    companion object {
        var context: Context? = null
            internal set
    }
}