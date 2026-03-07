package com.kori.app.feature.action

import com.kori.app.core.model.action.ClientTransferDraft
import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferReceipt
import com.kori.app.core.model.action.FinancialErrorCode

data class ClientTransferFormErrors(
    val recipientPhoneNumber: String? = null,
    val amount: String? = null,
)

sealed interface ClientTransferUiState {
    data class Form(
        val draft: ClientTransferDraft = ClientTransferDraft(),
        val errors: ClientTransferFormErrors = ClientTransferFormErrors(),
        val isLoading: Boolean = false,
    ) : ClientTransferUiState

    data class Confirmation(
        val quote: ClientTransferQuote,
        val isSubmitting: Boolean = false,
        val isConfirmDialogVisible: Boolean = false,
    ) : ClientTransferUiState

    data class Success(
        val receipt: ClientTransferReceipt,
        val idempotencyKey: String,
    ) : ClientTransferUiState

    data class Failure(
        val code: FinancialErrorCode,
        val userMessage: String,
        val technicalMessage: String? = null,
        val idempotencyKey: String,
    ) : ClientTransferUiState
}