package no.uio.in2000.team16.flynerd.api

import com.google.gson.GsonBuilder
import no.uio.in2000.team16.flynerd.*
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

    suspend fun byFlightIdArrivingOnResponse(
        flightId: FlightId,
        date: LocalDate,
        airportIATA: String? = null,
    ): FlightStatusResponse = service.byFlightNumberArrivingOn(
        flightId.airlineCode,
        flightId.flightNumber,
        date.year,
        date.month.value,
        date.dayOfMonth,
        airportIATA,
    )

    /**
     * Fetch flight with given flight id arriving on given date.
     *
     * @param flightId Identifier of flight.
     * @param date Arrival date, local to the arrival airport.
     * @param airportIATA IATA code of arrival airport.  If null, all arrival airports are accepted.
     *
     * @return The flight matching the criteria.  If no such flight is found, or the flight does not
     *   have all the information needed for the returned datastructures, then return null.
     *
     * @throws IOException An I/O error occurred talking to the API server.
     * @throws FlightStatusContentsException  The API server's response contents were invalid.
     * @throws FlightStatusErrorException The API server responded with a non-I/O error.
     * @throws RuntimeException Some other kind of error occured decoding the API server's response.
     */
    suspend fun byFlightIdArrivingOn(
        flightId: FlightId,
        date: LocalDate,
        airportIATA: String? = null,
    ): Flight? {
        val response = byFlightIdArrivingOnResponse(flightId, date, airportIATA)
        response.error?.let {
            throw it.toException()
        }
        if (response.flightStatuses == null || response.appendix == null) {
            throw FlightStatusContentsException("both error and OK response elements are absent")
        }
        return response.flightStatuses.toFlight(response.appendix)
    }
}

internal open class FlightStatusContentsException(
    message: String? = null,
    cause: Throwable? = null
) :
    RuntimeException(message, cause)

internal class FlightStatusErrorException(
    val statusCode: Int,
    val errorCode: String,
    val errorMessage: String,
    val id: UUID?,
    cause: Throwable? = null,
) : RuntimeException(cause) {
    override val message
        get() = "$errorCode ($statusCode): $errorMessage"
}

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
                "departureStatus=$departureStatus, " +
                "departureTimes=$departureTimes, " +
                "arrivalStatus=$arrivalStatus, " +
                "arrivalTimes=$arrivalTimes" +
                ")"
    }
}

private fun Array<FlightStatusFlightStatus>.toFlight(appendix: FlightStatusAppendix): Flight? {
    val sorted = this.sortedBy { it.operationalTimes.publishedDeparture?.dateUtc }.iterator()

    if (!sorted.hasNext()) {
        return null
    }

    val first = sorted.next()
    val airline = appendix.findAirline(first.carrierFsCode).toAirline() ?: return null

    val departure = first.toJunctureDeparture(appendix) ?: return null

    val flightNumberInt = first.flightNumberInt

    val flightIdIATA = try {
        FlightIdIATA(airline.iata, flightNumberInt)
    } catch (e: java.lang.IllegalArgumentException) {
        throw FlightStatusContentsException(cause = e)
    }

    val flightIdICAO = try {
        FlightIdICAO(airline.icao, flightNumberInt)
    } catch (e: java.lang.IllegalArgumentException) {
        throw FlightStatusContentsException(cause = e)
    }

    var flightStatusPrev = first
    val mids = mutableListOf<FlightJunctureMid>()
    for (flightStatus in sorted) {
        val mid = toJunctureMid(flightStatusPrev, flightStatus, appendix) ?: return null
        mids.add(mid)
        flightStatusPrev = flightStatus
    }

    val arrival = flightStatusPrev.toJunctureArrival(appendix) ?: return null

    return Flight(flightIdIATA, flightIdICAO, airline, departure, mids.toTypedArray(), arrival)
}

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
            throw FlightStatusContentsException("invalid flight number: $flightNumber", e)
        }
    }

