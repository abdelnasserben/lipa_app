package com.kori.app.data.repository.impl

import com.kori.app.app.KoriSession
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.auth.AuthState
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.repository.AuthService
import com.kori.app.data.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SessionRepositoryImpl(
    private val localStorage: LocalStorage,
    private val authService: AuthService,
) : SessionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _session = MutableStateFlow(buildSessionSnapshot())
    override val session: StateFlow<KoriSession> = _session.asStateFlow()

    init {
        scope.launch {
            authService.authState.collectLatest {
                _session.value = buildSessionSnapshot()
            }
        }
    }

    override fun selectRole(role: UserRole) {
        if (authService.isAuthenticated()) {
            _session.value = buildSessionSnapshot()
            return
        }

        localStorage.setActiveRole(role)
        _session.value = buildSessionSnapshot()
    }

    override fun switchRole(role: UserRole) {
        selectRole(role)
    }

    override fun clearRole() {
        if (authService.isAuthenticated()) {
            _session.value = buildSessionSnapshot()
            return
        }

        localStorage.setActiveRole(null)
        _session.value = buildSessionSnapshot()
    }

    private fun buildSessionSnapshot(): KoriSession {
        val authSession = (authService.authState.value as? AuthState.Authenticated)?.session
            ?: localStorage.getAuthSession()

        if (authSession != null) {
            localStorage.setActiveRole(authSession.userRole)
            return KoriSession(
                selectedRole = authSession.userRole,
                actorRef = authSession.actorRef,
                isRoleSelected = true,
                isAuthenticatedRole = true,
            )
        }

        val previewRole = localStorage.getActiveRole() ?: return KoriSession()
        return KoriSession(
            selectedRole = previewRole,
            isRoleSelected = true,
        )
    }
}
