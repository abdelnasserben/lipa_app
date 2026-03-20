package com.kori.app.feature.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentActionRepository

@Composable
fun AgentCardAddRoute(
    repository: AgentActionRepository,
) {
    val resources = LocalContext.current.resources
    val viewModel: AgentCardAddViewModel = viewModel(
        factory = AgentCardAddViewModel.factory(repository, resources),
    )
    val uiState by viewModel.uiState.collectAsState()

    AgentCardAddScreen(
        uiState = uiState,
        onPhoneChanged = viewModel::onPhoneChanged,
        onCardUidChanged = viewModel::onCardUidChanged,
        onPinChanged = viewModel::onPinChanged,
        onSubmit = viewModel::submit,
        onRestart = viewModel::restart,
    )
}
