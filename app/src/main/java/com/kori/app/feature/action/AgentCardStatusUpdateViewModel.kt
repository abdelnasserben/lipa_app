package com.kori.app.feature.action

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.action.AgentCardStatusUpdateDraft
import com.kori.app.core.model.action.AgentCardStatusUpdateResult
import com.kori.app.core.model.action.AgentCardTargetStatus
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentCardStatusUpdateViewModel(
    private val repository: AgentActionRepository,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentCardStatusUpdateUiState>(AgentCardStatusUpdateUiState.Form())
    val uiState: StateFlow<AgentCardStatusUpdateUiState> = _uiState.asStateFlow()

    fun onCardUidChanged(value: String) {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(cardUid = normalizeCardUid(value)),
            errors = current.errors.copy(cardUid = null),
        )
    }

    fun onTargetStatusChanged(value: AgentCardTargetStatus) {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(targetStatus = value),
            errors = current.errors.copy(targetStatus = null),
        )
    }

    fun onReasonChanged(value: String) {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(reason = value.take(255)),
            errors = current.errors.copy(reason = null),
        )
    }

    fun submit() {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        val errors = validate(current.draft)
        if (
            current.isSubmitting ||
            errors.cardUid != null ||
            errors.targetStatus != null ||
            errors.reason != null
        ) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        _uiState.value = current.copy(showConfirmModal = true)
    }

    fun dismissConfirmModal() {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        _uiState.value = current.copy(showConfirmModal = false)
    }

    fun confirmSubmit() {
        val current = _uiState.value as? AgentCardStatusUpdateUiState.Form ?: return
        val targetStatus = current.draft.targetStatus ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true, showConfirmModal = false)

            runCatching {
                repository.updateCardStatusAsAgent(
                    cardUid = current.draft.cardUid.trim(),
                    targetStatus = targetStatus,
                    reason = current.draft.reason.trim().takeIf { it.isNotBlank() },
                )
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentCardStatusUpdateResult.Success -> AgentCardStatusUpdateUiState.Success(result.receipt)
                    is AgentCardStatusUpdateResult.Failure -> AgentCardStatusUpdateUiState.Failure(
                        code = result.code.name,
                        userMessage = FinancialErrorMapper.userMessageFor(resources, result.code),
                    )
                }
            }.onFailure {
                _uiState.value = AgentCardStatusUpdateUiState.Failure(
                    code = "TECHNICAL_ERROR",
                    userMessage = resources.getString(R.string.error_network_retry),
                )
            }
        }
    }

    fun restart() {
        _uiState.value = AgentCardStatusUpdateUiState.Form()
    }

    private fun normalizeCardUid(raw: String): String {
        return raw.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(64)
    }

    private fun validate(draft: AgentCardStatusUpdateDraft): AgentCardStatusUpdateFormErrors {
        return AgentCardStatusUpdateFormErrors(
            cardUid = if (draft.cardUid.isBlank()) resources.getString(R.string.validation_card_reference_required) else null,
            targetStatus = if (draft.targetStatus == null) resources.getString(R.string.validation_card_status_required) else null,
            reason = if (draft.reason.length > 255) resources.getString(R.string.validation_reason_too_long) else null,
        )
    }

    companion object {
        fun factory(
            repository: AgentActionRepository,
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgentCardStatusUpdateViewModel(repository, resources) as T
                }
            }
        }
    }
}
