package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentMerchantWithdrawDraft
import com.kori.app.core.model.action.AgentMerchantWithdrawQuote
import com.kori.app.core.model.action.AgentMerchantWithdrawReceipt

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
        val model: FinancialConfirmationModel<AgentMerchantWithdrawQuote>,
    ) : AgentMerchantWithdrawUiState

    data class Success(
        val model: FinancialSuccessModel<AgentMerchantWithdrawReceipt>,
    ) : AgentMerchantWithdrawUiState

    data class Failure(
        val model: FinancialFailureModel,
    ) : AgentMerchantWithdrawUiState
}
