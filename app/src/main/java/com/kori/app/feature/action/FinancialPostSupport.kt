package com.kori.app.feature.action

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.action.ActionIntent
import com.kori.app.core.model.action.FinancialErrorCode
import com.kori.app.domain.idempotency.IdempotencyManager
import kotlinx.coroutines.launch

data class FinancialErrorDetails(
    val code: FinancialErrorCode,
    val userMessage: String,
    val technicalMessage: String,
    val idempotencyKey: String,
)

object FinancialErrorMapper {
    fun userMessageFor(
        resources: Resources,
        code: FinancialErrorCode,
    ): String {
        return when (code) {
            FinancialErrorCode.INSUFFICIENT_FUNDS -> resources.getString(R.string.financial_error_insufficient_funds)
            FinancialErrorCode.DAILY_LIMIT_EXCEEDED -> resources.getString(R.string.financial_error_daily_limit)
            FinancialErrorCode.UNAUTHORIZED -> resources.getString(R.string.financial_error_unauthorized)
            FinancialErrorCode.INVALID_STATUS -> resources.getString(R.string.financial_error_invalid_status)
        }
    }
}

inline fun <TResult> ViewModel.submitFinancialPost(
    resources: Resources,
    idempotencyManager: IdempotencyManager,
    intent: ActionIntent,
    idempotencyKey: String,
    crossinline onSetSubmitting: (Boolean) -> Unit,
    crossinline submitCall: suspend () -> TResult,
    crossinline onBusinessResult: (TResult) -> Unit,
    crossinline onTechnicalFailure: (FinancialErrorDetails) -> Unit,
) {
    val canStart = idempotencyManager.start(intent, idempotencyKey)
    if (!canStart) return

    viewModelScope.launch {
        onSetSubmitting(true)

        runCatching {
            submitCall()
        }.onSuccess { result ->
            onBusinessResult(result)
        }.onFailure { throwable ->
            idempotencyManager.onFailure(idempotencyKey)
            onTechnicalFailure(
                FinancialErrorDetails(
                    code = FinancialErrorCode.INVALID_STATUS,
                    userMessage = resources.getString(R.string.error_network_retry),
                    technicalMessage = throwable.message ?: "Unknown technical failure while submitting financial POST.",
                    idempotencyKey = idempotencyKey,
                ),
            )
        }
    }
}
