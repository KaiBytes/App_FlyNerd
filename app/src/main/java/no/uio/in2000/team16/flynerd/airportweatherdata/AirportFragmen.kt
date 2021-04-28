package no.uio.in2000.team16.flynerd.airportweatherdata

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView

import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.R

/**
 * AirportFragment class
 *
 * shows airport and weather information when choosing an elemenet in recyclerview
 *
 * used in: MainActivity.kt
 */

class AirportFragment(item : Airport) : Fragment() {
    val airportObject = item
    var temperatureView: TextView? = null
    var nameView: TextView? = null
    var cityName: TextView? = null
    var longitude: TextView? = null
    var latitude: TextView? = null
    var precipationView: TextView? = null
    var windView: TextView? = null
    var skyView: TextView? = null



    //called before on view created
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_airport, container, false)
        nameView = view.findViewById(R.id.airport)
        cityName = view.findViewById(R.id.city_name)
        longitude = view.findViewById(R.id.longtitude)
        latitude = view.findViewById(R.id.latitude)
        precipationView = view.findViewById(R.id.temperature)
        windView = view.findViewById(R.id.precipation)
        skyView = view.findViewById(R.id.weather)
        temperatureView = view.findViewById(R.id.wind)





        return view
    }

    /**
     * called last
     * lifecyclescope uses main dispatcher
     * more can be read by following link below
     * https://developer.android.com/topic/libraries/architecture/coroutines#lifecyclescope
     *
     * I quote : "A LifecycleScope is defined for each Lifecycle object.
     * Any coroutine launched in this scope is canceled when the Lifecycle is destroyed.
     * You can access the CoroutineScope of the Lifecycle either via
     * lifecycle.coroutineScope or lifecycleOwner.lifecycleScope properties."
     *
     * data is destoryed when fragment is swiped away
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            airportObject.callForecastAPI()
            nameView!!.text = airportObject.name
            cityName!!.text = airportObject.city
            longitude!!.text = airportObject.longtitude.toString()
            latitude!!.text = airportObject.latitude.toString()
            temperatureView!!.text = airportObject.getTemperature()
            precipationView!!.text = airportObject.getPrecipationAmount()
            windView!!.text = airportObject.getWindForce()
            skyView!!.text = airportObject.getCurrentWeather()
        }
    }

}
