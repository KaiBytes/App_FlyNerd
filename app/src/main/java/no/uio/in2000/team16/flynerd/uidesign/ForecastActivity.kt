package no.uio.in2000.team16.flynerd.uidesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import no.uio.in2000.team16.flynerd.*
import no.uio.in2000.team16.flynerd.airportweatherdata.ForecastViewModel

class ForecastActivity() : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //defining all views
    var temperatureView: TextView? = null
    var nameView: TextView? = null
    var cityName: TextView? = null
    var longitude: TextView? = null
    var latitude: TextView? = null
    var precipationView: TextView? = null
    var windView: TextView? = null
    var skyView: TextView? = null
    var pictureView : ImageView? = null
    var imageId : Int? = null


    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    var menu: Menu? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast_updated)

        //importing airport object from intent
        val i = getIntent()
        val airportObject : Airport = i.getSerializableExtra("item") as Airport

        val viewModel by viewModels<ForecastViewModel>()
        viewModel.callForecastAPI(airportObject)

        //initializing all views
        nameView = findViewById(R.id.airport)
        cityName = findViewById(R.id.city_name)
       // longitude = findViewById(R.id.longtitude)
       // latitude = findViewById(R.id.latitude)
        precipationView = findViewById(R.id.precipation)
        windView = findViewById(R.id.wind)
        skyView = findViewById(R.id.weather)
        temperatureView = findViewById(R.id.temperature)
        pictureView = findViewById(R.id.picture)

        //once livedata variable gets updated in forecastActivityViewModel,
        //information will be displayed. observer sniffs for updates
        //ui components will be updated with every change registered
        viewModel.forecastLiveData.observe(this, Observer {

            nameView!!.text = "Airport Name : ${airportObject.name}"
            cityName!!.text = "Serviced City : ${airportObject.city}"
            //longitude!!.text = "LON : ${airportObject.longtitude}"
            //latitude!!.text = "LAT : ${airportObject.latitude}"
            temperatureView!!.text = "Temperature : ${airportObject.getTemperature()}"
            precipationView!!.text = "Precipation Amount : ${airportObject.getPrecipationAmount()}"
            windView!!.text = "Wind Force : ${airportObject.getWindForce()}"
            skyView!!.text = "Current Weather : ${airportObject.getCurrentWeather()}"
            imageId = resources.getIdentifier(airportObject.getCurrentWeather().replace(" ", "_"), "drawable", packageName)
            Log.d("imageId", "imageId = $imageId, name of picture = ${airportObject.getCurrentWeather().replace(" ", "_")}")
            if (imageId != null){
                pictureView!!.setImageResource(imageId!!)
            }

        })



        // Navigation main menu you find for navigation menus

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
                val intent = Intent(this@ForecastActivity, MapActivity::class.java)
                startActivity(intent)
            }
            R.id.flightStatus -> {
                val intent = Intent(this@ForecastActivity, FlightStatusUI::class.java)
                startActivity(intent)

            }



            R.id.airportweather -> {
                val intent = Intent(this@ForecastActivity, AirportsListActivity::class.java)
                startActivity(intent)

            }


        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }
}