package com.kori.app.core.model.auth

import com.kori.app.core.model.UserRole

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Authenticating : AuthState
    data class Authenticated(
        val session: AuthSession,
    ) : AuthState

    data class Error(
        val message: String,
    ) : AuthState
}

data class AuthSession(
    val accessToken: String,
    val expiresAtIso: String,
    val subject: String,
    val issuer: String,
    val userRole: UserRole,
    val actorRef: String,
)
