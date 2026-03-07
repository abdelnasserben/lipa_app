package com.kori.app.data.repository

import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.AgentCardAddResult
import com.kori.app.core.model.action.AgentCardEnrollResult
import com.kori.app.core.model.action.AgentCardStatusUpdateResult
import com.kori.app.core.model.action.AgentCardTargetStatus
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawResult

interface AgentActionRepository {
    suspend fun quoteCashIn(
        phoneNumber: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentCashInQuote

    suspend fun submitCashIn(
        quote: AgentCashInQuote,
    ): AgentCashInResult

    suspend fun quoteMerchantWithdraw(
        merchantCode: String,
        amount: Long,
        idempotencyKey: String,
    ): AgentMerchantWithdrawQuote

    suspend fun submitMerchantWithdraw(
        quote: AgentMerchantWithdrawQuote,
    ): AgentMerchantWithdrawResult

    /** API: POST /api/v1/cards/enroll */
    suspend fun enrollCard(
        phoneNumber: String?,
        displayName: String,
        cardUid: String,
        pin: String,
    ): AgentCardEnrollResult

    /** API: POST /api/v1/cards/add */
    suspend fun addCardToClient(
        phoneNumber: String,
        cardUid: String,
        pin: String,
    ): AgentCardAddResult

    /** API: PATCH /api/v1/cards/{cardUid}/status/agent */
    suspend fun updateCardStatusAsAgent(
        cardUid: String,
        targetStatus: AgentCardTargetStatus,
        reason: String?,
    ): AgentCardStatusUpdateResult
}
