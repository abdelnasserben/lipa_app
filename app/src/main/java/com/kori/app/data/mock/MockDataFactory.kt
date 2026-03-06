package com.kori.app.data.mock

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
import java.time.Instant
import java.time.temporal.ChronoUnit

object MockDataFactory {

    private const val CLIENT_CODE = "CLI-0001"
    private const val MERCHANT_CODE = "MER-0042"
    private const val AGENT_CODE = "AGT-0010"

    fun clientDashboard(): ClientDashboardResponse {
        return ClientDashboardResponse(
            profile = ClientProfileResponse(
                code = CLIENT_CODE,
                displayName = "Amina Soilihi",
                phone = "+269 333 12 34",
                status = ActorStatus.ACTIVE,
                createdAt = isoDaysAgo(180),
            ),
            balance = ActorBalanceResponse(
                ownerRef = CLIENT_CODE,
                currency = CurrencyCode.KMF,
                balances = listOf(
                    BalanceItemResponse(BalanceKind.MAIN, 245_800L),
                ),
            ),
            cards = listOf(
                CardItem(
                    cardUid = "CARD-001",
                    status = "ACTIVE",
                    createdAt = isoDaysAgo(120),
                ),
                CardItem(
                    cardUid = "CARD-002",
                    status = "ACTIVE",
                    createdAt = isoDaysAgo(45),
                ),
            ),
            recentTransactions = clientTransactions().take(10),
            alerts = listOf(
                AlertItem(
                    code = "LOW_BALANCE_TIP",
                    message = "Gardez un solde suffisant pour vos transferts quotidiens.",
                ),
                AlertItem(
                    code = "CARD_SECURITY",
                    message = "Votre carte principale est active et prête à être utilisée.",
                ),
            ),
        )
    }

    fun merchantDashboard(): MerchantDashboardResponse {
        return MerchantDashboardResponse(
            profile = MerchantProfileResponse(
                code = MERCHANT_CODE,
                displayName = "Boutique Mbeni Express",
                status = ActorStatus.ACTIVE,
                createdAt = isoDaysAgo(420),
            ),
            balance = ActorBalanceResponse(
                ownerRef = MERCHANT_CODE,
                currency = CurrencyCode.KMF,
                balances = listOf(
                    BalanceItemResponse(BalanceKind.MAIN, 1_860_500L),
                ),
            ),
            kpis7d = Kpis7dResponse(
                txCount = 148,
                txVolume = 4_920_000L,
                failedCount = 3,
            ),
            recentTransactions = merchantTransactions().take(10),
            terminalsSummary = TerminalsSummaryResponse(
                total = 4,
                byStatus = mapOf(
                    "ONLINE" to 3,
                    "OFFLINE" to 1,
                ),
                staleTerminals = 1,
            ),
        )
    }

    fun agentDashboard(): AgentDashboardResponse {
        return AgentDashboardResponse(
            profile = AgentProfileResponse(
                code = AGENT_CODE,
                displayName = "Youssouf Agency Centre",
                status = ActorStatus.ACTIVE,
                createdAt = isoDaysAgo(540),
            ),
            balance = ActorBalanceResponse(
                ownerRef = AGENT_CODE,
                currency = CurrencyCode.KMF,
                balances = listOf(
                    BalanceItemResponse(BalanceKind.CASH, 780_000L),
                    BalanceItemResponse(BalanceKind.COMMISSION, 63_400L),
                    BalanceItemResponse(BalanceKind.MAIN, 120_000L),
                ),
            ),
            kpis7d = Kpis7dResponse(
                txCount = 89,
                txVolume = 7_350_000L,
                failedCount = 2,
            ),
            recentTransactions = agentTransactions().take(10),
            recentActivities = listOf(
                ActivityItem(
                    eventRef = "ACT-1001",
                    occurredAt = isoHoursAgo(2),
                    action = "CASH_IN_CONFIRMED",
                    resourceType = "TRANSACTION",
                    resourceRef = "TX-CASH-1001",
                    metadata = mapOf("clientPhone" to "+269 355 10 20"),
                ),
                ActivityItem(
                    eventRef = "ACT-1002",
                    occurredAt = isoHoursAgo(5),
                    action = "MERCHANT_WITHDRAW_CONFIRMED",
                    resourceType = "TRANSACTION",
                    resourceRef = "TX-MWD-1002",
                    metadata = mapOf("merchantCode" to "MER-0061"),
                ),
            ),
            alerts = listOf(
                AlertItem(
                    code = "COMMISSION_READY",
                    message = "Votre commission disponible a été mise à jour.",
                ),
                AlertItem(
                    code = "CASH_LEVEL_OK",
                    message = "Votre niveau de cash est suffisant pour les opérations du jour.",
                ),
            ),
        )
    }

