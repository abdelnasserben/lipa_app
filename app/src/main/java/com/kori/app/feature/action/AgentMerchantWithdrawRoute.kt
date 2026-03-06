package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentActionRepository

@Composable
fun AgentMerchantWithdrawRoute(
    repository: AgentActionRepository,
) {
    val viewModel: AgentMerchantWithdrawViewModel = viewModel(
        factory = AgentMerchantWithdrawViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsState()

    AgentMerchantWithdrawScreen(
        uiState = uiState,
        onMerchantCodeChanged = viewModel::onMerchantCodeChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onContinue = viewModel::requestQuote,
        onOpenConfirmDialog = viewModel::openConfirmDialog,
        onDismissConfirmDialog = viewModel::dismissConfirmDialog,
        onConfirm = viewModel::submit,
        onEdit = viewModel::edit,
        onRestart = viewModel::restart,
    )
}