package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.ClientTransferRepository
import com.kori.app.domain.idempotency.IdempotencyManager

@Composable
fun ClientTransferRoute(
    repository: ClientTransferRepository,
    idempotencyManager: IdempotencyManager,
) {
    val viewModel: ClientTransferViewModel = viewModel(
        factory = ClientTransferViewModel.factory(repository, idempotencyManager),
    )

    val uiState by viewModel.uiState.collectAsState()

    ClientTransferScreen(
        uiState = uiState,
        onRecipientChanged = viewModel::onRecipientChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onContinue = viewModel::requestQuote,
        onOpenConfirmDialog = viewModel::openConfirmDialog,
        onDismissConfirmDialog = viewModel::dismissConfirmDialog,
        onConfirm = viewModel::submitTransfer,
        onEdit = viewModel::editForm,
        onRestart = viewModel::restart,
    )
}
