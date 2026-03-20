package com.kori.app.feature.action

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
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
    private val resources: Resources,
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
                _uiState.value = AgentMerchantWithdrawUiState.Confirmation(model = FinancialConfirmationModel(quote = quote))
            }.onFailure {
                _uiState.value = current.copy(
                    isLoading = false,
                    errors = current.errors.copy(
                        amount = resources.getString(R.string.financial_quote_unavailable),
                    ),
                )
            }
        }
    }

    fun openConfirmDialog() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.model.isSubmitting) return
        _uiState.value = current.copy(model = current.model.copy(isConfirmDialogVisible = true))
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.model.isSubmitting) return
        _uiState.value = current.copy(model = current.model.copy(isConfirmDialogVisible = false))
    }

    fun submit() {
        val current = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return
        if (current.model.isSubmitting) return

        val intent = createActionIntent(merchantCode = current.model.quote.merchantCode, amount = current.model.quote.amount)

        submitFinancialPost(
            resources = resources,
            idempotencyManager = idempotencyManager,
            intent = intent,
            idempotencyKey = current.model.quote.idempotencyKey,
            onSetSubmitting = { isSubmitting ->
                val latest = _uiState.value as? AgentMerchantWithdrawUiState.Confirmation ?: return@submitFinancialPost
                _uiState.value = latest.copy(model = latest.model.copy(isSubmitting = isSubmitting, isConfirmDialogVisible = false))
            },
            submitCall = { repository.submitMerchantWithdraw(current.model.quote) },
            onBusinessResult = { result ->
                _uiState.value = when (result) {
                    is AgentMerchantWithdrawResult.Success -> {
                        idempotencyManager.onSuccess(current.model.quote.idempotencyKey)
                        AgentMerchantWithdrawUiState.Success(
                            model = FinancialSuccessModel(
                                receipt = result.receipt,
                                idempotencyKey = current.model.quote.idempotencyKey,
                            ),
                        )
                    }

                    is AgentMerchantWithdrawResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        AgentMerchantWithdrawUiState.Failure(
                            model = FinancialFailureModel(
                                code = result.code,
                                userMessage = FinancialErrorMapper.userMessageFor(resources, result.code),
                                technicalMessage = result.message,
                                idempotencyKey = result.idempotencyKey,
                            ),
                        )
                    }
                }
            },
            onTechnicalFailure = { failure ->
                _uiState.value = AgentMerchantWithdrawUiState.Failure(
                    model = failure.toFailureModel(),
                )
            },
        )
    }

    fun edit() {
        val state = _uiState.value
        if (state is AgentMerchantWithdrawUiState.Confirmation) {
            idempotencyManager.clear(createActionIntent(merchantCode = state.model.quote.merchantCode, amount = state.model.quote.amount))
        }

        val draft = when (state) {
            is AgentMerchantWithdrawUiState.Form -> state.draft
            is AgentMerchantWithdrawUiState.Confirmation -> AgentMerchantWithdrawDraft(
                merchantCode = FinancialInputRules.normalizeMerchantCodeInput(state.model.quote.merchantCode),
                amountInput = state.model.quote.amount.toString(),
            )

            is AgentMerchantWithdrawUiState.Success -> AgentMerchantWithdrawDraft()
            is AgentMerchantWithdrawUiState.Failure -> AgentMerchantWithdrawDraft()
        }
        _uiState.value = AgentMerchantWithdrawUiState.Form(draft = draft)
    }

    fun restart() {
        val state = _uiState.value
        if (state is AgentMerchantWithdrawUiState.Confirmation) {
            idempotencyManager.clear(createActionIntent(merchantCode = state.model.quote.merchantCode, amount = state.model.quote.amount))
        }
        _uiState.value = AgentMerchantWithdrawUiState.Form()
    }

    private fun createActionIntent(merchantCode: String, amount: Long): ActionIntent {
        return ActionIntent(type = ActionIntentType.MERCHANT_WITHDRAW, actor = merchantCode.trim().uppercase(), amount = amount)
    }

    private fun validate(draft: AgentMerchantWithdrawDraft): AgentMerchantWithdrawFormErrors {
        return AgentMerchantWithdrawFormErrors(
            merchantCode = FinancialInputRules.validateMerchantCode(
                raw = draft.merchantCode,
                resources = resources,
                fieldLabelResId = R.string.withdraw_merchant_label,
            ),
            amount = KmfAmountFormatters.validateAmount(
                resources = resources,
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
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AgentMerchantWithdrawViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                        resources = resources,
                    ) as T
                }
            }
        }
    }
}
