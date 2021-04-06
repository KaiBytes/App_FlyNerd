package no.uio.in2000.team16.flynerd

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.uio.in2000.team16.flynerd.api.FlightStatusRepository
import no.uio.in2000.team16.flynerd.util.CalendarUtil
import okhttp3.HttpUrl
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class DelayActivity : AppCompatActivity() {
    private val baseUrl = HttpUrl.get("https://api.flightstats.com/flex/flightstatus/rest/")
    private val appId = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val appKey = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT

    private var _flightDate: LocalDate = LocalDate.ofEpochDay(0)
        set(value) {
            field = value
            findViewById<Button>(R.id.flight_date).text =
                value.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            Log.i(TAG, "flight date set: $value")
        }

    internal var flightDate: LocalDate
        get() = _flightDate
        set(value) {
            _flightDate = value
            onFlightDateSet()
        }

    private lateinit var flightsAdapter: FlightsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delay)


        flightsAdapter = FlightsAdapter()
        findViewById<RecyclerView>(R.id.flights).run {
            adapter = flightsAdapter
            layoutManager = LinearLayoutManager(this@DelayActivity)
        }

        _flightDate = LocalDate.now()

        findViewById<EditText>(R.id.flight_id).setOnEditorActionListener { view, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    flightsFetch()
                    view.context.getSystemService<InputMethodManager>()
                        ?.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            false
        }

        findViewById<Button>(R.id.flight_date).setOnClickListener { onFlightDate() }
    }

    private fun onFlightDate() {
        FlightDatePickerFragment().show(supportFragmentManager, "flightDatePicker")
    }

    private fun onFlightDateSet() {
        if (findViewById<EditText>(R.id.flight_id).text.isNotEmpty()) {
            flightsFetch()
        }
    }

    private fun flightsFetch() {
        // input the flight number as string
        val flightNumberStr = findViewById<EditText>(R.id.flight_id).text
        val date = flightDate

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
            Log.i(TAG, "fetched ${flights.size} flights")
            withContext(Dispatchers.Main) {
                updateSummary(flights)
                flightsAdapter.flights = flights
            }
        }
    }

    private fun updateSummary(flights: List<Flight>) {
        val flight = flights.firstOrNull()
        if (flight != null) {
            findViewById<TextView>(R.id.flights_summary_id_iata).text =
                flight.flightIdIATA.toString()
            findViewById<TextView>(R.id.flights_summary_id_icao).text =
                flight.flightIdICAO.toString()
            findViewById<TextView>(R.id.flights_summary_airline_name).text = flight.airline.name
        } else {
            findViewById<TextView>(R.id.flights_summary_airline_name).text = "no flights found"
        }
    }

    companion object {
        const val TAG = "DelayActivity"
    }
}

internal class FlightDatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = requireActivity() as DelayActivity
        return CalendarUtil.of(activity.flightDate).let { calendar ->
            DatePickerDialog(
                requireActivity(),
                this,
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
            )
        }.apply {
            datePicker.minDate = Calendar.getInstance()
                .apply { add(Calendar.DAY_OF_MONTH, -DAYS_BEFORE) }
                .timeInMillis
            datePicker.maxDate = Calendar.getInstance()
                .apply { add(Calendar.DAY_OF_MONTH, +DAYS_AFTER) }
                .timeInMillis
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val activity = requireActivity() as DelayActivity
        activity.flightDate = LocalDate.of(year, month + 1, dayOfMonth)
    }

    companion object {
        private const val DAYS_BEFORE = 7
        private const val DAYS_AFTER = 3
    }
}
