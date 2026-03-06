package com.kori.app.data.repository

import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInResult
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
}