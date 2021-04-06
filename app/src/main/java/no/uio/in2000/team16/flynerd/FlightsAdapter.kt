package no.uio.in2000.team16.flynerd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

internal class FlightsAdapter() :
    RecyclerView.Adapter<FlightsAdapter.ViewHolder>() {

    private val _flights: MutableList<Flight> = mutableListOf()

    internal var flights: List<Flight>
        get() = _flights
        set(value) = _flights.run {
            clear()
            addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.flight_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flight = flights[position]
        holder.run {
            departure.bind(flight.departure)
            arrival.bind(flight.arrival)
            status.text = flight.status.description
        }
    }

    override fun getItemCount(): Int = flights.size

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val departure = view.findViewById<ConstraintLayout>(R.id.flight_item_departure)
            .let(::Juncture)
        val arrival = view.findViewById<ConstraintLayout>(R.id.flight_item_arrival)
            .let(::Juncture)
        val status = view.findViewById<TextView>(R.id.flight_item_status)

        class Juncture(view: View) {
            val iata = view.findViewById<TextView>(R.id.flight_juncture_iata)
            val name = view.findViewById<TextView>(R.id.flight_juncture_name)
            val city = view.findViewById<TextView>(R.id.flight_juncture_city)
            val country = view.findViewById<TextView>(R.id.flight_juncture_country)
            val expectedLocal = view.findViewById<TextView>(R.id.flight_juncture_expected_local)
            val expectedUtc = view.findViewById<TextView>(R.id.flight_juncture_expected_utc)
            val actualLabel = view.findViewById<TextView>(R.id.flight_juncture_actual_label)
            val actualLocal = view.findViewById<TextView>(R.id.flight_juncture_actual_local)
            val actualUtc = view.findViewById<TextView>(R.id.flight_juncture_actual_utc)
            val delay = view.findViewById<TextView>(R.id.flight_juncture_delay)

            fun bind(juncture: FlightJuncture) {
                val expected = juncture.published
                val actual = juncture.actual ?: juncture.estimated

                iata.text = juncture.airport.iata
                name.text = juncture.airport.name
                city.text = "${juncture.airport.city}, "
                country.text = juncture.airport.country
                expectedLocal.text = "${formatTime(expected.local)} "
                expectedUtc.text = "(${formatTime(expected.utc)} UTC)"
                actualLabel.text = juncture.actual?.let { "actual: " } ?: "estimated: "
                actualLocal.text = actual?.let { "${formatTime(it.local)} " } ?: "unknown"
                actualUtc.text = actual?.let { "(${formatTime(it.utc)} UTC)" } ?: ""
                delay.text = juncture.delay?.let { "${it.toMinutes()} min" } ?: "on time"
            }

            private fun formatTime(temporal: TemporalAccessor): String =
                DateTimeFormatter.ofPattern("HH:mm").format(temporal)
        }
    }
}