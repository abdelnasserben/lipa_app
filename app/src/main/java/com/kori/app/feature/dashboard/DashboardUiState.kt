package com.kori.app.feature.dashboard

import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val message: String) : DashboardUiState
    data class Client(val data: ClientDashboardResponse) : DashboardUiState
    data class Merchant(val data: MerchantDashboardResponse) : DashboardUiState
    data class Agent(val data: AgentDashboardResponse) : DashboardUiState
}