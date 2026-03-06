package com.kori.app.feature.transactions

import com.kori.app.core.model.transaction.TransactionItemResponse

data class TransactionsFilterState(
    val selectedType: String? = null,
    val selectedStatus: String? = null,
)

data class TransactionsContentState(
    val items: List<TransactionItemResponse> = emptyList(),
    val filters: TransactionsFilterState = TransactionsFilterState(),
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
)

sealed interface TransactionsUiState {
    data object Loading : TransactionsUiState
    data object Empty : TransactionsUiState
    data class Error(val message: String) : TransactionsUiState
    data class Content(val state: TransactionsContentState) : TransactionsUiState
}