package no.uio.in2000.team16.flynerd.api

import no.uio.in2000.team16.flynerd.FlightStatus
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDateTime
import java.time.ZonedDateTime

internal interface FlightStatusService {
    @GET("v2/json/flight/status/{carrier}/{flight}/arr/{year}/{month}/{day}")
    suspend fun byFlightNumberArrivingOn(
        @Path("carrier") airlineCode: String,
        @Path("flight") flightNumber: Int,
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Path("day") day: Int,
        @Query("airport") airport: String?,
    ): FlightStatusResponse
}

internal class FlightStatusResponse(
    val flightStatuses: Array<FlightStatusFlightStatus>?,
    val appendix: FlightStatusAppendix?,
    val error: FlightStatusError?,
) {
    override fun toString(): String {
        return "FlightStatusResponse(" +
                "flightStatuses=${flightStatuses?.contentToString()}, " +
                "appendix=$appendix, " +
                "error=$error" +
                ")"
    }
}

internal class FlightStatusFlightStatus(
    val carrierFsCode: String,
    val flightNumber: String,
    val departureAirportFsCode: String,
    val arrivalAirportFsCode: String,
    // NOTE: Check for null when using.  This must acutally never be null.  We only allow it to be
    // nullable because Gson sets it to null on an unrecognized value instead of throwing an error.
    val status: FlightStatus?,
    val operationalTimes: FlightStatusOperationalTimes,
    val delays: FlightStatusDelays?,
) {
    override fun toString(): String {
        return "FlightStatusFlightStatus(" +
                "carrierFsCode='$carrierFsCode', " +
                "flightNumber='$flightNumber', " +
                "departureAirportFsCode='$departureAirportFsCode', " +
                "arrivalAirportFsCode='$arrivalAirportFsCode', " +
                "status=$status, " +
                "operationalTimes=$operationalTimes, " +
                "delays=$delays" +
                ")"
    }
}

internal class FlightStatusOperationalTimes(
    val publishedDeparture: FlightStatusTimes?,
    val scheduledGateDeparture: FlightStatusTimes?,
    val estimatedGateDeparture: FlightStatusTimes?,
    val actualGateDeparture: FlightStatusTimes?,
    val publishedArrival: FlightStatusTimes?,
    val scheduledGateArrival: FlightStatusTimes?,
    val estimatedGateArrival: FlightStatusTimes?,
    val actualGateArrival: FlightStatusTimes?,
) {
    override fun toString(): String {
        return "FlightStatusOperationalTimes(" +
                "publishedDeparture=$publishedDeparture, " +
                "scheduledGateDeparture=$scheduledGateDeparture, " +
                "estimatedGateDeparture=$estimatedGateDeparture, " +
                "actualGateDeparture=$actualGateDeparture, " +
                "publishedArrival=$publishedArrival, " +
                "scheduledGateArrival=$scheduledGateArrival, " +
                "estimatedGateArrival=$estimatedGateArrival, " +
                "actualGateArrival=$actualGateArrival" +
                ")"
    }
}

internal class FlightStatusDelays(
    val departureGateDelayMinutes: Int?,
    val arrivalGateDelayMinutes: Int?,
) {
    override fun toString(): String {
        return "FlightStatusDelays(" +
                "departureGateDelayMinutes=$departureGateDelayMinutes, " +
                "arrivalGateDelayMinutes=$arrivalGateDelayMinutes" +
                ")"
    }
}

internal class FlightStatusTimes(val dateLocal: LocalDateTime?, val dateUtc: ZonedDateTime?) {
    override fun toString(): String {
        return "FlightStatusTimes(dateLocal=$dateLocal, dateUtc=$dateUtc)"
    }
}

internal class FlightStatusAppendix(
    val airlines: Array<FlightStatusAirline>,
    val airports: Array<FlightStatusAirport>,
) {
    override fun toString(): String {
        return "FlightStatusAppendix(" +
                "airlines=${airlines.contentToString()}, " +
                "airports=${airports.contentToString()}" +
                ")"
    }
}

internal class FlightStatusAirline(
    val fs: String,
    val iata: String?,
    val icao: String?,
    val name: String,
) {
    override fun toString(): String {
        return "FlightStatusAirline(fs='$fs', iata=$iata, icao=$icao, name='$name')"
    }
}

internal class FlightStatusAirport(
    val fs: String,
    val iata: String?,
    val name: String?,
    val city: String,
    val countryCode: String,
) {
    override fun toString(): String {
        return "FlightStatusAirport(" +
                "fs='$fs', " +
                "iata=$iata, " +
                "name=$name, " +
                "city='$city', " +
                "countryCode='$countryCode'" +
                ")"
    }
}

internal class FlightStatusError(
    val httpStatusCode: Int,
    val errorId: String,
    val errorMessage: String,
    val errorCode: String,
) {
    override fun toString(): String {
        return "FlightStatusError(" +
                "httpStatusCode=$httpStatusCode, " +
                "errorId='$errorId', " +
                "errorMessage='$errorMessage', " +
                "errorCode='$errorCode'" +
                ")"
    }
}
