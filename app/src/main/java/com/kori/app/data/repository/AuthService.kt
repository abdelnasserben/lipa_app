package com.kori.app.data.repository

import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val authState: StateFlow<AuthState>

    fun startLogin(activity: Activity)

    suspend fun handleAuthorizationResponse(intent: Intent)

    suspend fun getValidAccessToken(): String?

    fun isAuthenticated(): Boolean

    fun logout(activity: Activity)

    fun clearSession()
}
