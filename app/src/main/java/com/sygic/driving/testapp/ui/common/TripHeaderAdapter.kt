package com.sygic.driving.testapp.ui.common

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.formatDate
import com.sygic.driving.testapp.core.utils.formatHhMmSs
import com.sygic.driving.testapp.databinding.ItemTripHeaderBinding
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeader
import com.sygic.driving.testapp.domain.driving.model.DrivingTripHeaderStatus
import com.sygic.driving.testapp.domain.driving.model.DrivingTripStorage
import java.util.*

class TripHeaderAdapter(
    private val tripHeaders: List<DrivingTripHeader>,
    private val listener: TripActionListener
) : RecyclerView.Adapter<TripHeaderAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var binding: ItemTripHeaderBinding

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = ItemTripHeaderBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tripHeaders.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tripHeader = tripHeaders[position]
        holder.bind(tripHeader)
    }

    inner class ViewHolder(private val binding: ItemTripHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tripHeader: DrivingTripHeader) {
            binding.root.tag = tripHeader.id

            binding.date.text = tripHeader.startTime.formatDate()
            binding.startTime.text = tripHeader.startTime.formatHhMmSs()
            binding.endTime.text = tripHeader.endTime.formatHhMmSs()
            binding.imgSuccess.isVisible = tripHeader.status == DrivingTripHeaderStatus.Success
            binding.imgError.isVisible = tripHeader.status == DrivingTripHeaderStatus.Error
            binding.options.isVisible = tripHeader.storage == DrivingTripStorage.Local

            binding.options.setOnClickListener { view ->
                TripPopupMenu(context, view, tripHeader.id).show()
            }

            binding.root.setOnClickListener { view ->
                val tripFileName = view.tag as? String ?: return@setOnClickListener
                listener.onTripAction(TripAction.Selected(tripFileName))
            }
        }
    }

    inner class TripPopupMenu(
        context: Context,
        anchor: View,
        private val tripId: String
    ) : PopupMenu(context, anchor),
        PopupMenu.OnMenuItemClickListener {

        init {
            inflate(R.menu.menu_local_trip_item)
            setOnMenuItemClickListener(this)
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.showOnMap -> {
                    listener.onTripAction(TripAction.Selected(tripId))
                    true
                }
                R.id.send -> {
                    listener.onTripAction(TripAction.Send(tripId))
                    true
                }
                R.id.simulate -> {
                    listener.onTripAction(TripAction.Simulate(tripId))
                    true
                }
                R.id.simulate_fast -> {
                    listener.onTripAction(TripAction.SimulateFast(tripId))
                    true
                }
                R.id.delete -> {
                    listener.onTripAction(TripAction.Delete(tripId))
                    true
                }
                else -> false
            }
        }
    }

    sealed class TripAction {
        data class Selected(val id: String): TripAction()
        data class Simulate(val id: String): TripAction()
        data class SimulateFast(val id: String): TripAction()
        data class Send(val id: String): TripAction()
        data class Delete(val id: String): TripAction()
    }

    interface TripActionListener {
        fun onTripAction(action: TripAction)
    }

}

