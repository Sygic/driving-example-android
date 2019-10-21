package com.sygic.driving.example

import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.util.*

class User(context: Context) {

    companion object {
        const val PREFS = "DrivingPrefs"
        const val KEY_USER = "User"
    }

    val id: String

    init {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        var user = prefs.getString(KEY_USER, "") ?: ""
        if(!TextUtils.isEmpty(user)) {
            id = user
        }
        else {
            user = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER, user).apply()
            id = user
        }

        Log.d("User", "User ID is $id")
    }

}