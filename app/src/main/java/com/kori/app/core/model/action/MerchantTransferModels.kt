package com.kori.app.core.model.action

data class MerchantTransferDraft(
    val recipientMerchantCode: String = "",
    val amountInput: String = "",
)

data class MerchantTransferQuote(
    val recipientMerchantCode: String,
    val amount: Long,
    val fee: Long,
    val totalDebited: Long,
    val currency: String = "KMF",
    val idempotencyKey: String,
)

data class MerchantTransferReceipt(
    val transactionRef: String,
    val recipientMerchantCode: String,
    val amount: Long,
    val fee: Long,
    val totalDebited: Long,
    val createdAt: String,
    val currency: String = "KMF",
)

sealed interface MerchantTransferResult {
    data class Success(
        val receipt: MerchantTransferReceipt,
    ) : MerchantTransferResult

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : MerchantTransferResult
}