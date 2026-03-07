package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentCardEnrollDraft
import com.kori.app.core.model.action.AgentCardEnrollReceipt

data class AgentCardEnrollFormErrors(
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val cardUid: String? = null,
    val pin: String? = null,
)

sealed interface AgentCardEnrollUiState {
    data class Form(
        val draft: AgentCardEnrollDraft = AgentCardEnrollDraft(),
        val errors: AgentCardEnrollFormErrors = AgentCardEnrollFormErrors(),
        val isSubmitting: Boolean = false,
    ) : AgentCardEnrollUiState

    data class Success(
        val receipt: AgentCardEnrollReceipt,
    ) : AgentCardEnrollUiState

    data class Failure(
        val code: String,
        val userMessage: String,
    ) : AgentCardEnrollUiState
}
