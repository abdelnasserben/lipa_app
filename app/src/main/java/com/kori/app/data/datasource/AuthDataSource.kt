package com.kori.app.data.datasource

import com.kori.app.core.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthDataSource {
    val authState: StateFlow<AuthState>

    fun beginAuthentication()

    suspend fun completeAuthenticationSuccess()

    fun failAuthentication(message: String)

    fun logout()
}
