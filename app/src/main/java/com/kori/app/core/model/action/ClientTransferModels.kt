package com.kori.app.core.model.action

import com.kori.app.core.ui.FinancialInputRules

enum class FinancialErrorCode {
    INSUFFICIENT_FUNDS,
    DAILY_LIMIT_EXCEEDED,
    INVALID_STATUS,
    UNAUTHORIZED,
}

data class ClientTransferDraft(
    val recipientPhoneNumber: String = FinancialInputRules.formatComorosPhoneForDisplay(""),
    val amountInput: String = "",
)

data class ClientTransferQuote(
    val recipientPhoneNumber: String,
    val amount: Long,
    val fee: Long,
    val totalDebited: Long,
    val currency: String = "KMF",
    val idempotencyKey: String,
)

data class ClientTransferReceipt(
    val transactionRef: String,
    val recipientPhoneNumber: String,
    val amount: Long,
    val fee: Long,
    val totalDebited: Long,
    val createdAt: String,
    val currency: String = "KMF",
)

sealed interface ClientTransferResult {
    data class Success(
        val receipt: ClientTransferReceipt,
    ) : ClientTransferResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : ClientTransferResult
}