package com.kori.app.feature.action

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.MerchantTransferDraft
import com.kori.app.core.model.action.MerchantTransferResult
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.core.ui.KmfAmountFormatters
import com.kori.app.data.repository.MerchantTransferRepository
import com.kori.app.domain.idempotency.IdempotencyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MerchantTransferViewModel(
    private val repository: MerchantTransferRepository,
    private val idempotencyManager: IdempotencyManager,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MerchantTransferUiState>(MerchantTransferUiState.Form())
    val uiState: StateFlow<MerchantTransferUiState> = _uiState.asStateFlow()

    fun onRecipientChanged(value: String) {
        val current = _uiState.value as? MerchantTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(recipientMerchantCode = FinancialInputRules.normalizeMerchantCodeInput(value)),
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

        if (errors.recipientMerchantCode != null || errors.amount != null || current.isLoading) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = KmfAmountFormatters.parseToLong(current.draft.amountInput) ?: return
        val apiMerchantCode = FinancialInputRules.merchantCodeToApi(current.draft.recipientMerchantCode) ?: return
        val intent = createActionIntent(recipientMerchantCode = apiMerchantCode, amount = amount)
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteTransfer(
                    recipientMerchantCode = apiMerchantCode,
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = MerchantTransferUiState.Confirmation(model = FinancialConfirmationModel(quote = quote))
            }.onFailure {
                _uiState.value = current.copy(
                    isLoading = false,
                    errors = current.errors.copy(
                        amount = resources.getString(R.string.merchant_transfer_quote_unavailable),
                    ),
                )
            }
        }
    }

    fun openConfirmDialog() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.model.isSubmitting) return

        _uiState.value = current.copy(model = current.model.copy(isConfirmDialogVisible = true))
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.model.isSubmitting) return

        _uiState.value = current.copy(model = current.model.copy(isConfirmDialogVisible = false))
    }

    fun submitTransfer() {
        val current = _uiState.value as? MerchantTransferUiState.Confirmation ?: return
        if (current.model.isSubmitting) return

        val intent = createActionIntent(
            recipientMerchantCode = current.model.quote.recipientMerchantCode,
            amount = current.model.quote.amount,
        )

        submitFinancialPost(
            resources = resources,
            idempotencyManager = idempotencyManager,
            intent = intent,
            idempotencyKey = current.model.quote.idempotencyKey,
            onSetSubmitting = { isSubmitting ->
                val latest = _uiState.value as? MerchantTransferUiState.Confirmation ?: return@submitFinancialPost
                _uiState.value = latest.copy(model = latest.model.copy(isSubmitting = isSubmitting, isConfirmDialogVisible = false))
            },
            submitCall = { repository.submitTransfer(current.model.quote) },
            onBusinessResult = { result ->
                _uiState.value = when (result) {
                    is MerchantTransferResult.Success -> {
                        idempotencyManager.onSuccess(current.model.quote.idempotencyKey)
                        MerchantTransferUiState.Success(
                            model = FinancialSuccessModel(
                                receipt = result.receipt,
                                idempotencyKey = current.model.quote.idempotencyKey,
                            ),
                        )
                    }

                    is MerchantTransferResult.Failure -> {
                        idempotencyManager.onFailure(result.idempotencyKey)
                        MerchantTransferUiState.Failure(
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
                _uiState.value = MerchantTransferUiState.Failure(
                    model = failure.toFailureModel(),
                )
            },
        )
    }

    fun editForm() {
        val state = _uiState.value
        if (state is MerchantTransferUiState.Confirmation) {
            idempotencyManager.clear(
                createActionIntent(
                    recipientMerchantCode = state.model.quote.recipientMerchantCode,
                    amount = state.model.quote.amount,
                ),
            )
        }

        val draft = when (state) {
            is MerchantTransferUiState.Form -> state.draft
            is MerchantTransferUiState.Confirmation -> MerchantTransferDraft(
                recipientMerchantCode = FinancialInputRules.normalizeMerchantCodeInput(state.model.quote.recipientMerchantCode),
                amountInput = state.model.quote.amount.toString(),
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
                    recipientMerchantCode = state.model.quote.recipientMerchantCode,
                    amount = state.model.quote.amount,
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
        return MerchantTransferFormErrors(
            recipientMerchantCode = FinancialInputRules.validateMerchantCode(
                draft.recipientMerchantCode,
                resources = resources,
                fieldLabelResId = R.string.merchant_transfer_recipient_label,
            ),
            amount = KmfAmountFormatters.validateAmount(
                resources = resources,
                rawInput = draft.amountInput,
                min = FinancialFlowRules.MERCHANT_TRANSFER_MIN_AMOUNT,
                max = FinancialFlowRules.MERCHANT_TRANSFER_MAX_AMOUNT,
            ),
        )
    }

    companion object {
        fun factory(
            repository: MerchantTransferRepository,
            idempotencyManager: IdempotencyManager,
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MerchantTransferViewModel(
                        repository = repository,
                        idempotencyManager = idempotencyManager,
                        resources = resources,
                    ) as T
                }
            }
        }
    }
}
