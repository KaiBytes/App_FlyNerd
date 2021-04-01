package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.launch
import java.lang.System.exit
import java.text.SimpleDateFormat
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private val TAG = "FlightInfo"
    private val AppID = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val AppKey = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val flightApi = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/"
    private  var flightUrl = ""

    private var calendar: Calendar? = null
    private var dateFormat: SimpleDateFormat? = null
    private var userCurrentDate: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get current date from system
        calendar = Calendar.getInstance()
        dateFormat = SimpleDateFormat("yyyy/MM/dd")
        userCurrentDate = dateFormat!!.format(calendar?.getTime())

        // input the flight number as string
        var carrierStr = "AA"
        var flightNum = "100"
        var flightNumberStr = carrierStr + flightNum

        // checking if the flight number is valid withcheckFlightNumber function and use the flight number
        // else exit the system if flight number is not valid
        if(checkFlightNumber_IATA(flightNumberStr) || checkFlightNumber_ICAO(flightNumberStr)){
            // construct flight url from user api key, api-id , current date, and base url from api provider
            flightUrl = (flightApi + carrierStr+"/" + flightNum+ "/" + "arr/"+ userCurrentDate +"?appId=" + AppID + "&appKey=" + AppKey + "&utc=false")

            Log.i(TAG, flightUrl)
        }
        else{
            Log.i(TAG, "The flight number is invalid")
            exit(0)
        }

        //using couroutinScope & feul make http requsting and get data from flight API provider as json format
        lifecycleScope.launch {

            val jsonString = Fuel.get(flightUrl).awaitString()
            Log.d(TAG,"The Json data is :\n\n " +jsonString)
        }
    }

}

// Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for IATA Flight No.
private fun checkFlightNumber_IATA(flightNumberStr: String): Boolean {
    if (flightNumberStr.length < 3) {
        return false
    }
    if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1])) {
        return false
    }
    for (i in 2 until flightNumberStr.length) {
        if (!Character.isDigit(flightNumberStr[i])) {
            return false
        }
    }
    return true
}

// Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for  ICAO Flight No.
private fun checkFlightNumber_ICAO(flightNumberStr: String): Boolean {
    if (flightNumberStr.length < 4) {
        return false
    }
    if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1]) || !Character.isLetter(flightNumberStr[2])) {
        return false
    }
    for (i in 3 until flightNumberStr.length) {
        if (!Character.isDigit(flightNumberStr[i])) {
            return false
        }
    }
    return true
}

