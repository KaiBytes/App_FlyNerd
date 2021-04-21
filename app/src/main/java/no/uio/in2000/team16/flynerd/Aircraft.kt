package no.uio.in2000.team16.flynerd

import com.google.android.gms.maps.model.LatLng
import java.time.Instant

internal class AircraftStates(
    /**
     * Timestamp the states are associated with.
     *
     * Each state represents the state of an aircraft within the interval \[time − 1s, time\].
     */
    val time: Instant,

    /**
     * States of the retrieved aircrafts; can be empty.
     */
    val states: Array<AircraftState>,
) {
    override fun toString(): String {
        return "AircraftStates(time=$time, states=${states.contentToString()})"
    }
}

/**
 * State of a particular aircraft.
 */
internal class AircraftState(
    /**
     * Unique ICAO 24-bit address of the transponder, hex string.
     */
    val icao24: String,

    /**
     * Callsign of the aircraft (8 chars), if it has been received.
     */
    val callsign: String?,

    /**
     * Transponder code, aka Squawk.
     */
    val squawk: String?,

    /**
     * Country name, inferred from the ICAO 24-bit address.
     */
    val originCountry: String,

    /**
     * Timestamp for the last update in general.
     *
     * Updated for any new, valid message received from the transponder.
     */
    val lastContact: Instant,

    /**
     * Timestamp for the last position update, if position report was received in the past 15s.
     */
    val lastPosition: Instant?,

    /**
     * Position, WGS-84 coordinates.
     */
    val position: LatLng?,

    /**
     * Whether the position was retrieved from a surface position report.
     */
    val onGround: Boolean,

    /**
     * Barometric altitude, m.
     */
    val altitudeBarometricM: Double?,

    /**
     * Geometric altitude, m.
     */
    val altitudeGeometricM: Double?,

    /**
     * True track, decimal degrees clockwise from north = 0°.
     */
    val trueTrackDeg: Double?,

    /**
     * Velocity over ground, m/s.
     */
    val velocityMPerS: Double?,

    /**
     * Vertical rate, m/s.
     *
     * Rate of change in distance to ground.
     */
    val verticalRateMPerS: Double?,

    /**
     * Whether flight status indicates special purpose indicator.
     */
    val specialIndicator: Boolean,

    /**
     * Origin of the position.
     */
    val positionSource: AircraftPositionSource,

    /**
     * IDs of the receivers which contributed to this state vector, if filtering for sensor was used
     * in the request.
     */
    val sensors: Array<Int>?,
) {
    override fun toString(): String {
        return "AircraftState(" +
                "icao24='$icao24', " +
                "callsign=$callsign, " +
                "squawk=$squawk, " +
                "originCountry='$originCountry', " +
                "lastContact=$lastContact, " +
                "lastPosition=$lastPosition, " +
                "position=$position, " +
                "onGround=$onGround, " +
                "altitudeBarometricM=$altitudeBarometricM, " +
                "altitudeGeometricM=$altitudeGeometricM, " +
                "trueTrackDeg=$trueTrackDeg, " +
                "velocityMPerS=$velocityMPerS, " +
                "verticalRateMPerS=$verticalRateMPerS, " +
                "specialIndicator=$specialIndicator, " +
                "positionSource=$positionSource, " +
                "sensors=${sensors?.contentToString()}" +
                ")"
    }
}

internal enum class AircraftPositionSource {
    ADSB,
    ASTERIX,
    MLAT,
}