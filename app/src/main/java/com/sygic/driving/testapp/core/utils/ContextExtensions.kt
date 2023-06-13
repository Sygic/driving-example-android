package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.fragment.app.FragmentActivity
import com.sygic.driving.testapp.R
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
