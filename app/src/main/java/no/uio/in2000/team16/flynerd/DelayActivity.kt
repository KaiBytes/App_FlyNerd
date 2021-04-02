package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.in2000.team16.flynerd.api.FlightStatusRepository
import okhttp3.HttpUrl
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DelayActivity : AppCompatActivity() {
    private val baseUrl = HttpUrl.get("https://api.flightstats.com/flex/flightstatus/rest/")
    private val appId = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val appKey = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delay)

        findViewById<Button>(R.id.flights_fetch).setOnClickListener(this::onFlightsFetch)
    }

    private fun onFlightsFetch(view: View) {
        // input the flight number as string
        val flightNumberStr = findViewById<EditText>(R.id.flight_id_input).text
        val date = LocalDate.now().plusDays(0)

        // check if the flight number is valid and use the flight number, else show toast
        val flightId = try {
            FlightId.parse(flightNumberStr)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.localizedMessage ?: "$e", Toast.LENGTH_LONG).show()
            return
        }

        val repository = FlightStatusRepository(baseUrl, appId, appKey)

        // get data from repository and print it to log, show toast on error
        CoroutineScope(Dispatchers.IO).launch {
            val flights: List<Flight> = try {
                repository.byFlightIdArrivingOn(flightId, date)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DelayActivity,
                        e.localizedMessage ?: "$e",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                return@launch
            }
            for (flight in flights) {
                val from = flight.departure.airportIATA
                val to = flight.arrival.airportIATA
                val status = flight.status
                val delay = flight.arrival.delay?.let { "${it.toMinutes()} min" } ?: "not delayed"
                val scheduledLocal = flight.arrival.published.local
                    .format(DateTimeFormatter.ISO_TIME)
                val scheduledUtc = flight.arrival.published.utc
                    .format(DateTimeFormatter.ISO_TIME)
                Log.i(
                    TAG,
                    "flight $from -> $to, status: $status, delay: $delay, " +
                            "scheduled at $scheduledLocal ($scheduledUtc)"
                )
            }
            if (flights.isEmpty()) {
                Log.i(TAG, "no flights")
            }
        }
    }

    companion object {
        const val TAG = "DelayActivity"
    }
}