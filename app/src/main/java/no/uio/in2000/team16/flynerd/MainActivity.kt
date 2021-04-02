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
        val flightNumberStr = "AA100"
        val date = LocalDate.now().plusDays(0)

        // checking if the flight number is valid and use the flight number, else exit the system if
        // flight number is not valid
        val flightId = FlightId.parse(flightNumberStr)
        if (flightId == null) {
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
                flightId.airlineCode, flightId.flightNumber,
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
