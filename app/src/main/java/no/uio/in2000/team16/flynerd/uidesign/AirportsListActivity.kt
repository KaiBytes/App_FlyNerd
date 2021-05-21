package no.uio.in2000.team16.flynerd.uidesign

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.MapActivity
import no.uio.in2000.team16.flynerd.R
import no.uio.in2000.team16.flynerd.adapter.AirportAdapter
import no.uio.in2000.team16.flynerd.airportweatherdata.AirportsListViewModel

class AirportsListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //    var matchedAirports  = mutableListOf<Airport>()
    var recycleAdapter: RecyclerView.Adapter<AirportAdapter.ViewHolder>? = null
    val viewModel by viewModels<AirportsListViewModel>()

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        //fetching views
        val userInput = findViewById<AutoCompleteTextView>(R.id.searchFstatus)
        val cities: Array<out String> = resources.getStringArray(R.array.cities)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)

        //Setting up recyclerview
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView).also {
            it.layoutManager = layoutManager
        }

        //initializing airports database (without forecast data)

        viewModel.createDb(this, R.raw.intair_city)

        //soft keyboard pop up covers layout instead of pushing it upwards
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //setting up autocomplete functionality
        ArrayAdapter(this, android.R.layout.simple_list_item_1, cities).also { adapter ->
            userInput.setAdapter(adapter)
        }

        //search button implementation
        searchButton.setOnClickListener {
            //capitalizes all words in string
            val input: String = userInput.text.toString().capitalizeFirstLetter()

            //if input run IO coroutine to find airports that match search criteria
            if (input != null) {
                CoroutineScope(IO).launch {
                    viewModel.matchAirportWithCity(input)
                }
            }

            dismissKeyboard(this)
        }

        //show results once livedata variable in viewModel signals an update
        //this means that coroutine above was succesfull
        viewModel.matchedLiveData.observe(this@AirportsListActivity) {

            //show a toast with warning message when no matched airports have been found
            if (viewModel.matchedLiveData.value!!.size == 0) {
                Toast.makeText(
                    this,
                    "No airports are servicing this city.\nDid you spell the city name correctly?",
                    Toast.LENGTH_LONG
                ).show()
            }

            recycleAdapter = AirportAdapter(viewModel.matchedLiveData.value!!)
            recyclerView.adapter = recycleAdapter
        }

        // Navigation main menu

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

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
    }

    //function that capitalizes all words in a string. Needed in city names.
    private fun String.capitalizeFirstLetter() =
        this.split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecaseChar) }.trimEnd()

    private fun dismissKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (null != activity.currentFocus) imm.hideSoftInputFromWindow(
            activity.currentFocus!!.applicationWindowToken, 0
        )
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
                val intent = Intent(this@AirportsListActivity, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent =
                    Intent(this@AirportsListActivity, FlightStatusInfoSearchFlight::class.java)
                startActivity(intent)

            }

            R.id.airportweather -> {

            }
            R.id.Setting -> {
                val intent = Intent(this@AirportsListActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}