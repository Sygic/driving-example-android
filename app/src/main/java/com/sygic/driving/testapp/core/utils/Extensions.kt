package com.sygic.driving.testapp.core.utils

import kotlinx.coroutines.flow.SharingStarted

val <T> T.exhaustive: T
    get() = this

val WHILE_SUBSCRIBED_WITH_TIMEOUT = SharingStarted.WhileSubscribed(5000L)