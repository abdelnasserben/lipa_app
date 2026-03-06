package com.kori.app.feature.action

import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.core.model.action.MerchantTransferDraft
import com.kori.app.core.model.action.MerchantTransferQuote
import com.kori.app.core.model.action.MerchantTransferReceipt

data class MerchantTransferFormErrors(
    val recipientMerchantCode: String? = null,
    val amount: String? = null,
)

sealed interface MerchantTransferUiState {
    data class Form(
        val draft: MerchantTransferDraft = MerchantTransferDraft(),
        val errors: MerchantTransferFormErrors = MerchantTransferFormErrors(),
        val isLoading: Boolean = false,
    ) : MerchantTransferUiState

    data class Confirmation(
        val quote: MerchantTransferQuote,
        val isSubmitting: Boolean = false,
        val isConfirmDialogVisible: Boolean = false,
    ) : MerchantTransferUiState

    data class Success(
        val receipt: MerchantTransferReceipt,
        val idempotencyKey: String,
    ) : MerchantTransferUiState

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : MerchantTransferUiState
}