package no.uio.in2000.team16.flynerd

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView

class FlightStatus : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null
    var textView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_status)

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



        val search = findViewById<EditText>(R.id.searchFstatus)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)
        val result = findViewById<RelativeLayout>(R.id.result)
        val loading = findViewById<LottieAnimationView>(R.id.animationLoading2)

        result.setVisibility(View.GONE)
        loading.setVisibility(View.GONE)
        searchButton.setOnClickListener{
            loading.setVisibility(View.VISIBLE)
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)

            if (search.text.toString().trim().isEmpty()) {
                Toast.makeText(this,"empty", Toast.LENGTH_LONG).show()
            }
            else{
                //call api
                //update result in the cardview
                //show cardview
                Handler().postDelayed(
                    {
                        loading.setVisibility(View.GONE)
                        result.setVisibility(View.VISIBLE)
                    },
                    5000 // value in milliseconds
                )

            }

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
                val intent = Intent(this@FlightStatus, HomepageBoard::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {

            }

            R.id.flightDelay -> {
                val intent = Intent(this@FlightStatus, FlightDelay::class.java)
                startActivity(intent)
            }

            R.id.airportweather -> {
                val intent = Intent(this@FlightStatus, AirportWeather::class.java)
                startActivity(intent)
            }

            R.id.func4 -> {
                val intent = Intent(this@FlightStatus, Functionality4::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@FlightStatus, Functionality5::class.java)
                startActivity(intent)
            }

            R.id.func6 -> {
                val intent = Intent(this@FlightStatus, Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}
