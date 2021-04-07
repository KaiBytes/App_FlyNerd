package no.uio.in2000.team16.flynerd

import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * A flight identified by a flight id.
 *
 * Has a departure and an arrival airport, as well as zero or more intervening airports it lands at
 * and departs from on its route.
 */
internal class Flight(
    /**
     * IATA flight id.
     */
    val flightIdIATA: FlightIdIATA,

    /**
     * ICAO flight id, if available.
     */
    val flightIdICAO: FlightIdICAO?,

    /**
     * The operating carrier of the flight.
     */
    val airline: FlightAirline,

    /**
     * Information about the initial departure.
     */
    val departure: FlightJunctureDeparture,

    /**
     * Information about any intervening landings on the flight's route.
     */
    val mids: Array<FlightJunctureMid>,

    /**
     * Information about the final arrival.
     */
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
    /**
     * Place of arrival and/or departure.
     */
    val airport: FlightAirport
}

internal interface FlightJunctureDeparture : FlightJuncture {
    /**
     * Current status of departure.
     */
    val departureStatus: FlightStatus

    /**
     * Time information for departure.
     */
    val departureTimes: FlightJunctureTimes
}

internal interface FlightJunctureArrival : FlightJuncture {
    /**
     * Current status of arrival.
     */
    val arrivalStatus: FlightStatus

    /**
     * Time information for arrival.
     */
    val arrivalTimes: FlightJunctureTimes
}

internal interface FlightJunctureMid : FlightJunctureArrival, FlightJunctureDeparture

/**
 * Scheduled and operational times for an arrival at or departure from the gate.
 *
 * Note that any and all times may be null if they are unavailable.
 */
internal class FlightJunctureTimes(
    /**
     * Scheduled time.
     */
    val scheduled: FlightTime?,

    /**
     * Estimated time, based on current observations.
     */
    val estimated: FlightTime?,

    /**
     * Actual time, as observed.
     */
    val actual: FlightTime?,

    /**
     * Delay, as calculated based on the times.
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
     * Timestamp without timezone, local to the place of arrival or departure in question.
     */
    val local: LocalDateTime,

    /**
     * Timestamp with UTC timezone.
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
    /**
     * IATA code identifying the airline.
     */
    val iata: String,

    /**
     * ICAO code identifying the airline, if available.
     */
    val icao: String?,

    /**
     * Name of the airline.
     */
    val name: String,
) {
    override fun toString(): String {
        return "FlightAirline(iata='$iata', icao='$icao', name='$name')"
    }
}

/**
 * An airport some arrival or departure takes place at.
 */
internal class FlightAirport(
    /**
     * The IATA code identifying the airport.
     */
    val iata: String,

    /**
     * The name of the airport, if available.
     */
    val name: String?,

    /**
     * The city the airport is associated with.
     */
    val city: String,

    /**
     * Code for the country the airport is in.
     *
     * Standardized two-letter code.
     */
    val country: String,
) {
    override fun toString(): String {
        return "FlightAirport(iata='$iata', name=$name, city='$city', country='$country')"
    }
}

/**
 * Status of flight segment connected to an arrival or departure.
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
     * Short natural language description of the status.
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
 * Consists of an IATA or ICAO airline code and a number.
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
            val normalized = normalize(string)
            val (init, flightNumberStart) = normalized.getOrNull(2)?.let {
                if (it.isDigit()) {
                    ::FlightIdIATA to 2
                } else {
                    ::FlightIdICAO to 3
                }
            } ?: throw IllegalArgumentException("flight id is too short: '$string'")
            val airlineCode = normalized.substring(0 until flightNumberStart)
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
