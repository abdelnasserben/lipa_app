package com.kori.app.domain

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse
import com.kori.app.data.repository.DashboardRepository

sealed interface DashboardPayload {
    data class Client(val value: ClientDashboardResponse) : DashboardPayload
    data class Merchant(val value: MerchantDashboardResponse) : DashboardPayload
    data class Agent(val value: AgentDashboardResponse) : DashboardPayload
}

class GetDashboardUseCase(
    private val repository: DashboardRepository,
) {
    suspend operator fun invoke(role: UserRole): DashboardPayload {
        return when (role) {
            UserRole.CLIENT -> DashboardPayload.Client(repository.getClientDashboard())
            UserRole.MERCHANT -> DashboardPayload.Merchant(repository.getMerchantDashboard())
            UserRole.AGENT -> DashboardPayload.Agent(repository.getAgentDashboard())
        }
    }
}