package no.uio.in2000.team16.flynerd

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import no.uio.in2000.team16.flynerd.api.FlightstatusService
import no.uio.in2000.team16.flynerd.util.registerLocalDateTime
import no.uio.in2000.team16.flynerd.util.registerZonedDateTime
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val baseUrl = "https://api.flightstats.com/flex/flightstatus/rest/"
        val appId = "5df10a1e"
        val appKey = "056a82fd82f0b5109302dca04c0eb6db"

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
            .create<FlightstatusService>()

        runBlocking(Dispatchers.IO) {
            val date = LocalDate.now().minusDays(1)
            val response = service.flightStatusArrivingOn(
                "WF", 776,
                date.year, date.month.value, date.dayOfMonth,
            )
            Log.d(TAG, "$response")
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}