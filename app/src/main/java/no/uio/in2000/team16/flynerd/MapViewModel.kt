package no.uio.in2000.team16.flynerd

import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.*
import no.uio.in2000.team16.flynerd.api.OpenSkyRepository
import no.uio.in2000.team16.flynerd.mapdata.AircraftState
import no.uio.in2000.team16.flynerd.mapdata.AircraftStates
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * Manages data of [MapActivity] in a way that is independent of activity lifecycle.
 */
internal class MapViewModel(repository: OpenSkyRepository, bounds: LatLngBounds) : ViewModel() {
    private val _states by lazy {
        StatesLiveData(viewModelScope, repository, bounds)
    }

    /**
     * Aircraft states backed by repository providing live updates to observers.
     */
    val states: LiveData<MapAircraftStates>
        get() = _states
}

/**
 * Custom view model factory, needed to be able to use view models that take constructor arguments.
 */
internal class MapViewModelFactory(
    private val repository: OpenSkyRepository,
    private val bounds: LatLngBounds,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        MapViewModel(repository, bounds) as T
}

/**
 * Live data that runs live-update loop getting aircraft states from backing repository.
 *
 * Automatically (re-)starts and cancels loop based on whether there are any active observers.
 *
 * @param scope Scope to run update loop coroutine in.
 */
private class StatesLiveData(
    private val scope: CoroutineScope,
    private val repository: OpenSkyRepository,
    val bounds: LatLngBounds,
) : MutableLiveData<MapAircraftStates>() {
    private lateinit var job: Job

    override fun onActive() {
        job = (scope + Dispatchers.IO).launch {
            while (true) {
                val e = try {
                    val states = repository.statesAllNew(bounds = bounds)
                    Log.d(TAG, "update: ${states.states.size} (${states.time})")
                    postValue(states.toStates())
                    continue
                } catch (e: CancellationException) {
                    throw e
                } catch (e: RuntimeException) {
                    e
                } catch (e: IOException) {
                    e
                }
                Log.i(TAG, "cannot update states: $e")
                val duration = OpenSkyRepository.statesAllResolution
                Log.i(TAG, "waiting $duration before trying again ...")
                delay(duration.toMillis())
            }
        }
        Log.d(TAG, "updates resumed")
    }

    override fun onInactive() {
        job.cancel()
        Log.d(TAG, "updates paused")
    }

    companion object {
        const val TAG = "StatesLiveData"
    }
}

/**
 * Map-specific sequence of aircraft states.
 */
internal class MapAircraftStates(val states: Array<MapAircraftState>)

/**
 * Map-specific aircraft state.
 */
internal class MapAircraftState(
    val icao24: String,
    val callsign: String?,
    val position: LatLng,
    val altitudeM: Float,
    val rotation: Float,
    val movement: MapAircraftMovement?,
)

/**
 * Map-specific aircraft state movement information.
 */
internal class MapAircraftMovement(
    private val lastPosition: Instant,
    position: LatLng,
    trueTrackDeg: Double,
    private val velocityMPerS: Double,
) {
    private val latitudeRad = position.latitude / 180.0 * PI
    private val latitudeCos = cos(latitudeRad)
    private val latitudeSin = sin(latitudeRad)
    private val longitudeRad = position.longitude / 180.0 * PI
    private val trueTrackRad = trueTrackDeg / 180.0 * PI
    private val trueTrackCos = cos(trueTrackRad)
    private val trueTrackSin = sin(trueTrackRad)

    /**
     * Calculate projected position at given instant, assuming constant velocity.
     *
     * Based on last position and movement parameters and last update time.
     */
    fun calculatePosition(instant: Instant): LatLng {
        // Algorithm adapted from: https://www.edwilliams.org/avform147.htm#LL

        val timeS = Duration.between(lastPosition, instant).toMillis() / 1_000.0
        val distanceM = velocityMPerS * timeS
        val distanceRad = distanceM / EARTH_RADIUS_M

        val distanceSin = sin(distanceRad)
        val latitudeNewRad =
            asin(latitudeSin * cos(distanceRad) + latitudeCos * distanceSin * trueTrackCos)
        val longitudeNewRad = longitudeRad + asin(trueTrackSin * distanceSin / latitudeCos)

        val latitudeNewDeg = latitudeNewRad / PI * 180.0
        val longitudeNewDeg = longitudeNewRad / PI * 180.0
        return LatLng(latitudeNewDeg, longitudeNewDeg)
    }
}

/**
 * Convert repository [AircraftStates] to map-specific [MapAircraftStates].
 */
private fun AircraftStates.toStates(): MapAircraftStates {
    val states = this.states.asSequence()
        .map(AircraftState::toState)
        .filterNotNull()
        .toList()
        .toTypedArray()
    return MapAircraftStates(states)
}

/**
 * Convert repository [AircraftState] to map-specific [MapAircraftState].
 */
private fun AircraftState.toState(): MapAircraftState? {
    if (onGround) {
        return null
    }
    val position = this.position ?: return null
    val altitudeM = altitudeGeometricM?.toFloat() ?: altitudeBarometricM?.toFloat() ?: 0.0F
    val rotation = trueTrackDeg?.toFloat() ?: 0.0F
    val movement = toMovement()
    return MapAircraftState(icao24, callsign, position, altitudeM, rotation, movement)
}

/**
 * Extract map-specific movement information [MapAircraftMovement] from repository [AircraftState].
 */
private fun AircraftState.toMovement(): MapAircraftMovement? {
    val lastPosition = this.lastPosition ?: return null
    val position = this.position ?: return null
    val trueTrackDeg = this.trueTrackDeg ?: return null
    val velocityMPerS = this.velocityMPerS ?: return null
    return MapAircraftMovement(lastPosition, position, trueTrackDeg, velocityMPerS)
}

/**
 * Approximate radius of Earth in m, used in coordinate calculations.
 */
private const val EARTH_RADIUS_M = 6_366_710.0