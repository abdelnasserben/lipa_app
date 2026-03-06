package com.kori.app.app

import com.kori.app.core.model.UserRole

data class KoriSession(
    val selectedRole: UserRole? = null,
    val isRoleSelected: Boolean = false,
)