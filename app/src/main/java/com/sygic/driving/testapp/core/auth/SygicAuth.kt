package com.sygic.driving.testapp.core.auth

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sygic.driving.testapp.core.utils.Constants
import com.sygic.lib.auth.*
import okhttp3.*
import java.io.IOException

internal object SygicAuth {

    fun create(context: Context): Auth {
        val authHttp = AuthHttpImpl()
        val authStorage = AuthStorageImpl(context)

        val accountChangedListener = object: SignInStateChangeListener {
            override fun onSignedOutWithoutRequest(error: ErrorCode, errorMessage: String) {}
            override fun onStateChanged(newState: SignInState) {}
        }

        val authConfig = AuthConfig(
            url = Constants.AUTH_URL,
            appId = context.packageName,
            clientId = Constants.DRIVING_CLIENT_ID,
            clientSecret = null,
            deviceCode = context.deviceId())

        return Auth.build(authConfig, accountChangedListener, authHttp, authStorage)
    }
}

private const val AUTH_PREFS_FILE_NAME = "auth_prefs"

internal class AuthHttpImpl: AuthHttp {
    private val client = OkHttpClient()

    override fun sendRequest(
        request: AuthHttp.Request,
        responseCallback: AuthHttp.ResponseCallback
    ) {
        val requestBuilder = Request.Builder()

        val okHttpRequest: Request = with(requestBuilder) {
            url(request.getUrl())

            for(head in request.getHeaders().entries)
                addHeader(head.key, head.value)

            val requestBody = RequestBody.create(
                MediaType.get(request.getContentType()), request.getBody())

            post(requestBody)
            build()
        }

        val callback = object: Callback {
            override fun onResponse(call: Call, response: Response) {
                responseCallback.onSuccess(response.code(), response.body()?.string() ?: "")
            }

            override fun onFailure(call: Call, e: IOException) {
                responseCallback.onError(e.message.toString())
            }
        }

        client.newCall(okHttpRequest).enqueue(callback)
    }
}

internal class AuthStorageImpl(context: Context): Storage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        AUTH_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getInt(key: String, defaultValue: Int?): Int? =
        if (encryptedPrefs.contains(key))
            encryptedPrefs.getInt(key, defaultValue ?: 0)
        else
            null


    override fun getLong(key: String, defaultValue: Long?): Long? =
        if (encryptedPrefs.contains(key))
            encryptedPrefs.getLong(key, defaultValue ?: 0L)
        else
            null

    override fun getString(key: String, defaultValue: String?): String? =
        if (encryptedPrefs.contains(key))
            encryptedPrefs.getString(key, null)
        else
            null

    override fun setInt(key: String, value: Int) = encryptedPrefs.edit().putInt(key, value).apply()

    override fun setLong(key: String, value: Long) = encryptedPrefs.edit().putLong(key, value).apply()

    override fun setString(key: String, value: String) = encryptedPrefs.edit().putString(key, value).apply()

}

private fun Context.deviceId() = Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)