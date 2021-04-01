package no.uio.in2000.team16.flynerd.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal interface FlightstatusService {
    @GET("v2/json/flight/status/{carrier}/{flight}/arr/{year}/{month}/{day}")
    suspend fun flightStatusArrivingOn(
        @Path("carrier") airlineCode: String,
        @Path("flight") flightNumber: Int,
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int,
        @Query("airport") airport: String? = null,
    ): FlightStatusResponse
}

internal class FlightStatusResponse(val flightStatuses: Array<FlightStatus>?, val error: Error?) {
    override fun toString(): String {
        return "FlightStatusResponse(flightStatuses=${flightStatuses?.contentToString()}, error=$error)"
    }
}

internal class FlightStatus(
    val flightId: String,
    val carrierFsCode: String,
    val flightNumber: String,
    val departureAirportFsCode: String,
    val arrivalAirportFsCode: String,
    // NOTE: Chek for null when using.  This must acutally never be null.  We only allow it to be
    // nullable because Gson sets it to null on an unrecognized value instead of throwing an error.
    val status: Status?,
    val schedule: Schedule?,
    val operationalTimes: OperationalTimes,
    val delays: Delays?,
) {
    override fun toString() =
        "FlightStatus(flightId='$flightId', carrierFsCode='$carrierFsCode', " +
                "flightNumber='$flightNumber', departureAirporFsCode='$departureAirportFsCode', " +
                "arrivalAirportFsCode='$arrivalAirportFsCode', status=$status, " +
                "schedule=$schedule, operationalTimes=$operationalTimes, delays=$delays)"
}

internal enum class Status {
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

internal class Schedule(val uplines: Array<RouteFlight>?, val downlines: Array<RouteFlight?>) {
    override fun toString() =
        "Schedule(uplines=${uplines?.contentToString()}, downlines=${downlines.contentToString()})"
}

internal class OperationalTimes(
    val publishedDeparture: Times?,
    val publishedArrival: Times?,
    val estimatedGateDeparture: Times?,
    val actualGateDeparture: Times?,
    val estimatedGateArrival: Times?,
    val actualGateArrival: Times?,
) {
    override fun toString() =
        "OperationalTimes(publishedDeparture=$publishedDeparture, " +
                "publishedArrival=$publishedArrival, " +
                "estimatedGateDeparture=$estimatedGateDeparture, " +
                "actualGateDeparture=$actualGateDeparture, " +
                "estimatedGateArrival=$estimatedGateArrival, " +
                "actualGateArrival=$actualGateArrival)"
}

internal class Delays(
    val departureGateDelayMinutes: Int?,
    val arrivalGateDelayMinutes: Int?,
) {
    override fun toString() =
        "Delays(departureGateDelayMinutes=$departureGateDelayMinutes, " +
                "arrivalGateDelayMinutes=$arrivalGateDelayMinutes)"
}

internal class RouteFlight(val fsCode: String, val flightId: String?) {
    override fun toString() = "RouteFlight(fsCode='$fsCode', flightId=$flightId)"
}

internal class Times(val dateLocal: LocalDateTime?, val dateUtc: ZonedDateTime?) {
    override fun toString() = "Times(dateLocal=$dateLocal, dateUtc=$dateUtc)"
}

internal class Error(
    val httpStatusCode: Int,
    val errorId: String,
    val errorMessage: String,
    val errorCode: String,
) {
    override fun toString() =
        "Error(httpStatusCode=$httpStatusCode, errorId='$errorId', errorMessage='$errorMessage', " +
                "errorCode='$errorCode')"
}
