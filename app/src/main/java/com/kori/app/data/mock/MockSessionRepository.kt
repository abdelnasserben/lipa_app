package com.kori.app.data.mock

import com.kori.app.app.KoriSession
import com.kori.app.core.model.UserRole
import com.kori.app.data.local.LocalStorage
import com.kori.app.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockSessionRepository(
    private val localStorage: LocalStorage,
) : SessionRepository {

    private val _session = MutableStateFlow(
        localStorage.getActiveRole()?.let { role ->
            KoriSession(
                selectedRole = role,
                isRoleSelected = true,
            )
        } ?: KoriSession(),
    )
    override val session: StateFlow<KoriSession> = _session.asStateFlow()

    override fun selectRole(role: UserRole) {
        localStorage.setActiveRole(role)
        _session.value = KoriSession(
            selectedRole = role,
            isRoleSelected = true,
        )
    }

    override fun switchRole(role: UserRole) {
        localStorage.setActiveRole(role)
        _session.value = _session.value.copy(
            selectedRole = role,
            isRoleSelected = true,
        )
    }

    override fun clearRole() {
        localStorage.setActiveRole(null)
        _session.value = KoriSession()
    }
}
