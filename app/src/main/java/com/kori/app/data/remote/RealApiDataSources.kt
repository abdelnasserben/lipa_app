package com.kori.app.data.remote

import com.kori.app.core.model.UserRole
import com.kori.app.core.model.action.AgentCardAddResult
import com.kori.app.core.model.action.AgentCardEnrollResult
import com.kori.app.core.model.action.AgentCardStatusUpdateResult
import com.kori.app.core.model.action.AgentCardTargetStatus
import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInReceipt
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawReceipt
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferReceipt
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferReceipt
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.core.model.common.CursorPage
import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.dashboard.AgentDashboardResponse
import com.kori.app.core.model.dashboard.ClientDashboardResponse
import com.kori.app.core.model.dashboard.MerchantDashboardResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.data.datasource.AgentActionDataSource
import com.kori.app.data.datasource.ClientTransferDataSource
import com.kori.app.data.datasource.DashboardDataSource
import com.kori.app.data.datasource.MerchantTransferDataSource
import com.kori.app.data.datasource.ProfileDataSource
import com.kori.app.data.datasource.TransactionDataSource
import com.kori.app.data.repository.RoleProfilePayload
import com.kori.app.data.repository.TransactionQuery
import org.json.JSONObject
import java.time.Instant

class RealProfileDataSource(
    private val apiHttpClient: ApiHttpClient,
) : ProfileDataSource {
    override suspend fun getProfile(role: UserRole): RoleProfilePayload {
        val response = apiHttpClient.get(path = role.profilePath)
        return response.toRoleProfile(role)
    }
}

class RealDashboardDataSource(
    private val apiHttpClient: ApiHttpClient,
) : DashboardDataSource {
    override suspend fun getClientDashboard(): ClientDashboardResponse {
        val dashboard = apiHttpClient.get(UserRole.CLIENT.dashboardPath).toClientDashboard()
        val balance = apiHttpClient.get(UserRole.CLIENT.balancePath).toBalance()
        return dashboard.copy(balance = balance)
    }

    override suspend fun getMerchantDashboard(): MerchantDashboardResponse {
        val dashboard = apiHttpClient.get(UserRole.MERCHANT.dashboardPath).toMerchantDashboard()
        val balance = apiHttpClient.get(UserRole.MERCHANT.balancePath).toBalance()
        return dashboard.copy(balance = balance)
    }

    override suspend fun getAgentDashboard(): AgentDashboardResponse {
        val dashboard = apiHttpClient.get(UserRole.AGENT.dashboardPath).toAgentDashboard()
        val balance = apiHttpClient.get(UserRole.AGENT.balancePath).toBalance()
        return dashboard.copy(balance = balance)
    }
}

