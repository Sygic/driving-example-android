package com.sygic.driving.testapp.core.auth.utils

import com.sygic.lib.auth.Auth
import com.sygic.lib.auth.BuildHeadersCallback
import com.sygic.lib.auth.ErrorCode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthBuildHeadersException(
    val errorCode: ErrorCode,
    message: String
): Exception(message)

suspend fun Auth.awaitHeaders(): Map<String, String> =
    suspendCancellableCoroutine { continuation ->
        buildHeaders(object: BuildHeadersCallback {
            override fun onSuccess(headers: Map<String, String>) {
                continuation.resume(headers)
            }

            override fun onError(error: ErrorCode, errorMessage: String) {
                continuation.resumeWithException(AuthBuildHeadersException(error, errorMessage))
            }

        })
    }