package com.sygic.driving.testapp.ui.local_trips

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sygic.driving.testapp.R
import com.sygic.driving.testapp.core.utils.exhaustive
import com.sygic.driving.testapp.core.utils.launchAndRepeatWithViewLifecycle
import com.sygic.driving.testapp.databinding.FragmentLocalTripsBinding
import com.sygic.driving.testapp.ui.common.TripHeaderAdapter
import com.sygic.driving.testapp.core.utils.UiEvent
import com.sygic.driving.testapp.ui.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File

@AndroidEntryPoint
class LocalTripsFragment : Fragment() {

    private lateinit var binding: FragmentLocalTripsBinding
    private val viewModel: LocalTripsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLocalTripsBinding.inflate(inflater).apply {
            listTrips.layoutManager = LinearLayoutManager(context)
            listTrips.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            swipeRefresh.setOnRefreshListener { viewModel.refreshLocalTrips() }
        }

        // swipe refresh spinner
        launchAndRepeatWithViewLifecycle {
            viewModel.isRefreshing.collect { isRefreshing ->
                    binding.swipeRefresh.isRefreshing = isRefreshing
                }
        }

        // trips
        launchAndRepeatWithViewLifecycle {
            viewModel.trips.collect { trips ->
                    binding.listTrips.adapter = TripHeaderAdapter(trips, tripActionListener)
                }
        }

        // UI events
        launchAndRepeatWithViewLifecycle {
            viewModel.uiEvents.collect { event ->
                when(event) {
                    is UiEvent.NavigateTo ->
                        requireActivity().findNavController().navigate(event.navDirections)
                    is UiEvent.PopBackStack ->
                        requireActivity().findNavController().popBackStack()
                    is UiEvent.ShareFiles ->
                        requireContext().shareFiles(event.files)
                    is UiEvent.ShowToast ->
                        Toast.makeText(requireContext(), event.resId, Toast.LENGTH_LONG).show()
                }
            }
        }

        return binding.root
    }

    private val tripActionListener = object : TripHeaderAdapter.TripActionListener {

        override fun onTripAction(action: TripHeaderAdapter.TripAction) {
            when(action) {
                is TripHeaderAdapter.TripAction.Delete -> viewModel.onTripDelete(action.id)
                is TripHeaderAdapter.TripAction.Selected -> viewModel.onTripSelected(action.id)
                is TripHeaderAdapter.TripAction.Send -> viewModel.onTripSend(action.id)
                is TripHeaderAdapter.TripAction.Simulate -> viewModel.onTripSimulate(action.id)
                is TripHeaderAdapter.TripAction.SimulateFast -> viewModel.onTripSimulateFast(action.id)
            }.exhaustive
        }
    }
}

private fun Context.shareFiles(files: List<File>) {
    val uris = files.map { file ->
        FileProvider.getUriForFile(this, "com.sygic.driving.testapp.fileprovider", file)
    }
    val intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        putExtra(Intent.EXTRA_SUBJECT, R.string.local_trip_send_default_subject)
    }

    startActivity(intent)
}
