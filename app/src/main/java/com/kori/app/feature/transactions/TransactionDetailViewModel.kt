package com.kori.app.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionItemResponse
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.data.repository.TransactionQuery
import com.kori.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val role: UserRole,
    private val transactionRef: String,
    private val repository: TransactionRepository,
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
                findTransaction(transactionRef)
            }.onSuccess { transaction ->
                if (transaction == null) {
                    _uiState.value = TransactionDetailUiState.Error(
                        message = "Cette transaction est introuvable.",
                    )
                } else {
                    _uiState.value = TransactionDetailUiState.Content(
                        transaction = transaction,
                        timeline = buildTimeline(transaction),
                    )
                }
            }.onFailure {
                _uiState.value = TransactionDetailUiState.Error(
                    message = "Impossible de charger le détail de la transaction.",
                )
            }
        }
    }

    private suspend fun findTransaction(
        ref: String,
    ): TransactionItemResponse? {
        val query = TransactionQuery(limit = 50, sort = "-createdAt")

        val items = when (role) {
            UserRole.CLIENT -> repository.getClientTransactions(query).items
            UserRole.MERCHANT -> repository.getMerchantTransactions(query).items
            UserRole.AGENT -> repository.getAgentTransactions(query).items
        }

        return items.firstOrNull { it.transactionRef == ref }
    }

    private fun buildTimeline(
        transaction: TransactionItemResponse,
    ): List<TransactionTimelineStep> {
        val finalCompleted = transaction.status == TransactionStatus.COMPLETED
        val finalFailed = transaction.status == TransactionStatus.FAILED
        val finalReversed = transaction.status == TransactionStatus.REVERSED
        val finalPending = transaction.status == TransactionStatus.PENDING

        val lastTitle = when {
            finalCompleted -> "Completed"
            finalFailed -> "Failed"
            finalReversed -> "Reversed"
            finalPending -> "Pending"
            else -> transaction.status.name
        }

        return listOf(
            TransactionTimelineStep(
                title = "Requested",
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
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionDetailViewModel(
                        role = role,
                        transactionRef = transactionRef,
                        repository = repository,
                    ) as T
                }
            }
        }
    }
}