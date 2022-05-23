package com.sygic.driving.testapp.ui.trip_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sygic.driving.testapp.databinding.ItemTripDetailPropertyBinding

typealias NameValueProperty = Pair<String, String>

class TripDetailsPropertiesAdapter(private val items: List<NameValueProperty>):
    RecyclerView.Adapter<TripDetailsPropertiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTripDetailPropertyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemTripDetailPropertyBinding)
        : RecyclerView.ViewHolder(binding.root) {
            fun bind(property: NameValueProperty) {
                binding.propertyName.text = property.first
                binding.propertyValue.text = property.second
            }
        }
}