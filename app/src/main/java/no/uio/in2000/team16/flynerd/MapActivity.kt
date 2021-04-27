package no.uio.in2000.team16.flynerd

import android.Manifest
import android.animation.TimeAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import no.uio.in2000.team16.flynerd.api.OpenSkyRepository
import no.uio.in2000.team16.flynerd.uidesign.*
import java.time.Instant

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private val viewModel: MapViewModel by viewModels {
        MapViewModelFactory(OpenSkyRepository("https://opensky-network.org/api/"), BOUNDS_NORWAY)
    }

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null

    private lateinit var map: GoogleMap
    private var mapReady = false
    private var mapLayout = false

    private lateinit var aircraftMarkers: AircraftMarkers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.home_map)

        (supportFragmentManager.findFragmentById(R.id.map)!! as SupportMapFragment).let {
            it.getMapAsync(this)
            it.requireView().viewTreeObserver.addOnGlobalLayoutListener(this::onMapLayout)
        }



        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        //textView = findViewById(R.id.textView)
        toolbar = findViewById(R.id.toolbar)


        // toolbar
        setSupportActionBar(toolbar)

        // navigation

        // navigationbar
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setNavigationItemSelectedListener(this)
        navigationView?.setCheckedItem(R.id.nav_home)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapReady = true
        if (mapLayout) {
            onMapReadyAndLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onMapLayout() {
        if (mapLayout) {
            return
        }
        mapLayout = true
        if (mapReady) {
            onMapReadyAndLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onMapReadyAndLayout() {
        map.uiSettings.run {
            isMapToolbarEnabled = false
            isZoomControlsEnabled = true
        }
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(BOUNDS_NORWAY, 0))

        enableMyLocation()

        // The map is now ready and fully manipulable.

        aircraftMarkers = AircraftMarkers(
            map,
            ContextCompat.getDrawable(this, R.drawable.map_marker_aircraft)!!.toBitmap(),
        )
        viewModel.states.observe(this) { states ->
            lifecycleScope.launchWhenResumed {
                aircraftMarkers.update(states)
            }
        }
    }

    private fun enableMyLocation() {
        when (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                map.isMyLocationEnabled = true
                Log.d(TAG, "enabled my-location")
            }
            PackageManager.PERMISSION_DENIED -> requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                RequestCode.LOCATION.ordinal,
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (RequestCode.values().getOrNull(requestCode)) {
            RequestCode.LOCATION -> onRequestLocationResult(permissions, grantResults)
        }
    }

    private fun onRequestLocationResult(permissions: Array<out String>, grantResults: IntArray) {
        val results = permissions.asSequence().zip(grantResults.asSequence())
        val result = Manifest.permission.ACCESS_FINE_LOCATION to PackageManager.PERMISSION_GRANTED
        if (results.contains(result)) {
            enableMyLocation()
        } else {
            Log.i(TAG, "permissions not granted for my-location")
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.i(TAG, marker.title)
        val intent = Intent(this, FlightStatusInfo::class.java).apply {
            putExtra(FLIGHT_NUMBER, marker.title)
        }
        startActivity(intent)
        return false
    }

    companion object {
        const val TAG = "MapActivity"
        const val FLIGHT_NUMBER = "no.uio.in2000.team16.flynerd.FLIGHT_NUMBER"
    }


    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> {

            }
            R.id.flightStatus -> {
                val intent = Intent(this@MapActivity, FlightStatusUI::class.java)
                startActivity(intent)

            }

            R.id.flightDelay -> {
                val intent = Intent(this@MapActivity, FlightDelayUI::class.java)
                startActivity(intent)

            }

            R.id.airportweather -> {
                val intent = Intent(this@MapActivity, AirportWeatherUI::class.java)
                startActivity(intent)
            }

            R.id.func4 -> {
                val intent = Intent(this@MapActivity, FlightStatusInfo::class.java)
                startActivity(intent)
            }

            R.id.func5 -> {
                val intent = Intent(this@MapActivity, Functionality5::class.java)
                startActivity(intent)
            }

            R.id.func6 -> {
                val intent = Intent(this@MapActivity, Functionality6::class.java)
                startActivity(intent)
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }


}

private class AircraftMarkers(private val map: GoogleMap, private val bitmap: Bitmap) {
    private val markers = hashMapOf<String, AircraftMarker>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun update(states: MapAircraftStates) {
        for (marker in markers.values) {
            marker.upToDate = false
        }

        val markerOptions = MarkerOptions()
            .anchor(0.5F, 0.5F)
            .flat(true)
            .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            .position(BOUNDS_NORWAY.center)

        for (state in states.states) {
            val marker = markers[state.icao24]
                ?: AircraftMarker(map.addMarker(markerOptions.alpha(0.25F).title(state.icao24)))  
                    .also { markers[state.icao24] = it }

            marker.stationary.run {
                position = state.position
                rotation = state.rotation
                zIndex = state.altitudeM
            }

            if (state.movement != null) {
                val animated = (marker.animated
                    ?: map.addMarker(markerOptions.alpha(1.0F)).also { marker.animated = it })
                val animator = marker.animator ?: TimeAnimator().also { marker.animator = it }

                animated.run {
                    rotation = state.rotation
                    zIndex = state.altitudeM
                }
                animator.run {
                    cancel()
                    setTimeListener { _, _, _ ->
                        animated.position = state.movement.calculatePosition(Instant.now())
                    }
                    start()
                }
            } else {
                marker.animated?.remove()
                marker.animated = null
                marker.animator?.cancel()
                marker.animator = null
            }

            marker.upToDate = true
        }

        val it = markers.iterator()
        while (it.hasNext()) {
            val marker = it.next().value
            if (!marker.upToDate) {
                marker.run {
                    stationary.remove()
                    animated?.remove()
                    animator?.cancel()
                }
                it.remove()
            }
        }
    }

    private class AircraftMarker(val stationary: Marker) {
        var animated: Marker? = null
        var animator: TimeAnimator? = null
        var upToDate = false
    }
}

private enum class RequestCode {
    LOCATION,
}

val BOUNDS_NORWAY: LatLngBounds = LatLngBounds(LatLng(54.45, 4.08), LatLng(71.39, 31.77))