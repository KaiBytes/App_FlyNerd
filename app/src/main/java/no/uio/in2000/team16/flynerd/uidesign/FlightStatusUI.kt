package no.uio.in2000.team16.flynerd.uidesign

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import no.uio.in2000.team16.flynerd.Flight
import no.uio.in2000.team16.flynerd.FlightDisplayAdapter
import no.uio.in2000.team16.flynerd.FlightId
import no.uio.in2000.team16.flynerd.R
import no.uio.in2000.team16.flynerd.api.FlightStatusRepository
import okhttp3.HttpUrl
import java.time.LocalDate

class FlightStatusUI : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null

    private lateinit var flightDisplayAdapter: FlightDisplayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flight_status_main)

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

        flightDisplayAdapter = FlightDisplayAdapter()
        findViewById<RecyclerView>(R.id.flight_display).run {
            adapter = flightDisplayAdapter
            layoutManager = LinearLayoutManager(this@FlightStatusUI)
        }

        val search = findViewById<EditText>(R.id.searchFstatus)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)
        val loading = findViewById<LottieAnimationView>(R.id.animationLoading2)

        loading.setVisibility(View.GONE)
        searchButton.setOnClickListener {
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )

            if (search.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "empty", Toast.LENGTH_LONG).show()
            } else {
                //call api
                //update result in view
                //show view
                flightsFetch()
            }
        }
    }

    private fun flightsFetch() {
        val header = findViewById<ConstraintLayout>(R.id.flight_header)
        val display = findViewById<RecyclerView>(R.id.flight_display)
        header.visibility = View.GONE
        display.visibility = View.GONE

        // input the flight number as string
        val flightNumberStr = findViewById<EditText>(R.id.searchFstatus).text
        val date = LocalDate.now()

        // check if the flight number is valid and use the flight number, else show toast
        val flightId = try {
            FlightId.parse(flightNumberStr)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.localizedMessage ?: "$e", Toast.LENGTH_LONG).show()
            return
        }//

        val baseUrl = HttpUrl.get(getString(R.string.flightstats_base_url))
        val appId = getString(R.string.flightstats_app_id)
        val appKey = getString(R.string.flightstats_app_key)

        val repository = FlightStatusRepository(baseUrl, appId, appKey)

        // get data from repository and print it to log, show toast on error
        (lifecycleScope + Dispatchers.IO).launch {
            val loading = findViewById<LottieAnimationView>(R.id.animationLoading2)
            withContext(Dispatchers.Main) {
                loading.visibility = View.VISIBLE
            }

            val flight: Flight? = try {
                repository.byFlightIdArrivingOn(flightId, date)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FlightStatusUI,
                        e.localizedMessage ?: "$e",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                return@launch
            } finally {
                withContext(Dispatchers.Main) {
                    loading.visibility = View.GONE
                }
            }

            withContext(Dispatchers.Main) {
                bindHeader(flight)
                flightDisplayAdapter.flight = flight
                header.visibility = View.VISIBLE
                display.visibility = View.VISIBLE
            }

            if (flight != null) {
                val airports = listOf(flight.departure.airport.iata) +
                        flight.mids.map { it.airport.iata } +
                        listOf(flight.arrival.airport.iata)
                Log.i(
                    TAG,
                    "fetched flight: ${flight.flightIdIATA}, ${airports.joinToString(" -> ")}"
                )
                Log.d(TAG, "flight: $flight")
            } else {
                Log.i(TAG, "fetched flight: $flight")

            }
        }
    }

    private fun bindHeader(flight: Flight?) {
        if (flight != null) {
            findViewById<TextView>(R.id.flight_header_id_iata).text = flight.flightIdIATA.toString()
            findViewById<TextView>(R.id.flight_header_id_sep).visibility =
                flight.flightIdICAO?.let { View.VISIBLE } ?: View.INVISIBLE
            findViewById<TextView>(R.id.flight_header_id_icao).text =
                flight.flightIdICAO?.toString() ?: ""
            findViewById<TextView>(R.id.flight_header_airline).text = flight.airline.name
            findViewById<TextView>(R.id.flight_header_no_flights).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.flight_header_id_iata).text = ""
            findViewById<TextView>(R.id.flight_header_id_sep).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.flight_header_id_icao).text = ""
            findViewById<TextView>(R.id.flight_header_airline).text = ""
            findViewById<TextView>(R.id.flight_header_no_flights).visibility = View.VISIBLE
        }
    }

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
                val intent = Intent(this@FlightStatusUI, HomepageBoard::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {

            }

            R.id.flightDelay -> {
                val intent = Intent(this@FlightStatusUI, FlightDelayUI::class.java)
                startActivity(intent)
            }

            R.id.airportweather -> {
                val intent = Intent(this@FlightStatusUI, AirportWeatherUI::class.java)
                startActivity(intent)
            }

            R.id.func4 -> {
                val intent = Intent(this@FlightStatusUI, Functionality4::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@FlightStatusUI, Functionality5::class.java)
                startActivity(intent)
            }

            R.id.func6 -> {
                val intent = Intent(this@FlightStatusUI, Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        const val TAG = "FlightStatusUI"
        const val FLIGHT_NUMBER = "no.uio.in2000.team16.flynerd.uidesign.FLIGHT_NUMBER"

    }
}
