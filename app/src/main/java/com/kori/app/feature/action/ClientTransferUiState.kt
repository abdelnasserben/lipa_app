package com.kori.app.feature.action

import com.kori.app.core.model.action.ClientTransferDraft
import com.kori.app.core.model.action.ClientTransferQuote
import com.kori.app.core.model.action.ClientTransferReceipt

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
        val model: FinancialConfirmationModel<ClientTransferQuote>,
    ) : ClientTransferUiState

    data class Success(
        val model: FinancialSuccessModel<ClientTransferReceipt>,
    ) : ClientTransferUiState

    data class Failure(
        val model: FinancialFailureModel,
    ) : ClientTransferUiState
}
