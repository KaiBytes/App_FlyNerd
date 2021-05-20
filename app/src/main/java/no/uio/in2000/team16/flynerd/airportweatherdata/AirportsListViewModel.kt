package no.uio.in2000.team16.flynerd.airportweatherdata

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.Airport


/**
 * Class acts as a container to collect and store all airport objects to be created from hard coded
 * database intair_city.csv. Database can be found in the raw resource folder.
 *
 * Used in: AirportsList.kt
 *
 * Uses 3rd party package to read csv data. Documentation: https://github.com/doyaaaaaken/kotlin-csv
 *
 * partner class to the mainActivity class.
 * this particular class implements the viewmodel interface
 * attempt to follow MVVM principles
 *
 * creation of this class was necessary to clearly seperate responsibilities for each class
 * UI controllers like activities and fragments should only be responsible for displaying UI components
 * and responding to user interactions.
 * Assigning too many responsibilities to UI controllers bloats the class, makes it difficult to test
 * and negatively affects app performance
 *
 * this viewmodel class has the following responsibiliets
 * 1. prepare data for ui controller by reading csv database-> main activity
 * 2. retains the data when screen layout is reconfigured
 */

class AirportsListViewModel : ViewModel(){

    //livedata variables. These are awesome because they retain information even when layout configuration changes
    //they also destroy old values with every update.
    val airportsLiveData = MutableLiveData<MutableList<Airport>>()
    var matchedLiveData = MutableLiveData<MutableList<Airport>>()

    //create database containing airport objects from csv file
    fun createDb(context : Context, databaseId : Int) = viewModelScope.launch {

        // using 3rd party csv parser to create objects
        // documentation https://github.com/doyaaaaaken/kotlin-csv

        // telling csv parser how to read csv file
        val csvReader = csvReader {
            charset = "UTF-8"
            quoteChar = '"'
            delimiter = ';'
            escapeChar = '\\'
        }

        //read csv file and create airport objects asynchronously
        csvReader.openAsync(context.resources.openRawResource(databaseId)){
            val airportDb = mutableListOf<Airport>()
            readAllAsSequence().forEach { row ->
                airportDb.add(createAirport(row[0], row[1], row[2], row[3], row[4].toDouble(), row[5].toDouble()))
                airportsLiveData.postValue(airportDb)
            }
        }
    }

    //helper method for creating airport object.
    fun createAirport(ICAO : String, city : String?,  name : String, country : String, latitude : Double, longtitude : Double) : Airport {
        val tmp = Airport(ICAO,city, name, country, latitude, longtitude)
//        println(tmp)
        return tmp
    }

    //coroutine function which will match a user defined city to airports that service it.
    //result is stored in matchedLiveData variable
    fun matchAirportWithCity(cityName : String) = viewModelScope.launch {
        val matchedAirports = mutableListOf<Airport>()

        //only search if user defined a cityname, else add empty mutablelist to matchedlivedata.
        if (cityName.isNotBlank()){
            for (airport in airportsLiveData.value!!){
                if (airport.city == cityName){
                    Log.d("name of airport", airport.name)
                    matchedAirports.add(airport)
                }
            }
            matchedLiveData.postValue(matchedAirports)
        } else {
            matchedLiveData.postValue(matchedAirports)
        }



    }



}
