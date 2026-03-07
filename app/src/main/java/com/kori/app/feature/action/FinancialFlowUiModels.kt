package com.kori.app.feature.action

import com.kori.app.core.model.action.FinancialErrorCode

data class FinancialConfirmationModel<TQuote>(
    val quote: TQuote,
    val isSubmitting: Boolean = false,
    val isConfirmDialogVisible: Boolean = false,
)

data class FinancialSuccessModel<TReceipt>(
    val receipt: TReceipt,
    val idempotencyKey: String,
)

data class FinancialFailureModel(
    val code: FinancialErrorCode,
    val userMessage: String,
    val technicalMessage: String? = null,
    val idempotencyKey: String,
)

fun FinancialErrorDetails.toFailureModel(): FinancialFailureModel {
    return FinancialFailureModel(
        code = code,
        userMessage = userMessage,
        technicalMessage = technicalMessage,
        idempotencyKey = idempotencyKey,
    )
}
