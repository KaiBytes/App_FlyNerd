package no.uio.in2000.team16.flynerd.api

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.delay
import no.uio.in2000.team16.flynerd.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException
import java.time.Duration
import java.time.Instant

/**
 * Repository for the OpenSky API.
 *
 * Takes care of rate limiting by caching responses when appropriate.
 */
internal class OpenSkyRepository(
    baseUrl: String,
    connectTimeout: Duration = Duration.ofSeconds(60),
    readTimeout: Duration = Duration.ofSeconds(60),
) {
    private val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create<OpenSkyService>()

    /**
     * Cache for statesAll requests.
     */
    private val statesAllCache = mutableMapOf<Pair<Array<String>?, LatLngBounds?>, AircraftStates>()

    /**
     * Get cached response to given input arguments, if one is cached and it is not out of date.
     */
    private fun statesAllCached(
        icao24: Array<String>? = null,
        bounds: LatLngBounds? = null
    ): AircraftStates? {
        statesAllCache[icao24 to bounds]?.let { cached ->
            val updatePrev = statesAllUpdatePrev(Instant.now())
            // NOTE: We round the time to the nearest multiple of [statesAllResolution] because the
            // response time is sometimes off from it by a second.
            val cachedTimeRounded = cached.time.epochSecond
                .let { it + statesAllResolution.seconds / 2 }
                .let { it - it % statesAllResolution.seconds }
                .let(Instant::ofEpochSecond)
            if (cachedTimeRounded >= updatePrev) {
                return cached
            }
        }
        return null
    }

    /**
     * Retrieve all states of aircrafts obeying the given restrictions.
     *
     * If a cached version of the most recent API data is available, then it is return that instead
     * of fetching.
     *
     * @param icao24 The ICAO 24-bit addresses to accept; if null, accept all addresses.  Hex
     *   strings.
     * @param bounds Bounding box of positions to accept; if null, accept all positions.  WGS-84
     *   coordinates.
     * @return The aircraft states.
     * @throws IOException An I/O error occured talking to the API server.
     * @throws OpenSkyException The API server's response's contents were illegal.
     * @throws RuntimeException Some other kind of error occured decoding the API server's response.
     */
    suspend fun statesAll(
        icao24: Array<String>? = null,
        bounds: LatLngBounds? = null,
    ): AircraftStates {
        statesAllCached(icao24, bounds)?.let { cached ->
            return cached
        }
        val states = statesAllNoCache(icao24, bounds)
        statesAllCache[icao24 to bounds] = states
        return states
    }

    /**
     * Version of [statesAll] that waits for new data instead of using cached data.
     *
     * If a cached version of the most recent data is available, then suspend until the next API
     * update and fetch instead of returning it.  Otherwise, immediately fetch.
     */
    suspend fun statesAllNew(
        icao24: Array<String>? = null,
        bounds: LatLngBounds? = null,
    ): AircraftStates {
        statesAllCached(icao24, bounds)?.let { _ ->
            val now = Instant.now()
            val updateNext = statesAllUpdateNext(now)
            val duration = Duration.between(now, updateNext) + statesAllResolution.dividedBy(2)
            delay(duration.toMillis())
        }
        val states = statesAllNoCache(icao24, bounds)
        statesAllCache[icao24 to bounds] = states
        return states
    }

    /**
     * Version of [statesAll] with no caching.
     *
     * Always immediately fetch from the API.
     */
    suspend fun statesAllNoCache(
        icao24: Array<String>? = null,
        bounds: LatLngBounds? = null,
    ): AircraftStates {
        var lamin: Double? = null
        var lomin: Double? = null
        var lamax: Double? = null
        var lomax: Double? = null
        bounds?.run {
            lamin = southwest.latitude
            lomin = southwest.longitude
            lamax = northeast.latitude
            lomax = northeast.longitude
        }
        return service.statesAll(icao24, lamin, lomin, lamax, lomax).toStates()
    }

    companion object {
        /**
         * Time resolution between [statesAll] responses.
         *
         * Anonymous users can only retrieve data with a time resolution of 10 seconds, meaning the
         * API will return state vectors for time now − (now mod 10).
         */
        val statesAllResolution: Duration = Duration.ofSeconds(10)

        /**
         * Most recent update before or at given instant.
         */
        private fun statesAllUpdatePrev(instant: Instant): Instant =
            instant.epochSecond.let { sec ->
                Instant.ofEpochSecond(sec - (sec % statesAllResolution.seconds))
            }

        /**
         * Earliest update at or after given instant.
         */
        private fun statesAllUpdateNext(instant: Instant): Instant =
            statesAllUpdatePrev(instant) + statesAllResolution
    }
}

