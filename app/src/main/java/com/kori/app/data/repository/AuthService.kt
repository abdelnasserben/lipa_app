package com.kori.app.data.repository

import com.kori.app.core.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val authState: StateFlow<AuthState>

    fun beginAuthentication()

    suspend fun completeAuthenticationSuccess()

    fun failAuthentication(message: String)

    fun logout()
}