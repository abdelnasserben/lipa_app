package com.kori.app.app

import com.kori.app.core.model.UserRole

data class KoriSession(
    val selectedRole: UserRole? = null,
    val actorRef: String? = null,
    val isRoleSelected: Boolean = false,
    val isAuthenticatedRole: Boolean = false,
)
