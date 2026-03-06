package com.kori.app.feature.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.TransactionRepository

@Composable
fun TransactionDetailRoute(
    role: UserRole,
    transactionRef: String,
    repository: TransactionRepository,
    onBack: () -> Unit,
) {
    val viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModel.factory(
            role = role,
            transactionRef = transactionRef,
            repository = repository,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    TransactionDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onShareReceipt = {},
    )
}