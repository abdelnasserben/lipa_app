package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.AgentMerchantWithdrawDraft
import com.kori.app.core.model.action.AgentMerchantWithdrawResult
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.core.ui.KmfAmountFormatters
import com.kori.app.data.repository.AgentActionRepository
import com.kori.app.domain.idempotency.IdempotencyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgentMerchantWithdrawViewModel(
    private val repository: AgentActionRepository,
    private val idempotencyManager: IdempotencyManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentMerchantWithdrawUiState>(AgentMerchantWithdrawUiState.Form())
    val uiState: StateFlow<AgentMerchantWithdrawUiState> = _uiState.asStateFlow()

    fun onMerchantCodeChanged(value: String) {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(merchantCode = FinancialInputRules.normalizeMerchantCodeInput(value)),
            errors = current.errors.copy(merchantCode = null),
        )
    }

    fun onAmountChanged(value: String) {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = KmfAmountFormatters.normalizeInput(value)),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Form ?: return
        val errors = validate(current.draft)
        if (errors.merchantCode != null || errors.amount != null || current.isLoading) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = KmfAmountFormatters.parseToLong(current.draft.amountInput) ?: return
        val apiMerchantCode = FinancialInputRules.merchantCodeToApi(current.draft.merchantCode) ?: return
        val intent = createActionIntent(merchantCode = apiMerchantCode, amount = amount)
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteMerchantWithdraw(
                    merchantCode = apiMerchantCode,
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = AgentMerchantWithdrawUiState.Confirmation(quote = quote)
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

        val intent = createActionIntent(merchantCode = current.quote.merchantCode, amount = current.quote.amount)

        submitFinancialPost(
            idempotencyManager = idempotencyManager,
            intent = intent,
            idempotencyKey = current.quote.idempotencyKey,
            onSetSubmitting = { isSubmitting ->
                val latest = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return@submitFinancialPost
                _uiState.value = latest.copy(isSubmitting = isSubmitting, isConfirmDialogVisible = false)
            },
            submitCall = { repository.submitMerchantWithdraw(current.quote) },
            onBusinessResult = { result ->
                _uiState.value = when (result) {
                    is AgentMerchantWithdrawResult.Success -> {
                        idempotencyManager.onSuccess(current.quote.idempotencyKey)
                        AgentMerchantWithdrawUiState.Success(
                            receipt = result.receipt,
                            idempotencyKey = current.quote.idempotencyKey,
                        )
                    }

                    is AgentMerchantWithdrawResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        AgentMerchantWithdrawUiState.Failure(
                            code = result.code,
                            userMessage = FinancialErrorMapper.userMessageFor(result.code),
                            technicalMessage = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            },
            onTechnicalFailure = { failure ->
                _uiState.value = AgentMerchantWithdrawUiState.Failure(
                    code = failure.code,
                    userMessage = failure.userMessage,
                    technicalMessage = failure.technicalMessage,
                    idempotencyKey = failure.idempotencyKey,
                )
            },
        )
    }

    fun edit() {
        val state = _uiState.value
        if (state is AgentMerchantWithdrawUiState.Confirmation) {
            idempotencyManager.clear(createActionIntent(merchantCode = state.quote.merchantCode, amount = state.quote.amount))
        }

        val draft = when (state) {
            is AgentMerchantWithdrawUiState.Form -> state.draft
            is AgentMerchantWithdrawUiState.Confirmation -> AgentMerchantWithdrawDraft(
                merchantCode = FinancialInputRules.normalizeMerchantCodeInput(state.quote.merchantCode),
                amountInput = state.quote.amount.toString(),
            )

            is AgentMerchantWithdrawUiState.Success -> AgentMerchantWithdrawDraft()
            is AgentMerchantWithdrawUiState.Failure -> AgentMerchantWithdrawDraft()
        }
        _uiState.value = AgentMerchantWithdrawUiState.Form(draft = draft)
    }

    fun restart() {
        val state = _uiState.value
        if (state is AgentMerchantWithdrawUiState.Confirmation) {
            idempotencyManager.clear(createActionIntent(merchantCode = state.quote.merchantCode, amount = state.quote.amount))
        }
        _uiState.value = AgentMerchantWithdrawUiState.Form()
    }

    private fun createActionIntent(merchantCode: String, amount: Long): ActionIntent {
        return ActionIntent(type = ActionIntentType.MERCHANT_WITHDRAW, actor = merchantCode.trim().uppercase(), amount = amount)
    }

    private fun validate(draft: AgentMerchantWithdrawDraft): AgentMerchantWithdrawFormErrors {
        return AgentMerchantWithdrawFormErrors(
            merchantCode = FinancialInputRules.validateMerchantCode(draft.merchantCode),
            amount = KmfAmountFormatters.validateAmount(
                rawInput = draft.amountInput,
                min = FinancialFlowRules.MERCHANT_WITHDRAW_MIN_AMOUNT,
                max = FinancialFlowRules.MERCHANT_WITHDRAW_MAX_AMOUNT,
            ),
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
                    return AgentMerchantWithdrawViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                    ) as T
                }
            }
        }
    }
}
