package no.uio.in2000.team16.flynerd

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
import android.widget.TextView
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
import no.uio.in2000.team16.flynerd.adapter.AirportAdapter
import no.uio.in2000.team16.flynerd.airportweatherdata.Airport
import no.uio.in2000.team16.flynerd.airportweatherdata.AirportsList
import no.uio.in2000.team16.flynerd.uidesign.*
import java.util.*

class WeatherActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var matchedAirports  = mutableListOf<Airport>()
    var recycleAdapter: RecyclerView.Adapter<AirportAdapter.ViewHolder>? = null

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        //soft keyboard pop up covers layout instead of pushing it upwards
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //fetching views to edit
        val userInput = findViewById<AutoCompleteTextView>(R.id.searchFstatus)
        val cities: Array<out String> = resources.getStringArray(R.array.cities)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)

        //Setting up recyclerview
        val layoutManager: RecyclerView.LayoutManager? = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.recyclerView).layoutManager = layoutManager

        //setting up autocomplete functionality
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cities).also { adapter ->
            userInput.setAdapter(adapter)
        }

        //initializing airports database (without forecast data)
        val initializer = AirportsList(this, R.raw.intair_city)

        CoroutineScope(IO).launch {
            initializer.readAirports()
        }

        searchButton.setOnClickListener {
            //capitalizes all words in string
            val input : String? = userInput.text.toString().capitalizeFirstLetter()

            //clear results previous search
            matchedAirports.clear()

            CoroutineScope(IO).launch {
                //search for airports servicing user specified city
                for (airport in initializer.airports){
                    if (input == airport.city){
                        matchedAirports.add(airport)
                    }
                }

                runOnUiThread{
                    //show search results
                    recycleAdapter = AirportAdapter(matchedAirports)
                    findViewById<RecyclerView>(R.id.recyclerView).adapter = recycleAdapter
                }
            }
            dismissKeyboard(this)
        }

        // Navigation main menu

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
    }

    //function that capitalizes all words in a string. Needed in city names.
    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

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
                val intent = Intent(this@WeatherActivity, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent = Intent(this@WeatherActivity, FlightStatusUI::class.java)
                startActivity(intent)

            }

            R.id.flightDelay -> {

                val intent = Intent(this@WeatherActivity, FlightDelayUI::class.java)
                startActivity(intent)

            }

            R.id.airportweather -> {

            }

            R.id.func4 -> {
                val intent = Intent(this@WeatherActivity, FlightStatusInfo::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@WeatherActivity, Functionality5::class.java)
                startActivity(intent)

            }

            R.id.func6 -> {
                val intent = Intent(this@WeatherActivity, Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}