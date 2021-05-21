package no.uio.in2000.team16.flynerd.api

import com.google.gson.GsonBuilder
import no.uio.in2000.team16.flynerd.util.registerLocalDateTime
import no.uio.in2000.team16.flynerd.util.registerZonedDateTime
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.util.*

/**
 * Repository frontend for flight status API.
 *
 * @param baseUrl API base URL, up to but not including the version, with trailing slash.  E.g.
 *   <code>"https://api.flightstats.com/flex/flightstatus/rest/"</code>.
 * @param appId Application ID as provided by flightstats.  E.g. <code>"0a1b2c3d"</code>.
 * @param appKey Application key as provided by flightstats.  E.g.
 *   <code>"0a1b2c3d4f5a6b7c8d9e0f1a2b3c4d5e"</code>.
 */
internal class FlightStatusRepository(baseUrl: HttpUrl, appId: String, appKey: String) {
    private val gson = GsonBuilder()
        .registerLocalDateTime()
        .registerZonedDateTime()
        .create()

    private val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val url = chain.request()
                        .url()
                        .newBuilder()
                        .addQueryParameter("appId", appId)
                        .addQueryParameter("appKey", appKey)
                        .build()
                    chain.proceed(chain.request().newBuilder().url(url).build())
                }
                .build()
        )
        .build()
        .create<FlightStatusService>()

    /**
     * Fetch flight with given flight id arriving on given date.
     *
     * @param flightId Identifier of flight.
     * @param date Arrival date, local to the arrival airport.
     * @param airportIATA IATA code of arrival airport.  If null, all arrival airports are accepted.
     *
     * @return The flight matching the criteria.  Null if no such flight is found.
     *
     * @throws IOException An I/O error occurred talking to the API server.
     * @throws FlightStatusException (Appropriate subclass.) A non-I/O error occured.
     * @throws RuntimeException Some other kind of error occured decoding the API server's response.
     */
    suspend fun byFlightIdArrivingOn(
        flightId: FlightId,
        date: LocalDate,
        airportIATA: String? = null,
    ): Flight? {
        val response = service.byFlightNumberArrivingOn(
            flightId.airlineCode,
            flightId.flightNumber,
            date.year,
            date.month.value,
            date.dayOfMonth,
            airportIATA,
        )
        response.error?.let {
            throw it.toException()
        }
        if (response.flightStatuses == null || response.appendix == null) {
            throw FlightStatusIllegalException("neither error nor OK response elements present")
        }
        return response.flightStatuses.toFlight(response.appendix)
    }
}

/**
 * Non-I/O error concerning the API response.
 */
internal abstract class FlightStatusException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

/**
 * The API server's response does not have all the information needed for the returned
 * datastructures.
 *
 * This is not a serious error; it may be handled identically to the null returned case.
 */
internal class FlightStatusInsufficientException(val msg: String) : FlightStatusException() {
    override val message
        get() = "missing data: $msg"
}

/**
 * The API server's response's format was illegal.
 */
internal class FlightStatusIllegalException(
    val msg: String,
    cause: Throwable? = null,
) : FlightStatusException(cause = cause) {
    constructor(cause: Throwable) : this(cause.localizedMessage ?: cause.toString(), cause)

    override val message
        get() = "illegal API response: $msg"
}

/**
 * The API server responded with an error.
 *
 * Contains the details of the response error.
 */
internal class FlightStatusErrorException(
    val statusCode: Int,
    val errorCode: String,
    val errorMessage: String,
    val id: UUID?,
    cause: Throwable? = null,
) : FlightStatusException(cause = cause) {
    override val message
        get() = "API error $errorCode ($statusCode): $errorMessage"
}

/**
 * Concrete implementation of [FlightJunctureDeparture].
 */
private class FlightJunctureDepartureImpl(
    override val airport: FlightAirport,
    override val departureStatus: FlightStatus,
    override val departureTimes: FlightJunctureTimes,
) : FlightJunctureDeparture {
    override fun toString(): String {
        return "FlightJunctureDeparture(" +
                "airport=$airport, " +
                "departureStatus=$departureStatus, " +
                "departureTimes=$departureTimes" +
                ")"
    }
}

/**
 * Concrete implementation of [FlightJunctureArrival].
 */
