package no.uio.in2000.team16.flynerd.uidesign

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import no.uio.in2000.team16.flynerd.MapActivity
import no.uio.in2000.team16.flynerd.R
import no.uio.in2000.team16.flynerd.WeatherActivity
import no.uio.in2000.team16.flynerd.uidesign.*

class HomepageBoard : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null
    var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page_board)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        //textView = findViewById(R.id.textView)
        toolbar = findViewById(R.id.toolbar)


        // toolbar
        setSupportActionBar(toolbar)


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


            }
            R.id.flightStatus -> {
                val intent = Intent(this@HomepageBoard , FlightStatusUI::class.java)
                startActivity(intent)
            }

            R.id.flightDelay -> {
                val intent = Intent(this@HomepageBoard , FlightDelayUI::class.java)
                startActivity(intent)
            }

            R.id.airportweather -> {
                val intent = Intent(this@HomepageBoard , WeatherActivity::class.java)
                startActivity(intent)
            }

            R.id.func4 -> {
                val intent = Intent(this@HomepageBoard , MapActivity::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@HomepageBoard , Functionality5::class.java)
                startActivity(intent)
            }

            R.id.func6 -> {
                val intent = Intent(this@HomepageBoard , Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun goToFlightStatus(view: View?) {
        val intent = Intent(this, FlightStatusUI::class.java)
        startActivity(intent)
    }

    fun goToFlightDelay(view: View?) {
        val intent = Intent(this, FlightDelayUI::class.java)
        startActivity(intent)
    }

    fun goToAirportWeather(view: View?) {
        val intent = Intent(this, WeatherActivity::class.java)
        startActivity(intent)
    }

    fun goToFunctionality4(view: View?) {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    fun goToFunctionality5(view: View?) {
        val intent = Intent(this, Functionality5::class.java)
        startActivity(intent)
    }
    fun goToFunctionality6(view: View?) {
        val intent = Intent(this, Functionality4::class.java)
        startActivity(intent)
    }

}
