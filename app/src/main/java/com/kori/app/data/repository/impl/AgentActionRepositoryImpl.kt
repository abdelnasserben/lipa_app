package com.kori.app.data.repository.impl

import com.kori.app.core.model.action.AgentCardAddResult
import com.kori.app.core.model.action.AgentCardEnrollResult
import com.kori.app.core.model.action.AgentCardStatusUpdateResult
import com.kori.app.core.model.action.AgentCardTargetStatus
import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.data.datasource.AgentActionDataSource
import com.kori.app.data.repository.AgentActionRepository

class AgentActionRepositoryImpl(
    private val dataSource: AgentActionDataSource,
) : AgentActionRepository {
    override suspend fun quoteCashIn(phoneNumber: String, amount: Long, idempotencyKey: String): AgentCashInQuote =
        dataSource.quoteCashIn(phoneNumber, amount, idempotencyKey)

    override suspend fun submitCashIn(quote: AgentCashInQuote): AgentCashInResult = dataSource.submitCashIn(quote)

    override suspend fun quoteMerchantWithdraw(
        merchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentMerchantWithdrawQuote = dataSource.quoteMerchantWithdraw(merchantCode, amount, idempotencyKey)

    override suspend fun submitMerchantWithdraw(quote: AgentMerchantWithdrawQuote): AgentMerchantWithdrawResult =
        dataSource.submitMerchantWithdraw(quote)

    override suspend fun enrollCard(
        phoneNumber: String?,
        displayName: String,
        cardUid: String,
        pin: String,
    ): AgentCardEnrollResult = dataSource.enrollCard(phoneNumber, displayName, cardUid, pin)

    override suspend fun addCardToClient(phoneNumber: String, cardUid: String, pin: String): AgentCardAddResult =
        dataSource.addCardToClient(phoneNumber, cardUid, pin)

    override suspend fun updateCardStatusAsAgent(
        cardUid: String,
        targetStatus: AgentCardTargetStatus,
        reason: String?,
    ): AgentCardStatusUpdateResult = dataSource.updateCardStatusAsAgent(cardUid, targetStatus, reason)
}
