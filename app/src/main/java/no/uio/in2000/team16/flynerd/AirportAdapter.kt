package no.uio.in2000.team16.flynerd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView


/**
 * Needed for recyclerview in main activity.
 * https://developer.android.com/guide/topics/ui/layout/recyclerview#kotlin
 *
 * Used in: MainActivity.kt
 *
 * @param dataSet - passed from main activity. contains all airports that service a user specified city
 */

class AirportAdapter(val dataSet: MutableList<Airport>) : RecyclerView.Adapter<AirportAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AirportAdapter.ViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.element, parent, false)
        return ViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: AirportAdapter.ViewHolder, position: Int) {
        holder.airportName.text = dataSet[position].name
        holder.airportInfo.text = dataSet[position].country
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?){
                val activity = v!!.context as AppCompatActivity
                val forecastFragment = AirportFragment(dataSet[position])
                activity.supportFragmentManager.beginTransaction().replace(R.id.flContainer, forecastFragment).addToBackStack(null).commit()
            }
        })
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var airportName : TextView
        var airportInfo : TextView

        init {
            airportName = itemView.findViewById(R.id.airport_name)
            airportInfo = itemView.findViewById(R.id.airport_info)
        }
    }


}