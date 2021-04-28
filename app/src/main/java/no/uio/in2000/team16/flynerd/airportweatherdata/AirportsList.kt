package no.uio.in2000.team16.flynerd.airportweatherdata

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader


/**
 * AirportsList class
 * class creation warranted by creator principle
 * class acts as a container to collect and store all airport objects to be created from hard coded
 * database intair_city.csv. Database can be found in the raw resource folder.
 *
 * Used in: AirportsList.kt
 *
 * @param context - contains activity context of activity where this class is called.
 * @param databaseId - reference to the database to be read into airport objects
 * @param airports - variabl defined and initialized when creating an AirportsList object

 */

class AirportsList (val context : Context, var databaseId : Int, var airports : MutableList<Airport> = mutableListOf()){


    /**
     * uses 3rd party package to read csv data.
     * information can be found by following the link below
     * https://github.com/doyaaaaaken/kotlin-csv
     */
    fun readAirports() {
        val tsvReader = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

        tsvReader.open(context.resources.openRawResource(databaseId)) {
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
}
