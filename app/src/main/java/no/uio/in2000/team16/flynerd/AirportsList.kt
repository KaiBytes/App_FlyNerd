package no.uio.in2000.team16.flynerd

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.math.BigDecimal
import java.math.RoundingMode

class AirportsList (val context : Context, var resourceId : Int, var airports : MutableList<Airport> = mutableListOf()){

    fun readAirports() {
        val tsvReader = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

        tsvReader.open(context.resources.openRawResource(resourceId)) {
            readAllAsSequence().forEach { row ->
                appendToList(createAirport(row[0], row[1], row[2], row[3], row[4].toDouble(), row[5].toDouble()))
            }
        }
    }

    fun createAirport(ICAO : String, city : String?,  name : String, country : String, latitude : Double, longtitude : Double) : Airport {
        val tmp = Airport(ICAO,city, name, country, latitude, longtitude)
        println(tmp)
        return tmp
    }

    fun appendToList(airportObject : Airport) {
        airports.add(airportObject)
    }

    fun printAirports() {
        for (airport in airports){
            println(airport.toString())
        }
    }
}
