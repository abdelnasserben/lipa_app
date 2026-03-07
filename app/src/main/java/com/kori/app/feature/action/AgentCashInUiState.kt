package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentCashInDraft
import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInReceipt
import com.kori.app.core.model.action.FinancialErrorCode

data class AgentCashInFormErrors(
    val phoneNumber: String? = null,
    val amount: String? = null,
)

sealed interface AgentCashInUiState {
    data class Form(
        val draft: AgentCashInDraft = AgentCashInDraft(),
        val errors: AgentCashInFormErrors = AgentCashInFormErrors(),
        val isLoading: Boolean = false,
    ) : AgentCashInUiState

    data class Confirmation(
        val quote: AgentCashInQuote,
        val isSubmitting: Boolean = false,
        val isConfirmDialogVisible: Boolean = false,
    ) : AgentCashInUiState

    data class Success(
        val receipt: AgentCashInReceipt,
        val idempotencyKey: String,
    ) : AgentCashInUiState

    data class Failure(
        val code: FinancialErrorCode,
        val userMessage: String,
        val technicalMessage: String? = null,
        val idempotencyKey: String,
    ) : AgentCashInUiState
}