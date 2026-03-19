package com.kori.app.data.datasource

import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthDataSource {
    val authState: StateFlow<AuthState>

    fun startLogin(activity: Activity)

    suspend fun handleAuthorizationResponse(intent: Intent)

    suspend fun ensureFreshAccessToken(): String?

    fun isAuthenticated(): Boolean

    fun logout(activity: Activity)

    fun clearSession()
}
