package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentActionRepository

@Composable
fun AgentCardEnrollRoute(
    repository: AgentActionRepository,
) {
    val viewModel: AgentCardEnrollViewModel = viewModel(
        factory = AgentCardEnrollViewModel.factory(repository),
    )
    val uiState by viewModel.uiState.collectAsState()

    AgentCardEnrollScreen(
        uiState = uiState,
        onPhoneChanged = viewModel::onPhoneChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onCardUidChanged = viewModel::onCardUidChanged,
        onPinChanged = viewModel::onPinChanged,
        onSubmit = viewModel::submit,
        onRestart = viewModel::restart,
    )
}
