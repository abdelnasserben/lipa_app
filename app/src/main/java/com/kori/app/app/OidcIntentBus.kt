package com.kori.app.app

import android.content.Intent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object OidcIntentBus {
    private val _intents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val intents: SharedFlow<Intent> = _intents.asSharedFlow()

    fun publish(intent: Intent) {
        _intents.tryEmit(intent)
    }
}
