package com.kori.app.app

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object OidcIntentBus {
    private val _intent = MutableStateFlow<Intent?>(null)
    val intent: StateFlow<Intent?> = _intent.asStateFlow()

    fun publish(intent: Intent) {
        _intent.value = intent
    }

    fun clear() {
        _intent.value = null
    }
}
