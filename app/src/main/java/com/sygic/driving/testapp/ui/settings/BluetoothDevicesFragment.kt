package com.sygic.driving.testapp.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sygic.driving.testapp.core.utils.UiEvent
import com.sygic.driving.testapp.core.utils.launchAndRepeatWithViewLifecycle
import com.sygic.driving.testapp.databinding.FragmentBluetoothDevicesBinding
import com.sygic.driving.testapp.ui.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BluetoothDevicesFragment: Fragment() {

    private val viewModel: BluetoothDevicesViewModel by viewModels()
    private lateinit var binding: FragmentBluetoothDevicesBinding

    private val bluetoothDevicesAdapter = BluetoothDevicesAdapter {
        viewModel.onDeviceSelected(it)
    }

    private val requestBluetoothPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        viewModel.onBluetoothPermissionResult(granted)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBluetoothDevicesBinding.inflate(inflater, container, false).apply {
            btnGrantBtPermission.setOnClickListener {
                requestBluetoothPermission()
            }
            btnRefresh.setOnClickListener {
                viewModel.onStartScan()
            }
            listDevices.adapter = bluetoothDevicesAdapter
        }
        if(!hasBluetoothPermission()) {
            requestBluetoothPermission()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launchAndRepeatWithViewLifecycle {
            viewModel.uiEvent.collectLatest { event ->
                when(event) {
                    is UiEvent.PopBackStack -> requireActivity().findNavController().popBackStack()
                    is UiEvent.ShowToast ->
                        Toast.makeText(requireContext(), event.resId, Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.hasBluetoothPermission.collect { hasBtPermission ->
                binding.bluetoothDevicesContainer.isVisible = hasBtPermission == true
                binding.permissionWarningContainer.isVisible = hasBtPermission == false
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.startBluetoothScanEvent.collectLatest {
                startBluetoothScan()
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.devicesUpdatedEvent.collectLatest {
                bluetoothDevicesAdapter.submitList(viewModel.detectedDevices)
            }
        }
        launchAndRepeatWithViewLifecycle {
            viewModel.scanRunning.collectLatest {
                binding.progressScanning.isVisible = it
            }
        }
    }

    override fun onResume() {
        viewModel.onBluetoothPermissionResult(hasBluetoothPermission())
        viewModel.onStartScan()
        super.onResume()
    }

    private fun requestBluetoothPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermission.launch(arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADMIN
            ))
        }
    }

    private fun hasBluetoothPermission() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private suspend fun startBluetoothScan() {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                ?: return
        val bluetoothAdapter = bluetoothManager.adapter ?: return
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.device?.let {
                    viewModel.onBluetoothDeviceDetected(it)
                }
            }
        }
        try {
            viewModel.onStartBluetoothScan()

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            val filter = ScanFilter.Builder()
                .build()

            bluetoothAdapter.bluetoothLeScanner.startScan(listOf(filter), settings, scanCallback)

            // stop scan after 10 seconds
            delay(10_000L)
        } finally {
            bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
            viewModel.onStopBluetoothScan()
        }
    }
}