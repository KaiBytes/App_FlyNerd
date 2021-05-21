package no.uio.in2000.team16.flynerd.uidesign

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.R
import no.uio.in2000.team16.flynerd.flightData.Appendix
import no.uio.in2000.team16.flynerd.flightData.FlightData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * This class uses to get the flight information and used in displaying flight status as popup window
 * it is used in first home activity along google map displaying aircraft over given geographycal area , Norway & scandnivian region,
 *  it display the flight status information in popup window on user tap on aircraf on map
 */
class FlightStatusInfoPopUpWindow : AppCompatActivity() {
    lateinit var flightStatus: TextView
    lateinit var flightDelayResult: TextView
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

        flightStatus = findViewById(R.id.flightStatus)
        flightDelayResult = findViewById(R.id.flightResult)
        flightNumberRes = findViewById(R.id.flightNumberText)

        // Departure
        departureAirport = findViewById(R.id.departureAirport)
        departureDate = findViewById(R.id.departureDate)
        departureAirportName = findViewById(R.id.departureAirportName)
        departureAirportCity = findViewById(R.id.departureAirportCity)
        departureAirportCountry = findViewById(R.id.departureAirportCountry)
        departureAirportLonLat = findViewById(R.id.departureAirportLonLat)

        // Arrival
        arrivalAirport = findViewById(R.id.arrivalAirport)
        arrivalDate = findViewById(R.id.arrivalDate)
        arrivalAirportName = findViewById(R.id.arrivalAirportName)
        arrivalAirportCity = findViewById(R.id.arrivalAirportCity)
        arrivalAirportCountry = findViewById(R.id.arrivalAirportCountry)
        arrivalAirportLonLat = findViewById(R.id.arrivalAirportLonLat)

        //Excute onMarkClick function for intent and putExtra methods accessing flight info through flight number from
        // user on tap aircraf on map
        val flightStr = intent.getStringExtra(FLIGHT_NUMBER)!!

