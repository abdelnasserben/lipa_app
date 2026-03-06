package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentActionRepository

@Composable
fun AgentCashInRoute(
    repository: AgentActionRepository,
) {
    val viewModel: AgentCashInViewModel = viewModel(
        factory = AgentCashInViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsState()

    AgentCashInScreen(
        uiState = uiState,
        onPhoneChanged = viewModel::onPhoneChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onContinue = viewModel::requestQuote,
        onOpenConfirmDialog = viewModel::openConfirmDialog,
        onDismissConfirmDialog = viewModel::dismissConfirmDialog,
        onConfirm = viewModel::submit,
        onEdit = viewModel::edit,
        onRestart = viewModel::restart,
    )
}