class RealTransactionDataSource(
    private val apiHttpClient: ApiHttpClient,
) : TransactionDataSource {
    override suspend fun getClientTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        getTransactions(UserRole.CLIENT, query)

    override suspend fun getMerchantTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        getTransactions(UserRole.MERCHANT, query)

    override suspend fun getAgentTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        getTransactions(UserRole.AGENT, query)

    override suspend fun getClientTransactionDetail(transactionRef: String): TransactionItemResponse =
        getTransactionDetail(UserRole.CLIENT, transactionRef)

    override suspend fun getMerchantTransactionDetail(transactionRef: String): TransactionItemResponse =
        getTransactionDetail(UserRole.MERCHANT, transactionRef)

    override suspend fun getAgentTransactionDetail(transactionRef: String): TransactionItemResponse =
        getTransactionDetail(UserRole.AGENT, transactionRef)

    private suspend fun getTransactions(
        role: UserRole,
        query: TransactionQuery,
    ): CursorPagedResponse<TransactionItemResponse> {
        return runCatching {
            apiHttpClient.get(
                path = role.transactionsPath,
                query = query.toApiQuery(),
            ).toTransactionPage(role)
        }.recoverCatching {
            apiHttpClient.get(
                path = role.transactionsPath,
                query = query.toApiQuery(includeSort = false),
            ).toTransactionPage(role)
        }.getOrElse { throwable ->
            if (query.cursor != null) throw throwable
            fallbackToDashboardTransactions(role = role, query = query)
        }
    }

    private suspend fun fallbackToDashboardTransactions(
        role: UserRole,
        query: TransactionQuery,
    ): CursorPagedResponse<TransactionItemResponse> {
        val recentTransactions = when (role) {
            UserRole.CLIENT -> apiHttpClient.get(UserRole.CLIENT.dashboardPath).toClientDashboard().recentTransactions
            UserRole.MERCHANT -> apiHttpClient.get(UserRole.MERCHANT.dashboardPath).toMerchantDashboard().recentTransactions
            UserRole.AGENT -> apiHttpClient.get(UserRole.AGENT.dashboardPath).toAgentDashboard().recentTransactions
        }

        val filtered = recentTransactions
            .filter { item -> query.type == null || item.type == query.type }
            .filter { item -> query.status == null || item.status == query.status }
            .filter { item -> query.minAmount == null || item.amount >= query.minAmount }
            .filter { item -> query.maxAmount == null || item.amount <= query.maxAmount }
            .filter { item -> query.from == null || item.createdAt >= query.from }
            .filter { item -> query.to == null || item.createdAt <= query.to }
            .let { items ->
                when (query.sort) {
                    "createdAt" -> items.sortedBy { it.createdAt }
                    else -> items.sortedByDescending { it.createdAt }
                }
            }

        return CursorPagedResponse(
            items = filtered.take(query.limit),
            page = CursorPage(
                nextCursor = null,
                hasMore = false,
            ),
        )
    }

    private suspend fun getTransactionDetail(
        role: UserRole,
        transactionRef: String,
    ): TransactionItemResponse {
        return apiHttpClient.get(
            path = "${role.transactionsPath}/$transactionRef",
        ).toTransactionItem(role)
    }
}