        // function to display flight status info on the popup window on aircraft onclick
        flightStatusInfo(flightStr)
    }

    /**  this function process the most important task in this flight stutus information  class
     *  straem json api by using fuel library
     *  purses the data class and get the flight information
     * excute check flight number function and intace the right flight number as parameter
     * assign the obtained data class flight data to the textView UI classes
     *@param flightStr
     */
    fun flightStatusInfo(flightStr: String) {
        if (flightStr.isEmpty()) {
            flightStatus.text = getString(R.string.flight_popup_flight_id_empty)
            flightStatus.setTextColor(Color.RED)
        } else {
            var flightNumberStr = ""
            for (c in flightStr) {
                if (Character.isLetterOrDigit(c)) {
                    flightNumberStr += c
                }
            }

            // checking if the flight number is valid with checkFlightNumber function and use the flight number
            if (!(checkFlightNumberICAO(flightNumberStr) || checkFlightNumberIATA(
                    flightNumberStr
                ))
            ) {
                flightStatus.text = getString(R.string.flight_popup_flight_id_invalid)
                flightStatus.setTextColor(Color.RED)
                flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)
            }
            // if the above condition not fulfilled it mean we have valid flight number either IATA or ICAO
            // and so the code will continue to parse for json data and operate on functions
            else {
                flightStatus.text =
                    getString(R.string.flight_popup_flight_id_checking, flightNumberStr)
                flightStatus.setTextColor(Color.GREEN)
                flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                // define time to Get flight data based on the user input current time  in account
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                val formatted = current.format(formatter)

                // conditionally parsing data for flight number type : IATA
                if (checkFlightNumberIATA(flightNumberStr)) {
                    try {
                        val flightUrlIATA = (flightApi
                                + flightNumberStr.substring(0, 2) + "/" + flightNumberStr.substring(
                            2
                        ) + "/"
                                + "arr/" + formatted + "?appId=" + AppID
                                + "&appKey=" + AppKey
                                + "&utc=false")
                        Log.i(TAG, flightUrlIATA)

                        lifecycleScope.launch {
                            val jsonStr = Fuel.get(flightUrlIATA).awaitString()

                            if (jsonStr.isNotEmpty()) {
                                Log.i(TAG, jsonStr)

                                // parsing json data & intialize inner class data
                                val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                                if (flightData.flightStatuses.isNullOrEmpty()) {
                                    flightStatus.text =
                                        getString(R.string.flight_popup_flight_id_non_commercial)
                                    flightStatus.setTextColor(Color.RED)
                                } else {
                                    val flightStat = flightData.flightStatuses!![0]

                                    // Carrier + flight number
                                    Log.i(TAG, "Carrier = " + flightStat.carrierFsCode)
                                    Log.i(TAG, "Flight number = " + flightStat.flightNumber)
                                    flightNumberRes.text = getString(
                                        R.string.flight_popup_flight_id_text,
                                        flightStat.carrierFsCode,
                                        flightStat.flightNumber
                                    )
                                    // Departure
                                    Log.i(
                                        TAG,
                                        "Departure airport = " + flightStat.departureAirportFsCode
                                    )
                                    Log.i(
                                        TAG,
                                        "Departure date = " + flightStat.departureDate?.dateLocal
                                    )

                                    departureAirport.text =
                                        flightStat.departureAirportFsCode
                                    departureDate.text = flightStat.departureDate?.dateLocal

                                    getAirportInformation(
                                        flightData.appendix!!,
                                        flightStat.departureAirportFsCode!!,
                                        departureAirportName,
                                        departureAirportCity,
                                        departureAirportCountry,
                                        departureAirportLonLat,

                                        )
                                    getAirlineInformation(
                                        flightData.appendix!!,
                                        flightStat.carrierFsCode!!
                                    )

                                    // Arrival
                                    Log.i(
                                        TAG,
                                        "Arrival airport = " + flightStat.arrivalAirportFsCode
                                    )
                                    Log.i(
                                        TAG,
                                        "Arrival date = " + flightStat.arrivalDate?.dateLocal
                                    )
                                    arrivalAirport.text = flightStat.arrivalAirportFsCode
                                    arrivalDate.text = flightStat.arrivalDate?.dateLocal
                                    // get airport info
                                    getAirportInformation(
                                        flightData.appendix!!,
                                        flightStat.arrivalAirportFsCode!!,
                                        arrivalAirportName,
                                        arrivalAirportCity,
                                        arrivalAirportCountry,
                                        arrivalAirportLonLat,

                                        )

                                    //  get airline info
                                    getAirlineInformation(
                                        flightData.appendix!!,
                                        flightStat.carrierFsCode!!
                                    )

                                    when (flightStat.status) {
                                        "S" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_scheduled)
                                        }
                                        "A" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_in_air)
                                        }
                                        "L" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_landed)
                                        }
                                        else -> {
                                            flightStatus.text = flightStat.status
                                        }
                                    }
                                    flightStatus.setTextColor(Color.GREEN)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                                    if (flightStat.delays == null) {
                                        flightDelayResult.text =
                                            getString(R.string.flight_popup_delay_none)
                                        flightDelayResult.setTextColor(Color.GREEN)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    } else {
                                        val minutes =
                                            flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes
                                        flightDelayResult.text = resources.getQuantityString(
                                            R.plurals.flight_popup_delay,
                                            minutes,
                                            minutes
                                        )
                                        flightDelayResult.setTextColor(Color.YELLOW)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    }
                                }
                            }
                        }

                    } catch (e: Exception) {

                    }

                }// end for flightnumber type IATA

                // conditionally parsing data for flight number type : ICOA
                else {
                    try {

                        val flightUrlICOA = (flightApi
                                + flightNumberStr.substring(0, 3) + "/" + flightNumberStr.substring(
                            3
                        ) + "/"
                                + "arr/" + formatted + "?appId=" + AppID
                                + "&appKey=" + AppKey
                                + "&utc=false")
                        Log.i(TAG, flightUrlICOA)

                        lifecycleScope.launch {
                            val jsonStr = Fuel.get(flightUrlICOA).awaitString()

                            if (jsonStr.isNotEmpty()) {
                                Log.i(TAG, jsonStr)

                                // parsing json data & intialize inner class data
                                val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                                if (flightData.flightStatuses.isNullOrEmpty()) {
                                    flightStatus.text =
                                        getString(R.string.flight_popup_flight_id_non_commercial)
                                    flightStatus.setTextColor(Color.RED)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)
                                } else {
                                    val flightStat = flightData.flightStatuses!![0]
                                    Log.i(TAG, "FlightStatus = " + flightStat.status)

                                    // Carrier + flight number
                                    Log.i(TAG, "Carrier = " + flightStat.carrierFsCode)
                                    Log.i(TAG, "Flight number = " + flightStat.flightNumber)
                                    flightNumberRes.text = getString(
                                        R.string.flight_popup_flight_id_text,
                                        flightStat.carrierFsCode,
                                        flightStat.flightNumber
                                    )

                                    // Departure
                                    Log.i(
                                        TAG,
                                        "Departure airport = " + flightStat.departureAirportFsCode
                                    )
                                    Log.i(
                                        TAG,
                                        "Departure date = " + flightStat.departureDate?.dateLocal
                                    )

                                    departureAirport.text =
                                        flightStat.departureAirportFsCode
                                    departureDate.text = flightStat.departureDate?.dateLocal

                                    getAirportInformation(
                                        flightData.appendix!!,
                                        flightStat.departureAirportFsCode!!,
                                        departureAirportName,
                                        departureAirportCity,
                                        departureAirportCountry,
                                        departureAirportLonLat,

                                        )
                                    getAirlineInformation(
                                        flightData.appendix!!,
                                        flightStat.carrierFsCode!!
                                    )

                                    // Arrival

                                    arrivalAirport.text = flightStat.arrivalAirportFsCode
                                    arrivalDate.text = flightStat.arrivalDate?.dateLocal
                                    // get airport info
                                    getAirportInformation(
                                        flightData.appendix!!,
                                        flightStat.arrivalAirportFsCode!!,
                                        arrivalAirportName,
                                        arrivalAirportCity,
                                        arrivalAirportCountry,
                                        arrivalAirportLonLat,

                                        )

                                    //  get airline info
                                    getAirlineInformation(
                                        flightData.appendix!!,
                                        flightStat.carrierFsCode!!
                                    )

                                    when (flightStat.status) {
                                        "S" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_scheduled)
                                        }
                                        "A" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_in_air)
                                        }
                                        "L" -> {
                                            flightStatus.text =
                                                getString(R.string.flight_popup_flight_status_landed)
                                        }
                                        else -> {
                                            flightStatus.text = flightStat.status
                                        }
                                    }
                                    flightStatus.setTextColor(Color.GREEN)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                                    if (flightStat.delays == null) {
                                        flightDelayResult.text =
                                            getString(R.string.flight_popup_delay_none)
                                        flightDelayResult.setTextColor(Color.GREEN)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    } else {
                                        val minutes =
                                            flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes
                                        flightDelayResult.text = resources.getQuantityString(
                                            R.plurals.flight_popup_delay,
                                            minutes,
                                            minutes
                                        )
                                        flightDelayResult.setTextColor(Color.YELLOW)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }
                } // end of checking ICAO and purse data
            }
        }

    }

    /**
     * This function check if the flight number is valid standerd flight number type of IATA
     * Flight number should have 2first characters as carrier and followed a number, ex: AFR454
     *In the aviation industry, a flight number or flight designator is a code for an airline service
     *consisting of three-character airline designator and a 1 to 4 digit number for  ICAO Flight No.
     * https://en.wikipedia.org/wiki/Flight_number
     *@param flightNumberStr
     *@return true, boolean value
     */
    private fun checkFlightNumberIATA(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 3) {
            return false
        }
        if (!Character.isLetterOrDigit(flightNumberStr[0]) || !Character.isLetterOrDigit(
                flightNumberStr[1]
            )
        ) {
            return false
        }
        for (i in 2 until flightNumberStr.length) {
            if (!Character.isDigit(flightNumberStr[i])) {
                return false
            }
        }
        return true
    }

    /**
     * This function check if the flight number is valid standerd flight number type of ICAO
     * Flight number should have 3 first characters as carrier and followed a number, ex: AFR454
     *In the aviation industry, a flight number or flight designator is a code for an airline service
     *consisting of three-character airline designator and a 1 to 4 digit number for  ICAO Flight No.
     * https://en.wikipedia.org/wiki/Flight_number
     *@param flightNumberStr
     *@return true, boolean value
     */
    private fun checkFlightNumberICAO(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 4) {
            return false
        }
        if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1]) || !Character.isLetter(
                flightNumberStr[2]
            )
        ) {
            return false
        }
        for (i in 3 until flightNumberStr.length) {
            if (!Character.isLetterOrDigit(flightNumberStr[i])) {
                return false
            }
        }
        return true
    }

    /**
     * this get the airport info from the airport list in appendix data class
     *@param appendix
     *@param airportfs
     *@param airportName
     *@param airportCountry
     *@param  airportLonLat
     */
    private fun getAirportInformation(
        appendix: Appendix,
        airportfs: String,
        airportName: TextView,
        airportCity: TextView,
        airportCountry: TextView,
        airportLonLat: TextView,

        ) {
        for (airport in appendix.airports!!) {
            if (airport.fs == airportfs) {
                Log.i(TAG, "Airport name = " + airport.name)
                Log.i(TAG, "City = " + airport.city)
                Log.i(TAG, "Country = " + airport.countryName)
                Log.i(TAG, "Longitude = " + airport.longitude)
                Log.i(TAG, "Latitude = " + airport.latitude)
                Log.i(TAG, "Weather link = " + airport.weatherUrl)

                airportName.text = airport.name
                airportCity.text = airport.city
                airportCountry.text = airport.countryName
                airportLonLat.text =
                    getString(R.string.flight_popup_lon_lat, airport.longitude, airport.latitude)
            }
        }

    }

    /**
     * this get the airline info from the airline list in appendix data class
     *@param appendix
     *@param airlinefs
     */
    private fun getAirlineInformation(appendix: Appendix, airlinefs: String) {
        for (airline in appendix.airlines!!) {
            if (airline.fs == airlinefs) {

                Log.i(TAG, "AirLine name = " + airline.name)

            }
        }

    }

    companion object {
        private const val TAG = "FlightInfo"
        private const val FLIGHT_NUMBER = "no.uio.in2000.team16.flynerd.FLIGHT_NUMBER"
        private const val AppID = "f3304cbc"
        private const val AppKey = "85c5265447ef5293103d939ce04e8cd8"
        private const val flightApi =
            "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/"
    }

}