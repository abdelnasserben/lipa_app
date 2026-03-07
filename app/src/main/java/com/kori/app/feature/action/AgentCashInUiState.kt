package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentCashInDraft
import com.kori.app.core.model.action.AgentCashInQuote
import com.kori.app.core.model.action.AgentCashInReceipt

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
        val model: FinancialConfirmationModel<AgentCashInQuote>,
    ) : AgentCashInUiState

    data class Success(
        val model: FinancialSuccessModel<AgentCashInReceipt>,
    ) : AgentCashInUiState

    data class Failure(
        val model: FinancialFailureModel,
    ) : AgentCashInUiState
}
