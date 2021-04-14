package no.uio.in2000.team16.flynerd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class AirportWeather : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null
    var textView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airport_weather)

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
                val intent = Intent(this@AirportWeather, HomepageBoard::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent = Intent(this@AirportWeather, FlightStatus::class.java)
                startActivity(intent)

            }

            R.id.flightDelay -> {

                val intent = Intent(this@AirportWeather, FlightDelay::class.java)
                startActivity(intent)

            }

            R.id.airportweather -> {

            }

            R.id.func4 -> {
                val intent = Intent(this@AirportWeather, Functionality4::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@AirportWeather, Functionality5::class.java)
                startActivity(intent)
            }

            R.id.func6 -> {
                val intent = Intent(this@AirportWeather, Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}