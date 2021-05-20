package no.uio.in2000.team16.flynerd.api

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service acting as REST backend for OpenSky API.
 */
internal interface OpenSkyService {
    @GET("states/all")
    suspend fun statesAll(
        @Query("icao24") icao24: Array<String>?,
        @Query("lamin") lamin: Double?,
        @Query("lomin") lomin: Double?,
        @Query("lamax") lamax: Double?,
        @Query("lomax") lomax: Double?,
    ): OpenSkyStatesResponse
}

// Rest of file contains the data structures to convert JSON responses into.

internal class OpenSkyStatesResponse(val time: Long, val states: Array<Array<Any>>?) {
    override fun toString(): String {
        return "OpenSkyStatesResponse(time=$time, states=${states?.contentToString()})"
    }
}
