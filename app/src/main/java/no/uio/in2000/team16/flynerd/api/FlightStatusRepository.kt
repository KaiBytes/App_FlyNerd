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
     * Fetch flights with given flight id arriving on given date.
     *
     * @param flightId Identifier of flights.
     * @param date Arrival date, local to the arrival airport.
     * @param airportIATA IATA code of arrival airport.  If null, all arrival airports are accepted.
     *
     * @return List of flights matching the criteria.  Flights that don't have all the information
     *   needed for the returned datastructures are omitted from the list.
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
    ): List<Flight> {
        val response = byFlightIdArrivingOnResponse(flightId, date, airportIATA)
        response.error?.let {
            throw it.toException()
        }
        if (response.flightStatuses == null || response.appendix == null) {
            throw FlightStatusContentsException("both error and OK response elements are absent")
        }
        return response.flightStatuses.asSequence()
            .map { it.toFlight(response.appendix) }
            .filterNotNull()
            .toList()
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

private fun FlightStatusFlightStatus.toFlight(appendix: FlightStatusAppendix): Flight? {
    val airline = appendix.findAirline(carrierFsCode).toAirline() ?: return null
    val airportDeparture = appendix.findAirport(departureAirportFsCode)
    val airportArrival = appendix.findAirport(arrivalAirportFsCode)

    val flightNumberInt = flightNumber.indexOfFirst { char ->
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

    if (status == null) {
        throw FlightStatusContentsException("flight status is absent")
    }

    val departure = operationalTimes.toJunctureDeparture(airportDeparture, delays) ?: return null
    val arrival = operationalTimes.toJunctureArrival(airportArrival, delays) ?: return null

    return Flight(flightIdIATA, flightIdICAO, airline, status, departure, arrival)
}

private fun FlightStatusAirline.toAirline(): Airline? {
    val iata = this.iata ?: return null
    val icao = this.icao ?: return null
    val name = this.name
    return Airline(iata, icao, name)
}

private fun FlightStatusOperationalTimes.toJunctureDeparture(
    airport: FlightStatusAirport,
    delays: FlightStatusDelays?
) = toJuncture(
    airport,
    delays?.departureGateDelayMinutes,
    publishedDeparture,
    estimatedGateDeparture,
    actualGateDeparture,
)

private fun FlightStatusOperationalTimes.toJunctureArrival(
    airport: FlightStatusAirport,
    delays: FlightStatusDelays?
) = toJuncture(
    airport,
    delays?.arrivalGateDelayMinutes,
    publishedArrival,
    estimatedGateArrival,
    actualGateArrival,
)

private fun toJuncture(
    airport: FlightStatusAirport,
    delayMinutes: Int?,
    publishedTimes: FlightStatusTimes?,
    estimatedTimes: FlightStatusTimes?,
    actualTimes: FlightStatusTimes?,
): FlightJuncture? {
    val airport = airport.toAirport() ?: return null
    val published = publishedTimes?.toFlightTime() ?: return null
    val estimated = estimatedTimes?.toFlightTime()
    val actual = actualTimes?.toFlightTime()
    val delay = delayMinutes?.let(Int::toLong)?.let(Duration::ofMinutes)
    return FlightJuncture(airport, published, estimated, actual, delay)
}

private fun FlightStatusAirport.toAirport(): Airport? {
    val iata = this.iata ?: return null
    val name = this.name
    val city = this.city
    val country = this.countryCode
    return Airport(iata, name, city, country)

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
