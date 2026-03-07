package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.AgentCardEnrollResult
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentCardEnrollViewModel(
    private val repository: AgentActionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentCardEnrollUiState>(AgentCardEnrollUiState.Form())
    val uiState: StateFlow<AgentCardEnrollUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(value: String) {
        val current = _uiState.value as? AgentCardEnrollUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(phoneNumber = FinancialInputRules.normalizeComorosPhoneInput(value)),
            errors = current.errors.copy(phoneNumber = null),
        )
    }

    fun onDisplayNameChanged(value: String) {
        val current = _uiState.value as? AgentCardEnrollUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(displayName = value.take(120)),
            errors = current.errors.copy(displayName = null),
        )
    }

    fun onCardUidChanged(value: String) {
        val current = _uiState.value as? AgentCardEnrollUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(cardUid = normalizeCardUid(value)),
            errors = current.errors.copy(cardUid = null),
        )
    }

    fun onPinChanged(value: String) {
        val current = _uiState.value as? AgentCardEnrollUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(pin = value.filter(Char::isDigit).take(4)),
            errors = current.errors.copy(pin = null),
        )
    }

    fun submit() {
        val current = _uiState.value as? AgentCardEnrollUiState.Form ?: return
        val errors = validate(current.draft)
        if (
            current.isSubmitting ||
            errors.phoneNumber != null ||
            errors.displayName != null ||
            errors.cardUid != null ||
            errors.pin != null
        ) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)

            val phoneForApi = current.draft.phoneNumber
                .takeIf { it.filter(Char::isDigit).isNotBlank() }
                ?.let(FinancialInputRules::comorosPhoneToApi)

            runCatching {
                repository.enrollCard(
                    phoneNumber = phoneForApi,
                    displayName = current.draft.displayName.trim(),
                    cardUid = current.draft.cardUid.trim(),
                    pin = current.draft.pin,
                )
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentCardEnrollResult.Success -> AgentCardEnrollUiState.Success(result.receipt)
                    is AgentCardEnrollResult.Failure -> AgentCardEnrollUiState.Failure(
                        code = result.code.name,
                        userMessage = FinancialErrorMapper.userMessageFor(result.code),
                    )
                }
            }.onFailure {
                _uiState.value = AgentCardEnrollUiState.Failure(
                    code = "TECHNICAL_ERROR",
                    userMessage = "Une erreur réseau est survenue. Réessayez dans un instant.",
                )
            }
        }
    }

    fun restart() {
        _uiState.value = AgentCardEnrollUiState.Form()
    }

    private fun normalizeCardUid(raw: String): String {
        return raw.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(64)
    }

    private fun validate(draft: com.kori.app.core.model.action.AgentCardEnrollDraft): AgentCardEnrollFormErrors {
        val phoneDigits = draft.phoneNumber.filter(Char::isDigit)
        val phoneError = if (phoneDigits.isNotBlank()) {
            FinancialInputRules.validateComorosPhone(draft.phoneNumber, "le numéro du client")
        } else {
            null
        }

        return AgentCardEnrollFormErrors(
            phoneNumber = phoneError,
            displayName = if (draft.displayName.trim().length < 2) "Saisissez le nom du client." else null,
            cardUid = if (draft.cardUid.isBlank()) "Saisissez l’identifiant de la carte." else null,
            pin = when {
                draft.pin.isBlank() -> "Le code PIN est requis."
                draft.pin.length != 4 -> "Le code PIN doit contenir 4 chiffres."
                else -> null
            },
        )
    }

    companion object {
        fun factory(repository: AgentActionRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgentCardEnrollViewModel(repository) as T
                }
            }
        }
    }
}