class NetworkBackedClientTransferDataSource(
    private val apiHttpClient: ApiHttpClient,
) : ClientTransferDataSource {
    override suspend fun quoteTransfer(
        recipientPhoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): ClientTransferQuote {
        val fee = when {
            amount <= 5_000L -> 50L
            amount <= 20_000L -> 150L
            amount <= 50_000L -> 300L
            else -> 500L
        }
        return ClientTransferQuote(
            recipientPhoneNumber = recipientPhoneNumber,
            amount = amount,
            fee = fee,
            totalDebited = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitTransfer(quote: ClientTransferQuote): ClientTransferResult {
        return try {
            val response = apiHttpClient.post(
                path = "/api/v1/payments/client-transfer",
                idempotencyKey = quote.idempotencyKey,
                body = JSONObject()
                    .put("recipientPhoneNumber", quote.recipientPhoneNumber)
                    .put("amount", quote.amount),
            )
            ClientTransferResult.Success(
                receipt = ClientTransferReceipt(
                    transactionRef = response.optString("transactionId"),
                    recipientPhoneNumber = response.optString("recipientPhoneNumber").ifBlank { quote.recipientPhoneNumber },
                    amount = response.optLong("amount", quote.amount),
                    fee = response.optLong("fee", quote.fee),
                    totalDebited = response.optLong("totalDebited", quote.totalDebited),
                    createdAt = Instant.now().toString(),
                ),
            )
        } catch (exception: BackendApiBusinessException) {
            ClientTransferResult.Failure(
                code = mapFinancialErrorCode(exception.backendCode),
                message = exception.message,
                idempotencyKey = quote.idempotencyKey,
            )
        }
    }
}

class NetworkBackedMerchantTransferDataSource(
    private val apiHttpClient: ApiHttpClient,
) : MerchantTransferDataSource {
    override suspend fun quoteTransfer(
        recipientMerchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): MerchantTransferQuote {
        val fee = when {
            amount <= 50_000L -> 250L
            amount <= 150_000L -> 500L
            amount <= 300_000L -> 1_000L
            else -> 1_500L
        }
        return MerchantTransferQuote(
            recipientMerchantCode = recipientMerchantCode,
            amount = amount,
            fee = fee,
            totalDebited = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitTransfer(quote: MerchantTransferQuote): MerchantTransferResult {
        return try {
            val response = apiHttpClient.post(
                path = "/api/v1/payments/merchant-transfer",
                idempotencyKey = quote.idempotencyKey,
                body = JSONObject()
                    .put("recipientMerchantCode", quote.recipientMerchantCode)
                    .put("amount", quote.amount),
            )
            MerchantTransferResult.Success(
                receipt = MerchantTransferReceipt(
                    transactionRef = response.optString("transactionId"),
                    recipientMerchantCode = response.optString("recipientMerchantCode").ifBlank { quote.recipientMerchantCode },
                    amount = response.optLong("amount", quote.amount),
                    fee = response.optLong("fee", quote.fee),
                    totalDebited = response.optLong("totalDebited", quote.totalDebited),
                    createdAt = Instant.now().toString(),
                ),
            )
        } catch (exception: BackendApiBusinessException) {
            MerchantTransferResult.Failure(
                code = mapFinancialErrorCode(exception.backendCode),
                message = exception.message,
                idempotencyKey = quote.idempotencyKey,
            )
        }
    }
}

class NetworkBackedAgentActionDataSource(
    private val apiHttpClient: ApiHttpClient,
    private val fallback: AgentActionDataSource,
) : AgentActionDataSource {
    override suspend fun quoteCashIn(phoneNumber: String, amount: Long, idempotencyKey: String): AgentCashInQuote {
        return AgentCashInQuote(
            phoneNumber = phoneNumber,
            amount = amount,
            fee = 0L,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitCashIn(quote: AgentCashInQuote): AgentCashInResult {
        return try {
            val response = apiHttpClient.post(
                path = "/api/v1/payments/cash-in",
                idempotencyKey = quote.idempotencyKey,
                body = JSONObject()
                    .put("phoneNumber", quote.phoneNumber)
                    .put("amount", quote.amount),
            )
            AgentCashInResult.Success(
                receipt = AgentCashInReceipt(
                    transactionRef = response.optString("transactionId"),
                    clientPhoneNumber = response.optString("clientPhoneNumber").ifBlank { quote.phoneNumber },
                    amount = response.optLong("amount", quote.amount),
                    fee = quote.fee,
                    createdAt = Instant.now().toString(),
                ),
            )
        } catch (exception: BackendApiBusinessException) {
            AgentCashInResult.Failure(
                code = mapFinancialErrorCode(exception.backendCode),
                message = exception.message,
                idempotencyKey = quote.idempotencyKey,
            )
        }
    }

    override suspend fun quoteMerchantWithdraw(
        merchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentMerchantWithdrawQuote {
        val fee = when {
            amount <= 50_000L -> 250L
            amount <= 150_000L -> 500L
            amount <= 300_000L -> 1_000L
            else -> 1_500L
        }
        val commission = when {
            amount <= 50_000L -> 100L
            amount <= 150_000L -> 250L
            amount <= 300_000L -> 500L
            else -> 750L
        }
        return AgentMerchantWithdrawQuote(
            merchantCode = merchantCode,
            amount = amount,
            fee = fee,
            commission = commission,
            totalDebitedMerchant = amount + fee,
            idempotencyKey = idempotencyKey,
        )
    }

    override suspend fun submitMerchantWithdraw(quote: AgentMerchantWithdrawQuote): AgentMerchantWithdrawResult {
        return try {
            val response = apiHttpClient.post(
                path = "/api/v1/payments/merchant-withdraw",
                idempotencyKey = quote.idempotencyKey,
                body = JSONObject()
                    .put("merchantCode", quote.merchantCode)
                    .put("amount", quote.amount),
            )
            AgentMerchantWithdrawResult.Success(
                receipt = AgentMerchantWithdrawReceipt(
                    transactionRef = response.optString("transactionId"),
                    merchantCode = response.optString("merchantCode").ifBlank { quote.merchantCode },
                    amount = response.optLong("amount", quote.amount),
                    fee = response.optLong("fee", quote.fee),
                    commission = response.optLong("commission", quote.commission),
                    totalDebitedMerchant = response.optLong("totalDebitedMerchant", quote.totalDebitedMerchant),
                    createdAt = Instant.now().toString(),
                ),
            )
        } catch (exception: BackendApiBusinessException) {
            AgentMerchantWithdrawResult.Failure(
                code = mapFinancialErrorCode(exception.backendCode),
                message = exception.message,
                idempotencyKey = quote.idempotencyKey,
            )
        }
    }

    override suspend fun enrollCard(
        phoneNumber: String?,
        displayName: String,
        cardUid: String,
        pin: String,
    ): AgentCardEnrollResult = fallback.enrollCard(phoneNumber, displayName, cardUid, pin)

    override suspend fun addCardToClient(phoneNumber: String, cardUid: String, pin: String): AgentCardAddResult =
        fallback.addCardToClient(phoneNumber, cardUid, pin)

    override suspend fun updateCardStatusAsAgent(
        cardUid: String,
        targetStatus: AgentCardTargetStatus,
        reason: String?,
    ): AgentCardStatusUpdateResult = fallback.updateCardStatusAsAgent(cardUid, targetStatus, reason)
}

private fun TransactionQuery.toApiQuery(includeSort: Boolean = true): Map<String, String?> {
    return mapOf(
        "type" to type?.name,
        "status" to status?.name,
        "from" to from,
        "to" to to,
        "min" to minAmount?.toString(),
        "max" to maxAmount?.toString(),
        "sort" to sort.takeIf { includeSort },
        "cursor" to cursor,
        "limit" to limit.toString(),
    )
}

private val UserRole.profilePath: String
    get() = when (this) {
        UserRole.CLIENT -> "/api/v1/client/me/profile"
        UserRole.MERCHANT -> "/api/v1/merchant/me/profile"
        UserRole.AGENT -> "/api/v1/agent/me/profile"
    }

private val UserRole.dashboardPath: String
    get() = when (this) {
        UserRole.CLIENT -> "/api/v1/client/me/dashboard"
        UserRole.MERCHANT -> "/api/v1/merchant/me/dashboard"
        UserRole.AGENT -> "/api/v1/agent/me/dashboard"
    }

private val UserRole.transactionsPath: String
    get() = when (this) {
        UserRole.CLIENT -> "/api/v1/client/me/transactions"
        UserRole.MERCHANT -> "/api/v1/merchant/me/transactions"
        UserRole.AGENT -> "/api/v1/agent/me/transactions"
    }

private val UserRole.balancePath: String
    get() = when (this) {
        UserRole.CLIENT -> "/api/v1/client/me/balance"
        UserRole.MERCHANT -> "/api/v1/merchant/me/balance"
        UserRole.AGENT -> "/api/v1/agent/me/balance"
    }

private fun mapFinancialErrorCode(rawCode: String): FinancialErrorCode {
    return when (rawCode.uppercase()) {
        "INSUFFICIENT_FUNDS" -> FinancialErrorCode.INSUFFICIENT_FUNDS
        "DAILY_LIMIT_EXCEEDED", "MAX_TRANSACTION_EXCEEDED" -> FinancialErrorCode.DAILY_LIMIT_EXCEEDED
        "UNAUTHORIZED", "FORBIDDEN" -> FinancialErrorCode.UNAUTHORIZED
        else -> FinancialErrorCode.INVALID_STATUS
    }
}
