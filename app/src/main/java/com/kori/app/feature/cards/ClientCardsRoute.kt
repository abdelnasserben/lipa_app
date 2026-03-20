package com.kori.app.feature.cards

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.data.repository.ClientCardRepository

@Composable
fun ClientCardsRoute(
    repository: ClientCardRepository,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val viewModel: ClientCardsViewModel = viewModel(
        factory = ClientCardsViewModel.factory(repository, resources),
    )

    val uiState by viewModel.uiState.collectAsState()

    ClientCardsScreen(
        uiState = uiState,
        onRetry = viewModel::load,
        modifier = modifier,
    )
}
