package com.kori.app.app

import androidx.compose.runtime.Stable
import com.kori.app.core.model.UserRole
import com.kori.app.data.mock.MockSessionRepository
import kotlinx.coroutines.flow.StateFlow

@Stable
class KoriAppState(
    val sessionRepository: MockSessionRepository,
) {
    val session: StateFlow<KoriSession> = sessionRepository.session

    fun selectRole(role: UserRole) {
        sessionRepository.selectRole(role)
    }

    fun switchRole(role: UserRole) {
        sessionRepository.switchRole(role)
    }

    fun logoutToRolePicker() {
        sessionRepository.clearRole()
    }
}