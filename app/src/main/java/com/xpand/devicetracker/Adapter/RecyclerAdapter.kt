package com.xpand.devicetracker.Adapter

import android.content.Context
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpand.devicetracker.Model.LocationModel
import com.xpand.devicetracker.R

class RecyclerAdapter(private var Dataset:ArrayList<LocationModel>, private val context: Context) : RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>(){

    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewLatitude: TextView = view.findViewById(R.id.textview_lat)
        val textViewLongitude: TextView = view.findViewById(R.id.textview_long)
        val textViewLocation: TextView = view.findViewById(R.id.textview_location)
        val textViewTime: TextView = view.findViewById(R.id.textview_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.location_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewLatitude = holder.textViewLatitude
        val textViewLongitude = holder.textViewLongitude
        val textViewLocation = holder.textViewLocation
        val textViewTime = holder.textViewTime

        textViewLatitude.text = "Lat:${ Dataset[position].latitude }"
        textViewLongitude.text = "Lng:${Dataset[position].longitude}"
        textViewLocation.text = Dataset[position].location
        textViewTime.text = Dataset[position].time
    }

    override fun getItemCount() = Dataset.size
}