package com.kori.app.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kori.app.core.model.UserRole
import com.kori.app.domain.GetDashboardUseCase

@Composable
fun DashboardRoute(
    role: UserRole,
    getDashboardUseCase: GetDashboardUseCase,
    onOpenProfile: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenAction: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(
            role = role,
            getDashboardUseCase = getDashboardUseCase,
        ),
    )

    val uiState by viewModel.uiState.collectAsState()

    DashboardScreen(
        role = role,
        uiState = uiState,
        onRetry = viewModel::load,
        onOpenProfile = onOpenProfile,
        onOpenTransactions = onOpenTransactions,
        onOpenAction = onOpenAction,
    )
}