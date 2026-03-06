package com.kori.app.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kori.app.core.model.UserRole
import com.kori.app.core.model.transaction.TransactionStatus
import com.kori.app.core.model.transaction.TransactionType
import com.kori.app.data.repository.TransactionQuery
import com.kori.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val role: UserRole,
    private val repository: TransactionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransactionsUiState>(TransactionsUiState.Loading)
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    private var currentFilters = TransactionsFilterState()

    init {
        refresh()
    }

    fun refresh() {
        loadWithFilters(currentFilters)
    }

    fun applyFilters(filters: TransactionsFilterState) {
        currentFilters = filters
        loadWithFilters(currentFilters)
    }

    fun clearFilters() {
        currentFilters = TransactionsFilterState()
        loadWithFilters(currentFilters)
    }

    fun loadMore() {
        val content = _uiState.value as? TransactionsUiState.Content ?: return
        if (content.state.isLoadingMore || !content.state.hasMore) return

        viewModelScope.launch {
            _uiState.value = content.copy(
                state = content.state.copy(isLoadingMore = true),
            )

            runCatching {
                fetchPage(
                    cursor = content.state.nextCursor,
                    filters = currentFilters,
                )
            }.onSuccess { result ->
                _uiState.value = TransactionsUiState.Content(
                    content.state.copy(
                        items = content.state.items + result.items,
                        isLoadingMore = false,
                        hasMore = result.page.hasMore,
                        nextCursor = result.page.nextCursor,
                        filters = currentFilters,
                    ),
                )
            }.onFailure {
                _uiState.value = content.copy(
                    state = content.state.copy(isLoadingMore = false),
                )
            }
        }
    }

    private fun loadWithFilters(filters: TransactionsFilterState) {
        viewModelScope.launch {
            _uiState.value = TransactionsUiState.Loading

            runCatching {
                fetchPage(cursor = null, filters = filters)
            }.onSuccess { result ->
                _uiState.value = if (result.items.isEmpty()) {
                    TransactionsUiState.Empty(filters = filters)
                } else {
                    TransactionsUiState.Content(
                        TransactionsContentState(
                            items = result.items,
                            filters = filters,
                            isLoadingInitial = false,
                            isLoadingMore = false,
                            hasMore = result.page.hasMore,
                            nextCursor = result.page.nextCursor,
                        ),
                    )
                }
            }.onFailure {
                _uiState.value = TransactionsUiState.Error(
                    message = "Impossible de charger les transactions pour le moment.",
                )
            }
        }
    }

    private suspend fun fetchPage(
        cursor: String?,
        filters: TransactionsFilterState,
    ) = when (role) {
        UserRole.CLIENT -> repository.getClientTransactions(
            query = buildQuery(cursor, filters),
        )

        UserRole.MERCHANT -> repository.getMerchantTransactions(
            query = buildQuery(cursor, filters),
        )

        UserRole.AGENT -> repository.getAgentTransactions(
            query = buildQuery(cursor, filters),
        )
    }

    private fun buildQuery(
        cursor: String?,
        filters: TransactionsFilterState,
    ): TransactionQuery {
        return TransactionQuery(
            type = filters.selectedType?.let(TransactionType::valueOf),
            status = filters.selectedStatus?.let(TransactionStatus::valueOf),
            from = filters.from.takeIf { it.isNotBlank() },
            to = filters.to.takeIf { it.isNotBlank() },
            minAmount = filters.minAmount.toLongOrNull(),
            maxAmount = filters.maxAmount.toLongOrNull(),
            cursor = cursor,
            limit = 10,
            sort = filters.sort.backendValue,
        )
    }

    companion object {
        fun factory(
            role: UserRole,
            repository: TransactionRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TransactionsViewModel(
                        role = role,
                        repository = repository,
                    ) as T
                }
            }
        }
    }
}