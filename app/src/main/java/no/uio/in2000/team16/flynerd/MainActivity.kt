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
import kotlinx.coroutines.Job
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    var domesticAirportList : MutableList<Airport?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        val initializer = AirportsList()
        initializer.readAirports()

        runBlocking {
            initializer.callAPI()
        }

        // I tell the program to wait 2 s so the above thread can finish before it prints out all airports.
        Thread.sleep(2000)
        initializer.printAirports()

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