private class FlightJunctureArrivalImpl(
    override val airport: FlightAirport,
    override val arrivalStatus: FlightStatus,
    override val arrivalTimes: FlightJunctureTimes,
) : FlightJunctureArrival {
    override fun toString(): String {
        return "FlightJunctureArrival(" +
                "airport=$airport, " +
                "arrivalStatus=$arrivalStatus, " +
                "arrivalTimes=$arrivalTimes" +
                ")"
    }
}

/**
 * Concrete implementation of [FlightJunctureMid].
 */
private class FlightJunctureMidImpl(
    override val airport: FlightAirport,
    override val departureStatus: FlightStatus,
    override val departureTimes: FlightJunctureTimes,
    override val arrivalStatus: FlightStatus,
    override val arrivalTimes: FlightJunctureTimes,
) : FlightJunctureMid {
    override fun toString(): String {
        return "FlightJunctureMid(" +
                "airport=$airport, " +
                "arrivalStatus=$arrivalStatus, " +
                "arrivalTimes=$arrivalTimes" +
                "departureStatus=$departureStatus, " +
                "departureTimes=$departureTimes, " +
                ")"
    }
}

/**
 * Convert service [FlightStatusFlightStatus] array to repository [Flight].
 */
private fun Array<FlightStatusFlightStatus>.toFlight(appendix: FlightStatusAppendix): Flight? {
    val sorted = this.sortedBy { it.operationalTimes.publishedDeparture?.dateUtc }.iterator()

    if (!sorted.hasNext()) {
        return null
    }
    val first = sorted.next()

    val airline = appendix.findAirline(first.carrierFsCode).toAirline()

    val departure = first.toJunctureDeparture(appendix)

    val flightNumberInt = first.flightNumberInt

    val flightIdIATA = try {
        FlightIdIATA(airline.iata, flightNumberInt)
    } catch (e: java.lang.IllegalArgumentException) {
        throw FlightStatusIllegalException(e)
    }

    val flightIdICAO = airline.icao?.let { airlineICAO ->
        try {
            FlightIdICAO(airlineICAO, flightNumberInt)
        } catch (e: java.lang.IllegalArgumentException) {
            throw FlightStatusIllegalException(e)
        }
    }

    var flightStatusPrev = first
    val mids = mutableListOf<FlightJunctureMid>()
    for (flightStatus in sorted) {
        val mid = toJunctureMid(flightStatusPrev, flightStatus, appendix)
        mids.add(mid)
        flightStatusPrev = flightStatus
    }

    val arrival = flightStatusPrev.toJunctureArrival(appendix)

    return Flight(flightIdIATA, flightIdICAO, airline, departure, mids.toTypedArray(), arrival)
}

/**
 * Retrieve flight number as integer from service [FlightStatusFlightStatus].
 */
private val FlightStatusFlightStatus.flightNumberInt
    get() = flightNumber.indexOfFirst { char ->
        !char.isDigit()
    }.let { index ->
        if (index >= 0) {
            index
        } else {
            flightNumber.length
        }
    }.let { index ->
        try {
            flightNumber.substring(0 until index).toInt()
        } catch (e: NumberFormatException) {
            throw FlightStatusIllegalException("invalid flight number: $flightNumber", cause = e)
        }
    }

/**
 * Extract the departure juncture of service [FlightStatusFlightStatus].
 */
private fun FlightStatusFlightStatus.toJunctureDeparture(appendix: FlightStatusAppendix):
        FlightJunctureDeparture {
    val airport = appendix.findAirport(departureAirportFsCode).toAirport()
    if (status == null) {
        throw FlightStatusIllegalException("no flight status")
    }
    val departureTimes = this.operationalTimes.toJunctureTimesDeparture(delays)
    return FlightJunctureDepartureImpl(airport, status, departureTimes)
}

/**
 * Extract the arrival juncture of service [FlightStatusFlightStatus].
 */
private fun FlightStatusFlightStatus.toJunctureArrival(appendix: FlightStatusAppendix):
        FlightJunctureArrival {
    val airport = appendix.findAirport(arrivalAirportFsCode).toAirport()
    if (status == null) {
        throw FlightStatusIllegalException("no flight status")
    }
    val arrivalTimes = this.operationalTimes.toJunctureTimesArrival(delays)
    return FlightJunctureArrivalImpl(airport, status, arrivalTimes)
}

/**
 * Extract the mid juncture between two service [FlightStatusFlightStatus] objects.
 */
