package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sygic.driving.testapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

val <T> T.exhaustive: T
    get() = this

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

val Context.countryIso: String
    get() {
        (getSystemService(FragmentActivity.TELEPHONY_SERVICE) as? TelephonyManager)?.let {
            return it.simCountryIso
        }
        return ""
    }


fun Context.formattedDateOrNoValue(date: Date?): String {
    return if (date == null) getString(R.string.no_value) else formatHhMmSs(date)
}

fun Fragment.formattedDateOrNoValue(date: Date?): String {
    return requireContext().formattedDateOrNoValue(date)
}

fun Fragment.formattedDateOrNoValue(time: Long?): String {
    val date = if (time == null) null else Date(time)
    return formattedDateOrNoValue(date)
}

fun Context.formattedDoubleOrNoValue(number: Double?, decimalDigits: Int): String {
    return number?.format(decimalDigits) ?: getString(R.string.no_value)
}

fun Fragment.formattedDoubleOrNoValue(number: Double?, decimalDigits: Int): String {
    return requireContext().formattedDoubleOrNoValue(number, decimalDigits)
}

fun Context.getBoolean(@BoolRes resId: Int): Boolean {
    return resources.getBoolean(resId)
}

fun Context.getInteger(@IntegerRes resId: Int): Int {
    return resources.getInteger(resId)
}

fun Context.getAndroidId(): String {
    return Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
}

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}


private val dateFormatHhMmSs = SimpleDateFormat("HH:mm:ss", Locale.US)

fun formatHhMmSs(date: Date): String {
    return dateFormatHhMmSs.format(date)
}

fun Long.formatDurationHhMmSs(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0)
        String.format("%d:%02d:%02d", h, m, s)
    else
        String.format("%d:%02d", m, s)
}

fun Context.formattedDurationOrNoValue(count: Long?): String {
    return count?.formatDurationHhMmSs() ?: getString(R.string.no_value)
}

fun Fragment.formattedDurationOrNoValue(count: Long?): String {
    return requireContext().formattedDurationOrNoValue(count)
}

fun Context.getStringFormat(@StringRes resId: Int, vararg args: Any): String {
    return String.format(getString(resId), *args)
}

fun Context.shareFile(file: File, subject: String, body: String) {
    val uri = FileProvider.getUriForFile(this, "com.sygic.driving.testapp.fileprovider", file)
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    startActivity(intent)
}

fun Context.shareMultipleFiles(files: List<File>, subject: String) {
    val uris = files.map { file ->
        FileProvider.getUriForFile(this, "com.sygic.driving.testapp.fileprovider", file)
    }
    val intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    startActivity(intent)
}

val WHILE_SUBSCRIBED_WITH_TIMEOUT = SharingStarted.WhileSubscribed(5000L)

fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model.capitalize()
        } else {
            manufacturer.capitalize() + " " + model
        }
    }