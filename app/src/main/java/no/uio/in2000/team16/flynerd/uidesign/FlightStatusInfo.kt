package no.uio.in2000.team16.flynerd.uidesign

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.TextView
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import no.uio.in2000.team16.flynerd.MapActivity
import no.uio.in2000.team16.flynerd.R
import no.uio.in2000.team16.flynerd.WeatherActivity
import no.uio.in2000.team16.flynerd.flightData.Appendix
import no.uio.in2000.team16.flynerd.flightData.FlightData
import no.uio.in2000.team16.flynerd.uidesign.*

class FlightStatusInfo : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private  val TAG = "FlightInfo"
    private val AppID = "25891f46" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private  val AppKey = "10cad6d6fa38acdc48fc9f1a6b7b5adb" //  USE YOUR OWN API-key FROM YOUR SIGN UP ACCOUNT
    private  val flightApi = "https://api.flightstats.com/flex/flightstatus/rest/v2/json/flight/status/"


    lateinit var flightNumberStr: EditText // accept from user the flight number
    lateinit var flightStatus: TextView   // display the flight status
    lateinit var flightDelayResult: TextView // display delay result
    lateinit var SubmitToCheckFlight: Button
    lateinit var flightNumberRes: TextView

    //navigation drawer
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null


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
        setContentView(R.layout.flight_status_main2)


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        //textView = findViewById(R.id.textView)
        toolbar = findViewById(R.id.toolbar)


        // toolbar
        setSupportActionBar(toolbar)

        // navigation

        // navigationbar
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setNavigationItemSelectedListener(this)
        navigationView?.setCheckedItem(R.id.nav_home)



        // Intialize the buttons and textView and editText for simple UI user intereaction
        //flightNumberStr = findViewById<EditText>(R.id.flightNumber)  // EditTxt // accept from user
        //SubmitToCheckFlight = findViewById<Button>(R.id.flightNumberButton) // Button
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



        val search = findViewById<EditText>(R.id.searchFstatus)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)
        val result = findViewById<RelativeLayout>(R.id.result)
        val loading = findViewById<LottieAnimationView>(R.id.animationLoading2)

        result.setVisibility(View.GONE)
        loading.setVisibility(View.GONE)




        // implementation of on clicklistener for check flight button
        searchButton.setOnClickListener(View.OnClickListener {

            loading.setVisibility(View.VISIBLE)
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)

            val flightStr = search.text.toString()

            // condition for checking flight number and user iput string
            if (flightStr.isEmpty()) {
                flightStatus.text = "Flight number is empty!"
                flightStatus.setTextColor(Color.RED)
            }
            // checking if the flight number is valid with checkFlightNumber function and use the flight number
            else if ( !(checkFlightNumber_ICAO(flightStr)|| checkFlightNumber_IATA(flightStr))) {
                flightStatus.text = "Flight number is invalid!"
                flightStatus.setTextColor(Color.RED)
            }
            // if the above condition not fulfilled it mean we have valid flight number either IATA or ICAO
            // and so the code will continue to parse for json data and operate on functions
            else {
                flightStatus.text = "Checking $flightStr"
                flightStatus.setTextColor(Color.BLUE)

                // define time to Get flight data based on the user input current time  in account
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
                val formatted = current.format(formatter)


                // conditionally parsing data for flight number type : IATA
                if(checkFlightNumber_IATA(flightStr)){

                    val flightUrl_IATA = (flightApi
                            + flightStr.substring(0, 2) + "/" + flightStr.substring(2) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")
                    Log.i(TAG, flightUrl_IATA)

                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_IATA).awaitString()

                        if (jsonStr != null) {
                            Log.i(TAG, jsonStr)

                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                flightStatus.text = "No info about the flight!"
                                flightStatus.setTextColor(Color.RED)
                            } else {
                                val flightStat = flightData.flightStatuses!![0]
                                Log.i(TAG, "FlightStatus = " + flightStat.status)



                                // Carrier + flight number
                                Log.i(TAG, "Carrier = " + flightStat.carrierFsCode);
                                Log.i(TAG, "Flight number = " + flightStat.flightNumber);
                                flightNumberRes.text = "Flight number : " + flightStat.carrierFsCode +  flightStat.flightNumber

                                // Departure
                                Log.i(TAG, "Departure airport = " + flightStat.departureAirportFsCode);
                                Log.i(TAG, "Departure date = " + flightStat.departureDate?.dateLocal);

                                departureAirport.text = flightStat.departureAirportFsCode  // "\n datee1: "+flightStat.departureDate?.dateLocal;
                                departureDate.text = flightStat.departureDate?.dateLocal;

                                getAirportInformation(flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!, departureAirportName, departureAirportCity, departureAirportCountry, departureAirportLonLat);
                                getAirlineInformation(flightData.appendix!!, flightStat.carrierFsCode!!);

                                // Arrival
                                Log.i(TAG, "Arrival airport = " + flightStat.arrivalAirportFsCode);
                                Log.i(TAG, "Arrival date = " + flightStat.arrivalDate?.dateLocal);
                                arrivalAirport.text = flightStat.arrivalAirportFsCode;
                                arrivalDate.text = flightStat.arrivalDate?.dateLocal;
                                // get airport info
                                getAirportInformation(flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat);

                                //  get airline info
                                getAirlineInformation(flightData.appendix!!, flightStat.carrierFsCode!!);




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
                                    flightDelayResult.text = "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                    flightDelayResult.setTextColor(Color.RED)
                                }
                            }
                        }


                    }


                }// end for flightnumber type IATA


                // conditionally parsing data for flight number type : ICOA
                else {

                    val flightUrl_ICOA = (flightApi
                            + flightStr.substring(0, 3) + "/" + flightStr.substring(3) + "/"
                            + "arr/" + formatted + "?appId=" + AppID
                            + "&appKey=" + AppKey
                            + "&utc=false")
                    Log.i(TAG, flightUrl_ICOA)

                    lifecycleScope.launch {
                        val jsonStr = Fuel.get(flightUrl_ICOA).awaitString()

                        if (jsonStr != null) {
                            Log.i(TAG, jsonStr)

                            // parsing json data & intialize inner class data
                            val flightData = Gson().fromJson(jsonStr, FlightData::class.java)

                            if (flightData.flightStatuses.isNullOrEmpty()) {
                                flightStatus.text = "No info about the flight!"
                                flightStatus.setTextColor(Color.RED)
                            } else {
                                val flightStat = flightData.flightStatuses!![0]
                                Log.i(TAG, "FlightStatus = " + flightStat.status)



                                // Carrier + flight number
                                Log.i(TAG, "Carrier = " + flightStat.carrierFsCode);
                                Log.i(TAG, "Flight number = " + flightStat.flightNumber);
                                flightNumberRes.text = "Flight number : " + flightStat.carrierFsCode +  flightStat.flightNumber

                                // Departure
                                Log.i(TAG, "Departure airport = " + flightStat.departureAirportFsCode);
                                Log.i(TAG, "Departure date = " + flightStat.departureDate?.dateLocal);

                                departureAirport.text = flightStat.departureAirportFsCode  // "\n datee1: "+flightStat.departureDate?.dateLocal;
                                departureDate.text = flightStat.departureDate?.dateLocal;

                                getAirportInformation(flightData.appendix!!,
                                    flightStat.departureAirportFsCode!!, departureAirportName, departureAirportCity, departureAirportCountry, departureAirportLonLat);
                                getAirlineInformation(flightData.appendix!!, flightStat.carrierFsCode!!);

                                // Arrival
                                Log.i(TAG, "Arrival airport = " + flightStat.arrivalAirportFsCode);
                                Log.i(TAG, "Arrival date = " + flightStat.arrivalDate?.dateLocal);
                                arrivalAirport.text = flightStat.arrivalAirportFsCode;
                                arrivalDate.text = flightStat.arrivalDate?.dateLocal;
                                // get airport info
                                getAirportInformation(flightData.appendix!!,
                                    flightStat.arrivalAirportFsCode!!,
                                    arrivalAirportName,
                                    arrivalAirportCity,
                                    arrivalAirportCountry,
                                    arrivalAirportLonLat);

                                //  get airline info
                                getAirlineInformation(flightData.appendix!!, flightStat.carrierFsCode!!);




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
                                    flightDelayResult.text = "Delay: " + (flightStat.delays!!.departureRunwayDelayMinutes + flightStat.delays!!.arrivalGateDelayMinutes) + " minutes"
                                    flightDelayResult.setTextColor(Color.RED)
                                }
                            }
                        }


                    }


                }//end for flightnumber type  ICOA


                //handler

                Handler().postDelayed(
                    {
                        loading.setVisibility(View.GONE)
                        result.setVisibility(View.VISIBLE)
                    },
                    5000 // value in milliseconds
                )


            }
        }) // end of onclickListener
    }


    // Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for IATA Flight No.
    // https://en.wikipedia.org/wiki/Flight_number
    private fun checkFlightNumber_IATA(flightNumberStr: String): Boolean {
        if (flightNumberStr.length < 3) {
            return false
        }
        if (!Character.isLetterOrDigit(flightNumberStr[0]) || !Character.isLetterOrDigit(flightNumberStr[1])) {
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
        if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1]) || !Character.isLetter(flightNumberStr[2])) {
            return false
        }
        for (i in 3 until flightNumberStr.length) {
            if (!Character.isLetterOrDigit(flightNumberStr[i])) {
                return false
            }
        }
        return true
    }

    private fun getAirportInformation(appendix : Appendix,
                                      airportfs : String,
                                      airportName : TextView,
                                      airportCity : TextView,
                                      airportCountry : TextView,
                                      airportLonLat : TextView) {
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
            }
        }

    }

    private fun getAirlineInformation(appendix : Appendix, airlinefs : String){
        for (airline in appendix.airlines!!) {
            if (airline.fs == airlinefs) {

                Log.i(TAG, "AirLine name = " + airline.name)

            }
        }

    }



    // navigations



    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this@FlightStatusInfo, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent = Intent(this@FlightStatusInfo, FlightStatusUI::class.java)
                startActivity(intent)

            }



            R.id.airportweather -> {
                val intent = Intent(this@FlightStatusInfo, WeatherActivity::class.java)
                startActivity(intent)
            }



        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }


    companion object {
        const val TAG = "MapActivity"
        const val FLIGHT_NUMBER = "no.uio.in2000.team16.flynerd.FLIGHT_NUMBER"
    }

}