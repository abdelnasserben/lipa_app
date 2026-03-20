package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.MerchantTransferRepository
import com.kori.app.domain.idempotency.IdempotencyManager

@Composable
fun MerchantTransferRoute(
    repository: MerchantTransferRepository,
    idempotencyManager: IdempotencyManager,
) {
    val resources = LocalContext.current.resources
    val viewModel: MerchantTransferViewModel = viewModel(
        factory = MerchantTransferViewModel.factory(repository, idempotencyManager, resources),
    )

    val uiState by viewModel.uiState.collectAsState()

    MerchantTransferScreen(
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
