package com.kori.app.feature.agentsearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.AgentSearchRepository

@Composable
fun AgentSearchRoute(
    repository: AgentSearchRepository,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val viewModel: AgentSearchViewModel = viewModel(
        factory = AgentSearchViewModel.factory(repository, resources),
    )

    val uiState by viewModel.uiState.collectAsState()

    AgentSearchScreen(
        uiState = uiState,
        onSearch = viewModel::search,
        modifier = modifier,
    )
}
