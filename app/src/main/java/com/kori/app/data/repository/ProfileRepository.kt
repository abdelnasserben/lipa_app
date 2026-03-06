package com.kori.app.data.repository

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.profile.AgentProfileResponse
import com.kori.app.core.model.profile.ClientProfileResponse
import com.kori.app.core.model.profile.MerchantProfileResponse

sealed interface RoleProfilePayload {
    data class Client(val value: ClientProfileResponse) : RoleProfilePayload
    data class Merchant(val value: MerchantProfileResponse) : RoleProfilePayload
    data class Agent(val value: AgentProfileResponse) : RoleProfilePayload
}

interface ProfileRepository {
    suspend fun getProfile(role: UserRole): RoleProfilePayload
}