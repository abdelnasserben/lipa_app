package com.kori.app.data.mock

import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.ProfileRepository
import com.kori.app.data.repository.RoleProfilePayload
import kotlinx.coroutines.delay

class MockProfileRepository : ProfileRepository {

    override suspend fun getProfile(role: UserRole): RoleProfilePayload {
        delay(250)

        return when (role) {
            UserRole.CLIENT -> RoleProfilePayload.Client(
                MockDataFactory.clientDashboard().profile,
            )

            UserRole.MERCHANT -> RoleProfilePayload.Merchant(
                MockDataFactory.merchantDashboard().profile,
            )

            UserRole.AGENT -> RoleProfilePayload.Agent(
                MockDataFactory.agentDashboard().profile,
            )
        }
    }
}