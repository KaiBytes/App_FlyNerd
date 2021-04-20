package no.uio.in2000.team16.flynerd

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private val baseUrl = HttpUrl.get(getString(R.string.flightstats_base_url))
    private val appId = getString(R.string.flightstats_app_id)
    private val appKey = getString(R.string.flightstats_app_key)

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

    private lateinit var flightDisplayAdapter: FlightDisplayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delay)

        flightDisplayAdapter = FlightDisplayAdapter()
        findViewById<RecyclerView>(R.id.flight_display).run {
            adapter = flightDisplayAdapter
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
            val flight: Flight? = try {
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
            withContext(Dispatchers.Main) {
                bindHeader(flight)
                flightDisplayAdapter.flight = flight
            }
            if (flight != null) {
                val airports = listOf(flight.departure.airport.iata) +
                        flight.mids.map { it.airport.iata } +
                        listOf(flight.arrival.airport.iata)
                Log.i(
                    TAG,
                    "fetched flight: ${flight.flightIdIATA}, ${airports.joinToString(" -> ")}"
                )
                Log.d(TAG, "flight: $flight")
            } else {
                Log.i(TAG, "fetched flight: $flight")

            }
        }
    }

    private fun bindHeader(flight: Flight?) {
        if (flight != null) {
            findViewById<TextView>(R.id.flight_header_id_iata).text = flight.flightIdIATA.toString()
            findViewById<TextView>(R.id.flight_header_id_sep).visibility =
                flight.flightIdICAO?.let { View.VISIBLE } ?: View.INVISIBLE
            findViewById<TextView>(R.id.flight_header_id_icao).text =
                flight.flightIdICAO?.toString() ?: ""
            findViewById<TextView>(R.id.flight_header_airline).text = flight.airline.name
            findViewById<TextView>(R.id.flight_header_no_flights).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.flight_header_id_iata).text = ""
            findViewById<TextView>(R.id.flight_header_id_sep).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.flight_header_id_icao).text = ""
            findViewById<TextView>(R.id.flight_header_airline).text = ""
            findViewById<TextView>(R.id.flight_header_no_flights).visibility = View.VISIBLE
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
