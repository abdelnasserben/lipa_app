package com.kori.app.data.repository

import com.kori.app.app.KoriSession
import com.kori.app.core.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val session: StateFlow<KoriSession>

    fun selectRole(role: UserRole)

    fun switchRole(role: UserRole)

    fun clearRole()
}
