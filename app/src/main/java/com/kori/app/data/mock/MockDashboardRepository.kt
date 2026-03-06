package com.kori.app.data.mock

import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse
import com.kori.app.data.repository.DashboardRepository
import kotlinx.coroutines.delay

class MockDashboardRepository : DashboardRepository {

    override suspend fun getClientDashboard(): ClientDashboardResponse {
        delay(350)
        return MockDataFactory.clientDashboard()
    }

    override suspend fun getMerchantDashboard(): MerchantDashboardResponse {
        delay(350)
        return MockDataFactory.merchantDashboard()
    }

    override suspend fun getAgentDashboard(): AgentDashboardResponse {
        delay(350)
        return MockDataFactory.agentDashboard()
    }
}