package com.kori.app.data.remote

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.balance.ActorBalanceResponse
import com.kori.app.core.model.balance.BalanceItemResponse
import com.kori.app.core.model.common.ActorStatus
import com.kori.app.core.model.common.BalanceKind
import com.kori.app.core.model.common.CurrencyCode
import com.kori.app.core.model.common.CursorPage
import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.dashboard.ActivityItem
import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.AlertItem
import com.kori.app.core.model.dashboard.CardItem
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.Kpis7dResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse
import com.kori.app.core.model.dashboard.TerminalsSummaryResponse
import com.kori.app.core.model.profile.AgentProfileResponse
import com.kori.app.core.model.profile.ClientProfileResponse
import com.kori.app.core.model.profile.MerchantProfileResponse
import com.kori.app.core.model.transaction.TransactionCounterparty
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType
import com.kori.app.data.repository.RoleProfilePayload
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.toTransactionPage(role: UserRole): CursorPagedResponse<TransactionItemResponse> {
    val items = optJSONArray("items").toTransactionItems(role)
    val pageJson = optJSONObject("page")
    return CursorPagedResponse(
        items = items,
        page = CursorPage(
            nextCursor = pageJson?.optString("nextCursor")?.ifBlank { null },
            hasMore = pageJson?.optBoolean("hasMore") == true,
        ),
    )
}

internal fun JSONObject.toTransactionItem(role: UserRole): TransactionItemResponse {
    val type = optEnum("type", TransactionType.CLIENT_TRANSFER)
    val clientCode = optStringOrNull("clientCode")
    val merchantCode = optStringOrNull("merchantCode")
    val agentCode = optStringOrNull("agentCode")
    val terminalUid = optStringOrNull("terminalUid")
    val originalTransactionRef = optStringOrNull("originalTransactionRef")
    val recipientPhoneNumber = optStringOrNull("recipientPhoneNumber")
    val phoneNumber = optStringOrNull("phoneNumber")
    val counterpartyDisplay = optStringOrNull("counterpartyDisplayName")
        ?: optStringOrNull("displayName")
        ?: merchantCode
        ?: agentCode
        ?: clientCode
        ?: terminalUid
        ?: recipientPhoneNumber
        ?: phoneNumber
        ?: originalTransactionRef
        ?: type.name.replace('_', ' ')

    return TransactionItemResponse(
        transactionRef = optString("transactionRef").ifBlank { optString("transactionId") },
        type = type,
        status = optEnum("status", TransactionStatus.PENDING),
        amount = optLongSafely("amount"),
        fee = optLongSafelyOrNull("fee"),
        totalDebited = optLongSafelyOrNull("totalDebited"),
        currency = optEnum("currency", CurrencyCode.KMF),
        createdAt = optString("createdAt").ifBlank { optString("timestamp") },
        counterparty = TransactionCounterparty(
            displayName = counterpartyDisplay,
            phone = recipientPhoneNumber ?: phoneNumber,
            code = when (role) {
                UserRole.CLIENT -> merchantCode ?: agentCode ?: clientCode
                UserRole.MERCHANT -> clientCode ?: agentCode ?: merchantCode
                UserRole.AGENT -> clientCode ?: merchantCode ?: agentCode
            },
        ),
    )
}

internal fun JSONObject.toRoleProfile(role: UserRole): RoleProfilePayload {
    return when (role) {
        UserRole.CLIENT -> RoleProfilePayload.Client(toClientProfile())
        UserRole.MERCHANT -> RoleProfilePayload.Merchant(toMerchantProfile())
        UserRole.AGENT -> RoleProfilePayload.Agent(toAgentProfile())
    }
}

internal fun JSONObject.toClientProfile(): ClientProfileResponse {
    return ClientProfileResponse(
        code = optString("code"),
        displayName = optStringOrNull("displayName") ?: optString("code"),
        phone = optStringOrNull("phone") ?: "—",
        status = optEnum("status", ActorStatus.ACTIVE),
        createdAt = optString("createdAt"),
    )
}

internal fun JSONObject.toMerchantProfile(): MerchantProfileResponse {
    return MerchantProfileResponse(
        code = optString("code"),
        displayName = optStringOrNull("displayName") ?: optString("code"),
        status = optEnum("status", ActorStatus.ACTIVE),
        createdAt = optString("createdAt"),
    )
}

internal fun JSONObject.toAgentProfile(): AgentProfileResponse {
    return AgentProfileResponse(
        code = optString("code"),
        displayName = optStringOrNull("displayName") ?: optString("code"),
        status = optEnum("status", ActorStatus.ACTIVE),
        createdAt = optString("createdAt"),
    )
}

internal fun JSONObject.toBalance(): ActorBalanceResponse {
    return ActorBalanceResponse(
        ownerRef = optString("ownerRef"),
        currency = optEnum("currency", CurrencyCode.KMF),
        balances = optJSONArray("balances").toBalanceItems(),
    )
}

