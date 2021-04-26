package no.uio.in2000.team16.flynerd

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    var matchedAirports  = mutableListOf<Airport>()
    var recycleAdapter: RecyclerView.Adapter<AirportAdapter.ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //soft keyboard pop up covers layout instead of pushing it upwards
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        //fetching views to edit
        val userInput = findViewById<AutoCompleteTextView>(R.id.searchFstatus)
        val cities: Array<out String> = resources.getStringArray(R.array.cities)
        val searchButton = findViewById<Button>(R.id.buttonSearchFStatus)

        //setting up recyclerview
        val layoutManager: RecyclerView.LayoutManager? = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.recyclerView).layoutManager = layoutManager

        //setting up autocomplete functionality
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cities).also { adapter ->
            userInput.setAdapter(adapter)
        }

        //initializing airports database (without forecast data)
        val initializer = AirportsList(this, R.raw.intair_city)

        CoroutineScope(IO).launch {
            initializer.readAirports()
        }

        searchButton.setOnClickListener {

            //capitalizes all words in string
            val input : String? = userInput.text.toString().capitalizeFirstLetter()
            matchedAirports.clear()
            CoroutineScope(IO).launch {
                for (airport in initializer.airports){
                    if (input == airport.city){
                        matchedAirports.add(airport)
                    }
                }

                runOnUiThread{
//                Log.d("partyList populated?", partyList.toString())
                    recycleAdapter = AirportAdapter(matchedAirports)
                    findViewById<RecyclerView>(R.id.recyclerView).adapter = recycleAdapter
                }
            }
            dismissKeyboard(this)

        }
    }

    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

    private fun dismissKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (null != activity.currentFocus) imm.hideSoftInputFromWindow(
            activity.currentFocus!!.applicationWindowToken, 0
        )
    }
}