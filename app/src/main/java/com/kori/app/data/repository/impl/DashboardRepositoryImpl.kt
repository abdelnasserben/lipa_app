package com.kori.app.data.repository.impl

import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse
import com.kori.app.data.datasource.DashboardDataSource
import com.kori.app.data.repository.DashboardRepository

class DashboardRepositoryImpl(
    private val dataSource: DashboardDataSource,
) : DashboardRepository {
    override suspend fun getClientDashboard(): ClientDashboardResponse = dataSource.getClientDashboard()

    override suspend fun getMerchantDashboard(): MerchantDashboardResponse = dataSource.getMerchantDashboard()

    override suspend fun getAgentDashboard(): AgentDashboardResponse = dataSource.getAgentDashboard()
}
