package com.sygic.driving.testapp.core.utils

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * SingleEvent creates a [Channel] where values can be emitted via [emit] function. Events can be
 * collected via [flow]. Every emitted value will be collected exactly once. SingleEvent is suitable
 * for one-shot events from ViewModel to UI layer (e.g. show toast, navigation events, etc).
 *
 * There should never be more that 1 collector of [flow]!
 */
class SingleEvent<T> {

    private val channel = Channel<T>()

    val flow
        get() = channel.receiveAsFlow()

    suspend fun emit(value: T) {
        channel.send(value)
    }
}