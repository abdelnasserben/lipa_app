package com.kori.app.data.datasource

import com.kori.app.core.model.action.AgentCardAddResult
import com.kori.app.core.model.action.AgentCardEnrollResult
import com.kori.app.core.model.action.AgentCardStatusUpdateResult
import com.kori.app.core.model.action.AgentCardTargetStatus
import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferResult

interface ClientTransferDataSource {
    suspend fun quoteTransfer(recipientPhoneNumber: String, amount: Long, idempotencyKey: String): ClientTransferQuote
    suspend fun submitTransfer(quote: ClientTransferQuote): ClientTransferResult
}

interface MerchantTransferDataSource {
    suspend fun quoteTransfer(recipientMerchantCode: String, amount: Long, idempotencyKey: String): MerchantTransferQuote
    suspend fun submitTransfer(quote: MerchantTransferQuote): MerchantTransferResult
}

interface AgentActionDataSource {
    suspend fun quoteCashIn(phoneNumber: String, amount: Long, idempotencyKey: String): AgentCashInQuote
    suspend fun submitCashIn(quote: AgentCashInQuote): AgentCashInResult
    suspend fun quoteMerchantWithdraw(merchantCode: String, amount: Long, idempotencyKey: String): AgentMerchantWithdrawQuote
    suspend fun submitMerchantWithdraw(quote: AgentMerchantWithdrawQuote): AgentMerchantWithdrawResult
    suspend fun enrollCard(phoneNumber: String?, displayName: String, cardUid: String, pin: String): AgentCardEnrollResult
    suspend fun addCardToClient(phoneNumber: String, cardUid: String, pin: String): AgentCardAddResult
    suspend fun updateCardStatusAsAgent(cardUid: String, targetStatus: AgentCardTargetStatus, reason: String?): AgentCardStatusUpdateResult
}
