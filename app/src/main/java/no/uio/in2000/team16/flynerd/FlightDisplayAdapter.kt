package no.uio.in2000.team16.flynerd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAmount

internal class FlightDisplayAdapter() :
    RecyclerView.Adapter<FlightDisplayAdapter.ViewHolder>() {
    internal var flight: Flight? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int =
        if (position % 2 == 0) {
            ViewType.JUNCTURE
        } else {
            ViewType.PROGRESS
        }.ordinal

    override fun getItemCount(): Int = flight?.let { (it.mids.size + 2) * 2 - 1 } ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewTypeInt: Int): ViewHolder {
        val (init, resource) = when (ViewType.values()[viewTypeInt]) {
            ViewType.JUNCTURE -> ::JunctureHolder to R.layout.flight_display_juncture
            ViewType.PROGRESS -> ::ProgressHolder to R.layout.flight_display_progress
        }
        return init(LayoutInflater.from(parent.context).inflate(resource, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val flight = this.flight!!
        when (val index = position / 2) {
            0 -> holder.bind(flight.departure, flight.mids.firstOrNull() ?: flight.arrival)
            flight.mids.size + 1 -> holder.bind(flight.arrival)
            else -> holder.bind(
                flight.mids[index - 1],
                flight.mids.getOrNull(index) ?: flight.arrival
            )
        }
    }

    internal abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(junctureDeparture: FlightJunctureDeparture, next: FlightJunctureArrival)
        abstract fun bind(junctureArrival: FlightJunctureArrival)
        abstract fun bind(junctureMid: FlightJunctureMid, next: FlightJunctureArrival)
    }

    internal class JunctureHolder(view: View) : ViewHolder(view) {
        private val airportIATA: TextView = view.findViewById(R.id.flight_display_airport_iata)
        private val airportName: TextView = view.findViewById(R.id.flight_display_airport_name)
        private val airportPlace: TextView = view.findViewById(R.id.flight_display_airport_place)
        private val endpointArrival: ImageView =
            view.findViewById(R.id.flight_display_endpoint_arrival)
        private val endpointDeparture: ImageView =
            view.findViewById(R.id.flight_display_endpoint_departure)

        private val timesArrival =
            Times(view.findViewById<ConstraintLayout>(R.id.flight_display_times_arrival))
        private val timesDeparture =
            Times(view.findViewById<ConstraintLayout>(R.id.flight_display_times_departure))

        override fun bind(junctureDeparture: FlightJunctureDeparture, next: FlightJunctureArrival) {
            bindAirport(junctureDeparture.airport)
            bindDeparture(junctureDeparture.departureTimes)
        }

        override fun bind(junctureArrival: FlightJunctureArrival) {
            bindAirport(junctureArrival.airport)
            bindArrival(junctureArrival.arrivalTimes)
        }

        override fun bind(junctureMid: FlightJunctureMid, next: FlightJunctureArrival) {
            bindAirport(junctureMid.airport)
            bindDeparture(junctureMid.departureTimes)
            bindArrival(junctureMid.arrivalTimes)
        }

        private fun bindAirport(airport: FlightAirport) {
            endpointArrival.visibility = View.GONE
            timesArrival.view.visibility = View.GONE
            endpointDeparture.visibility = View.GONE
            timesDeparture.view.visibility = View.GONE

            airportIATA.text = airport.iata
            airportName.text = airport.name ?: "(unknown)"
            airportPlace.text = "${airport.city}, ${airport.country}"
        }

        private fun bindDeparture(departureTimes: FlightJunctureTimes) {
            endpointDeparture.visibility = View.VISIBLE
            timesDeparture.run {
                view.visibility = View.VISIBLE
                bind(departureTimes)
            }
        }

        private fun bindArrival(arrivalTimes: FlightJunctureTimes) {
            endpointArrival.visibility = View.VISIBLE
            timesArrival.run {
                view.visibility = View.VISIBLE
                bind(arrivalTimes)
            }
        }

        private class Times(val view: View) {
            val actualLabel: TextView = view.findViewById(R.id.flight_display_juncture_actual_label)
            val scheduledLocal: TextView =
                view.findViewById(R.id.flight_display_juncture_scheduled_local)
            val scheduledUtc: TextView =
                view.findViewById(R.id.flight_display_juncture_scheduled_utc)
            val actualLocal: TextView = view.findViewById(R.id.flight_display_juncture_actual_local)
            val actualUtc: TextView = view.findViewById(R.id.flight_display_juncture_actual_utc)
            val delay: TextView = view.findViewById(R.id.flight_display_juncture_delay)

            fun bind(times: FlightJunctureTimes) {
                actualLabel.text = times.actual?.let { "actual" } ?: "estimated"
                scheduledLocal.text = times.scheduled?.local?.let(this::formatTime)
                    ?.let { "$it " }
                    ?: "(unknown)"
                scheduledUtc.text = times.scheduled?.utc?.let(this::formatTime)
                    ?.let { "($it UTC)" }
                    ?: ""
                actualLocal.text = (times.actual ?: times.estimated)?.local?.let(this::formatTime)
                    ?.let { "$it " }
                    ?: "(unknown)"
                actualUtc.text = (times.actual ?: times.estimated)?.utc?.let(this::formatTime)
                    ?.let { "($it UTC)" }
                    ?: ""
                delay.text = times.delay?.toMinutes()?.let { "$it min" } ?: "on time"
            }

            private fun formatTime(time: TemporalAccessor) =
                DateTimeFormatter.ofPattern("HH:mm").format(time)
        }
    }

    internal class ProgressHolder(view: View) : ViewHolder(view) {
        private val status: TextView = view.findViewById(R.id.flight_display_status)
        private val remainingLabel: TextView =
            view.findViewById(R.id.flight_display_remaining_label)
        private val remaining: TextView = view.findViewById(R.id.flight_display_remaining)

        override fun bind(junctureDeparture: FlightJunctureDeparture, next: FlightJunctureArrival) {
            status.text = junctureDeparture.departureStatus.description
            remainingLabel.text = ""
            remaining.text = ""
            when (junctureDeparture.departureStatus) {
                FlightStatus.ACTIVE, FlightStatus.DIVERTED, FlightStatus.REDIRECTED -> {
                    next.arrivalTimes.run { actual ?: estimated ?: scheduled }?.let { arrival ->
                        val label = next.arrivalTimes.run {
                            actual?.let { "actual" } ?: estimated?.let { "estimated" }
                            ?: "scheduled"
                        }
                        val duration = Duration.between(Instant.now(), arrival.utc.toInstant())
                        remainingLabel.text = "$label remaining"
                        remaining.text = formatDuration(duration)
                    }
                }
            }
        }

        override fun bind(junctureArrival: FlightJunctureArrival) {
            throw IllegalStateException("trying to bind progress view holder to arrival juncture")
        }

        override fun bind(junctureMid: FlightJunctureMid, next: FlightJunctureArrival) {
            bind(junctureMid as FlightJunctureDeparture, next)
        }

        private fun formatDuration(duration: TemporalAmount): String {
            val seconds = duration[ChronoUnit.SECONDS]
            val minutes = seconds / 60
            val mm = minutes % 60
            val hh = minutes / 60
            return "%02d:%02d".format(hh, mm)
        }
    }

    private enum class ViewType {
        JUNCTURE,
        PROGRESS,
    }
}