internal fun JSONObject.toClientDashboard(): ClientDashboardResponse {
    return ClientDashboardResponse(
        profile = optJSONObject("profile")?.toClientProfile() ?: JSONObject().toClientProfile(),
        balance = optJSONObject("balance")?.toBalance() ?: JSONObject().toBalance(),
        cards = optJSONArray("cards").toCards(),
        recentTransactions = optJSONArray("recentTransactions").toTransactionItems(UserRole.CLIENT),
        alerts = optJSONArray("alerts").toAlerts(),
    )
}

internal fun JSONObject.toMerchantDashboard(): MerchantDashboardResponse {
    return MerchantDashboardResponse(
        profile = optJSONObject("profile")?.toMerchantProfile() ?: JSONObject().toMerchantProfile(),
        balance = optJSONObject("balance")?.toBalance() ?: JSONObject().toBalance(),
        kpis7d = optJSONObject("kpis7d")?.toKpis7d() ?: Kpis7dResponse(0, 0, 0),
        recentTransactions = optJSONArray("recentTransactions").toTransactionItems(UserRole.MERCHANT),
        terminalsSummary = optJSONObject("terminalsSummary")?.toTerminalsSummary()
            ?: TerminalsSummaryResponse(0, emptyMap(), 0),
    )
}

internal fun JSONObject.toAgentDashboard(): AgentDashboardResponse {
    return AgentDashboardResponse(
        profile = optJSONObject("profile")?.toAgentProfile() ?: JSONObject().toAgentProfile(),
        balance = optJSONObject("balance")?.toBalance() ?: JSONObject().toBalance(),
        kpis7d = optJSONObject("kpis7d")?.toKpis7d() ?: Kpis7dResponse(0, 0, 0),
        recentTransactions = optJSONArray("recentTransactions").toTransactionItems(UserRole.AGENT),
        recentActivities = optJSONArray("recentActivities").toActivities(),
        alerts = optJSONArray("alerts").toAlerts(),
    )
}

private fun JSONArray?.toTransactionItems(role: UserRole): List<TransactionItemResponse> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(item.toTransactionItem(role))
        }
    }
}

private fun JSONArray?.toCards(): List<CardItem> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                CardItem(
                    cardUid = item.optString("cardUid"),
                    status = item.optString("status"),
                    createdAt = item.optString("createdAt"),
                ),
            )
        }
    }
}

private fun JSONArray?.toAlerts(): List<AlertItem> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(AlertItem(code = item.optString("code"), message = item.optString("message")))
        }
    }
}

private fun JSONArray?.toActivities(): List<ActivityItem> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                ActivityItem(
                    eventRef = item.optString("eventRef"),
                    occurredAt = item.optString("occurredAt"),
                    action = item.optString("action"),
                    resourceType = item.optString("resourceType"),
                    resourceRef = item.optString("resourceRef"),
                    metadata = item.optJSONObject("metadata").toStringMap(),
                ),
            )
        }
    }
}

private fun JSONObject.toKpis7d(): Kpis7dResponse {
    return Kpis7dResponse(
        txCount = optLongSafely("txCount"),
        txVolume = optLongSafely("txVolume"),
        failedCount = optLongSafely("failedCount"),
    )
}

private fun JSONObject.toTerminalsSummary(): TerminalsSummaryResponse {
    val byStatusJson = optJSONObject("byStatus")
    val byStatus = buildMap<String, Int> {
        if (byStatusJson != null) {
            val keys = byStatusJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                put(key, byStatusJson.optInt(key))
            }
        }
    }

    return TerminalsSummaryResponse(
        total = optLongSafely("total"),
        byStatus = byStatus,
        staleTerminals = optLongSafely("staleTerminals"),
    )
}

private fun JSONArray?.toBalanceItems(): List<BalanceItemResponse> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                BalanceItemResponse(
                    kind = item.optEnum("kind", BalanceKind.MAIN),
                    amount = item.optLongSafely("amount"),
                ),
            )
        }
    }
}

private fun JSONObject?.toStringMap(): Map<String, String> {
    if (this == null) return emptyMap()
    return buildMap {
        val keys = keys()
        while (keys.hasNext()) {
            val key = keys.next()
            put(key, opt(key)?.toString().orEmpty())
        }
    }
}

private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T {
    val raw = optStringOrNull(key) ?: return fallback
    return enumValues<T>().firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: fallback
}

private fun JSONObject.optStringOrNull(key: String): String? {
    return optString(key).ifBlank { null }
}

private fun JSONObject.optLongSafely(key: String): Long = optLongSafelyOrNull(key) ?: 0L

private fun JSONObject.optLongSafelyOrNull(key: String): Long? {
    val value = opt(key) ?: return null
    return when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}
