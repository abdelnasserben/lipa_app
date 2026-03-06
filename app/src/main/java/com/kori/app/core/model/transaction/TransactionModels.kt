package com.kori.app.core.model.transaction

import com.kori.app.core.model.common.CurrencyCode

enum class TransactionType {
    CARD_PAYMENT,
    CASH_IN,
    CLIENT_TRANSFER,
    MERCHANT_TRANSFER,
    MERCHANT_WITHDRAW,
    REVERSAL,
    AGENT_BANK_DEPOSIT,
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED,
}

data class TransactionCounterparty(
    val displayName: String,
    val phone: String? = null,
    val code: String? = null,
)

data class TransactionItemResponse(
    val transactionRef: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val amount: Long,
    val fee: Long? = null,
    val totalDebited: Long? = null,
    val currency: CurrencyCode = CurrencyCode.KMF,
    val createdAt: String,
    val counterparty: TransactionCounterparty,
)