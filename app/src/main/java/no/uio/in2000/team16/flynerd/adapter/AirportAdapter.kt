package no.uio.in2000.team16.flynerd

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import no.uio.in2000.team16.flynerd.uidesign.ForecastActivity

/**
 * Needed for recyclerview in main activity.
 * https://developer.android.com/guide/topics/ui/layout/recyclerview#kotlin
 *
 * Used in: MainActivity.kt
 *
 * @param dataSet - passed from main activity. contains all airports that service a user specified city
 */

class AirportAdapter(val dataSet: MutableList<Airport>) :
    RecyclerView.Adapter<AirportAdapter.ViewHolder>() {


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
        val context = holder.itemView.context
        val intent: Intent = Intent(context, ForecastActivity::class.java)
        Log.d("position", position.toString())

        intent.putExtra("item", dataSet[position])
        holder.itemView.setOnClickListener {
            context.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var airportName: TextView
        var airportInfo: TextView

        init {
            airportName = itemView.findViewById(R.id.airport_name)
            airportInfo = itemView.findViewById(R.id.airport_info)
        }
    }
}