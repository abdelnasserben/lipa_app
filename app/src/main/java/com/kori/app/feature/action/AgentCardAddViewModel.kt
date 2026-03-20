package com.kori.app.feature.action

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.action.AgentCardAddDraft
import com.kori.app.core.model.action.AgentCardAddResult
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentCardAddViewModel(
    private val repository: AgentActionRepository,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentCardAddUiState>(AgentCardAddUiState.Form())
    val uiState: StateFlow<AgentCardAddUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(value: String) {
        val current = _uiState.value as? AgentCardAddUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(phoneNumber = FinancialInputRules.normalizeComorosPhoneInput(value)),
            errors = current.errors.copy(phoneNumber = null),
        )
    }

    fun onCardUidChanged(value: String) {
        val current = _uiState.value as? AgentCardAddUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(cardUid = normalizeCardUid(value)),
            errors = current.errors.copy(cardUid = null),
        )
    }

    fun onPinChanged(value: String) {
        val current = _uiState.value as? AgentCardAddUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(pin = value.filter(Char::isDigit).take(4)),
            errors = current.errors.copy(pin = null),
        )
    }

    fun submit() {
        val current = _uiState.value as? AgentCardAddUiState.Form ?: return
        val errors = validate(current.draft)
        if (current.isSubmitting || errors.phoneNumber != null || errors.cardUid != null || errors.pin != null) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val apiPhone = FinancialInputRules.comorosPhoneToApi(current.draft.phoneNumber) ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)

            runCatching {
                repository.addCardToClient(
                    phoneNumber = apiPhone,
                    cardUid = current.draft.cardUid.trim(),
                    pin = current.draft.pin,
                )
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentCardAddResult.Success -> AgentCardAddUiState.Success(result.receipt)
                    is AgentCardAddResult.Failure -> AgentCardAddUiState.Failure(
                        code = result.code.name,
                        userMessage = FinancialErrorMapper.userMessageFor(resources, result.code),
                    )
                }
            }.onFailure {
                _uiState.value = AgentCardAddUiState.Failure(
                    code = "TECHNICAL_ERROR",
                    userMessage = resources.getString(R.string.error_network_retry),
                )
            }
        }
    }

    fun restart() {
        _uiState.value = AgentCardAddUiState.Form()
    }

    private fun normalizeCardUid(raw: String): String {
        return raw.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(64)
    }

    private fun validate(draft: AgentCardAddDraft): AgentCardAddFormErrors {
        return AgentCardAddFormErrors(
            phoneNumber = FinancialInputRules.validateComorosPhone(
                raw = draft.phoneNumber,
                resources = resources,
                fieldLabelResId = R.string.card_add_phone_label,
            ),
            cardUid = if (draft.cardUid.isBlank()) resources.getString(R.string.validation_card_reference_required) else null,
            pin = when {
                draft.pin.isBlank() -> resources.getString(R.string.validation_pin_required)
                draft.pin.length != 4 -> resources.getString(R.string.validation_pin_length)
                else -> null
            },
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
                    return AgentCardAddViewModel(repository, resources) as T
                }
            }
        }
    }
}
