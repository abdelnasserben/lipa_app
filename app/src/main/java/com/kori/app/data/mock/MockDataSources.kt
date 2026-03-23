package com.kori.app.data.mock

import com.kori.app.core.model.common.CursorPagedResponse
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.data.datasource.AgentActionDataSource
import com.kori.app.data.datasource.ClientTransferDataSource
import com.kori.app.data.datasource.DashboardDataSource
import com.kori.app.data.datasource.MerchantTransferDataSource
import com.kori.app.data.datasource.ProfileDataSource
import com.kori.app.data.datasource.TransactionDataSource
import com.kori.app.data.repository.RoleProfilePayload
import com.kori.app.data.repository.TransactionQuery

class MockProfileDataSource(
    private val repository: MockProfileRepository,
) : ProfileDataSource {
    override suspend fun getProfile(role: com.kori.app.core.model.UserRole): RoleProfilePayload = repository.getProfile(role)
}

class MockDashboardDataSource(
    private val repository: MockDashboardRepository,
) : DashboardDataSource {
    override suspend fun getClientDashboard() = repository.getClientDashboard()
    override suspend fun getMerchantDashboard() = repository.getMerchantDashboard()
    override suspend fun getAgentDashboard() = repository.getAgentDashboard()
}

class MockTransactionDataSource(
    private val repository: MockTransactionRepository,
) : TransactionDataSource {
    override suspend fun getClientTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        repository.getClientTransactions(query)

    override suspend fun getMerchantTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        repository.getMerchantTransactions(query)

    override suspend fun getAgentTransactions(query: TransactionQuery): CursorPagedResponse<TransactionItemResponse> =
        repository.getAgentTransactions(query)

    override suspend fun getClientTransactionDetail(transactionRef: String): TransactionItemResponse =
        repository.getClientTransactionDetail(transactionRef)

    override suspend fun getMerchantTransactionDetail(transactionRef: String): TransactionItemResponse =
        repository.getMerchantTransactionDetail(transactionRef)

    override suspend fun getAgentTransactionDetail(transactionRef: String): TransactionItemResponse =
        repository.getAgentTransactionDetail(transactionRef)
}

class MockClientTransferDataSource(
    private val repository: MockClientTransferRepository,
) : ClientTransferDataSource {
    override suspend fun quoteTransfer(recipientPhoneNumber: String, amount: Long, idempotencyKey: String) =
        repository.quoteTransfer(recipientPhoneNumber, amount, idempotencyKey)

    override suspend fun submitTransfer(quote: com.kori.app.core.model.action.ClientTransferQuote) =
        repository.submitTransfer(quote)
}

class MockMerchantTransferDataSource(
    private val repository: MockMerchantTransferRepository,
) : MerchantTransferDataSource {
    override suspend fun quoteTransfer(recipientMerchantCode: String, amount: Long, idempotencyKey: String) =
        repository.quoteTransfer(recipientMerchantCode, amount, idempotencyKey)

    override suspend fun submitTransfer(quote: com.kori.app.core.model.action.MerchantTransferQuote) =
        repository.submitTransfer(quote)
}

class MockAgentActionDataSource(
    private val repository: MockAgentActionRepository,
) : AgentActionDataSource {
    override suspend fun quoteCashIn(phoneNumber: String, amount: Long, idempotencyKey: String) =
        repository.quoteCashIn(phoneNumber, amount, idempotencyKey)

    override suspend fun submitCashIn(quote: com.kori.app.core.model.action.AgentCashInQuote) =
        repository.submitCashIn(quote)

    override suspend fun quoteMerchantWithdraw(merchantCode: String, amount: Long, idempotencyKey: String) =
        repository.quoteMerchantWithdraw(merchantCode, amount, idempotencyKey)

    override suspend fun submitMerchantWithdraw(quote: com.kori.app.core.model.action.AgentMerchantWithdrawQuote) =
        repository.submitMerchantWithdraw(quote)

    override suspend fun enrollCard(phoneNumber: String?, displayName: String, cardUid: String, pin: String) =
        repository.enrollCard(phoneNumber, displayName, cardUid, pin)

    override suspend fun addCardToClient(phoneNumber: String, cardUid: String, pin: String) =
        repository.addCardToClient(phoneNumber, cardUid, pin)

    override suspend fun updateCardStatusAsAgent(
        cardUid: String,
        targetStatus: com.kori.app.core.model.action.AgentCardTargetStatus,
        reason: String?,
    ) = repository.updateCardStatusAsAgent(cardUid, targetStatus, reason)
}
