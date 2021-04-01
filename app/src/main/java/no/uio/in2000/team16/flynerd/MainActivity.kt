package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class MainActivity : AppCompatActivity() {

    var domesticAirportList : MutableList<Airport?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tsvReader = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

        tsvReader.open(resources.openRawResource(R.raw.domair)) {
            readAllAsSequence().forEach { row ->
                append_to_domesticAirportList(create_Airport(row[0], row[2], row[1], row[3].toDouble(), row[4].toDouble()))
            }
        }

        for (airport in domesticAirportList){
//            println(airport.toString())
        }
    }

    fun append_to_domesticAirportList(airportObject : Airport?) {
        domesticAirportList.add(airportObject)
    }

    fun create_Airport(ICAO : String, name : String, country : String, latitude : Double, longtitude : Double) : Airport? {
        var tmpAirport : Airport? = Airport(ICAO, name, country, latitude, longtitude);
        return tmpAirport
    }
}