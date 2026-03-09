package com.kori.app.data.datasource

import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.RoleProfilePayload

interface ProfileDataSource {
    suspend fun getProfile(role: UserRole): RoleProfilePayload
}