/**
 * The API server's response's format was illegal.
 */
internal class OpenSkyException(val msg: String, cause: Throwable? = null) :
    RuntimeException(cause) {
    override val message
        get() = "illegal API response: $msg"
}

/**
 * Convert service [OpenSkyStatesResponse] to repository [AircraftStates].
 */
private fun OpenSkyStatesResponse.toStates(): AircraftStates {
    val time = this.time.let(Instant::ofEpochSecond)
    val states = try {
        this.states?.map(Array<Any>::toState)?.toTypedArray() ?: arrayOf()
    } catch (e: ClassCastException) {
        throw OpenSkyException("unexpected type in state: ${e.localizedMessage}", e)
    } catch (e: IndexOutOfBoundsException) {
        throw OpenSkyException("malformed state vector: ${e.localizedMessage}", e)
    }
    return AircraftStates(time, states)
}

/**
 * Convert raw state vector from service [OpenSkyStatesResponse] to repository [AircraftState].
 */
private fun Array<Any>.toState(): AircraftState {
    val icao24 = this[0] as String
    val callsign = this[1] as String?
    val originCountry = this[2] as String
    val lastPosition = (this[3] as Double?)?.let(Double::toLong)?.let(Instant::ofEpochSecond)
    val lastContact = (this[4] as Double).let(Double::toLong).let(Instant::ofEpochSecond)
    val position = toPosition(this[6] as Double?, this[5] as Double?)
    val altitudeBarometricM = this[7] as Double?
    val onGround = this[8] as Boolean
    val velocityMPerS = this[9] as Double?
    val trueTrackDeg = this[10] as Double?
    val verticalRateMPerS = this[11] as Double?

    @Suppress("UNCHECKED_CAST")
    val sensors = (this[12] as Array<Double>?)?.map(Double::toInt)?.toTypedArray()

    val altitudeGeometricM = this[13] as Double?
    val squawk = this[14] as String?
    val specialIndicator = this[15] as Boolean
    val positionSource = (this[16] as Double).let(Double::toInt).let(Int::toPositionSource)

    return AircraftState(
        icao24,
        callsign,
        squawk,
        originCountry,
        lastContact,
        lastPosition,
        position,
        onGround,
        altitudeBarometricM,
        altitudeGeometricM,
        trueTrackDeg,
        velocityMPerS,
        verticalRateMPerS,
        specialIndicator,
        positionSource,
        sensors,
    )
}

/**
 * Convert lat/lon pair to position iff both are non-null.
 */
private fun toPosition(lat: Double?, lon: Double?): LatLng? =
    lat?.let { latitude ->
        lon?.let { longitude ->
            LatLng(latitude, longitude)
        }
    }

/**
 * Convert raw integer constant from service [OpenSkyStatesResponse] to repository
 * [AircraftPositionSource].
 */
private fun Int.toPositionSource(): AircraftPositionSource =
    try {
        AircraftPositionSource.values()[this]
    } catch (e: IndexOutOfBoundsException) {
        throw OpenSkyException("unrecognized aircraft position source $this", e)
    }