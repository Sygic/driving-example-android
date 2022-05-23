package com.sygic.driving.testapp.ui.server_trips

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sygic.driving.testapp.core.utils.launchAndRepeatWithViewLifecycle
import com.sygic.driving.testapp.databinding.FragmentServerTripsBinding
import com.sygic.driving.testapp.ui.common.TripHeaderAdapter
import com.sygic.driving.testapp.core.utils.UiEvent
import com.sygic.driving.testapp.ui.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ServerTripsFragment : Fragment() {

    private val viewModel: ServerTripsViewModel by viewModels()
    private lateinit var binding: FragmentServerTripsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServerTripsBinding.inflate(inflater).apply {
            listTrips.layoutManager = LinearLayoutManager(context)
            listTrips.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            swipeRefresh.setOnRefreshListener {
                viewModel.onSwipeRefresh()
            }
        }

        // trips
        launchAndRepeatWithViewLifecycle {
            viewModel.trips.collect { trips ->
                binding.listTrips.adapter = TripHeaderAdapter(trips, tripActionListener)
            }
        }

        // is refreshing
        launchAndRepeatWithViewLifecycle {
            viewModel.isRefreshing.collect { isRefreshing ->
                binding.swipeRefresh.isRefreshing = isRefreshing
            }
        }

        // error
        launchAndRepeatWithViewLifecycle {
            viewModel.error.collect { error ->
                val errorVisible = error != null
                binding.error.isVisible = errorVisible
                binding.error.text = error
                binding.listTrips.isVisible = !errorVisible
            }
        }

        // UI events
        launchAndRepeatWithViewLifecycle {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is UiEvent.NavigateTo ->
                        requireActivity().findNavController().navigate(event.navDirections)
                    else -> Unit
                }
            }
        }

        return binding.root
    }

    private val tripActionListener = object : TripHeaderAdapter.TripActionListener {
        override fun onTripAction(action: TripHeaderAdapter.TripAction) {
            when(action) {
                is TripHeaderAdapter.TripAction.Selected -> viewModel.onTripSelected(action.id)
                else -> Unit
            }
        }
    }
}
