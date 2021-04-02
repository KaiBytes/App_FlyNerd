package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.uio.in2000.team16.flynerd.api.FlightStatusRepository
import okhttp3.HttpUrl
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://api.flightstats.com/flex/flightstatus/rest/"
    private val appId = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val appKey = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // input the flight number as string
        val flightNumberStr = "AA100"
        val date = LocalDate.now().plusDays(0)

        // checking if the flight number is valid and use the flight number, else exit the system if
        // flight number is not valid
        val flightId = FlightId.parse(flightNumberStr)
        if (flightId == null) {
            Log.i(TAG, "The flight number is invalid")
            return
        }

        val repository = FlightStatusRepository(baseUrl, appId, appKey)

        //using couroutinScope & feul make http requsting and get data from flight API provider as json format
        runBlocking(Dispatchers.IO) {
            try {
                val flights = repository.byFlightIdArrivingOn(flightId, date)
                Log.i(TAG, "$flights")
            } catch (e: Exception) {
                Log.i(TAG, "$e")
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
