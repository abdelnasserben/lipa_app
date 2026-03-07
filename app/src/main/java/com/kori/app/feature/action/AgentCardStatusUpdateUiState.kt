package com.kori.app.feature.action

import com.kori.app.core.model.action.AgentCardStatusUpdateDraft
import com.kori.app.core.model.action.AgentCardStatusUpdateReceipt

data class AgentCardStatusUpdateFormErrors(
    val cardUid: String? = null,
    val targetStatus: String? = null,
    val reason: String? = null,
)

sealed interface AgentCardStatusUpdateUiState {
    data class Form(
        val draft: AgentCardStatusUpdateDraft = AgentCardStatusUpdateDraft(),
        val errors: AgentCardStatusUpdateFormErrors = AgentCardStatusUpdateFormErrors(),
        val isSubmitting: Boolean = false,
        val showConfirmModal: Boolean = false,
    ) : AgentCardStatusUpdateUiState

    data class Success(
        val receipt: AgentCardStatusUpdateReceipt,
    ) : AgentCardStatusUpdateUiState

    data class Failure(
        val code: String,
        val userMessage: String,
    ) : AgentCardStatusUpdateUiState
}
