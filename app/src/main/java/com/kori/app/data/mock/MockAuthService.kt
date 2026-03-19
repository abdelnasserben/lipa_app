package com.kori.app.data.mock

import android.app.Activity
import android.content.Intent
import com.kori.app.core.model.auth.AuthSession
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.repository.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class MockAuthService(
    private val localStorage: LocalStorage,
) : AuthService {

    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var expirationJob: Job? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        restorePersistedSession()
    }

    override fun startLogin(activity: Activity) {
        _authState.value = AuthState.Authenticating
    }

    override suspend fun handleAuthorizationResponse(intent: Intent) {
        _authState.value = AuthState.Authenticating
        delay(1200)

        val session = AuthSession(
            accessToken = "atk_${UUID.randomUUID()}_${UUID.randomUUID()}",
            expiresAtIso = Instant.now().plus(55, ChronoUnit.MINUTES).toString(),
            subject = "mock-user-kori",
            issuer = "https://mock.keycloak.kori.local/realms/kori",
        )

        localStorage.setAuthSession(session)
        _authState.value = AuthState.Authenticated(session)
        scheduleSessionExpiration(session)
    }

    override suspend fun getValidAccessToken(): String? {
        val session = (authState.value as? AuthState.Authenticated)?.session ?: return null
        val expiresAt = runCatching { Instant.parse(session.expiresAtIso) }.getOrNull() ?: return null

        return if (Instant.now().isBefore(expiresAt)) {
            session.accessToken
        } else {
            clearSession()
            null
        }
    }

    override fun isAuthenticated(): Boolean = authState.value is AuthState.Authenticated

    override fun logout(activity: Activity) {
        clearSession()
    }

    override fun clearSession() {
        expirationJob?.cancel()
        expirationJob = null
        localStorage.setAuthSession(null)
        _authState.value = AuthState.Unauthenticated
    }

    private fun restorePersistedSession() {
        val session = localStorage.getAuthSession() ?: return
        val expiresAt = runCatching { Instant.parse(session.expiresAtIso) }.getOrNull()

        if (expiresAt == null || !Instant.now().isBefore(expiresAt)) {
            clearSession()
            return
        }

        _authState.value = AuthState.Authenticated(session)
        scheduleSessionExpiration(session)
    }

    private fun scheduleSessionExpiration(session: AuthSession) {
        expirationJob?.cancel()
        val expiresAt = runCatching { Instant.parse(session.expiresAtIso) }.getOrNull() ?: run {
            clearSession()
            return
        }
        val delayMillis = Duration.between(Instant.now(), expiresAt).toMillis()

        if (delayMillis <= 0L) {
            clearSession()
            return
        }

        expirationJob = sessionScope.launch {
            delay(delayMillis)
            clearSession()
        }
    }
}
