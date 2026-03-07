package com.kori.app.core.model.action

import com.kori.app.core.ui.FinancialInputRules

data class AgentCashInDraft(
    val phoneNumber: String = FinancialInputRules.formatComorosPhoneForDisplay(""),
    val amountInput: String = "",
)

data class AgentCashInQuote(
    val phoneNumber: String,
    val amount: Long,
    val fee: Long,
    val idempotencyKey: String,
    val currency: String = "KMF",
)

data class AgentCashInReceipt(
    val transactionRef: String,
    val clientPhoneNumber: String,
    val amount: Long,
    val fee: Long,
    val createdAt: String,
    val currency: String = "KMF",
)

sealed interface AgentCashInResult {
    data class Success(
        val receipt: AgentCashInReceipt,
    ) : AgentCashInResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : AgentCashInResult
}

data class AgentMerchantWithdrawDraft(
    val merchantCode: String = FinancialInputRules.normalizeMerchantCodeInput(""),
    val amountInput: String = "",
)

data class AgentMerchantWithdrawQuote(
    val merchantCode: String,
    val amount: Long,
    val fee: Long,
    val commission: Long,
    val totalDebitedMerchant: Long,
    val idempotencyKey: String,
    val currency: String = "KMF",
)

data class AgentMerchantWithdrawReceipt(
    val transactionRef: String,
    val merchantCode: String,
    val amount: Long,
    val fee: Long,
    val commission: Long,
    val totalDebitedMerchant: Long,
    val createdAt: String,
    val currency: String = "KMF",
)

sealed interface AgentMerchantWithdrawResult {
    data class Success(
        val receipt: AgentMerchantWithdrawReceipt,
    ) : AgentMerchantWithdrawResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : AgentMerchantWithdrawResult
}
