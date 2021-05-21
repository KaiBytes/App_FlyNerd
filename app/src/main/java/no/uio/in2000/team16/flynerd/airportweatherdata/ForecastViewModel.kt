package no.uio.in2000.team16.flynerd.airportweatherdata

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import kotlinx.coroutines.launch
import no.uio.in2000.team16.flynerd.Airport

/**
 * partner class to the forecast activity class.
 * this particular class implements the viewmodel interface
 * attempt to follow MVVM principles
 *
 * creation of this class was necessary to clearly seperate responsibilities for each class
 * UI controllers like activities and fragments should only be responsible for displaying UI components
 * and responding to user interactions.
 * Assigning too many responsibilities to UI controllers bloats the class, makes it difficult to test
 * and bnegatively affects app performance
 *
 * this viewmodel class has the following responsibiliets
 * 1. prepare data for ui controller by calling API-> forecast activity
 * 2. retains the data when screen layout is reconfigured
 *
 */
class ForecastViewModel : ViewModel() {

    //manages and retains data for forecast activity
    val forecastLiveData = MutableLiveData<Forecast>()

    fun callForecastAPI(instance: Airport) {
        // "lat=${String.format("%.4f", instance.latitude)}&lon=${String.format("%.4f", instance.longtitude)}"
        val baseURL = "https://in2000-apiproxy.ifi.uio.no/weatherapi/locationforecast/2.0/compact?"
        //formatted lat/lon coordinates to satisfy conditions set by MET
        //arguments to parameters
        val geoLocation = "lat=${instance.latitude}&lon=${instance.longtitude}"
        Log.d("geolocation", geoLocation)
        val requestURL = "$baseURL$geoLocation"
        val gson = Gson()

        viewModelScope.launch {
            try {
                val response =
                    Fuel.get(requestURL).header("User-Agent", "FLyNerd gjchocopasta@gmail.com")
                        .awaitString()
                Log.d("output", response)
                instance.weatherForecast = gson.fromJson(response, Forecast::class.java)
                forecastLiveData.postValue(instance.weatherForecast)
            } catch (exception: FuelError) {
                Log.d("Fuel", "[ERROR] could not fetch data! $exception")
            }
        }
    }
}