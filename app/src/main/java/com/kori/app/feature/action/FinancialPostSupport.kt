package com.kori.app.feature.action

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    fun userMessageFor(code: FinancialErrorCode): String {
        return when (code) {
            FinancialErrorCode.INSUFFICIENT_FUNDS -> "Solde insuffisant pour terminer cette opération."
            FinancialErrorCode.DAILY_LIMIT_EXCEEDED -> "Votre plafond autorisé est atteint pour le moment."
            FinancialErrorCode.UNAUTHORIZED -> "Votre session ne permet pas cette action actuellement."
            FinancialErrorCode.INVALID_STATUS -> "L'opération est momentanément indisponible."
        }
    }
}

inline fun <TResult> ViewModel.submitFinancialPost(
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
                    userMessage = "Une erreur réseau est survenue. Réessayez dans un instant.",
                    technicalMessage = throwable.message ?: "Unknown technical failure while submitting financial POST.",
                    idempotencyKey = idempotencyKey,
                ),
            )
        }
    }
}
