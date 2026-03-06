package com.kori.app.feature.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.TransactionRepository

@Composable
fun TransactionsRoute(
    role: UserRole,
    repository: TransactionRepository,
    onTransactionClick: (String) -> Unit,
) {
    val viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.factory(
            role = role,
            repository = repository,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    TransactionsScreen(
        role = role,
        uiState = uiState,
        onRetry = viewModel::refresh,
        onApplyFilters = viewModel::applyFilters,
        onClearFilters = viewModel::clearFilters,
        onLoadMore = viewModel::loadMore,
        onTransactionClick = onTransactionClick,
    )
}