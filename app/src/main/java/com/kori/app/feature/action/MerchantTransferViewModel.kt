package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.core.model.action.MerchantTransferDraft
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.data.repository.MerchantTransferRepository
import com.kori.app.domain.idempotency.IdempotencyManager
import com.kori.app.core.ui.KmfAmountFormatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantTransferViewModel(
    private val repository: MerchantTransferRepository,
    private val idempotencyManager: IdempotencyManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantTransferUiState>(MerchantTransferUiState.Form())
    val uiState: StateFlow<MerchantTransferUiState> = _uiState.asStateFlow()

    fun onRecipientChanged(value: String) {
        val current = _uiState.value as? MerchantTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(recipientMerchantCode = value.uppercase()),
            errors = current.errors.copy(recipientMerchantCode = null),
        )
    }

    fun onAmountChanged(value: String) {
        val sanitized = KmfAmountFormatters.normalizeInput(value)
        val current = _uiState.value as? MerchantTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = sanitized),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? MerchantTransferUiState.Form ?: return
        val errors = validate(current.draft)

        if (errors.recipientMerchantCode != null || errors.amount != null) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = KmfAmountFormatters.parseToLong(current.draft.amountInput) ?: return
        val intent = createActionIntent(
            recipientMerchantCode = current.draft.recipientMerchantCode,
            amount = amount,
        )
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteTransfer(
                    recipientMerchantCode = current.draft.recipientMerchantCode.trim().uppercase(),
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = MerchantTransferUiState.Confirmation(
                    quote = quote,
                    isSubmitting = false,
                    isConfirmDialogVisible = false,
                )
            }.onFailure {
                _uiState.value = current.copy(
                    isLoading = false,
                    errors = current.errors.copy(
                        amount = "Impossible de préparer ce transfert pour le moment.",
                    ),
                )
            }
        }
    }

    fun openConfirmDialog() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        _uiState.value = current.copy(
            isConfirmDialogVisible = true,
        )
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        _uiState.value = current.copy(
            isConfirmDialogVisible = false,
        )
    }

    fun submitTransfer() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        val intent = createActionIntent(
            recipientMerchantCode = current.quote.recipientMerchantCode,
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
                    is MerchantTransferResult.Success -> {
                        idempotencyManager.onSuccess(current.quote.idempotencyKey)
                        MerchantTransferUiState.Success(
                            receipt = result.receipt,
                            idempotencyKey = current.quote.idempotencyKey,
                        )
                    }

                    is MerchantTransferResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        MerchantTransferUiState.Failure(
                            code = result.code,
                            message = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            }.onFailure {
                idempotencyManager.onFailure(current.quote.idempotencyKey)
                _uiState.value = MerchantTransferUiState.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Une erreur inattendue est survenue pendant le transfert marchand.",
                    idempotencyKey = current.quote.idempotencyKey,
                )
            }
        }
    }

    fun editForm() {
        val state = _uiState.value
        if (state is MerchantTransferUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    recipientMerchantCode = state.quote.recipientMerchantCode,
                    amount = state.quote.amount,
                ),
            )
        }

        val draft = when (state) {
            is MerchantTransferUiState.Form -> state.draft
            is MerchantTransferUiState.Confirmation -> MerchantTransferDraft(
                recipientMerchantCode = state.quote.recipientMerchantCode,
                amountInput = state.quote.amount.toString(),
            )

            is MerchantTransferUiState.Success -> MerchantTransferDraft()
            is MerchantTransferUiState.Failure -> MerchantTransferDraft()
        }

        _uiState.value = MerchantTransferUiState.Form(draft = draft)
    }

    fun restart() {
        val state = _uiState.value
        if (state is MerchantTransferUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    recipientMerchantCode = state.quote.recipientMerchantCode,
                    amount = state.quote.amount,
                ),
            )
        }
        _uiState.value = MerchantTransferUiState.Form()
    }

    private fun createActionIntent(
        recipientMerchantCode: String,
        amount: Long,
    ): ActionIntent {
        return ActionIntent(
            type = ActionIntentType.MERCHANT_TRANSFER,
            actor = recipientMerchantCode.trim().uppercase(),
            amount = amount,
        )
    }

    private fun validate(
        draft: MerchantTransferDraft,
    ): MerchantTransferFormErrors {
        val merchantCode = draft.recipientMerchantCode.trim().uppercase()
        val merchantCodeError = when {
            merchantCode.isBlank() -> "Saisissez le code marchand bénéficiaire."
            merchantCode.length < 5 -> "Le code marchand paraît incomplet."
            else -> null
        }

        val amountError = KmfAmountFormatters.validateAmount(draft.amountInput)

        return MerchantTransferFormErrors(
            recipientMerchantCode = merchantCodeError,
            amount = amountError,
        )
    }

    companion object {
        fun factory(
            repository: MerchantTransferRepository,
            idempotencyManager: IdempotencyManager,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MerchantTransferViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                    ) as T
                }
            }
        }
    }
}
