package no.uio.in2000.team16.flynerd

import android.content.res.Resources
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import no.uio.in2000.team16.flynerd.MainActivity.Companion.context

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

        for (airport in airports){
            println(airport.toString())
            println("Dupa")
        }

    }

    fun append_to_domesticAirportList(airportObject : Airport?) {
        airports.add(airportObject)
    }

    // TODO remove redundant function
    fun create_Airport(ICAO : String, name : String, country : String, latitude : Double, longtitude : Double) : Airport? {
        var tmpAirport : Airport? = Airport(ICAO, name, country, latitude, longtitude, null, null);
        return tmpAirport
    }
}
