package com.sygic.driving.testapp.core.utils

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
 * is in and out of `minActiveState` lifecycle state.
 */
inline fun Fragment.launchAndRepeatWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

fun Fragment.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
}



fun Fragment.formattedDateOrNoValue(date: Date?): String {
    return requireContext().formattedDateOrNoValue(date)
}

fun Fragment.formattedDateOrNoValue(time: Long?): String {
    val date = if (time == null) null else Date(time)
    return formattedDateOrNoValue(date)
}


fun Fragment.formattedDoubleOrNoValue(number: Double?, decimalDigits: Int): String {
    return requireContext().formattedDoubleOrNoValue(number, decimalDigits)
}


fun Fragment.formattedDurationOrNoValue(count: Long?): String {
    return requireContext().formattedDurationOrNoValue(count)
}
