package no.uio.in2000.team16.flynerd.uidesign


import android.annotation.SuppressLint
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
            val fnStr = "Flight number is empty!"
            flightStatus.text = fnStr
            flightStatus.setTextColor(Color.RED)
        } else {
            var flightNumberStr = ""
            for (c in flightStr) {
                if (Character.isLetterOrDigit(c)) {
                    flightNumberStr += c
                }
            }

            // checking if the flight number is valid with checkFlightNumber function and use the flight number
            if (!(checkFlightNumber_ICAO(flightNumberStr) || checkFlightNumber_IATA(
                    flightNumberStr
                ))
            ) {
                val fnInv = "Flight number is invalid!"
                flightStatus.text = fnInv
                flightStatus.setTextColor(Color.RED)
                flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)
            }
            // if the above condition not fulfilled it mean we have valid flight number either IATA or ICAO
            // and so the code will continue to parse for json data and operate on functions
            else {
                val checkFNum = "Checking $flightNumberStr"
                flightStatus.text = checkFNum
                flightStatus.setTextColor(Color.GREEN)
                flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                // define time to Get flight data based on the user input current time  in account
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                val formatted = current.format(formatter)

                // conditionally parsing data for flight number type : IATA
                if (checkFlightNumber_IATA(flightNumberStr)) {
                    try {
                        val flightUrl_IATA = (flightApi
                                + flightNumberStr.substring(0, 2) + "/" + flightNumberStr.substring(
                            2
                        ) + "/"
                                + "arr/" + formatted + "?appId=" + AppID
                                + "&appKey=" + AppKey
                                + "&utc=false")
                        Log.i(TAG, flightUrl_IATA)

                        lifecycleScope.launch {
                            val jsonStr = Fuel.get(flightUrl_IATA).awaitString()

                            if (jsonStr.isNotEmpty()) {
                                Log.i(TAG, jsonStr)

                                // parsing json data & intialize inner class data
                                val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                                if (flightData.flightStatuses.isNullOrEmpty()) {
                                    val nonAirCraftCommericialNotFound =  "This is  non-commercial aircraft \n Currently We  provide flight status information for commercial flight!"
                                    flightStatus.text =nonAirCraftCommericialNotFound

                                    flightStatus.setTextColor(Color.RED)
                                } else {
                                    val flightStat = flightData.flightStatuses!![0]

                                    // Carrier + flight number
                                    Log.i(TAG, "Carrier = " + flightStat.carrierFsCode)
                                    Log.i(TAG, "Flight number = " + flightStat.flightNumber)
                                    val flNumResu = "Flight number : " + flightStat.carrierFsCode + flightStat.flightNumber
                                    flightNumberRes.text = flNumResu


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
                                            val fltSc = "Flight scheduled"
                                            flightStatus.text = fltSc
                                        }
                                        "A" -> {
                                            val flInAir = "Flight on Air"
                                            flightStatus.text =  flInAir
                                        }
                                        "L" -> {
                                            val Fllanded = "Flight landed"
                                            flightStatus.text = Fllanded
                                        }
                                        else -> {
                                            flightStatus.text = flightStat.status
                                        }
                                    }
                                    flightStatus.setTextColor(Color.GREEN)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                                    if (flightStat.delays == null) {
                                        val noDelatText = "No delay!"
                                        flightDelayResult.text = noDelatText
                                        flightDelayResult.setTextColor(Color.GREEN)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    } else {
                                        val delayTxt = "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                        flightDelayResult.text = delayTxt
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

                        val flightUrl_ICOA = (flightApi
                                + flightNumberStr.substring(0, 3) + "/" + flightNumberStr.substring(
                            3
                        ) + "/"
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
                                    val nonCommricialAirCraftNotFound =  "This is  non-commercial aircraft, \nCurrently We  provide flight status information for commercial flight! "
                                    flightStatus.text = nonCommricialAirCraftNotFound

                                    flightStatus.setTextColor(Color.RED)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)
                                } else {
                                    val flightStat = flightData.flightStatuses!![0]
                                    Log.i(TAG, "FlightStatus = " + flightStat.status)

                                    // Carrier + flight number
                                    Log.i(TAG, "Carrier = " + flightStat.carrierFsCode)
                                    Log.i(TAG, "Flight number = " + flightStat.flightNumber)
                                    val flRsult =  "Flight number : " + flightStat.carrierFsCode + flightStat.flightNumber
                                    flightNumberRes.text =  flRsult


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
                                            val fltSc = "Flight scheduled"
                                            flightStatus.text = fltSc
                                        }
                                        "A" -> {
                                            val flInAir = "Flight on Air"
                                            flightStatus.text =  flInAir
                                        }
                                        "L" -> {
                                            val Fllanded = "Flight landed"
                                            flightStatus.text = Fllanded
                                        }
                                        else -> {
                                            flightStatus.text = flightStat.status
                                        }
                                    }
                                    flightStatus.setTextColor(Color.GREEN)
                                    flightStatus.setTypeface(null, Typeface.BOLD_ITALIC)

                                    if (flightStat.delays == null) {
                                        val noDelatText = "No delay!"
                                        flightDelayResult.text = noDelatText
                                        flightDelayResult.setTextColor(Color.GREEN)
                                        flightDelayResult.setTypeface(null, Typeface.BOLD_ITALIC)
                                    } else {
                                        val delayTxt = "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                        flightDelayResult.text = delayTxt

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
    private fun checkFlightNumber_IATA(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 3) {
            throw Exception(" too short flight number!")
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
    private fun checkFlightNumber_ICAO(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 4) {
            throw Exception(" too short flight number!")
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
                // // "Longitude = " + airport.longitude + " - Latitude = " + airport.latitude warnning error
                val geoLocation = "Longitude = " + airport.longitude + " - Latitude = " + airport.latitude
                airportLonLat.text = geoLocation

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