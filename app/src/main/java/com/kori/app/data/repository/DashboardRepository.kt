package com.kori.app.data.repository

import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse

interface DashboardRepository {
    suspend fun getClientDashboard(): ClientDashboardResponse
    suspend fun getMerchantDashboard(): MerchantDashboardResponse
    suspend fun getAgentDashboard(): AgentDashboardResponse
}