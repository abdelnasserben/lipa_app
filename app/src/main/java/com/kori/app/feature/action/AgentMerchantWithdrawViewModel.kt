package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.AgentMerchantWithdrawDraft
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.data.repository.AgentActionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AgentMerchantWithdrawViewModel(
    private val repository: AgentActionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentMerchantWithdrawUiState>(AgentMerchantWithdrawUiState.Form())
    val uiState: StateFlow<AgentMerchantWithdrawUiState> = _uiState.asStateFlow()

    fun onMerchantCodeChanged(value: String) {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(merchantCode = value.uppercase()),
            errors = current.errors.copy(merchantCode = null),
        )
    }

    fun onAmountChanged(value: String) {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = value.filter { it.isDigit() }),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        val errors = validate(current.draft)
        if (errors.merchantCode != null || errors.amount != null) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = current.draft.amountInput.toLong()
        val idempotencyKey = UUID.randomUUID().toString()

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteMerchantWithdraw(
                    merchantCode = current.draft.merchantCode.trim().uppercase(),
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = AgentMerchantWithdrawUiState.Confirmation(
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
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.isSubmitting) return
        _uiState.value = current.copy(isConfirmDialogVisible = true)
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.isSubmitting) return
        _uiState.value = current.copy(isConfirmDialogVisible = false)
    }

    fun submit() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.isSubmitting) return

        viewModelScope.launch {
            _uiState.value = current.copy(
                isSubmitting = true,
                isConfirmDialogVisible = false,
            )

            runCatching {
                repository.submitMerchantWithdraw(current.quote)
            }.onSuccess { result ->
                _uiState.value = when (result) {
                    is AgentMerchantWithdrawResult.Success -> AgentMerchantWithdrawUiState.Success(
                        receipt = result.receipt,
                        idempotencyKey = current.quote.idempotencyKey,
                    )

                    is AgentMerchantWithdrawResult.Failure -> AgentMerchantWithdrawUiState.Failure(
                        code = result.code,
                        message = result.message,
                        idempotencyKey = result.idempotencyKey,
                    )
                }
            }.onFailure {
                _uiState.value = AgentMerchantWithdrawUiState.Failure(
                    code = FinancialErrorCode.INVALID_STATUS,
                    message = "Une erreur inattendue est survenue pendant le retrait marchand.",
                    idempotencyKey = current.quote.idempotencyKey,
                )
            }
        }
    }

    fun edit() {
        val state = _uiState.value
        val draft = when (state) {
            is AgentMerchantWithdrawUiState.Form -> state.draft
            is AgentMerchantWithdrawUiState.Confirmation -> AgentMerchantWithdrawDraft(
                merchantCode = state.quote.merchantCode,
                amountInput = state.quote.amount.toString(),
            )

            is AgentMerchantWithdrawUiState.Success -> AgentMerchantWithdrawDraft()
            is AgentMerchantWithdrawUiState.Failure -> AgentMerchantWithdrawDraft()
        }
        _uiState.value = AgentMerchantWithdrawUiState.Form(draft = draft)
    }

    fun restart() {
        _uiState.value = AgentMerchantWithdrawUiState.Form()
    }

    private fun validate(draft: AgentMerchantWithdrawDraft): AgentMerchantWithdrawFormErrors {
        val merchantCode = draft.merchantCode.trim().uppercase()
        val amount = draft.amountInput.toLongOrNull()

        return AgentMerchantWithdrawFormErrors(
            merchantCode = when {
                merchantCode.isBlank() -> "Saisissez le code marchand."
                merchantCode.length < 5 -> "Le code marchand paraît incomplet."
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
                    return AgentMerchantWithdrawViewModel(repository) as T
                }
            }
        }
    }
}