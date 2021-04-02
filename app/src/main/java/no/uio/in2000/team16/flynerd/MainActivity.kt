package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.uio.in2000.team16.flynerd.api.FlightStatusService
import no.uio.in2000.team16.flynerd.util.registerLocalDateTime
import no.uio.in2000.team16.flynerd.util.registerZonedDateTime
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://api.flightstats.com/flex/flightstatus/rest/"
    private val appId = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT
    private val appKey = "" //  USE YOUR OWN API-ID FROM YOUR SIGN UP ACCOUNT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // input the flight number as string
        val carrierStr = "AA"
        val flightNum = 100
        val date = LocalDate.now().plusDays(0)
        val flightNumberStr = carrierStr + flightNum

        // checking if the flight number is valid withcheckFlightNumber function and use the flight
        // number else exit the system if flight number is not valid
        if (!checkFlightNumber_IATA(flightNumberStr) && !checkFlightNumber_ICAO(flightNumberStr)) {
            Log.i(TAG, "The flight number is invalid")
            return
        }

        val gson = GsonBuilder().registerLocalDateTime().registerZonedDateTime().create()

        val service = Retrofit.Builder()
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

        //using couroutinScope & feul make http requsting and get data from flight API provider as json format
        runBlocking(Dispatchers.IO) {
            val response = service.byFlightNumberArrivingOn(
                carrierStr, flightNum,
                date.year, date.month.value, date.dayOfMonth,
                null,
            )
            Log.i(TAG, "$response")
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}

// Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for IATA Flight No.
private fun checkFlightNumber_IATA(flightNumberStr: String): Boolean {
    if (flightNumberStr.length < 3) {
        return false
    }
    if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1])) {
        return false
    }
    for (i in 2 until flightNumberStr.length) {
        if (!Character.isDigit(flightNumberStr[i])) {
            return false
        }
    }
    return true
}

// Flight number should have 2 first characters as carrier and followed a number, ex: AF100
//In the aviation industry, a flight number or flight designator is a code for an airline service
// consisting of two-character airline designator and a 1 to 4 digit number for  ICAO Flight No.
private fun checkFlightNumber_ICAO(flightNumberStr: String): Boolean {
    if (flightNumberStr.length < 4) {
        return false
    }
    if (!Character.isLetter(flightNumberStr[0]) || !Character.isLetter(flightNumberStr[1]) || !Character.isLetter(
            flightNumberStr[2]
        )
    ) {
        return false
    }
    for (i in 3 until flightNumberStr.length) {
        if (!Character.isDigit(flightNumberStr[i])) {
            return false
        }
    }
    return true
}
