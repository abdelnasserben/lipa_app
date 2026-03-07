package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentCardAddDraft
import com.kori.app.core.model.action.AgentCardAddReceipt

data class AgentCardAddFormErrors(
    val phoneNumber: String? = null,
    val cardUid: String? = null,
    val pin: String? = null,
)

sealed interface AgentCardAddUiState {
    data class Form(
        val draft: AgentCardAddDraft = AgentCardAddDraft(),
        val errors: AgentCardAddFormErrors = AgentCardAddFormErrors(),
        val isSubmitting: Boolean = false,
    ) : AgentCardAddUiState

    data class Success(
        val receipt: AgentCardAddReceipt,
    ) : AgentCardAddUiState

    data class Failure(
        val code: String,
        val userMessage: String,
    ) : AgentCardAddUiState
}
