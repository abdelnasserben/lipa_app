package com.kori.app.feature.transactions

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.R
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.ui.timelineLabel
import com.kori.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val role: UserRole,
    private val transactionRef: String,
    private val repository: TransactionRepository,
    private val resources: Resources,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionDetailUiState>(TransactionDetailUiState.Loading)
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = TransactionDetailUiState.Loading

            runCatching {
                loadTransactionDetail(transactionRef)
            }.onSuccess { transaction ->
                _uiState.value = TransactionDetailUiState.Content(
                    transaction = transaction,
                    timeline = buildTimeline(transaction),
                )
            }.onFailure {
                _uiState.value = TransactionDetailUiState.Error(
                    message = resources.getString(R.string.transaction_detail_error_message),
                )
            }
        }
    }

    private suspend fun loadTransactionDetail(ref: String): TransactionItemResponse {
        return when (role) {
            UserRole.CLIENT -> repository.getClientTransactionDetail(ref)
            UserRole.MERCHANT -> repository.getMerchantTransactionDetail(ref)
            UserRole.AGENT -> repository.getAgentTransactionDetail(ref)
        }
    }

    private fun buildTimeline(
        transaction: TransactionItemResponse,
    ): List<TransactionTimelineStep> {
        val finalCompleted = transaction.status == TransactionStatus.COMPLETED
        val finalFailed = transaction.status == TransactionStatus.FAILED
        val finalReversed = transaction.status == TransactionStatus.REVERSED
        val finalPending = transaction.status == TransactionStatus.PENDING

        val lastTitle = when {
            finalCompleted -> transaction.status.timelineLabel(resources)
            finalFailed -> transaction.status.timelineLabel(resources)
            finalReversed -> transaction.status.timelineLabel(resources)
            finalPending -> transaction.status.timelineLabel(resources)
            else -> transaction.status.timelineLabel(resources)
        }

        return listOf(
            TransactionTimelineStep(
                title = resources.getString(R.string.transaction_timeline_started),
                isCompleted = true,
            ),
            TransactionTimelineStep(
                title = lastTitle,
                isCompleted = finalCompleted || finalFailed || finalReversed || finalPending,
            ),
        )
    }

    companion object {
        fun factory(
            role: UserRole,
            transactionRef: String,
            repository: TransactionRepository,
            resources: Resources,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionDetailViewModel(
                        role = role,
                        transactionRef = transactionRef,
                        repository = repository,
                        resources = resources,
                    ) as T
                }
            }
        }
    }
}