    fun clientTransactions(): List<TransactionItemResponse> {
        return listOf(
            TransactionItemResponse(
                transactionRef = "TX-CL-1001",
                type = TransactionType.CLIENT_TRANSFER,
                status = TransactionStatus.COMPLETED,
                amount = 15_000L,
                fee = 150L,
                totalDebited = 15_150L,
                createdAt = isoHoursAgo(3),
                counterparty = TransactionCounterparty(
                    displayName = "Salma Ahmed",
                    phone = "+269 355 99 88",
                    code = "CLI-0091",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-CL-1002",
                type = TransactionType.CARD_PAYMENT,
                status = TransactionStatus.COMPLETED,
                amount = 8_500L,
                fee = 0L,
                totalDebited = 8_500L,
                createdAt = isoHoursAgo(10),
                counterparty = TransactionCounterparty(
                    displayName = "Marché Volovolo",
                    code = "MER-0015",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-CL-1003",
                type = TransactionType.CASH_IN,
                status = TransactionStatus.COMPLETED,
                amount = 50_000L,
                fee = 0L,
                totalDebited = 0L,
                createdAt = isoDaysAgo(1),
                counterparty = TransactionCounterparty(
                    displayName = "Agent Mutsamudu",
                    code = "AGT-0020",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-CL-1004",
                type = TransactionType.REVERSAL,
                status = TransactionStatus.REVERSED,
                amount = 4_000L,
                fee = 0L,
                totalDebited = 0L,
                createdAt = isoDaysAgo(2),
                counterparty = TransactionCounterparty(
                    displayName = "Boutique Rahma",
                    code = "MER-0040",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-CL-1005",
                type = TransactionType.CLIENT_TRANSFER,
                status = TransactionStatus.FAILED,
                amount = 120_000L,
                fee = 0L,
                totalDebited = 0L,
                createdAt = isoDaysAgo(4),
                counterparty = TransactionCounterparty(
                    displayName = "Ibrahim Ali",
                    phone = "+269 322 00 11",
                    code = "CLI-0077",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-CL-1006",
                type = TransactionType.CARD_PAYMENT,
                status = TransactionStatus.COMPLETED,
                amount = 2_750L,
                fee = 0L,
                totalDebited = 2_750L,
                createdAt = isoDaysAgo(5),
                counterparty = TransactionCounterparty(
                    displayName = "KORI Shop",
                    code = "MER-0100",
                ),
            ),
        )
    }

    fun merchantTransactions(): List<TransactionItemResponse> {
        return listOf(
            TransactionItemResponse(
                transactionRef = "TX-MR-2001",
                type = TransactionType.CARD_PAYMENT,
                status = TransactionStatus.COMPLETED,
                amount = 75_000L,
                fee = 750L,
                totalDebited = null,
                createdAt = isoHoursAgo(1),
                counterparty = TransactionCounterparty(
                    displayName = "Client Wallet",
                    code = "CLI-0142",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-MR-2002",
                type = TransactionType.MERCHANT_TRANSFER,
                status = TransactionStatus.COMPLETED,
                amount = 250_000L,
                fee = 1_250L,
                totalDebited = 251_250L,
                createdAt = isoHoursAgo(7),
                counterparty = TransactionCounterparty(
                    displayName = "Superette Medina",
                    code = "MER-0088",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-MR-2003",
                type = TransactionType.MERCHANT_WITHDRAW,
                status = TransactionStatus.COMPLETED,
                amount = 300_000L,
                fee = 1_500L,
                totalDebited = 301_500L,
                createdAt = isoDaysAgo(1),
                counterparty = TransactionCounterparty(
                    displayName = "Agent Moroni Nord",
                    code = "AGT-0008",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-MR-2004",
                type = TransactionType.CARD_PAYMENT,
                status = TransactionStatus.PENDING,
                amount = 18_000L,
                fee = 180L,
                totalDebited = null,
                createdAt = isoDaysAgo(2),
                counterparty = TransactionCounterparty(
                    displayName = "Client Wallet",
                    code = "CLI-0220",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-MR-2005",
                type = TransactionType.REVERSAL,
                status = TransactionStatus.REVERSED,
                amount = 6_500L,
                fee = 0L,
                totalDebited = 0L,
                createdAt = isoDaysAgo(3),
                counterparty = TransactionCounterparty(
                    displayName = "Terminal #03",
                    code = "TERM-003",
                ),
            ),
        )
    }

    fun agentTransactions(): List<TransactionItemResponse> {
        return listOf(
            TransactionItemResponse(
                transactionRef = "TX-AG-3001",
                type = TransactionType.CASH_IN,
                status = TransactionStatus.COMPLETED,
                amount = 120_000L,
                fee = 600L,
                totalDebited = 120_600L,
                createdAt = isoHoursAgo(2),
                counterparty = TransactionCounterparty(
                    displayName = "Mariam Abdou",
                    phone = "+269 355 10 20",
                    code = "CLI-0301",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-AG-3002",
                type = TransactionType.MERCHANT_WITHDRAW,
                status = TransactionStatus.COMPLETED,
                amount = 210_000L,
                fee = 1_050L,
                totalDebited = 211_050L,
                createdAt = isoHoursAgo(6),
                counterparty = TransactionCounterparty(
                    displayName = "Boulangerie Salim",
                    code = "MER-0061",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-AG-3003",
                type = TransactionType.CASH_IN,
                status = TransactionStatus.FAILED,
                amount = 500_000L,
                fee = 0L,
                totalDebited = 0L,
                createdAt = isoDaysAgo(1),
                counterparty = TransactionCounterparty(
                    displayName = "Ali Madi",
                    phone = "+269 399 80 10",
                    code = "CLI-0408",
                ),
            ),
            TransactionItemResponse(
                transactionRef = "TX-AG-3004",
                type = TransactionType.AGENT_BANK_DEPOSIT,
                status = TransactionStatus.COMPLETED,
                amount = 1_000_000L,
                fee = 0L,
                totalDebited = 1_000_000L,
                createdAt = isoDaysAgo(3),
                counterparty = TransactionCounterparty(
                    displayName = "Banque partenaire",
                    code = "BANK-001",
                ),
            ),
        )
    }

    fun pagedTransactions(
        source: List<TransactionItemResponse>,
        cursor: String?,
        limit: Int,
    ): CursorPagedResponse<TransactionItemResponse> {
        val safeLimit = limit.coerceIn(1, 50)
        val startIndex = cursor?.toIntOrNull() ?: 0
        val endIndex = (startIndex + safeLimit).coerceAtMost(source.size)
        val pageItems = source.subList(startIndex, endIndex)
        val hasMore = endIndex < source.size
        val nextCursor = if (hasMore) endIndex.toString() else null

        return CursorPagedResponse(
            items = pageItems,
            page = CursorPage(
                nextCursor = nextCursor,
                hasMore = hasMore,
            ),
        )
    }

    private fun isoDaysAgo(days: Long): String {
        return Instant.now().minus(days, ChronoUnit.DAYS).toString()
    }

    private fun isoHoursAgo(hours: Long): String {
        return Instant.now().minus(hours, ChronoUnit.HOURS).toString()
    }
}