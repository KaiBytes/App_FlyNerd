package no.uio.in2000.team16.flynerd

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.NonCancellable.join
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AirportFragment(item : Airport) : Fragment() {

    val airportObject = item
    var temperatureView : TextView? = null
    var nameView : TextView? = null
    var precipationView : TextView? = null
    var windView : TextView? = null
    var skyView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            airportObject.callForecastAPI()
        }
        Log.d("fragment", "calll succeeded")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_airport, container,
            false)
        nameView  = view.findViewById(R.id.textView)
        precipationView  = view.findViewById(R.id.textView2)
        windView  = view.findViewById(R.id.textView4)
        skyView  = view.findViewById(R.id.textView5)
        temperatureView = view.findViewById(R.id.textView3)

        temperatureView!!.text = airportObject.getTemperature()
        precipationView!!.text = airportObject.getPrecipationAmount()
        windView!!.text = airportObject.getWindForce()
        nameView!!.text = airportObject.name
        skyView!!.text = airportObject.getCurrentWeather()
        return view
    }

}