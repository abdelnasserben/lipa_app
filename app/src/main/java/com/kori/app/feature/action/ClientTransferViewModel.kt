package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.ActionIntentType
import com.kori.app.core.model.action.ClientTransferDraft
import com.kori.app.core.model.action.ClientTransferResult
import com.kori.app.core.ui.FinancialInputRules
import com.kori.app.core.ui.KmfAmountFormatters
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
            draft = current.draft.copy(recipientPhoneNumber = FinancialInputRules.normalizeComorosPhoneInput(value)),
            errors = current.errors.copy(recipientPhoneNumber = null),
        )
    }

    fun onAmountChanged(value: String) {
        val sanitized = KmfAmountFormatters.normalizeInput(value)
        val current = _uiState.value as? ClientTransferUiState.Form ?: return
        _uiState.value = current.copy(
            draft = current.draft.copy(amountInput = sanitized),
            errors = current.errors.copy(amount = null),
        )
    }

    fun requestQuote() {
        val current = _uiState.value as? ClientTransferUiState.Form ?: return
        val errors = validate(current.draft)

        if (errors.recipientPhoneNumber != null || errors.amount != null || current.isLoading) {
            _uiState.value = current.copy(errors = errors)
            return
        }

        val amount = KmfAmountFormatters.parseToLong(current.draft.amountInput) ?: return
        val apiPhone = FinancialInputRules.comorosPhoneToApi(current.draft.recipientPhoneNumber) ?: return
        val intent = createActionIntent(recipientPhoneNumber = apiPhone, amount = amount)
        val idempotencyKey = idempotencyManager.getOrCreateIdempotencyKey(intent)

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            runCatching {
                repository.quoteTransfer(
                    recipientPhoneNumber = apiPhone,
                    amount = amount,
                    idempotencyKey = idempotencyKey,
                )
            }.onSuccess { quote ->
                _uiState.value = ClientTransferUiState.Confirmation(quote = quote)
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

        _uiState.value = current.copy(isConfirmDialogVisible = true)
    }

    fun dismissConfirmDialog() {
        val current = _uiState.value as? ClientTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        _uiState.value = current.copy(isConfirmDialogVisible = false)
    }

    fun submitTransfer() {
        val current = _uiState.value as? ClientTransferUiState.Confirmation ?: return
        if (current.isSubmitting) return

        val intent = createActionIntent(
            recipientPhoneNumber = current.quote.recipientPhoneNumber,
            amount = current.quote.amount,
        )

        submitFinancialPost(
            idempotencyManager = idempotencyManager,
            intent = intent,
            idempotencyKey = current.quote.idempotencyKey,
            onSetSubmitting = { isSubmitting ->
                val latest = _uiState.value as? ClientTransferUiState.Confirmation ?: return@submitFinancialPost
                _uiState.value = latest.copy(isSubmitting = isSubmitting, isConfirmDialogVisible = false)
            },
            submitCall = { repository.submitTransfer(current.quote) },
            onBusinessResult = { result ->
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
                            userMessage = FinancialErrorMapper.userMessageFor(result.code),
                            technicalMessage = result.message,
                            idempotencyKey = result.idempotencyKey,
                        )
                    }
                }
            },
            onTechnicalFailure = { failure ->
                _uiState.value = ClientTransferUiState.Failure(
                    code = failure.code,
                    userMessage = failure.userMessage,
                    technicalMessage = failure.technicalMessage,
                    idempotencyKey = failure.idempotencyKey,
                )
            },
        )
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
                recipientPhoneNumber = FinancialInputRules.normalizeComorosPhoneInput(state.quote.recipientPhoneNumber),
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
        return ClientTransferFormErrors(
            recipientPhoneNumber = FinancialInputRules.validateComorosPhone(
                draft.recipientPhoneNumber,
                "le numéro du bénéficiaire",
            ),
            amount = KmfAmountFormatters.validateAmount(
                rawInput = draft.amountInput,
                min = FinancialFlowRules.CLIENT_TRANSFER_MIN_AMOUNT,
                max = FinancialFlowRules.CLIENT_TRANSFER_MAX_AMOUNT,
            ),
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
