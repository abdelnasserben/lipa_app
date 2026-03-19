package com.kori.app.data.repository

import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val authState: StateFlow<AuthState>

    fun beginAuthentication(activity: Activity)

    suspend fun completeAuthenticationFromIntent(intent: Intent)

    suspend fun refreshSessionIfNeeded()

    fun logout()
}
