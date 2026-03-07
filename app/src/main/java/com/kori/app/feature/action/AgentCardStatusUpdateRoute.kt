package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentActionRepository

@Composable
fun AgentCardStatusUpdateRoute(
    repository: AgentActionRepository,
) {
    val viewModel: AgentCardStatusUpdateViewModel = viewModel(
        factory = AgentCardStatusUpdateViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsState()

    AgentCardStatusUpdateScreen(
        uiState = uiState,
        onCardUidChanged = viewModel::onCardUidChanged,
        onTargetStatusChanged = viewModel::onTargetStatusChanged,
        onReasonChanged = viewModel::onReasonChanged,
        onSubmit = viewModel::submit,
        onConfirmSubmit = viewModel::confirmSubmit,
        onDismissConfirm = viewModel::dismissConfirmModal,
        onRestart = viewModel::restart,
    )
}
