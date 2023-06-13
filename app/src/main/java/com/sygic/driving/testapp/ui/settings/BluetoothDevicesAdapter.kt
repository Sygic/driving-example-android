package com.sygic.driving.testapp.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sygic.driving.testapp.databinding.ItemBluetoothDeviceBinding

class BluetoothDevicesAdapter(
    private val onDeviceSelected: (BluetoothDevice) -> Unit
) : ListAdapter<BluetoothDevice, BluetoothDevicesAdapter.BluetoothDeviceViewHolder>(BluetoothDeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBluetoothDeviceBinding.inflate(inflater, parent, false)
        return BluetoothDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
    }

    inner class BluetoothDeviceViewHolder(
        private val binding: ItemBluetoothDeviceBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice) {
            binding.deviceName.text = device.name ?: "N/A"
            binding.deviceAddress.text = device.address
            binding.root.setOnClickListener { onDeviceSelected(device) }
        }
    }
}

class BluetoothDeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {

    override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        // Compare the unique identifier of the Bluetooth devices
        return oldItem.address == newItem.address
    }

    @SuppressLint("MissingPermission")
    override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
        // Compare the content of the Bluetooth devices (e.g., name, address)
        return oldItem.name == newItem.name && oldItem.address == newItem.address
    }
}