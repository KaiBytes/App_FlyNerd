package no.uio.in2000.team16.flynerd

import no.uio.in2000.team16.flynerd.flightData.Appendix
import no.uio.in2000.team16.flynerd.flightData.FlightData



import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.uidesign.AirportsListActivity

import no.uio.in2000.team16.flynerd.uidesign.FlightStatusUI

import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FlightStatusInfo : AppCompatActivity() {


    lateinit var flightStatus: TextView   // display the flight status
    lateinit var flightDelayResult: TextView // display delay result
    lateinit var flightNumberRes: TextView

    // Departure
    lateinit var departureAirport: TextView
    lateinit var departureDate: TextView
    lateinit var departureAirportName: TextView
    lateinit var departureAirportCity: TextView
    lateinit var departureAirportCountry: TextView
    lateinit var departureAirportLonLat: TextView

    // Arrival
    lateinit var arrivalAirport: TextView
    lateinit var arrivalDate: TextView
    lateinit var arrivalAirportName: TextView
    lateinit var arrivalAirportCity: TextView
    lateinit var arrivalAirportCountry: TextView
    lateinit var arrivalAirportLonLat: TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flight_status_popupwindow)

        flightStatus = findViewById<TextView>(R.id.flightStatus)
        flightDelayResult = findViewById<TextView>(R.id.flightResult)
        flightNumberRes = findViewById<TextView>(R.id.flightNumberText)

        // Departure
        departureAirport = findViewById<TextView>(R.id.departureAirport)
        departureDate = findViewById<TextView>(R.id.departureDate)
        departureAirportName = findViewById<TextView>(R.id.departureAirportName)
        departureAirportCity = findViewById<TextView>(R.id.departureAirportCity)
        departureAirportCountry = findViewById<TextView>(R.id.departureAirportCountry)
        departureAirportLonLat = findViewById<TextView>(R.id.departureAirportLonLat)

        // Arrival
        arrivalAirport = findViewById<TextView>(R.id.arrivalAirport)
        arrivalDate = findViewById<TextView>(R.id.arrivalDate)
        arrivalAirportName = findViewById<TextView>(R.id.arrivalAirportName)
        arrivalAirportCity = findViewById<TextView>(R.id.arrivalAirportCity)
        arrivalAirportCountry = findViewById<TextView>(R.id.arrivalAirportCountry)
        arrivalAirportLonLat = findViewById<TextView>(R.id.arrivalAirportLonLat)

         //Excute onMarkClick function for intent and putExtra methods accessing flight info through flight number from
        // user on tap aircraf on map
        val flightStr = intent.getStringExtra(FLIGHT_NUMBER)

        // FUNCTION TO RETURN FLIGHT INFO AS ON UI LAYOUT
        flightStatusInfo(flightStr)


        runOnUiThread{
            // FUNCTION TO RETURN STRING FLIGHT INFO IN LOG
            val statusInfo = getFlightStatusInfo(flightStr)

            Log.i(TAG, "flight status info 111111111111 = " + statusInfo );

        }


        //}) // end of onclickListener

    }


    fun flightStatusInfo(flightStr: String){


        if (flightStr!!.isEmpty()) {
            flightStatus.text = "Flight number is empty!"
            flightStatus.setTextColor(Color.RED)
        } else {
            var flightNumberStr = "";
            for (c in flightStr) {
                if (Character.isLetterOrDigit(c)) {
                    flightNumberStr += c;
                }
            }

            // checking if the flight number is valid with checkFlightNumber function and use the flight number
            if (!(checkFlightNumber_ICAO(flightNumberStr) || checkFlightNumber_IATA(flightNumberStr))) {
                flightStatus.text = "Flight number is invalid!"
                flightStatus.setTextColor(Color.RED)
            }
            // if the above condition not fulfilled it mean we have valid flight number either IATA or ICAO
            // and so the code will continue to parse for json data and operate on functions
            else {
                flightStatus.text = "Checking $flightNumberStr"
                flightStatus.setTextColor(Color.BLUE)

                // define time to Get flight data based on the user input current time  in account
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                val formatted = current.format(formatter)


                // conditionally parsing data for flight number type : IATA
                if (checkFlightNumber_IATA(flightNumberStr)) {

                    val flightUrl_IATA = (flightApi
                            + flightNumberStr.substring(0, 2) + "/" + flightNumberStr.substring(2) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")
                   // Log.i(TAG, flightUrl_IATA)

                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_IATA).awaitString()

                        if (jsonStr.isNotEmpty()) {
                            Log.i(TAG, jsonStr)

                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                flightStatus.text = "This is  non-commercial aircraft \n Currently We  provide flight status information for commercial flight!"
                                flightStatus.setTextColor(Color.RED)
                            } else {
                                val flightStat = flightData.flightStatuses!![0]
                               // Log.i(TAG, "FlightStatus = " + flightStat.status)


                                // Carrier + flight number
                                Log.i(TAG, "Carrier = " + flightStat.carrierFsCode);
                                Log.i(TAG, "Flight number = " + flightStat.flightNumber);
                                flightNumberRes.text =
                                    "Flight number : " + flightStat.carrierFsCode + flightStat.flightNumber

                                // Departure
                                Log.i(
                                   TAG,
                                    "Departure airport = " + flightStat.departureAirportFsCode
                                );
                               Log.i(
                                   TAG,
                                    "Departure date = " + flightStat.departureDate?.dateLocal
                               );

                                departureAirport.text =
                                    flightStat.departureAirportFsCode
                                departureDate.text = flightStat.departureDate?.dateLocal;

                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!,
                                    departureAirportName,
                                    departureAirportCity,
                                    departureAirportCountry,
                                    departureAirportLonLat,
                                    null
                                )
                                getAirlineInformation(
                                    flightData.appendix!!,
                                    flightStat.carrierFsCode!!
                                )

                                // Arrival
                               Log.i(TAG, "Arrival airport = " + flightStat.arrivalAirportFsCode);
                               Log.i(TAG, "Arrival date = " + flightStat.arrivalDate?.dateLocal);
                                arrivalAirport.text = flightStat.arrivalAirportFsCode;
                                arrivalDate.text = flightStat.arrivalDate?.dateLocal;
                                // get airport info
                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat,
                                    null
                                )

                                //  get airline info
                                getAirlineInformation(
                                    flightData.appendix!!,
                                    flightStat.carrierFsCode!!
                                )

                                when (flightStat.status) {
                                    "S" -> {
                                        flightStatus.text = "Flight scheduled"
                                    }
                                    "A" -> {
                                        flightStatus.text = "Flight on Air"
                                    }
                                    "L" -> {
                                        flightStatus.text = "Flight landed"
                                    }
                                    else -> {
                                        flightStatus.text = flightStat.status
                                    }
                                }
                                flightStatus.setTextColor(Color.BLUE)

                                if (flightStat.delays == null) {
                                    flightDelayResult.text = "No delay!"
                                    flightDelayResult.setTextColor(Color.BLUE)
                                } else {
                                    flightDelayResult.text =
                                        "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                    flightDelayResult.setTextColor(Color.RED)
                                }
                            }
                        }


                    }


                }// end for flightnumber type IATA


                // conditionally parsing data for flight number type : ICOA
                else {

                    val flightUrl_ICOA = (flightApi
                            + flightNumberStr.substring(0, 3) + "/" + flightNumberStr.substring(3) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")
                    Log.i(TAG, flightUrl_ICOA)

                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_ICOA).awaitString()

                        if (jsonStr.isNotEmpty()) {
                            Log.i(TAG, jsonStr)

                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                flightStatus.text = "This is  non-commercial aircraft, \nCurrently We  provide flight status information for commercial flight! "
                                flightStatus.setTextColor(Color.RED)
                            } else {
                                val flightStat = flightData.flightStatuses!![0]
                               Log.i(TAG, "FlightStatus = " + flightStat.status)


                                // Carrier + flight number
                                Log.i(TAG, "Carrier = " + flightStat.carrierFsCode);
                               Log.i(TAG, "Flight number = " + flightStat.flightNumber);
                                flightNumberRes.text =
                                    "Flight number : " + flightStat.carrierFsCode + flightStat.flightNumber

                                // Departure
                               Log.i(
                                    TAG,
                                    "Departure airport = " + flightStat.departureAirportFsCode
                                );
                                Log.i(
                                    TAG,
                                    "Departure date = " + flightStat.departureDate?.dateLocal
                                );

                                departureAirport.text =
                                    flightStat.departureAirportFsCode
                                departureDate.text = flightStat.departureDate?.dateLocal;

                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!,
                                    departureAirportName,
                                    departureAirportCity,
                                    departureAirportCountry,
                                    departureAirportLonLat,
                                    null
                                )
                                getAirlineInformation(
                                    flightData.appendix!!,
                                    flightStat.carrierFsCode!!
                                )

                                // Arrival
                             //   Log.i(TAG, "Arrival airport = " + flightStat.arrivalAirportFsCode);
                               // Log.i(TAG, "Arrival date = " + flightStat.arrivalDate?.dateLocal);
                                arrivalAirport.text = flightStat.arrivalAirportFsCode;
                                arrivalDate.text = flightStat.arrivalDate?.dateLocal;
                                // get airport info
                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat,
                                    null
                                )

                                //  get airline info
                                getAirlineInformation(
                                    flightData.appendix!!,
                                    flightStat.carrierFsCode!!
                                )

                                when (flightStat.status) {
                                    "S" -> {
                                        flightStatus.text = "Flight scheduled"
                                    }
                                    "A" -> {
                                        flightStatus.text = "Flight on Air"
                                    }
                                    "L" -> {
                                        flightStatus.text = "Flight landed"
                                    }
                                    else -> {
                                        flightStatus.text = flightStat.status
                                    }
                                }
                                flightStatus.setTextColor(Color.BLUE)

                                if (flightStat.delays == null) {
                                    flightDelayResult.text = "No delay!"
                                    flightDelayResult.setTextColor(Color.BLUE)
                                } else {
                                    flightDelayResult.text =
                                        "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                    flightDelayResult.setTextColor(Color.RED)
                                }
                            }
                        }


                    }


                }
            }
        }

    }


    // Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for IATA Flight No.
    // https://en.wikipedia.org/wiki/Flight_number
    private fun checkFlightNumber_IATA(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 3) {
            return false
        }
        if (!Character.isLetterOrDigit(flightNumberStr[0]) || !Character.isLetterOrDigit(
                flightNumberStr[1]
            )) {
            return false
        }
        for (i in 2 until flightNumberStr.length) {
            if (!Character.isDigit(flightNumberStr[i])) {
                return false
            }
        }
        return true
    }

    // Flight number should have 3 first characters as carrier and followed a number, ex: AFR454
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of three-character airline designator and a 1 to 4 digit number for  ICAO Flight No.
    // https://en.wikipedia.org/wiki/Flight_number
    private fun checkFlightNumber_ICAO(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 4) {
            return false
        }
        if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1]) || !Character.isLetter(
                flightNumberStr[2]
            )) {
            return false
        }
        for (i in 3 until flightNumberStr.length) {
            if (!Character.isLetterOrDigit(flightNumberStr[i])) {
                return false
            }
        }
        return true
    }

    private fun getAirportInformation(
        appendix: Appendix,
        airportfs: String,
        airportName: TextView,
        airportCity: TextView,
        airportCountry: TextView,
        airportLonLat: TextView,
        sb: StringBuilder?
    ) {
        for (airport in appendix.airports!!) {
            if (airport.fs == airportfs) {
                Log.i(TAG, "Airport name = " + airport.name)
                Log.i(TAG, "City = " + airport.city)
                Log.i(TAG, "Country = " + airport.countryName)
                Log.i(TAG, "Longitude = " + airport.longitude)
                Log.i(TAG, "Latitude = " + airport.latitude)
                Log.i(TAG, "Weather link = " + airport.weatherUrl)


                airportName.text = airport.name;
                airportCity.text = airport.city;
                airportCountry.text = airport.countryName;
                airportLonLat.text = "Longitude = " + airport.longitude + " - Latitude = " + airport.latitude;

                if (sb != null) {
                    sb.append(airport.name)
                    sb.append("\n")
                    sb.append(airport.city)
                    sb.append("\n")
                    sb.append(airport.countryName)
                    sb.append("\n")
                    sb.append("Longitude = ")
                    sb.append(airport.longitude)
                    sb.append(" - Latitude = ")
                    sb.append(airport.latitude)
                    sb.append("\n")
                }
            }
        }

    }

    private fun getAirlineInformation(appendix: Appendix, airlinefs: String){
        for (airline in appendix.airlines!!) {
            if (airline.fs == airlinefs) {

                Log.i(TAG, "AirLine name = " + airline.name)

            }
        }

    }

    fun getFlightStatusInfo(flightNum: String) : String {
        var sb = StringBuilder()

        if (flightNum.isEmpty()) {
            sb.append("Flight number is empty!")
        } else {
            var flightNumberStr = "";
            for (c in flightNum) {
                if (Character.isLetterOrDigit(c)) {
                    flightNumberStr += c;
                }
            }

            // checking if the flight number is valid with checkFlightNumber function and use the flight number
            if (!(checkFlightNumber_ICAO(flightNumberStr) || checkFlightNumber_IATA(flightNumberStr))) {
                sb.append("Flight number is invalid!")
            }
            // if the above condition not fulfilled it mean we have valid flight number either IATA or ICAO
            // and so the code will continue to parse for json data and operate on functions
            else {
                // define time to Get flight data based on the user input current time  in account
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                val formatted = current.format(formatter)

                // conditionally parsing data for flight number type : IATA
                if (checkFlightNumber_IATA(flightNumberStr)) {

                    val flightUrl_IATA = (flightApi
                            + flightNumberStr.substring(0, 2) + "/" + flightNumberStr.substring(2) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")
                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_IATA).awaitString()

                        if (jsonStr.isNotEmpty()) {
                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                sb.append("No info about the flight!")
                            } else {
                                val flightStat = flightData.flightStatuses!![0]

                                sb.append("Flight number : ")
                                sb.append(flightStat.carrierFsCode)
                                sb.append(flightStat.flightNumber)
                                sb.append("\n")

                                // Departure
                                sb.append(flightStat.departureAirportFsCode)
                                sb.append(flightStat.departureDate?.dateLocal)
                                sb.append("\n")

                                // get airport info
                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!,
                                    departureAirportName,
                                    departureAirportCity,
                                    departureAirportCountry,
                                    departureAirportLonLat,
                                    sb
                                )
                                sb.append("\n")

                                // Arrival
                                sb.append(flightStat.arrivalAirportFsCode)
                                sb.append(flightStat.arrivalDate?.dateLocal)
                                sb.append("\n")

                                // get airport info
                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat,
                                    sb
                                )
                                sb.append("\n")

                                when (flightStat.status) {
                                    "S" -> {
                                        sb.append("Flight scheduled")
                                    }
                                    "A" -> {
                                        sb.append("Flight on Air")
                                    }
                                    "L" -> {
                                        sb.append("Flight landed")
                                    }
                                    else -> {
                                        sb.append(flightStat.status)
                                    }
                                }
                                sb.append("\n")

                                if (flightStat.delays == null) {
                                    sb.append("No delay!")
                                } else {
                                    sb.append("Delay: ")
                                    sb.append(flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes)
                                    sb.append(" minutes")
                                }
                                sb.append("\n")
                            }
                        }
                    }
                }// end for flightnumber type IATA

                // conditionally parsing data for flight number type : ICOA
                else {

                    val flightUrl_ICOA = (flightApi
                            + flightNumberStr.substring(0, 3) + "/" + flightNumberStr.substring(3) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")

                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_ICOA).awaitString()

                        if (jsonStr.isNotEmpty()) {
                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                sb.append("No info about the flight!")
                            } else {
                                val flightStat = flightData.flightStatuses!![0]

                                // Carrier + flight number
                                sb.append("Flight number : ")
                                sb.append(flightStat.carrierFsCode)
                                sb.append(flightStat.flightNumber)
                                sb.append("\n")

                                // Departure
                                sb.append(flightStat.departureAirportFsCode)
                                sb.append(flightStat.departureDate?.dateLocal)
                                sb.append("\n")

                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!,
                                    departureAirportName,
                                    departureAirportCity,
                                    departureAirportCountry,
                                    departureAirportLonLat,
                                    sb
                                )

                                sb.append("\n")

                                // Arrival
                                sb.append(flightStat.arrivalAirportFsCode)
                                sb.append(flightStat.arrivalDate?.dateLocal)
                                sb.append("\n")

                                // get airport info
                                getAirportInformation(
                                    flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat,
                                    sb
                                )

                                sb.append("\n")

                                when (flightStat.status) {
                                    "S" -> {
                                        sb.append("Flight scheduled")
                                    }
                                    "A" -> {
                                        sb.append("Flight on Air")
                                    }
                                    "L" -> {
                                        sb.append("Flight landed")
                                    }
                                    else -> {
                                        sb.append(flightStat.status)
                                    }
                                }

                                sb.append("\n")

                                if (flightStat.delays == null) {
                                    sb.append("No delay!")
                                } else {
                                    sb.append("Delay: ")
                                    sb.append(flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes)
                                    sb.append(" minutes")
                                }
                                sb.append("\n")
                            }
                        }
                    }
                }//end for flightnumber type  ICOA
            }
        }

        return sb.toString()
    }

    companion object {
        private const val TAG = "FlightInfo"
        private const val FLIGHT_NUMBER = "no.uio.in2000.team16.flynerd.FLIGHT_NUMBER"
        private const val AppID = "1738962c"
        private const val AppKey = "165179280d479dabbc4596b0269189aa"
        private const val flightApi = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/"
    }

}