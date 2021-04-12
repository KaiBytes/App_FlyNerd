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
                appendDomesticAirportList(createAirport(row[0], row[2], row[1], row[3].toDouble(), row[4].toDouble()))
            }
        }
    }


    // TODO remove redundant function
    fun createAirport(ICAO : String, name : String, country : String, latitude : Double, longtitude : Double) : Airport? {
        val tmpAirport : Airport? = Airport(ICAO, name, country, latitude, longtitude)
        return tmpAirport
    }

    fun appendDomesticAirportList(airportObject : Airport?) {
        airports.add(airportObject)
    }


    fun printAirports() {
        for (airport in airports){
            println(airport.toString())
        }
    }
}
