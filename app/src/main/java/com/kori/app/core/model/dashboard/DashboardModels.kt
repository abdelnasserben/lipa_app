package com.kori.app.core.model.dashboard

import com.kori.app.core.model.balance.ActorBalanceResponse
import com.kori.app.core.model.profile.AgentProfileResponse
import com.kori.app.core.model.profile.ClientProfileResponse
import com.kori.app.core.model.profile.MerchantProfileResponse
import com.kori.app.core.model.transaction.TransactionItemResponse

data class AlertItem(
    val code: String,
    val message: String,
)

data class CardItem(
    val cardUid: String,
    val status: String,
    val createdAt: String,
)

data class Kpis7dResponse(
    val txCount: Long,
    val txVolume: Long,
    val failedCount: Long,
)

data class TerminalsSummaryResponse(
    val total: Long,
    val byStatus: Map<String, Int>,
    val staleTerminals: Long,
)

data class ActivityItem(
    val eventRef: String,
    val occurredAt: String,
    val action: String,
    val resourceType: String,
    val resourceRef: String,
    val metadata: Map<String, String> = emptyMap(),
)

data class ClientDashboardResponse(
    val profile: ClientProfileResponse,
    val balance: ActorBalanceResponse,
    val cards: List<CardItem>,
    val recentTransactions: List<TransactionItemResponse>,
    val alerts: List<AlertItem>,
)

data class MerchantDashboardResponse(
    val profile: MerchantProfileResponse,
    val balance: ActorBalanceResponse,
    val kpis7d: Kpis7dResponse,
    val recentTransactions: List<TransactionItemResponse>,
    val terminalsSummary: TerminalsSummaryResponse,
)

data class AgentDashboardResponse(
    val profile: AgentProfileResponse,
    val balance: ActorBalanceResponse,
    val kpis7d: Kpis7dResponse,
    val recentTransactions: List<TransactionItemResponse>,
    val recentActivities: List<ActivityItem>,
    val alerts: List<AlertItem>,
)