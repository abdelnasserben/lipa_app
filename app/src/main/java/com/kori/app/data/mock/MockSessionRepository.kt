package com.kori.app.data.mock

import com.kori.app.app.KoriSession
import com.kori.app.core.model.UserRole
import com.kori.app.data.local.LocalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockSessionRepository(
    private val localStorage: LocalStorage,
) {

    private val _session = MutableStateFlow(
        localStorage.getActiveRole()?.let { role ->
            KoriSession(
                selectedRole = role,
                isRoleSelected = true,
            )
        } ?: KoriSession(),
    )
    val session: StateFlow<KoriSession> = _session.asStateFlow()

    fun selectRole(role: UserRole) {
        localStorage.setActiveRole(role)
        _session.value = KoriSession(
            selectedRole = role,
            isRoleSelected = true,
        )
    }

    fun switchRole(role: UserRole) {
        localStorage.setActiveRole(role)
        _session.value = _session.value.copy(
            selectedRole = role,
            isRoleSelected = true,
        )
    }

    fun clearRole() {
        localStorage.setActiveRole(null)
        _session.value = KoriSession()
    }
}
