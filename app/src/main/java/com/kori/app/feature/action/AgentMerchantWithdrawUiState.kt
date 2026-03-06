package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentMerchantWithdrawDraft
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawReceipt
import com.kori.app.core.model.action.FinancialErrorCode

data class AgentMerchantWithdrawFormErrors(
    val merchantCode: String? = null,
    val amount: String? = null,
)

sealed interface AgentMerchantWithdrawUiState {
    data class Form(
        val draft: AgentMerchantWithdrawDraft = AgentMerchantWithdrawDraft(),
        val errors: AgentMerchantWithdrawFormErrors = AgentMerchantWithdrawFormErrors(),
        val isLoading: Boolean = false,
    ) : AgentMerchantWithdrawUiState

    data class Confirmation(
        val quote: AgentMerchantWithdrawQuote,
        val isSubmitting: Boolean = false,
        val isConfirmDialogVisible: Boolean = false,
    ) : AgentMerchantWithdrawUiState

    data class Success(
        val receipt: AgentMerchantWithdrawReceipt,
        val idempotencyKey: String,
    ) : AgentMerchantWithdrawUiState

    data class Failure(
        val code: FinancialErrorCode,
        val message: String,
        val idempotencyKey: String,
    ) : AgentMerchantWithdrawUiState
}