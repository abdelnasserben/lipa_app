package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.AgentCashInDraft
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AgentCashInViewModel(
    private val repository: AgentActionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentCashInUiState>(AgentCashInUiState.Form())
    val uiState: StateFlow<AgentCashInUiState> = _uiState.asStateFlow()

    fun onPhoneChanged(value: String) {
        val current = _uiState.value as? AgentCashInUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(phoneNumber = value),
            errors = current.errors.copy(phoneNumber = null),
        )
    }

    fun onAmountChanged(value: String) {
        val current = _uiState.value as? AgentCashInUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = value.filter { it.isDigit() }),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? AgentCashInUiState.Form ?: return
        val errors = validate(current.draft)
        if (errors.phoneNumber != null || errors.amount != null) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = current.draft.amountInput.toLong()
        val idempotencyKey = UUID.randomUUID().toString()

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteCashIn(
                    phoneNumber = current.draft.phoneNumber.trim(),
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = AgentCashInUiState.Confirmation(
                    quote = quote,
                )
            }.onFailure {
                _uiState.value = current.copy(
                    isLoading = false,
                    errors = current.errors.copy(
                        amount = "Impossible de préparer cette opération pour le moment.",
                    ),
                )
            }
        }
    }

    fun openConfirmDialog() {
        val current = _uiState.value as? AgentCashInUiState.Confirmation ?: return
        if (current.isSubmitting) return
        _uiState.value = current.copy(isConfirmDialogVisible = true)
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? AgentCashInUiState.Confirmation ?: return
        if (current.isSubmitting) return
        _uiState.value = current.copy(isConfirmDialogVisible = false)
    }

    fun submit() {
        val current = _uiState.value as? AgentCashInUiState.Confirmation ?: return
        if (current.isSubmitting) return

        viewModelScope.launch {
            _uiState.value = current.copy(
                isSubmitting = true,
                isConfirmDialogVisible = false,
            )

            runCatching {
                repository.submitCashIn(current.quote)
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentCashInResult.Success -> AgentCashInUiState.Success(
                        receipt = result.receipt,
                        idempotencyKey = current.quote.idempotencyKey,
                    )

                    is AgentCashInResult.Failure -> AgentCashInUiState.Failure(
                        code = result.code,
                        message = result.message,
                        idempotencyKey = result.idempotencyKey,
                    )
                }
            }.onFailure {
                _uiState.value = AgentCashInUiState.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Une erreur inattendue est survenue pendant le cash-in.",
                    idempotencyKey = current.quote.idempotencyKey,
                )
            }
        }
    }

    fun edit() {
        val state = _uiState.value
        val draft = when (state) {
            is AgentCashInUiState.Form -> state.draft
            is AgentCashInUiState.Confirmation -> AgentCashInDraft(
                phoneNumber = state.quote.phoneNumber,
                amountInput = state.quote.amount.toString(),
            )

            is AgentCashInUiState.Success -> AgentCashInDraft()
            is AgentCashInUiState.Failure -> AgentCashInDraft()
        }
        _uiState.value = AgentCashInUiState.Form(draft = draft)
    }

    fun restart() {
        _uiState.value = AgentCashInUiState.Form()
    }

    private fun validate(draft: AgentCashInDraft): AgentCashInFormErrors {
        val phone = draft.phoneNumber.trim()
        val amount = draft.amountInput.toLongOrNull()

        return AgentCashInFormErrors(
            phoneNumber = when {
                phone.isBlank() -> "Saisissez le numéro du client."
                phone.length < 7 -> "Le numéro paraît incomplet."
                else -> null
            },
            amount = when {
                draft.amountInput.isBlank() -> "Saisissez un montant."
                amount == null -> "Montant invalide."
                amount <= 0L -> "Le montant doit être supérieur à zéro."
                else -> null
            },
        )
    }

    companion object {
        fun factory(repository: AgentActionRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgentCashInViewModel(repository) as T
                }
            }
        }
    }
}