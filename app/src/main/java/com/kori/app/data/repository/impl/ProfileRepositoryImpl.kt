package com.kori.app.data.repository.impl

import com.kori.app.core.model.UserRole
import com.kori.app.data.datasource.ProfileDataSource
import com.kori.app.data.repository.ProfileRepository
import com.kori.app.data.repository.RoleProfilePayload

class ProfileRepositoryImpl(
    private val dataSource: ProfileDataSource,
) : ProfileRepository {
    override suspend fun getProfile(role: UserRole): RoleProfilePayload = dataSource.getProfile(role)
}
