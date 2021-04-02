package no.uio.in2000.team16.flynerd

import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal class Flight(
    val flightIdIATA: FlightIdIATA,
    val flightIdICAO: FlightIdICAO,
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
    val airportIATA: String,
    val published: FlightTime,
    val estimated: FlightTime?,
    val actual: FlightTime?,
    val delay: Duration?,
) {
    override fun toString(): String {
        return "FlightJuncture(" +
                "airportIATA='$airportIATA', " +
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
    override fun toString(): String {
        return "$airlineCode$flightNumber"
    }

    companion object {
        fun parse(string: String) = FlightIdIATA.parse(string) ?: FlightIdICAO.parse(string)
    }
}

internal class FlightIdIATA(airlineIATA: String, flightNumber: Int) :
    FlightId(airlineIATA, flightNumber)

internal class FlightIdICAO(airlineICAO: String, flightNumber: Int) :
    FlightId(airlineICAO, flightNumber)