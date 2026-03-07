package com.kori.app.data.mock

import com.kori.app.core.model.auth.AuthSession
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockAuthService(
    private val localStorage: LocalStorage,
) : AuthService {

    private val _authState = MutableStateFlow<AuthState>(
        localStorage.getAuthSession()?.let { session ->
            AuthState.Authenticated(session)
        } ?: AuthState.Unauthenticated,
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override fun beginAuthentication() {
        _authState.value = AuthState.Authenticating
    }

    override suspend fun completeAuthenticationSuccess() {
        _authState.value = AuthState.Authenticating
        delay(1200)

        val session = AuthSession(
            accessToken = "atk_${UUID.randomUUID()}_${UUID.randomUUID()}",
            refreshToken = "rtk_${UUID.randomUUID()}_${UUID.randomUUID()}",
            expiresAtIso = Instant.now().plus(55, ChronoUnit.MINUTES).toString(),
            subject = "mock-user-kori",
            issuer = "https://mock.keycloak.kori.local/realms/kori",
        )

        localStorage.setAuthSession(session)
        _authState.value = AuthState.Authenticated(session)
    }

    override fun failAuthentication(message: String) {
        _authState.value = AuthState.Error(message)
    }

    override fun logout() {
        localStorage.setAuthSession(null)
        _authState.value = AuthState.Unauthenticated
    }
}
