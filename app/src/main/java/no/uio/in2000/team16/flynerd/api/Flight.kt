package no.uio.in2000.team16.flynerd

import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * A flight identified by a flight id.
 *
 * Has a departure and an arrival juncture, as well as zero or more intervening junctures it both
 * lands at and departs from, on its route.
 */
internal class Flight(
    val flightIdIATA: FlightIdIATA,
    val flightIdICAO: FlightIdICAO?,
    val airline: FlightAirline,
    val departure: FlightJunctureDeparture,
    val mids: Array<FlightJunctureMid>,
    val arrival: FlightJunctureArrival,
) {
    override fun toString(): String {
        return "Flight(" +
                "flightIdIATA=$flightIdIATA, " +
                "flightIdICAO=$flightIdICAO, " +
                "airline=$airline, " +
                "departure=$departure, " +
                "mids=${mids.contentToString()}, " +
                "arrival=$arrival" +
                ")"
    }
}

/**
 * An arrival and/or departure, and associated information in relation to some flight.
 */
internal interface FlightJuncture {
    val airport: FlightAirport
}

/**
 * A departure, and associated information.
 */
internal interface FlightJunctureDeparture : FlightJuncture {
    val departureStatus: FlightStatus
    val departureTimes: FlightJunctureTimes
}

/**
 * An arrival, and associated information.
 */
internal interface FlightJunctureArrival : FlightJuncture {
    val arrivalStatus: FlightStatus
    val arrivalTimes: FlightJunctureTimes
}

/**
 * An arrival *and* departure, and associated sets of information.
 */
internal interface FlightJunctureMid : FlightJunctureArrival, FlightJunctureDeparture

/**
 * Scheduled and operational times for an arrival at or departure from the gate.
 *
 * Note that any and all times may be null if they are unavailable.
 */
internal class FlightJunctureTimes(
    val scheduled: FlightTime?,
    /**
     * Based on current observations.
     */
    val estimated: FlightTime?,
    /**
     * As observed.
     */
    val actual: FlightTime?,
    /**
     * As calculated based on the times.
     *
     * Rounded to one minute granularity.
     */
    val delay: Duration?,
) {
    override fun toString(): String {
        return "FlightJunctureTimes(" +
                "scheduled=$scheduled, " +
                "estimated=$estimated, " +
                "actual=$actual, " +
                "delay=$delay" +
                ")"
    }
}

/**
 * A single point in time represented as both local and UTC.
 */
internal class FlightTime(
    /**
     * Local to the place of arrival or departure in question.
     */
    val local: LocalDateTime,
    /**
     * With UTC timezone.
     */
    val utc: ZonedDateTime,
) {
    override fun toString(): String {
        return "FlightTime(local=$local, utc=$utc)"
    }
}

/**
 * An airline operating some flight.
 */
internal class FlightAirline(
    val iata: String,
    val icao: String?,
    val name: String,
) {
    override fun toString(): String {
        return "FlightAirline(iata='$iata', icao='$icao', name='$name')"
    }
}

/**
 * An airport some arrival and/or departure takes place at.
 */
internal class FlightAirport(
    val iata: String,
    val name: String?,
    val city: String,
    /**
     * Standardized two-letter code.
     */
    val country: String,
) {
    override fun toString(): String {
        return "FlightAirport(iata='$iata', name=$name, city='$city', country='$country')"
    }
}

/**
 * Status of flight segment to an arrival or from a departure.
 */
internal enum class FlightStatus {
    @SerializedName("A")
    ACTIVE,

    @SerializedName("C")
    CANCELLED,

    @SerializedName("D")
    DIVERTED,

    @SerializedName("DN")
    DATA_SOURCE_NEEDED,

    @SerializedName("L")
    LANDED,

    @SerializedName("NO")
    NOT_OPERATIONAL,

    @SerializedName("R")
    REDIRECTED,

    @SerializedName("S")
    SCHEDULED,

    @SerializedName("U")
    UNKNOWN;

    /**
     * Short natural language description of the status, for use in UI.
     */
    val description: String
        get() = when (this) {
            ACTIVE -> "in air"
            CANCELLED -> "canceled"
            DIVERTED -> "diverted"
            DATA_SOURCE_NEEDED -> "(data source needed)"
            LANDED -> "landed"
            NOT_OPERATIONAL -> "not operational"
            REDIRECTED -> "redirected"
            SCHEDULED -> "scheduled"
            UNKNOWN -> "(unknown)"
        }
}

/**
 * Identifier for flights.
 *
 * Consists of an IATA or ICAO airline code, and a number.
 */
internal abstract class FlightId(val airlineCode: String, val flightNumber: Int) {
    init {
        if (flightNumber !in 1..9999) {
            throw IllegalArgumentException("invalid flight id number: $flightNumber")
        }
    }

    override fun toString(): String {
        return "$airlineCode$flightNumber"
    }

    companion object {
        /**
         * Normalize and parse input string as a flight id.
         *
         * @return A subclass of [FlightId] whose format the input string conforms to.
         *
         * @throws IllegalArgumentException (With suitable error message.)  The input string is not
         *   a valid flight id .
         */
        fun parse(string: CharSequence): FlightId {
            // First, normalize (all uppercase, no whitespace, etc.).
            val normalized = normalize(string)
            // Decide whether it is IATA or ICAO.
            val (init, flightNumberStart) = normalized.getOrNull(2)?.let {
                if (it.isDigit()) {
                    ::FlightIdIATA to 2
                } else {
                    ::FlightIdICAO to 3
                }
            } ?: throw IllegalArgumentException("flight id is too short: '$string'")
            // Code part.
            val airlineCode = normalized.substring(0 until flightNumberStart)
            // Number part.
            val flightNumberStr = normalized.substring(flightNumberStart)
            val flightNumber = try {
                flightNumberStr.toInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("invalid flight id number: '$flightNumberStr'")
            }
            return init(airlineCode, flightNumber)
        }

        private fun normalize(string: CharSequence): CharSequence {
            val normalized = string.asSequence()
                // Remove any whitespace.
                .filterNot(Char::isWhitespace)
                // Convert all to uppercase.
                .map(Char::toUpperCase)
                .toList()
                .toCharArray()
                .let(::String)
            // Remove any operational suffix.
            return if (normalized.lastOrNull()?.isLetter() == true) {
                normalized.dropLast(1)
            } else {
                normalized
            }
        }
    }
}

internal class FlightIdIATA(airlineIATA: String, flightNumber: Int) :
    FlightId(airlineIATA, flightNumber) {
    init {
        if (airlineIATA.length != 2 || !airlineIATA.all(Char::isLetterOrDigit)) {
            throw IllegalArgumentException("invalid flight id IATA code: '$airlineIATA'")
        }
    }
}

internal class FlightIdICAO(airlineICAO: String, flightNumber: Int) :
    FlightId(airlineICAO, flightNumber) {
    init {
        if (airlineICAO.length != 3 || !airlineICAO.all(Char::isLetter)) {
            throw IllegalArgumentException("invalid flight id ICAO code: '$airlineICAO'")
        }
    }
}
