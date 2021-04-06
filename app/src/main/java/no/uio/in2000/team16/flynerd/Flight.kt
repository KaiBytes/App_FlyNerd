package no.uio.in2000.team16.flynerd

import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal class Flight(
    val flightIdIATA: FlightIdIATA,
    val flightIdICAO: FlightIdICAO,
    val airline: Airline,
    val status: FlightStatus,
    val departure: FlightJuncture,
    val arrival: FlightJuncture,
) {
    override fun toString(): String {
        return "Flight(" +
                "flightIdIATA=$flightIdIATA, " +
                "flightIdICAO=$flightIdICAO, " +
                "status=$status, " +
                "departure=$departure, " +
                "arrival=$arrival" +
                ")"
    }
}

internal class FlightJuncture(
    val airport: Airport,
    val published: FlightTime,
    val estimated: FlightTime?,
    val actual: FlightTime?,
    val delay: Duration?,
) {
    override fun toString(): String {
        return "FlightJuncture(" +
                "airport='$airport', " +
                "published=$published, " +
                "estimated=$estimated, " +
                "actual=$actual, " +
                "delay=$delay" +
                ")"
    }
}

internal class FlightTime(val local: LocalDateTime, val utc: ZonedDateTime) {
    override fun toString(): String {
        return "FlightTime(local=$local, utc=$utc)"
    }
}

internal class Airline(val iata: String, val icao: String, val name: String) {
    override fun toString(): String {
        return "Airline(iata='$iata', icao='$icao', name='$name')"
    }
}

internal class Airport(val iata: String, val name: String?, val city: String, val country: String) {
    override fun toString(): String {
        return "Airport(iata='$iata', name=$name, city='$city', country='$country')"
    }
}

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
    UNKNOWN,
}

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
