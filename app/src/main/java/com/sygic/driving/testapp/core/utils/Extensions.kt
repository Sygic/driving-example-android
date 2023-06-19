package com.sygic.driving.testapp.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.SharingStarted
import java.io.File

val <T> T.exhaustive: T
    get() = this

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