package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.core.model.action.MerchantTransferDraft
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.data.repository.MerchantTransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MerchantTransferViewModel(
    private val repository: MerchantTransferRepository,
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
        val sanitized = value.filter { it.isDigit() }
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

        val amount = current.draft.amountInput.toLong()
        val idempotencyKey = UUID.randomUUID().toString()

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
                        MerchantTransferUiState.Success(
                            receipt = result.receipt,
                            idempotencyKey = current.quote.idempotencyKey,
                        )
                    }

                    is MerchantTransferResult.Failure -> {
                        MerchantTransferUiState.Failure(
                            code = result.code,
                            message = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            }.onFailure {
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
        _uiState.value = MerchantTransferUiState.Form()
    }

    private fun validate(
        draft: MerchantTransferDraft,
    ): MerchantTransferFormErrors {
        val merchantCode = draft.recipientMerchantCode.trim().uppercase()
        val amountValue = draft.amountInput.toLongOrNull()

        val merchantCodeError = when {
            merchantCode.isBlank() -> "Saisissez le code marchand bénéficiaire."
            merchantCode.length < 5 -> "Le code marchand paraît incomplet."
            else -> null
        }

        val amountError = when {
            draft.amountInput.isBlank() -> "Saisissez un montant."
            amountValue == null -> "Montant invalide."
            amountValue <= 0L -> "Le montant doit être supérieur à zéro."
            else -> null
        }

        return MerchantTransferFormErrors(
            recipientMerchantCode = merchantCodeError,
            amount = amountError,
        )
    }

    companion object {
        fun factory(
            repository: MerchantTransferRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MerchantTransferViewModel(
                        repository = repository,
                    ) as T
                }
            }
        }
    }
}