private fun toJunctureMid(
    prev: FlightStatusFlightStatus,
    next: FlightStatusFlightStatus,
    appendix: FlightStatusAppendix
): FlightJunctureMid {
    val junctureArrival = prev.toJunctureArrival(appendix)
    val junctureDeparture = next.toJunctureDeparture(appendix)
    if (junctureDeparture.airport.iata != junctureArrival.airport.iata) {
        throw FlightStatusIllegalException(
            "mismatched flight juncture: arrival ${junctureArrival.airport.iata} != " +
                    "departure ${junctureDeparture.airport.iata}"
        )
    }
    return FlightJunctureMidImpl(
        junctureArrival.airport,
        junctureDeparture.departureStatus,
        junctureDeparture.departureTimes,
        junctureArrival.arrivalStatus,
        junctureArrival.arrivalTimes,
    )
}

/**
 * Convert service [FlightStatusAirline] to repository [FlightAirline].
 */
private fun FlightStatusAirline.toAirline(): FlightAirline {
    val iata = this.iata
        ?: throw FlightStatusInsufficientException("no IATA code in airline: $this")
    val icao = this.icao
    val name = this.name
    return FlightAirline(iata, icao, name)
}

/**
 * Extract departure times from service [FlightStatusOperationalTimes] and [FlightStatusDelays].
 */
private fun FlightStatusOperationalTimes.toJunctureTimesDeparture(
    delays: FlightStatusDelays?
) = toJunctureTimes(
    delays?.departureGateDelayMinutes,
    publishedDeparture,
    scheduledGateDeparture,
    estimatedGateDeparture,
    actualGateDeparture,
)

/**
 * Extract arrival times from service [FlightStatusOperationalTimes] and [FlightStatusDelays].
 */
private fun FlightStatusOperationalTimes.toJunctureTimesArrival(
    delays: FlightStatusDelays?
) = toJunctureTimes(
    delays?.arrivalGateDelayMinutes,
    publishedArrival,
    scheduledGateArrival,
    estimatedGateArrival,
    actualGateArrival,
)

/**
 * Construct [FlightJunctureTimes] from service [FlightStatusTimes] objects and delay.
 */
private fun toJunctureTimes(
    delayMinutes: Int?,
    publishedTimes: FlightStatusTimes?,
    scheduledTimes: FlightStatusTimes?,
    estimatedTimes: FlightStatusTimes?,
    actualTimes: FlightStatusTimes?,
): FlightJunctureTimes {
    val scheduled = (scheduledTimes ?: publishedTimes)?.toFlightTime()
    val estimated = estimatedTimes?.toFlightTime()
    val actual = actualTimes?.toFlightTime()
    val delay = delayMinutes?.let(Int::toLong)?.let(Duration::ofMinutes)
    return FlightJunctureTimes(scheduled, estimated, actual, delay)
}

/**
 * Convert service [FlightStatusAirport] to repository [FlightAirport].
 */
private fun FlightStatusAirport.toAirport(): FlightAirport {
    val iata = this.iata
        ?: throw FlightStatusInsufficientException("no IATA code in airport: $this")
    val name = this.name
    val city = this.city
    val country = this.countryCode
    return FlightAirport(iata, name, city, country)
}

/**
 * Convert service [FlightStatusTimes] to repository [FlightTime].
 */
private fun FlightStatusTimes.toFlightTime(): FlightTime {
    val local = dateLocal
        ?: throw FlightStatusInsufficientException("no local time in times: $this")
    val utc = dateUtc
        ?: throw FlightStatusInsufficientException("no UTC time in times: $this")
    return FlightTime(local, utc)
}

/**
 * Find airline in service appendix.
 */
private fun FlightStatusAppendix.findAirline(fsCode: String) =
    airlines.find { it.fs == fsCode }
        ?: throw FlightStatusIllegalException("airline not in appendix: fs code $fsCode")

/**
 * Find airport in service appendix.
 */
private fun FlightStatusAppendix.findAirport(fsCode: String) =
    airports.find { it.fs == fsCode }
        ?: throw FlightStatusIllegalException("airport not in appendix: fs code $fsCode")

/**
 * Convert service [FlightStatusError] to appropriate repository exception.
 */
private fun FlightStatusError.toException(): FlightStatusErrorException {
    val id = try {
        UUID.fromString(errorId)
    } catch (e: IllegalArgumentException) {
        null
    }
    return FlightStatusErrorException(httpStatusCode, errorCode, errorMessage, id)
}
