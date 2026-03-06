package com.kori.app.feature.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.data.repository.ActivityRepository

@Composable
fun ActivityRoute(
    role: UserRole,
    repository: ActivityRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActivityViewModel = viewModel(
        factory = ActivityViewModel.factory(
            role = role,
            repository = repository,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    ActivityScreen(
        role = role,
        uiState = uiState,
        onRetry = viewModel::refresh,
        onApplyFilters = viewModel::applyFilters,
        onClearFilters = viewModel::clearFilters,
        modifier = modifier,
    )
}