private fun FlightStatusFlightStatus.toJunctureDeparture(appendix: FlightStatusAppendix):
        FlightJunctureDeparture? {
    val airport = appendix.findAirport(departureAirportFsCode).toAirport() ?: return null
    if (status == null) {
        throw FlightStatusContentsException("flight status is absent")
    }
    val departureTimes = this.operationalTimes.toJunctureTimesDeparture(delays) ?: return null
    return FlightJunctureDepartureImpl(airport, status, departureTimes)
}

private fun FlightStatusFlightStatus.toJunctureArrival(appendix: FlightStatusAppendix):
        FlightJunctureArrival? {
    val airport = appendix.findAirport(arrivalAirportFsCode).toAirport() ?: return null
    if (status == null) {
        throw FlightStatusContentsException("flight status is absent")
    }
    val arrivalTimes = this.operationalTimes.toJunctureTimesArrival(delays) ?: return null
    return FlightJunctureArrivalImpl(airport, status, arrivalTimes)
}

private fun toJunctureMid(
    prev: FlightStatusFlightStatus,
    next: FlightStatusFlightStatus,
    appendix: FlightStatusAppendix
): FlightJunctureMid? {
    val junctureArrival = prev.toJunctureArrival(appendix) ?: return null
    val junctureDeparture = next.toJunctureDeparture(appendix) ?: return null
    if (junctureDeparture.airport.iata != junctureArrival.airport.iata) {
        throw FlightStatusContentsException(
            "invalid flight juncture: arrival ${junctureArrival.airport.iata} != " +
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

private fun FlightStatusAirline.toAirline(): FlightAirline? {
    val iata = this.iata ?: return null
    val icao = this.icao ?: return null
    val name = this.name
    return FlightAirline(iata, icao, name)
}

private fun FlightStatusOperationalTimes.toJunctureTimesDeparture(
    delays: FlightStatusDelays?
) = toJunctureTimes(
    delays?.departureGateDelayMinutes,
    publishedDeparture,
    estimatedGateDeparture,
    actualGateDeparture,
)

private fun FlightStatusOperationalTimes.toJunctureTimesArrival(
    delays: FlightStatusDelays?
) = toJunctureTimes(
    delays?.arrivalGateDelayMinutes,
    publishedArrival,
    estimatedGateArrival,
    actualGateArrival,
)

private fun toJunctureTimes(
    delayMinutes: Int?,
    publishedTimes: FlightStatusTimes?,
    estimatedTimes: FlightStatusTimes?,
    actualTimes: FlightStatusTimes?,
): FlightJunctureTimes? {
    val published = publishedTimes?.toFlightTime() ?: return null
    val estimated = estimatedTimes?.toFlightTime()
    val actual = actualTimes?.toFlightTime()
    val delay = delayMinutes?.let(Int::toLong)?.let(Duration::ofMinutes)
    return FlightJunctureTimes(published, estimated, actual, delay)
}

private fun FlightStatusAirport.toAirport(): FlightAirport? {
    val iata = this.iata ?: return null
    val name = this.name
    val city = this.city
    val country = this.countryCode
    return FlightAirport(iata, name, city, country)
}

private fun FlightStatusTimes.toFlightTime(): FlightTime? {
    val local = dateLocal ?: return null
    val utc = dateUtc ?: return null
    return FlightTime(local, utc)
}

private fun FlightStatusAppendix.findAirline(fsCode: String) =
    airlines.find { it.fs == fsCode }
        ?: throw FlightStatusContentsException("airline not in appendix: fs code $fsCode")

private fun FlightStatusAppendix.findAirport(fsCode: String) =
    airports.find { it.fs == fsCode }
        ?: throw FlightStatusContentsException("airport not in appendix: fs code $fsCode")

private fun FlightStatusError.toException(): FlightStatusErrorException {
    val id = try {
        UUID.fromString(errorId)
    } catch (e: IllegalArgumentException) {
        null
    }
    return FlightStatusErrorException(httpStatusCode, errorCode, errorMessage, id)
}
