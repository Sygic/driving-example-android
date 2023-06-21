package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.sygic.driving.testapp.R
import java.io.File
import java.util.Date


fun Context.getBoolean(@BoolRes resId: Int): Boolean = resources.getBoolean(resId)
fun Context.getInteger(@IntegerRes resId: Int): Int = resources.getInteger(resId)
fun Context.getAndroidId(): String = Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)

fun Context.formattedDoubleOrNoValue(number: Double?, decimalDigits: Int): String =
    number?.format(decimalDigits) ?: getString(R.string.no_value)

fun Context.formattedDateOrNoValue(date: Date?): String = date?.formatHhMmSs() ?: getString(R.string.no_value)

val Context.countryIso: String
    get() {
        (getSystemService(FragmentActivity.TELEPHONY_SERVICE) as? TelephonyManager)?.let {
            return it.simCountryIso
        }
        return ""
    }

fun Context.formattedDurationOrNoValue(count: Long?): String = count?.formatDurationHhMmSs() ?: getString(R.string.no_value)


fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
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

