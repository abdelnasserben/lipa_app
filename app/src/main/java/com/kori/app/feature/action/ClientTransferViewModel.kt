package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.ClientTransferDraft
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.ClientTransferRepository
import com.kori.app.domain.idempotency.IdempotencyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientTransferViewModel(
    private val repository: ClientTransferRepository,
    private val idempotencyManager: IdempotencyManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientTransferUiState>(ClientTransferUiState.Form())
    val uiState: StateFlow<ClientTransferUiState> = _uiState.asStateFlow()

    fun onRecipientChanged(value: String) {
        val current = _uiState.value as? ClientTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(recipientPhoneNumber = value),
            errors = current.errors.copy(recipientPhoneNumber = null),
        )
    }

    fun onAmountChanged(value: String) {
        val sanitized = value.filter { it.isDigit() }
        val current = _uiState.value as? ClientTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = sanitized),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? ClientTransferUiState.Form ?: return
        val errors = validate(current.draft)

        if (errors.recipientPhoneNumber != null || errors.amount != null) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = current.draft.amountInput.toLong()
        val intent = createActionIntent(
            recipientPhoneNumber = current.draft.recipientPhoneNumber,
            amount = amount,
        )
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteTransfer(
                    recipientPhoneNumber = current.draft.recipientPhoneNumber.trim(),
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = ClientTransferUiState.Confirmation(
                    quote = quote,
                    isSubmitting = false,
                    isConfirmDialogVisible = false,
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
        val current = _uiState.value as? ClientTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        _uiState.value = current.copy(
            isConfirmDialogVisible = true,
        )
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? ClientTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        _uiState.value = current.copy(
            isConfirmDialogVisible = false,
        )
    }

    fun submitTransfer() {
        val current = _uiState.value as? ClientTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        val intent = createActionIntent(
            recipientPhoneNumber = current.quote.recipientPhoneNumber,
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
                repository.submitTransfer(current.quote)
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is ClientTransferResult.Success -> {
                        idempotencyManager.onSuccess(current.quote.idempotencyKey)
                        ClientTransferUiState.Success(
                            receipt = result.receipt,
                            idempotencyKey = current.quote.idempotencyKey,
                        )
                    }

                    is ClientTransferResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        ClientTransferUiState.Failure(
                            code = result.code,
                            message = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            }.onFailure {
                idempotencyManager.onFailure(current.quote.idempotencyKey)
                _uiState.value = ClientTransferUiState.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Une erreur inattendue est survenue pendant le transfert.",
                    idempotencyKey = current.quote.idempotencyKey,
                )
            }
        }
    }

    fun editForm() {
        val state = _uiState.value
        if (state is ClientTransferUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    recipientPhoneNumber = state.quote.recipientPhoneNumber,
                    amount = state.quote.amount,
                ),
            )
        }

        val draft = when (state) {
            is ClientTransferUiState.Form -> state.draft
            is ClientTransferUiState.Confirmation -> ClientTransferDraft(
                recipientPhoneNumber = state.quote.recipientPhoneNumber,
                amountInput = state.quote.amount.toString(),
            )

            is ClientTransferUiState.Success -> ClientTransferDraft()
            is ClientTransferUiState.Failure -> ClientTransferDraft()
        }

        _uiState.value = ClientTransferUiState.Form(draft = draft)
    }

    fun restart() {
        val state = _uiState.value
        if (state is ClientTransferUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    recipientPhoneNumber = state.quote.recipientPhoneNumber,
                    amount = state.quote.amount,
                ),
            )
        }
        _uiState.value = ClientTransferUiState.Form()
    }

    private fun createActionIntent(
        recipientPhoneNumber: String,
        amount: Long,
    ): ActionIntent {
        return ActionIntent(
            type = ActionIntentType.CLIENT_TRANSFER,
            actor = recipientPhoneNumber.trim(),
            amount = amount,
        )
    }

    private fun validate(
        draft: ClientTransferDraft,
    ): ClientTransferFormErrors {
        val phone = draft.recipientPhoneNumber.trim()
        val amountValue = draft.amountInput.toLongOrNull()

        val phoneError = when {
            phone.isBlank() -> "Saisissez le numéro du bénéficiaire."
            phone.length < 7 -> "Le numéro paraît incomplet."
            else -> null
        }

        val amountError = when {
            draft.amountInput.isBlank() -> "Saisissez un montant."
            amountValue == null -> "Montant invalide."
            amountValue <= 0L -> "Le montant doit être supérieur à zéro."
            else -> null
        }

        return ClientTransferFormErrors(
            recipientPhoneNumber = phoneError,
            amount = amountError,
        )
    }

    companion object {
        fun factory(
            repository: ClientTransferRepository,
            idempotencyManager: IdempotencyManager,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ClientTransferViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                    ) as T
                }
            }
        }
    }
}
