package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.AgentCashInDraft
import com.kori.app.core.model.action.AgentCashInResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.AgentActionRepository
import com.kori.app.domain.idempotency.IdempotencyManager
import com.kori.app.core.ui.KmfAmountFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentCashInViewModel(
    private val repository: AgentActionRepository,
    private val idempotencyManager: IdempotencyManager,
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
            draft = current.draft.copy(amountInput = KmfAmountFormatters.normalizeInput(value)),
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

        val amount = KmfAmountFormatters.parseToLong(current.draft.amountInput) ?: return
        val intent = createActionIntent(
            phoneNumber = current.draft.phoneNumber,
            amount = amount,
        )
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

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

        val intent = createActionIntent(
            phoneNumber = current.quote.phoneNumber,
            amount = current.quote.amount,
        )
        val canStart = idempotencyManager.start(intent, current.quote.idempotencyKey)
        if (!canStart) return

        viewModelScope.launch {
            _uiState.value = current.copy(
                isSubmitting = true,
                isConfirmDialogVisible = false,
            )

            runCatching {
                repository.submitCashIn(current.quote)
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentCashInResult.Success -> {
                        idempotencyManager.onSuccess(current.quote.idempotencyKey)
                        AgentCashInUiState.Success(
                            receipt = result.receipt,
                            idempotencyKey = current.quote.idempotencyKey,
                        )
                    }

                    is AgentCashInResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        AgentCashInUiState.Failure(
                            code = result.code,
                            message = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            }.onFailure {
                idempotencyManager.onFailure(current.quote.idempotencyKey)
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
        if (state is AgentCashInUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    phoneNumber = state.quote.phoneNumber,
                    amount = state.quote.amount,
                ),
            )
        }

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
        val state = _uiState.value
        if (state is AgentCashInUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    phoneNumber = state.quote.phoneNumber,
                    amount = state.quote.amount,
                ),
            )
        }
        _uiState.value = AgentCashInUiState.Form()
    }

    private fun createActionIntent(
        phoneNumber: String,
        amount: Long,
    ): ActionIntent {
        return ActionIntent(
            type = ActionIntentType.CASH_IN,
            actor = phoneNumber.trim(),
            amount = amount,
        )
    }

    private fun validate(draft: AgentCashInDraft): AgentCashInFormErrors {
        val phone = draft.phoneNumber.trim()
        return AgentCashInFormErrors(
            phoneNumber = when {
                phone.isBlank() -> "Saisissez le numéro du client."
                phone.length < 7 -> "Le numéro paraît incomplet."
                else -> null
            },
            amount = KmfAmountFormatters.validateAmount(draft.amountInput),
        )
    }

    companion object {
        fun factory(
            repository: AgentActionRepository,
            idempotencyManager: IdempotencyManager,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgentCashInViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                    ) as T
                }
            }
        }
    }
}
