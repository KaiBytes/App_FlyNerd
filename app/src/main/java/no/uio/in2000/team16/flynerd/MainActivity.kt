package no.uio.in2000.team16.flynerd

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.InputStream
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Job
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val initializer = AirportsList(this, R.raw.domair)
        initializer.readAirports()

        runBlocking {
            initializer.airports[0].callForecastAPI()
        }
    }
}